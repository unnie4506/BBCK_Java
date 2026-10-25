package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.*;
import vn.edu.quanlyhomestay.model.*;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import vn.edu.quanlyhomestay.util.ThoiGianUtil;
import vn.edu.quanlyhomestay.util.CauHinhDatCoc;
import java.math.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Nghiệp vụ tạo, sửa và đọc đơn; không phụ thuộc Servlet hay phiên HTTP. */
public class DonDatPhongService {
    private final DonDatPhongDAO donDAO = new DonDatPhongDAO();
    private final ChiTietDatPhongDAO chiTietDAO = new ChiTietDatPhongDAO();
    private final PhongDatDAO phongDAO = new PhongDatDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhatKyThaoTacService nhatKy = new NhatKyThaoTacService();
    private final TinhTienService tinhTien = new TinhTienService();

    public Map<String, Object> taoDon(DonDatPhongYeuCau yeuCau, NguoiThaoTac nguoi) throws SQLException {
        return taoDon(yeuCau, nguoi, 1);
    }

    public Map<String, Object> taoDon(DonDatPhongYeuCau yeuCau, NguoiThaoTac nguoi, int soKhach) throws SQLException {
        return taoDon(yeuCau, nguoi, soKhach, false);
    }

    public Map<String, Object> taoDonChoCoc(DonDatPhongYeuCau yeuCau, NguoiThaoTac nguoi, int soKhach)
            throws SQLException {
        return taoDon(yeuCau, nguoi, soKhach, true);
    }

