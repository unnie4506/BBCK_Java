package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.*;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.util.*;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Phần nhận phòng được dùng chung để trạng thái đơn và thao tác hủy được tuần tự hóa. */
public class NhanTraPhongService {
    private final DonDatPhongDAO donDAO = new DonDatPhongDAO();
    private final CongKhaiDAO dao = new CongKhaiDAO();
    private final PhongDatDAO phongDAO = new PhongDatDAO();
    private final ChiTietDatPhongDAO chiTietDAO = new ChiTietDatPhongDAO();
    private final DonDatPhongService donService = new DonDatPhongService();
    private final NhatKyThaoTacService nhatKy = new NhatKyThaoTacService();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final TinhTienService tinhTien = new TinhTienService();

    public Map<String, Object> nhanPhong(long maChiTiet, boolean xacNhanNhanSom, NguoiThaoTac actor)
            throws SQLException {
        if (actor == null || !"QUAN_LY".equals(actor.getLoai()))
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Chỉ Quản lý mới được nhận phòng.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> ref = dao.donTheoChiTiet(c, maChiTiet);
                if (ref.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_CHI_TIET", "Không tìm thấy chi tiết đặt phòng.");
                long maDon = ((Number) ref.get(0).get("maDonDatPhong")).longValue();
                List<Map<String, Object>> don = donDAO.khoaDeSua(c, maDon);
                if (don.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                if (!Set.of("DA_DAT", "DANG_O").contains(don.get(0).get("trangThai")))
                    throw new NghiepVuException(409, "DON_CHUA_XAC_NHAN_COC",
                            "Chỉ đơn đã xác nhận cọc mới được nhận phòng.");
                List<Map<String, Object>> dong = dao.chiTietKhoa(c, maChiTiet);
                if (dong.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_CHI_TIET", "Không tìm thấy chi tiết đặt phòng.");
                Map<String, Object> x = dong.get(0);
                if (!"DA_DAT".equals(x.get("trangThai")))
                    throw new NghiepVuException(409, "CHI_TIET_KHONG_THE_NHAN",
                            "Chi tiết không còn ở trạng thái Đã đặt.");
                if ("DA_HUY".equals(don.get(0).get("trangThai")) || "HOAN_TAT".equals(don.get(0).get("trangThai")))
                    throw new NghiepVuException(409, "DON_DA_KET_THUC", "Đơn đã hủy hoặc hoàn tất.");
                long maPhong = ((Number) x.get("maPhong")).longValue();
                List<Map<String, Object>> phong = phongDAO.layDeKhoa(c, maPhong);
                if (phong.isEmpty() || !Boolean.TRUE.equals(phong.get(0).get("dangHoatDong"))
                        || !"TRONG".equals(phong.get(0).get("trangThai")))
                    throw new NghiepVuException(409, "PHONG_KHONG_SAN_SANG",
                            "Phòng không ở trạng thái Trống; hãy đổi phòng hoặc xử lý trạng thái phòng.");
                List<Map<String, Object>> khach = dao.thongTinKhachTheoDon(c, maDon);
                if (khach.isEmpty() || khach.get(0).get("soGiayTo") == null
                        || String.valueOf(khach.get(0).get("soGiayTo")).isBlank())
                    throw new NghiepVuException(422, "THIEU_THONG_TIN_KHACH",
                            "Hồ sơ khách hàng cần có số giấy tờ trước khi nhận phòng.");
                LocalDate ngayNhan = LocalDate.parse(String.valueOf(x.get("ngayNhan"))), homNay = ThoiGianUtil.homNay();
                if (homNay.isBefore(ngayNhan)) {
                    if (!chiTietDAO.trungLich(c, maPhong, homNay, ngayNhan, maChiTiet).isEmpty())
                        throw new NghiepVuException(409, "TRUNG_LICH",
                                "Phòng đang có đơn khác trước ngày nhận đã đặt.");
                    if (!xacNhanNhanSom)
                        throw new NghiepVuException(409, "CAN_XAC_NHAN",
                                "Đây là nhận phòng sớm. Xác nhận tiếp tục để nhận hôm nay.");
                }
                if (dao.nhanPhong(c, maChiTiet) != 1 || dao.doiTrangThaiPhong(c, maPhong, "DANG_O") != 1)
                    throw new NghiepVuException(409, "CHI_TIET_VUA_THAY_DOI",
                            "Không thể cập nhật trạng thái nhận phòng.");
                donService.capNhatTrangThaiTongHop(c, maDon);
                nhatKy.ghi(c, actor, maDon, "QUAN_LY_NHAN_PHONG", "Nhận phòng " + phong.get(0).get("soPhong") + ".");
                c.commit();
                Map<String, Object> kq = new LinkedHashMap<>();
                kq.put("maDonDatPhong", maDon);
                kq.put("maChiTietDatPhong", maChiTiet);
                kq.put("trangThaiChiTiet", "DANG_O");
                kq.put("trangThaiDon", "DANG_O");
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Chỉ gia hạn ngày trả cho phòng đang ở; toàn bộ kiểm tra và cập nhật nằm trong một transaction. */
    public Map<String, Object> giaHan(long maChiTiet, LocalDate ngayTraMoi, NguoiThaoTac actor)
            throws SQLException {
        if (actor == null || !"QUAN_LY".equals(actor.getLoai()))
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Chỉ Quản lý mới được gia hạn phòng.");
        if (ngayTraMoi == null)
            throw new NghiepVuException(422, "NGAY_TRA_KHONG_HOP_LE", "Vui lòng chọn ngày trả mới.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> ref = dao.donTheoChiTiet(c, maChiTiet);
                if (ref.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_CHI_TIET", "Không tìm thấy chi tiết đặt phòng.");
                long maDon = ((Number) ref.get(0).get("maDonDatPhong")).longValue();
                List<Map<String, Object>> don = donDAO.khoaDeSua(c, maDon);
                if (don.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                if (!"DANG_O".equals(don.get(0).get("trangThai")))
                    throw new NghiepVuException(409, "DON_KHONG_DANG_O", "Chỉ đơn đang lưu trú mới được gia hạn.");
                if (!hoaDonDAO.theoDon(c, maDon, true).isEmpty())
                    throw new NghiepVuException(409, "DON_DA_CO_HOA_DON", "Đơn đã lập hóa đơn, không thể gia hạn.");

                Map<String, Object> dong = null;
                for (Map<String, Object> x : chiTietDAO.khoaTheoDon(c, maDon))
                    if (((Number) x.get("maChiTietDatPhong")).longValue() == maChiTiet)
                        dong = x;
                if (dong == null)
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_CHI_TIET", "Không tìm thấy chi tiết đặt phòng.");
                if (!"DANG_O".equals(dong.get("trangThai")))
                    throw new NghiepVuException(409, "CHI_TIET_KHONG_DANG_O", "Chỉ phòng đang ở mới được gia hạn.");

                long maPhong = ((Number) dong.get("maPhong")).longValue();
                List<Map<String, Object>> phong = phongDAO.layDeKhoa(c, maPhong);
                if (phong.isEmpty() || !Boolean.TRUE.equals(phong.get(0).get("dangHoatDong"))
                        || !Boolean.TRUE.equals(phong.get(0).get("loaiDangHoatDong"))
                        || !"DANG_O".equals(phong.get(0).get("trangThai")))
                    throw new NghiepVuException(409, "PHONG_KHONG_DANG_O", "Phòng không còn ở trạng thái Đang ở.");

                LocalDate ngayNhan = LocalDate.parse(String.valueOf(dong.get("ngayNhan")));
                LocalDate ngayTraCu = LocalDate.parse(String.valueOf(dong.get("ngayTra")));
                if (!ngayTraMoi.isAfter(ngayTraCu))
                    throw new NghiepVuException(422, "CHI_DUOC_GIA_HAN", "Ngày trả mới phải sau ngày trả hiện tại.");
                if (!chiTietDAO.trungLich(c, maPhong, ngayNhan, ngayTraMoi, maChiTiet).isEmpty())
                    throw new NghiepVuException(409, "TRUNG_LICH",
                            "Phòng " + phong.get(0).get("soPhong") + " đã có đơn khác trong khoảng ngày gia hạn.");

                BigDecimal giaThuong = (BigDecimal) dong.get("giaNgayThuongLucDat");
                BigDecimal giaCuoiTuan = (BigDecimal) dong.get("giaCuoiTuanLucDat");
                BigDecimal tienPhong = tinhTien.tinhTienPhong(ngayNhan, ngayTraMoi, giaThuong, giaCuoiTuan)
                        .getTienPhong();
                if (chiTietDAO.giaHan(c, maChiTiet, ngayNhan, ngayTraMoi, giaThuong, giaCuoiTuan, tienPhong) != 1)
                    throw new NghiepVuException(409, "CHI_TIET_VUA_THAY_DOI", "Không thể gia hạn, vui lòng tải lại đơn.");
                nhatKy.ghi(c, actor, maDon, "QUAN_LY_GIA_HAN_PHONG",
                        "Gia hạn phòng " + phong.get(0).get("soPhong") + " đến " + ngayTraMoi + ".");
                c.commit();

                Map<String, Object> kq = new LinkedHashMap<>();
                kq.put("maDonDatPhong", maDon);
                kq.put("maChiTietDatPhong", maChiTiet);
                kq.put("ngayTra", ngayTraMoi);
                kq.put("tienPhong", tienPhong);
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public Map<String, Object> traPhong(long maChiTiet, LocalDate ngayTraThucTe, NguoiThaoTac actor)
            throws SQLException {
        if (actor == null || !"QUAN_LY".equals(actor.getLoai()))
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Chỉ Quản lý mới được trả phòng.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> ref = dao.donTheoChiTiet(c, maChiTiet);
                if (ref.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_CHI_TIET", "Không tìm thấy chi tiết đặt phòng.");
                long maDon = ((Number) ref.get(0).get("maDonDatPhong")).longValue();
                List<Map<String, Object>> don = donDAO.khoaDeSua(c, maDon);
                if (don.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                if ("DA_HUY".equals(don.get(0).get("trangThai")))
                    throw new NghiepVuException(409, "DON_DA_HUY", "Đơn đã hủy nên không thể trả phòng.");
                List<Map<String, Object>> dong = dao.cacDongDon(c, maDon);
                Map<String, Object> hienTai = null;
                for (Map<String, Object> x : dong)
                    if (((Number) x.get("maChiTietDatPhong")).longValue() == maChiTiet)
                        hienTai = x;
                if (hienTai == null || !"DANG_O".equals(hienTai.get("trangThai")))
                    throw new NghiepVuException(409, "CHI_TIET_KHONG_DANG_O", "Chỉ phòng đang ở mới được trả.");
                long maPhong = ((Number) hienTai.get("maPhong")).longValue();
                List<Map<String, Object>> phong = phongDAO.layDeKhoa(c, maPhong);
                if (phong.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_PHONG", "Không tìm thấy phòng.");
                if (!"DANG_O".equals(phong.get(0).get("trangThai")))
                    throw new NghiepVuException(409, "PHONG_KHONG_DANG_O",
                            "Trạng thái phòng không khớp trạng thái chi tiết đang ở.");
                LocalDate ngayNhan = ngay(hienTai.get("ngayNhan")), ngayTraDuKien = ngay(hienTai.get("ngayTra")),
                        homNay = ThoiGianUtil.homNay();
                boolean traSom = ngayTraThucTe != null;
                // Giả định đã chốt: không gửi ngày trả thực tế nghĩa là tính đủ đến ngày trả dự kiến; trả sớm phải gửi ngày cụ thể.
                if (traSom && (!ngayTraThucTe.isAfter(ngayNhan) || !ngayTraThucTe.isBefore(ngayTraDuKien)
                        || ngayTraThucTe.isAfter(homNay)))
                    throw new NghiepVuException(422, "NGAY_TRA_THUC_TE_KHONG_HOP_LE",
                            "Ngày trả sớm phải sau ngày nhận, trước ngày trả dự kiến và không ở tương lai.");
                BigDecimal giaThuong = (BigDecimal) hienTai.get("giaNgayThuongLucDat"),
                        giaCuoiTuan = (BigDecimal) hienTai.get("giaCuoiTuanLucDat");
                LocalDate ngayChot = traSom ? ngayTraThucTe : ngayTraDuKien;
                BigDecimal tienDong = traSom
                        ? tinhTien.tinhTienPhong(ngayNhan, ngayChot, giaThuong, giaCuoiTuan).getTienPhong()
                        : (BigDecimal) hienTai.get("tienPhong");
                boolean conPhongKhac = false;
                for (Map<String, Object> x : dong)
                    if (((Number) x.get("maChiTietDatPhong")).longValue() != maChiTiet
                            && Set.of("DA_DAT", "DANG_O").contains(x.get("trangThai")))
                        conPhongKhac = true;
                if (conPhongKhac) {
                    if (chiTietDAO.traPhong(c, maChiTiet, ngayChot, tienDong, traSom) != 1
                            || dao.doiTrangThaiPhong(c, maPhong, "DANG_DON") != 1)
                        throw new NghiepVuException(409, "PHONG_VUA_THAY_DOI",
                                "Không thể cập nhật trả phòng, vui lòng tải lại.");
                    donService.capNhatTrangThaiTongHop(c, maDon);
                    nhatKy.ghi(c, actor, maDon, "QUAN_LY_TRA_PHONG", "Trả phòng " + phong.get(0).get("soPhong") + ".");
                    c.commit();
                    Map<String, Object> kq = new LinkedHashMap<>();
                    kq.put("maDonDatPhong", maDon);
                    kq.put("maChiTietDatPhong", maChiTiet);
                    kq.put("daLapHoaDon", false);
                    kq.put("trangThaiChiTiet", "DA_TRA");
                    return kq;
                }
                // BR-15 (Admin): chỉ lập một hóa đơn khi phòng này là phòng cuối cùng được trả.
                List<Map<String, Object>> hoaDonCu = hoaDonDAO.theoDon(c, maDon, true);
                if (!hoaDonCu.isEmpty())
                    throw new NghiepVuException(409, "HOA_DON_DA_TON_TAI",
                            "Đơn đã có hóa đơn, hãy xử lý thanh toán hóa đơn hiện có.");
                BigDecimal tongTien = BigDecimal.ZERO;
                for (Map<String, Object> x : dong)
                    if (!"DA_HUY".equals(x.get("trangThai")))
                        tongTien = tongTien.add(x == hienTai ? tienDong : (BigDecimal) x.get("tienPhong"));
                if (traSom
                        && chiTietDAO.giaHan(c, maChiTiet, ngayNhan, ngayChot, giaThuong, giaCuoiTuan, tienDong) != 1)
                    throw new NghiepVuException(409, "CHI_TIET_VUA_THAY_DOI", "Không thể chốt ngày trả sớm.");
                List<Map<String, Object>> coc = dao.tongCocDaNhan(c, maDon);
                BigDecimal cocConGiu = coc.isEmpty() ? BigDecimal.ZERO : (BigDecimal) coc.get(0).get("tienCocConLai");
                BigDecimal cocTru = cocConGiu.min(tongTien);
                BigDecimal conNo = tongTien.subtract(cocTru).max(BigDecimal.ZERO);
                boolean daThuDu = conNo.signum() == 0;
                String soHoaDon = "HD-"
                        + java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                        + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
                long maHoaDon = hoaDonDAO.them(c, maDon, soHoaDon, tongTien, cocTru, conNo, daThuDu);
                BigDecimal cocDu = cocConGiu.subtract(cocTru);
                if (cocDu.signum() > 0)
                    dao.themYeuCauHoanCoc(c, maDon, cocDu,
                            "Hoàn phần cọc dư sau khi lập hóa đơn; Quản lý xác nhận sau khi hoàn tiền thật.");
                if (daThuDu) {
                    if (chiTietDAO.traPhong(c, maChiTiet, ngayChot, tienDong, traSom) != 1
                            || dao.doiTrangThaiPhong(c, maPhong, "DANG_DON") != 1)
                        throw new NghiepVuException(409, "PHONG_VUA_THAY_DOI", "Không thể cập nhật trả phòng.");
                    donService.capNhatTrangThaiTongHop(c, maDon);
                }
                // BR-10 (Admin): ghi nhật ký trả phòng/lập hóa đơn cùng transaction.
                nhatKy.ghi(c, actor, maDon, daThuDu ? "QUAN_LY_TRA_PHONG" : "QUAN_LY_LAP_HOA_DON_CHO_THU",
                        daThuDu ? "Trả phòng cuối, lập hóa đơn " + soHoaDon + "."
                                : "Lập hóa đơn " + soHoaDon + ", còn phải thu " + conNo.toPlainString()
                                        + " đồng; phòng cuối vẫn đang ở.");
                c.commit();
                Map<String, Object> kq = new LinkedHashMap<>();
                kq.put("maDonDatPhong", maDon);
                kq.put("maChiTietDatPhong", maChiTiet);
                kq.put("maHoaDon", maHoaDon);
                kq.put("soHoaDon", soHoaDon);
                kq.put("tienPhong", tongTien);
                kq.put("tienCocDuocTru", cocTru);
                kq.put("soTienConPhaiThu", conNo);
                kq.put("daLapHoaDon", true);
                kq.put("daHoanTatCheckOut", daThuDu);
                kq.put("trangThaiHoaDon", daThuDu ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN");
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Tính thử tiền trả phòng, chỉ đọc dữ liệu và không tạo hóa đơn hay đổi trạng thái. */
    public Map<String, Object> xemTruocTraPhong(long maChiTiet, LocalDate ngayTraThucTe, NguoiThaoTac actor)
            throws SQLException {
        if (actor == null || !"QUAN_LY".equals(actor.getLoai()))
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Chỉ Quản lý mới được xem trước tiền trả phòng.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> thamChieu = dao.donTheoChiTiet(c, maChiTiet);
            if (thamChieu.isEmpty())
                throw new NghiepVuException(404, "KHONG_TIM_THAY_CHI_TIET", "Không tìm thấy chi tiết đặt phòng.");
            long maDon = ((Number) thamChieu.get(0).get("maDonDatPhong")).longValue();
            List<Map<String, Object>> cacDong = dao.cacDongDon(c, maDon);
            Map<String, Object> hienTai = null;
            for (Map<String, Object> dong : cacDong)
                if (((Number) dong.get("maChiTietDatPhong")).longValue() == maChiTiet)
                    hienTai = dong;
            if (hienTai == null || !"DANG_O".equals(hienTai.get("trangThai")))
                throw new NghiepVuException(409, "CHI_TIET_KHONG_DANG_O",
                        "Chỉ phòng đang ở mới được tính tiền trả phòng.");

            LocalDate ngayNhan = ngay(hienTai.get("ngayNhan"));
            LocalDate ngayDuKien = ngay(hienTai.get("ngayTra"));
            LocalDate homNay = ThoiGianUtil.homNay();
            boolean traSom = ngayTraThucTe != null;
            if (traSom && (!ngayTraThucTe.isAfter(ngayNhan) || !ngayTraThucTe.isBefore(ngayDuKien)
                    || ngayTraThucTe.isAfter(homNay)))
                throw new NghiepVuException(422, "NGAY_TRA_THUC_TE_KHONG_HOP_LE",
                        "Ngày trả sớm phải sau ngày nhận, trước ngày trả dự kiến và không ở tương lai.");

            LocalDate ngayChot = traSom ? ngayTraThucTe : ngayDuKien;
            BigDecimal giaThuong = (BigDecimal) hienTai.get("giaNgayThuongLucDat");
            BigDecimal giaCuoiTuan = (BigDecimal) hienTai.get("giaCuoiTuanLucDat");
            TinhTienService.KetQuaTinhTien ketQua = tinhTien.tinhTienPhong(ngayNhan, ngayChot, giaThuong, giaCuoiTuan);
            BigDecimal tienPhongPhongNay = traSom ? ketQua.getTienPhong() : (BigDecimal) hienTai.get("tienPhong");
            BigDecimal tongTienPhongDon = BigDecimal.ZERO;
            boolean laPhongCuoi = true;
            for (Map<String, Object> dong : cacDong) {
                String trangThai = (String) dong.get("trangThai");
                if ("DA_HUY".equals(trangThai))
                    continue;
                if (((Number) dong.get("maChiTietDatPhong")).longValue() == maChiTiet)
                    tongTienPhongDon = tongTienPhongDon.add(tienPhongPhongNay);
                else {
                    tongTienPhongDon = tongTienPhongDon.add((BigDecimal) dong.get("tienPhong"));
                    if ("DA_DAT".equals(trangThai) || "DANG_O".equals(trangThai))
                        laPhongCuoi = false;
                }
            }

            List<Map<String, Object>> coc = dao.tongCocDaNhan(c, maDon);
            BigDecimal cocConGiu = coc.isEmpty() ? BigDecimal.ZERO : (BigDecimal) coc.get(0).get("tienCocConLai");
            BigDecimal cocDuocTru = laPhongCuoi ? cocConGiu.min(tongTienPhongDon) : BigDecimal.ZERO;
            BigDecimal daThanhToan = BigDecimal.ZERO;
            List<Map<String, Object>> hoaDon = hoaDonDAO.theoDon(c, maDon, false);
            if (!hoaDon.isEmpty())
                daThanhToan = hoaDonDAO.tongDaThanhToan(c, ((Number) hoaDon.get(0).get("maHoaDon")).longValue());
            BigDecimal conPhaiThu = laPhongCuoi
                    ? tongTienPhongDon.subtract(cocDuocTru).subtract(daThanhToan).max(BigDecimal.ZERO)
                    : BigDecimal.ZERO;

            Map<String, Object> ketQuaXemTruoc = new LinkedHashMap<>();
            ketQuaXemTruoc.put("maDonDatPhong", maDon);
            ketQuaXemTruoc.put("maChiTietDatPhong", maChiTiet);
            ketQuaXemTruoc.put("soPhong", hienTai.get("soPhong"));
            ketQuaXemTruoc.put("ngayNhan", ngayNhan);
            ketQuaXemTruoc.put("ngayTraDuKien", ngayDuKien);
            ketQuaXemTruoc.put("ngayTraTinhTien", ngayChot);
            ketQuaXemTruoc.put("soDem", ketQua.getSoDem());
            ketQuaXemTruoc.put("soDemNgayThuong", ketQua.getSoDemNgayThuong());
            ketQuaXemTruoc.put("soDemCuoiTuan", ketQua.getSoDemCuoiTuan());
            ketQuaXemTruoc.put("tienPhongPhongNay", tienPhongPhongNay);
            ketQuaXemTruoc.put("tongTienPhongDon", tongTienPhongDon);
            ketQuaXemTruoc.put("tienCocConGiu", cocConGiu);
            ketQuaXemTruoc.put("tienCocDuocTru", cocDuocTru);
            ketQuaXemTruoc.put("tongDaThanhToan", daThanhToan);
            ketQuaXemTruoc.put("soTienConPhaiThu", conPhaiThu);
            ketQuaXemTruoc.put("laPhongCuoi", laPhongCuoi);
            ketQuaXemTruoc.put("seLapHoaDon", laPhongCuoi && hoaDon.isEmpty());
            ketQuaXemTruoc.put("thongBao", laPhongCuoi ? "Đây là phòng cuối của đơn. Xác nhận trả phòng để lập hóa đơn."
                    : "Đơn còn phòng khác chưa trả. Lần trả này chưa lập hóa đơn.");
            return ketQuaXemTruoc;
        }
    }

    private LocalDate ngay(Object x) {
        return x instanceof LocalDate ? (LocalDate) x : LocalDate.parse(String.valueOf(x));
    }
}