    private Map<String, Object> taoDon(DonDatPhongYeuCau yeuCau, NguoiThaoTac nguoi, int soKhach,
            boolean choCoc) throws SQLException {
        kiemTraNguoi(nguoi);
        kiemTraYeuCau(yeuCau);
        boolean khachVangLai = "QUAN_LY".equals(nguoi.getLoai()) && yeuCau.getMaKhachHang() == null;
        if (khachVangLai) kiemTraKhachVangLai(yeuCau.getKhachHangVangLai());
        else if (yeuCau.getMaKhachHang() == null || yeuCau.getMaKhachHang() <= 0)
            throw loi(422, "THIEU_KHACH_HANG", "Hãy chọn khách hàng hoặc nhập thông tin khách vãng lai.");
        if (yeuCau.getDanhSachPhong().isEmpty())
            throw loi(422, "THIEU_PHONG", "Đơn phải có ít nhất một phòng.");
        if (soKhach < 1 || soKhach > 20)
            throw loi(422, "SO_KHACH_KHONG_HOP_LE", "Số khách phải từ 1 đến 20.");
        LocalDate homNay = ThoiGianUtil.homNay();
        Set<Long> maPhong = new TreeSet<>();
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietDatPhongYeuCau dong : yeuCau.getDanhSachPhong()) {
            kiemTraNgay(dong.getNgayNhan(), dong.getNgayTra(), true, homNay);
            maPhong.add(dong.getMaPhong());
        }
        // BR-01 (Admin): kiểm tra chéo các dòng cùng đơn để chúng không tự giữ trùng lịch.
        kiemTraCacDongTrungNhau(yeuCau.getDanhSachPhong());
        BigDecimal tienCoc = choCoc ? BigDecimal.ZERO : chuanTien(yeuCau.getTienCoc());
        if (tienCoc.signum() < 0)
            throw loi(422, "TIEN_COC_KHONG_HOP_LE", "Tiền cọc không được âm.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                if ("KHACH_HANG".equals(nguoi.getLoai())) {
                    // BR-05 (Khách): khóa hồ sơ khách trước khi đếm để các yêu cầu đồng thời không vượt ba đơn.
                    if (!donDAO.khoaHoSoKhachHang(c, yeuCau.getMaKhachHang()))
                        throw loi(403, "KHONG_CO_QUYEN", "Hồ sơ khách hàng không hoạt động hoặc không hợp lệ.");
                    if (!khachHangDAO.taiKhoanThuocHoSo(c, nguoi.getMa(), yeuCau.getMaKhachHang()))
                        throw loi(403, "KHONG_CO_QUYEN", "Bạn chỉ được tạo đơn cho hồ sơ của mình.");
                    if (donDAO.demDonDangChoTheoTaiKhoan(c, nguoi.getMa()) >= 3)
                        throw loi(409, "DA_DAT_TOI_DA", "Bạn đã có tối đa 3 đơn Đã đặt hoặc đang chờ cọc còn hạn.");
                }
                long maKhachDat = yeuCau.getMaKhachHang() == null ? 0 : yeuCau.getMaKhachHang();
                if (khachVangLai) {
                    KhachHangVangLaiYeuCau khachMoi = yeuCau.getKhachHangVangLai();
                    String sdt = vn.edu.quanlyhomestay.util.TienIchKiemTraDuLieu.chuanHoaSoDienThoaiVietNam(khachMoi.getSoDienThoai());
                    String giayTo = khachMoi.getSoGiayTo().trim();
                    String email = khachMoi.getThuDienTu() == null ? null : khachMoi.getThuDienTu().trim();
                    if (email != null && !email.isBlank() && khachHangDAO.thuDienTuDaTonTai(c, email))
                        throw loi(409, "EMAIL_KHACH_DA_TON_TAI",
                                "Email này đã thuộc một hồ sơ khách hàng. Hãy chọn hồ sơ có sẵn hoặc dùng email khác.");
                    if (khachHangDAO.hoSoTrungThongTin(c, sdt, giayTo))
                        throw loi(409, "KHACH_VANG_LAI_TRUNG", "Số điện thoại hoặc giấy tờ đã có hồ sơ. Hãy chọn khách hàng có sẵn.");
                    maKhachDat = khachHangDAO.themHoSoVangLai(c, khachMoi.getHoTen().trim(), sdt,
                            email == null || email.isBlank() ? null : email, giayTo);
                } else if (!"KHACH_HANG".equals(nguoi.getLoai()) && !khachHangDAO.hoSoDangHoatDong(c, maKhachDat))
                    throw loi(404, "KHACH_HANG_KHONG_HOP_LE", "Không tìm thấy hồ sơ khách hàng đang hoạt động.");
                Map<Long, Map<String, Object>> phongDaKhoa = khoaPhong(c, maPhong);
                List<DongDatDaTinh> cacDong = new ArrayList<>();
                int tongSucChua = 0;
                List<Map<String, Object>> cacPhongTrung = new ArrayList<>();
                for (ChiTietDatPhongYeuCau dong : yeuCau.getDanhSachPhong()) {
                    Map<String, Object> phong = kiemTraPhongDat(phongDaKhoa, dong.getMaPhong());
                    tongSucChua += ((Number) phong.get("sucChua")).intValue();
                    // BR-01 (Admin): khóa phòng trước, rồi kiểm tra lịch lần cuối trong transaction tạo đơn.
                    if (!chiTietDAO.trungLich(c, dong.getMaPhong(), dong.getNgayNhan(), dong.getNgayTra(), null)
                            .isEmpty()) {
                        Map<String, Object> biTrung = new LinkedHashMap<>();
                        biTrung.put("maPhong", dong.getMaPhong());
                        biTrung.put("soPhong", phong.get("soPhong"));
                        cacPhongTrung.add(biTrung);
                        continue;
                    }
                    BigDecimal giaThuong = (BigDecimal) phong.get("giaNgayThuong"),
                            giaCuoiTuan = (BigDecimal) phong.get("giaCuoiTuan");
                    TinhTienService.KetQuaTinhTien tien = tinhTien.tinhTienPhong(dong.getNgayNhan(), dong.getNgayTra(),
                            giaThuong, giaCuoiTuan);
                    tong = tong.add(tien.getTienPhong());
                    cacDong.add(new DongDatDaTinh(dong, giaThuong, giaCuoiTuan, tien.getTienPhong()));
                }
                if (!cacPhongTrung.isEmpty())
                    throw new NghiepVuException(409, "TRUNG_LICH",
                            "Một hoặc nhiều phòng đã có đơn trong khoảng ngày này.",
                            Map.of("danhSachPhongBiTrung", cacPhongTrung));
                // BR-04 (Khách): cho phép ghép nhiều phòng nhưng tổng sức chứa phải đủ số khách.
                if ("KHACH_HANG".equals(nguoi.getLoai()) && tongSucChua < soKhach)
                    throw loi(422, "SUC_CHUA_KHONG_DU", "Tổng sức chứa các phòng đã chọn chưa đủ số khách.");
                // BR-14 (Admin): tiền cọc ở cấp đơn, không âm và không vượt tổng tiền phòng chưa hủy.
                if (choCoc) {
                    tienCoc = tong.multiply(CauHinhDatCoc.tyLeCoc()).divide(new BigDecimal("100"), 0,
                            java.math.RoundingMode.HALF_UP);
                }
                if (tienCoc.compareTo(tong) > 0)
                    throw loi(422, "COC_VUOT_TIEN_PHONG", "Tiền cọc không được vượt tổng tiền phòng tạm tính.");
                String maTraCuu = "HS-"
                        + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase(Locale.ROOT);
                long maDon;
                if (choCoc) {
                    if (!"KHACH_HANG".equals(nguoi.getLoai()))
                        throw loi(403, "KHONG_CO_QUYEN", "Luồng chờ cọc chỉ dành cho khách đặt trực tuyến.");
                    maDon = donDAO.themDonChoCoc(c, maTraCuu, maKhachDat, nguoi, yeuCau.getGhiChu(), tienCoc,
                            soKhach, CauHinhDatCoc.thoiGianGiuChoPhut());
                } else {
                    maDon = donDAO.them(c, maTraCuu, maKhachDat, nguoi, yeuCau.getGhiChu(), tienCoc, soKhach);
                }
                for (DongDatDaTinh dong : cacDong)
                    chiTietDAO.them(c, maDon, dong.yeuCau.getMaPhong(), dong.yeuCau.getNgayNhan(),
                            dong.yeuCau.getNgayTra(), dong.giaThuong, dong.giaCuoiTuan, dong.tienPhong);
                if (!choCoc) capNhatTrangThaiTongHop(c, maDon);
                // BR-10 (Admin): ghi nhật ký tạo đơn cùng transaction với đơn và các dòng chi tiết.
                nhatKy.ghi(c, nguoi, maDon, "TAO_DON_DAT_PHONG",
                        "Tạo đơn " + maTraCuu + " gồm " + cacDong.size() + " phòng.");
                c.commit();
                Map<String, Object> kq = new LinkedHashMap<>();
                kq.put("maDonDatPhong", maDon);
                kq.put("maTraCuuDon", maTraCuu);
                kq.put("tongTienPhong", tong);
                kq.put("tienCoc", tienCoc);
                kq.put("tienCocYeuCau", tienCoc);
                kq.put("tienCocDaNhan", BigDecimal.ZERO);
                kq.put("trangThai", choCoc ? "CHO_COC" : "DA_DAT");
                if (choCoc) {
                    List<Map<String, Object>> thoiGian = donDAO.donKhoa(c, maDon);
                    kq.put("hanGiuCho", thoiGian.get(0).get("hanGiuCho"));
                    kq.put("thoiGianMayChuMs", thoiGian.get(0).get("thoiGianMayChuMs"));
                }
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public Map<String, Object> suaDon(long maDon, DonDatPhongYeuCau yeuCau, NguoiThaoTac nguoi) throws SQLException {
        kiemTraNguoi(nguoi);
        kiemTraYeuCau(yeuCau);
        if (yeuCau.getDanhSachPhong() == null)
            throw loi(422, "THIEU_PHONG", "Thiếu danh sách phòng trong yêu cầu.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> donDaKhoa = donDAO.khoaDeSua(c, maDon);
                if (donDaKhoa.isEmpty())
                    throw loi(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                Map<String, Object> don = donDaKhoa.get(0);
                kiemTraChuDon(c, maDon, nguoi);
                String trangThaiDon = (String) don.get("trangThai");
                if (Set.of("CHO_COC", "HET_HAN", "HOAN_TAT", "DA_HUY").contains(trangThaiDon))
                    throw loi(409, "DON_DA_KHOA", "Đơn chờ cọc, hết hạn, hoàn tất hoặc đã hủy nên không thể sửa ở luồng đơn thường.");
                if ("DANG_O".equals(trangThaiDon))
                    throw loi(409, "DON_DANG_O_CHI_DUOC_GIA_HAN",
                            "Đơn đang lưu trú chỉ được gia hạn ngày trả qua chức năng Gia hạn phòng.");
                List<Map<String, Object>> cu = chiTietDAO.khoaTheoDon(c, maDon);
                Map<Long, Map<String, Object>> theoMa = new HashMap<>();
                TreeSet<Long> maPhongCanKhoa = new TreeSet<>();
                for (Map<String, Object> d : cu) {
                    long id = soLong(d.get("maChiTietDatPhong"));
                    theoMa.put(id, d);
                    if (!Set.of("DA_TRA", "DA_HUY").contains(d.get("trangThai")))
                        maPhongCanKhoa.add(soLong(d.get("maPhong")));
                }
                Set<Long> maChiTietDaGui = new HashSet<>();
                for (ChiTietDatPhongYeuCau d : yeuCau.getDanhSachPhong()) {
                    if (d.getNgayNhan() == null || d.getNgayTra() == null || !d.getNgayTra().isAfter(d.getNgayNhan())
                            || java.time.temporal.ChronoUnit.DAYS.between(d.getNgayNhan(), d.getNgayTra()) > 30)
                        throw loi(422, "NGAY_KHONG_HOP_LE", "Ngày trả phải sau ngày nhận và mỗi phòng tối đa 30 đêm.");
                    if (d.getMaChiTiet() != null) {
                        if (!maChiTietDaGui.add(d.getMaChiTiet()))
                            throw loi(422, "CHI_TIET_TRUNG", "Một chi tiết bị gửi lặp trong đơn.");
                        if (!theoMa.containsKey(d.getMaChiTiet()))
                            throw loi(404, "KHONG_TIM_THAY_CHI_TIET", "Chi tiết phòng không thuộc đơn này.");
                    } else {
                        kiemTraNgay(d.getNgayNhan(), d.getNgayTra(), true, ThoiGianUtil.homNay());
                    }
                    if (d.getMaPhong() > 0)
                        maPhongCanKhoa.add(d.getMaPhong());
                }
                // BR-01 (Admin): kiểm tra chéo các khoảng ngày mới khai báo trong cùng đơn.
                kiemTraCacDongTrungNhau(yeuCau.getDanhSachPhong());
                Map<Long, Map<String, Object>> phongDaKhoa = khoaPhong(c, maPhongCanKhoa);
                for (Map<String, Object> dongCu : cu) {
                    long id = soLong(dongCu.get("maChiTietDatPhong"));
                    String tt = (String) dongCu.get("trangThai");
                    ChiTietDatPhongYeuCau gui = timYeuCauTheoMa(yeuCau, id);
                    if (Set.of("DA_TRA", "DA_HUY").contains(tt)) {
                        if (gui != null && !khongDoi(gui, dongCu))
                            throw loi(409, "CHI_TIET_DA_KET_THUC", "Chi tiết đã trả hoặc đã hủy không được sửa.");
                        continue;
                    }
                    if ("DANG_O".equals(tt)) {
                        if (gui == null)
                            throw loi(409, "PHONG_DANG_O", "Không thể bỏ phòng đang có khách; hãy check-out trước.");
                        xuLyGiaHan(c, id, dongCu, gui, phongDaKhoa);
                        continue;
                    }
                    if (!"DA_DAT".equals(tt))
                        throw loi(409, "TRANG_THAI_KHONG_HOP_LE", "Trạng thái chi tiết không cho phép sửa.");
                    if (gui == null) { // BR-12 (Admin): bỏ phòng khi sửa chỉ chuyển dòng sang DA_HUY, không tính hoàn cọc.
                        chiTietDAO.boPhong(c, id, "bỏ khi sửa đơn");
                        capNhatTrangThaiTongHop(c, maDon);
                        continue;
                    }
                    xuLyDongDaDat(c, id, dongCu, gui, phongDaKhoa);
                }
                for (ChiTietDatPhongYeuCau gui : yeuCau.getDanhSachPhong())
                    if (gui.getMaChiTiet() == null) {
                        kiemTraNgay(gui.getNgayNhan(), gui.getNgayTra(), true, ThoiGianUtil.homNay());
                        Map<String, Object> phong = kiemTraPhongDat(phongDaKhoa, gui.getMaPhong());
                        if (!chiTietDAO.trungLich(c, gui.getMaPhong(), gui.getNgayNhan(), gui.getNgayTra(), null)
                                .isEmpty())
                            throw trungLich(gui.getMaPhong(), phong);
                        BigDecimal giaThuong = (BigDecimal) phong.get("giaNgayThuong"),
                                giaCuoiTuan = (BigDecimal) phong.get("giaCuoiTuan");
                        BigDecimal tien = tinhTien
                                .tinhTienPhong(gui.getNgayNhan(), gui.getNgayTra(), giaThuong, giaCuoiTuan)
                                .getTienPhong();
                        chiTietDAO.them(c, maDon, gui.getMaPhong(), gui.getNgayNhan(), gui.getNgayTra(), giaThuong,
                                giaCuoiTuan, tien);
                    }
                List<Map<String, Object>> sauSua = chiTietDAO.khoaTheoDon(c, maDon);
                BigDecimal tong = tongTienChuaHuy(sauSua);
                BigDecimal tienCoc = chuanTien(yeuCau.getTienCoc());
                if (tienCoc.signum() < 0 || tienCoc.compareTo(tong) > 0)
                    throw loi(422, "COC_VUOT_TIEN_PHONG",
                            "Tiền cọc không được âm hoặc vượt tổng tiền phòng sau khi sửa.");
                if (donDAO.capNhat(c, maDon, yeuCau.getGhiChu(), tienCoc) != 1)
                    throw loi(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                capNhatTrangThaiTongHop(c, maDon);
                // BR-10 (Admin): ghi nhật ký sửa đơn và mọi thay đổi dòng trong cùng transaction.
                nhatKy.ghi(c, nguoi, maDon, "SUA_DON_DAT_PHONG", "Cập nhật ghi chú, tiền cọc hoặc danh sách phòng.");
                c.commit();
                Map<String, Object> kq = new LinkedHashMap<>();
                kq.put("maDonDatPhong", maDon);
                kq.put("tongTienPhong", tong);
                kq.put("tienCoc", tienCoc);
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void xuLyDongDaDat(Connection c, long id, Map<String, Object> cu, ChiTietDatPhongYeuCau moi,
            Map<Long, Map<String, Object>> phongDaKhoa) throws SQLException {
        long phongCu = soLong(cu.get("maPhong"));
        LocalDate nhanCu = ngay(cu.get("ngayNhan")), traCu = ngay(cu.get("ngayTra"));
        boolean doiNgay = !nhanCu.equals(moi.getNgayNhan()) || !traCu.equals(moi.getNgayTra()),
                doiPhong = phongCu != moi.getMaPhong();
        if (!doiNgay && !doiPhong)
            return;
        if (doiNgay)
            kiemTraNgay(moi.getNgayNhan(), moi.getNgayTra(), true, ThoiGianUtil.homNay());
        Map<String, Object> phong = kiemTraPhongDat(phongDaKhoa, moi.getMaPhong());
        if (!chiTietDAO.trungLich(c, moi.getMaPhong(), moi.getNgayNhan(), moi.getNgayTra(), id).isEmpty())
            throw trungLich(moi.getMaPhong(), phong);
        BigDecimal giaThuong = (BigDecimal) phong.get("giaNgayThuong"),
                giaCuoiTuan = (BigDecimal) phong.get("giaCuoiTuan");
        BigDecimal tien = tinhTien.tinhTienPhong(moi.getNgayNhan(), moi.getNgayTra(), giaThuong, giaCuoiTuan)
                .getTienPhong();
        if (chiTietDAO.capNhatDatPhong(c, id, moi.getMaPhong(), moi.getNgayNhan(), moi.getNgayTra(), giaThuong,
                giaCuoiTuan, tien) != 1)
            throw loi(409, "CHI_TIET_BI_THAY_DOI", "Chi tiết đặt phòng vừa thay đổi, hãy tải lại đơn.");
    }

    private void xuLyGiaHan(Connection c, long id, Map<String, Object> cu, ChiTietDatPhongYeuCau moi,
            Map<Long, Map<String, Object>> phongDaKhoa) throws SQLException {
        long maPhong = soLong(cu.get("maPhong"));
        LocalDate nhan = ngay(cu.get("ngayNhan")), traCu = ngay(cu.get("ngayTra"));
        if (moi.getMaPhong() != maPhong || !nhan.equals(moi.getNgayNhan()))
            throw loi(409, "PHONG_DANG_O", "Phòng đang ở chỉ được gia hạn ngày trả.");
        if (moi.getNgayTra().isEqual(traCu))
            return;
        if (!moi.getNgayTra().isAfter(traCu))
            throw loi(422, "CHI_DUOC_GIA_HAN", "Với phòng đang ở, chỉ được tăng ngày trả dự kiến.");
        kiemTraNgay(nhan, moi.getNgayTra(), false, ThoiGianUtil.homNay());
        Map<String, Object> phong = kiemTraPhongDat(phongDaKhoa, maPhong);
        if (!chiTietDAO.trungLich(c, maPhong, nhan, moi.getNgayTra(), id).isEmpty())
            throw trungLich(maPhong, phong);
        BigDecimal giaThuong = (BigDecimal) cu.get("giaNgayThuongLucDat"),
                giaCuoiTuan = (BigDecimal) cu.get("giaCuoiTuanLucDat");
        BigDecimal tien = tinhTien.tinhTienPhong(nhan, moi.getNgayTra(), giaThuong, giaCuoiTuan).getTienPhong();
        if (chiTietDAO.giaHan(c, id, nhan, moi.getNgayTra(), giaThuong, giaCuoiTuan, tien) != 1)
            throw loi(409, "CHI_TIET_BI_THAY_DOI", "Không thể gia hạn chi tiết đặt phòng.");
    }

    public Map<String, Object> layDon(long maDon, NguoiThaoTac nguoi) throws SQLException {
        kiemTraNguoi(nguoi);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            kiemTraChuDon(c, maDon, nguoi);
            List<Map<String, Object>> ds = donDAO.layDonDayDu(c, maDon);
            if (ds.isEmpty())
                throw loi(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
            Map<String, Object> don = ds.get(0);
            List<Map<String, Object>> chiTiet = donDAO.chiTietDon(c, maDon);
            for (Map<String, Object> d : chiTiet)
                themSoDem(d);
            don.put("danhSachPhong", chiTiet);
            don.put("tongTienPhong", tongTienChuaHuy(chiTiet));
            don.put("soTienHoanCoc", BigDecimal.ZERO);
            don.put("danhSachTienCoc", new HoaDonDAO().tienCocCuaDon(c, maDon));
            return don;
        }
    }

    public Map<String, Object> timDon(String tuKhoa, String trangThai, LocalDate tuNgay, LocalDate denNgay, int trang,
            int kichThuoc, NguoiThaoTac nguoi) throws SQLException {
        return timDon(tuKhoa, trangThai, tuNgay, denNgay, null, trang, kichThuoc, nguoi);
    }

    public Map<String, Object> timDon(String tuKhoa, String trangThai, LocalDate tuNgay, LocalDate denNgay,
            String nguonDat, int trang, int kichThuoc, NguoiThaoTac nguoi) throws SQLException {
        kiemTraNguoi(nguoi);
        if (trangThai != null && !trangThai.isBlank()
                && !Set.of("DA_DAT", "DANG_O", "HOAN_TAT", "DA_HUY").contains(trangThai))
            throw loi(400, "TRANG_THAI_KHONG_HOP_LE", "Trạng thái đơn không hợp lệ.");
        if (tuNgay != null && denNgay != null && denNgay.isBefore(tuNgay))
            throw loi(400, "KHOANG_NGAY_KHONG_HOP_LE", "Ngày kết thúc phải bằng hoặc sau ngày bắt đầu.");
        if (nguonDat != null && !nguonDat.isBlank() && !Set.of("QUAN_LY", "TRUC_TUYEN").contains(nguonDat))
            throw loi(400, "NGUON_DAT_KHONG_HOP_LE", "Nguồn đặt không hợp lệ.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            Long maKhach = "KHACH_HANG".equals(nguoi.getLoai()) ? nguoi.getMa() : null;
            Map<String, Object> ketQua = donDAO.timKiem(c, tuKhoa, trangThai,
                    tuNgay == null ? null : java.sql.Date.valueOf(tuNgay),
                    denNgay == null ? null : java.sql.Date.valueOf(denNgay), maKhach, nguonDat, kichThuoc,
                    (trang - 1) * kichThuoc);
            ketQua.put("trang", trang);
            ketQua.put("kichThuoc", kichThuoc);
            long tong = ((Number) ketQua.get("tongSoBanGhi")).longValue();
            ketQua.put("tongSoTrang", (tong + kichThuoc - 1) / kichThuoc);
            return ketQua;
        }
    }

    public List<Map<String, Object>> lich(LocalDate tu, LocalDate den, NguoiThaoTac nguoi) throws SQLException {
        kiemTraNguoi(nguoi);
        if (tu == null || den == null || den.isBefore(tu))
            throw loi(400, "KHOANG_NGAY_KHONG_HOP_LE", "Khoảng ngày xem lịch không hợp lệ.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            return donDAO.lich(c, java.sql.Date.valueOf(tu), java.sql.Date.valueOf(den));
        }
    }

    /** BR-12 (Admin): đây là phương thức duy nhất cập nhật trạng thái tổng hợp của đơn sau khi dòng đổi trạng thái. */
    public void capNhatTrangThaiTongHop(Connection c, long maDon) throws SQLException {
        List<Map<String, Object>> ds = chiTietDAO.khoaTheoDon(c, maDon);
        boolean coDat = false, coO = false, coTra = false, coHoatDong = false;
        for (Map<String, Object> d : ds) {
            String tt = (String) d.get("trangThai");
            if ("DA_DAT".equals(tt))
                coDat = true;
            if ("DANG_O".equals(tt))
                coO = true;
            if ("DA_TRA".equals(tt))
                coTra = true;
            if (!"DA_HUY".equals(tt))
                coHoatDong = true;
        }
        String tt = !coHoatDong ? "DA_HUY" : coO ? "DANG_O" : coDat ? "DA_DAT" : coTra ? "HOAN_TAT" : "DA_HUY";
        donDAO.capNhatTrangThai(c, maDon, tt);
    }

    private Map<Long, Map<String, Object>> khoaPhong(Connection c, Set<Long> cacMa) throws SQLException {
        Map<Long, Map<String, Object>> ds = new HashMap<>();
        for (long ma : cacMa) {
            List<Map<String, Object>> row = phongDAO.layDeKhoa(c, ma);
            if (!row.isEmpty())
                ds.put(ma, row.get(0));
        }
        return ds;
    }

    private Map<String, Object> kiemTraPhongDat(Map<Long, Map<String, Object>> ds, long ma) {
        Map<String, Object> p = ds.get(ma);
        if (p == null || !Boolean.TRUE.equals(p.get("dangHoatDong")) || !Boolean.TRUE.equals(p.get("loaiDangHoatDong")))
            throw loi(422, "PHONG_KHONG_HOAT_DONG",
                    "Phòng không tồn tại, đã ngừng sử dụng hoặc loại phòng đã ngừng sử dụng.");
        if ("BAO_TRI".equals(p.get("trangThai")))
            throw loi(409, "PHONG_BAO_TRI", "Phòng đang bảo trì, hãy chọn phòng khác.");
        return p;
    }

    private NghiepVuException trungLich(long maPhong, Map<String, Object> p) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("maPhong", maPhong);
        d.put("soPhong", p == null ? null : p.get("soPhong"));
        return new NghiepVuException(409, "TRUNG_LICH",
                "Phòng " + (p == null ? maPhong : p.get("soPhong")) + " đã có đơn trong khoảng ngày này.", d);
    }

    private void kiemTraYeuCau(DonDatPhongYeuCau y) {
        if (y == null)
            throw loi(422, "YEU_CAU_KHONG_HOP_LE", "Thiếu thông tin đơn đặt phòng.");
        if (y.getGhiChu() != null && y.getGhiChu().length() > 1000)
            throw loi(422, "GHI_CHU_QUA_DAI", "Ghi chú tối đa 1000 ký tự.");
        if (y.getDanhSachPhong() == null)
            throw loi(422, "THIEU_PHONG", "Danh sách phòng không được để trống.");
    }

    private void kiemTraKhachVangLai(KhachHangVangLaiYeuCau k) {
        if (k == null || k.getHoTen() == null || k.getHoTen().isBlank() || k.getHoTen().trim().length() > 120)
            throw loi(422, "THIEU_TEN_KHACH", "Tên khách từ 1 đến 120 ký tự.");
        String sdt = vn.edu.quanlyhomestay.util.TienIchKiemTraDuLieu.chuanHoaSoDienThoaiVietNam(k.getSoDienThoai());
        if (sdt == null) throw loi(422, "SO_DIEN_THOAI_KHONG_HOP_LE", "Số điện thoại khách không hợp lệ.");
        if (k.getSoGiayTo() == null || k.getSoGiayTo().isBlank() || k.getSoGiayTo().trim().length() > 30)
            throw loi(422, "THIEU_GIAY_TO", "Cần nhập số giấy tờ để làm thủ tục nhận phòng.");
        if (k.getThuDienTu() != null && !k.getThuDienTu().isBlank()
                && !vn.edu.quanlyhomestay.util.TienIchKiemTraDuLieu.laThuDienTu(k.getThuDienTu().trim()))
            throw loi(422, "EMAIL_KHONG_HOP_LE", "Email khách không đúng định dạng.");
    }

    private void kiemTraNgay(LocalDate nhan, LocalDate tra, boolean canNgayTuongLai, LocalDate homNay) {
        if (nhan == null || tra == null || !tra.isAfter(nhan))
            throw loi(422, "NGAY_KHONG_HOP_LE", "Ngày trả phải sau ngày nhận.");
        if (canNgayTuongLai && nhan.isBefore(homNay))
            throw loi(422, "NGAY_NHAN_QUA_KHU", "Ngày nhận không được ở quá khứ.");
        if (java.time.temporal.ChronoUnit.DAYS.between(nhan, tra) > 30)
            throw loi(422, "VUOT_QUA_30_DEM", "Mỗi phòng được đặt tối đa 30 đêm.");
    }

    // BR-02 (Admin): mỗi khoảng phải có ngày trả sau ngày nhận và không vượt 30 đêm.
    private void kiemTraCacDongTrungNhau(List<ChiTietDatPhongYeuCau> cacDong) {
        for (int i = 0; i < cacDong.size(); i++) {
            ChiTietDatPhongYeuCau a = cacDong.get(i);
            if (a.getNgayNhan() == null || a.getNgayTra() == null)
                continue;
            for (int j = i + 1; j < cacDong.size(); j++) {
                ChiTietDatPhongYeuCau b = cacDong.get(j);
                if (b.getNgayNhan() != null && b.getNgayTra() != null && a.getMaPhong() == b.getMaPhong()
                        && a.getNgayNhan().isBefore(b.getNgayTra()) && a.getNgayTra().isAfter(b.getNgayNhan()))
                    throw loi(409, "TRUNG_LICH_TRONG_DON", "Một phòng bị đặt trùng trong hai dòng của cùng đơn.");
            }
        }
    }

    private BigDecimal chuanTien(BigDecimal n) {
        return (n == null ? BigDecimal.ZERO : n).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal tongTienChuaHuy(List<Map<String, Object>> ds) {
        BigDecimal tong = BigDecimal.ZERO;
        for (Map<String, Object> d : ds)
            if (!"DA_HUY".equals(d.get("trangThai")))
                tong = tong.add((BigDecimal) d.get("tienPhong"));
        return tong;
    }

    private void themSoDem(Map<String, Object> d) {
        LocalDate nhan = ngay(d.get("ngayNhan")), tra = ngay(d.get("ngayTra"));
        long thuong = 0, cuoi = 0;
        for (LocalDate n = nhan; n.isBefore(tra); n = n.plusDays(1)) {
            if (n.getDayOfWeek() == java.time.DayOfWeek.FRIDAY || n.getDayOfWeek() == java.time.DayOfWeek.SATURDAY)
                cuoi++;
            else
                thuong++;
        }
        d.put("soDem", thuong + cuoi);
        d.put("soDemNgayThuong", thuong);
        d.put("soDemCuoiTuan", cuoi);
    }

    private ChiTietDatPhongYeuCau timYeuCauTheoMa(DonDatPhongYeuCau y, long ma) {
        for (ChiTietDatPhongYeuCau d : y.getDanhSachPhong())
            if (d.getMaChiTiet() != null && d.getMaChiTiet() == ma)
                return d;
        return null;
    }

    private boolean khongDoi(ChiTietDatPhongYeuCau y, Map<String, Object> d) {
        return y.getMaPhong() == soLong(d.get("maPhong")) && Objects.equals(y.getNgayNhan(), ngay(d.get("ngayNhan")))
                && Objects.equals(y.getNgayTra(), ngay(d.get("ngayTra")));
    }

    private LocalDate ngay(Object x) {
        return x instanceof LocalDate ? (LocalDate) x : LocalDate.parse(String.valueOf(x));
    }

    private long soLong(Object x) {
        return ((Number) x).longValue();
    }

    private void kiemTraChuDon(Connection c, long maDon, NguoiThaoTac n) throws SQLException {
        if ("KHACH_HANG".equals(n.getLoai()) && !donDAO.thuocKhachHang(c, maDon, n.getMa()))
            throw loi(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
    }

    private void kiemTraNguoi(NguoiThaoTac n) {
        if (n == null || n.getMa() <= 0 || (!"QUAN_LY".equals(n.getLoai()) && !"KHACH_HANG".equals(n.getLoai())))
            throw loi(403, "KHONG_CO_QUYEN", "Người thao tác không hợp lệ.");
    }

    private NghiepVuException loi(int ma, String maLoi, String thongBao) {
        return new NghiepVuException(ma, maLoi, thongBao);
    }

    private static class DongDatDaTinh {
        private final ChiTietDatPhongYeuCau yeuCau;
        private final BigDecimal giaThuong, giaCuoiTuan, tienPhong;

        private DongDatDaTinh(ChiTietDatPhongYeuCau y, BigDecimal t, BigDecimal c, BigDecimal p) {
            yeuCau = y;
            giaThuong = t;
            giaCuoiTuan = c;
            tienPhong = p;
        }
    }
}
