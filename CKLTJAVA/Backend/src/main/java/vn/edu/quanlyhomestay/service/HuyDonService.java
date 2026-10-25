package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.HoanCocDAO;
import vn.edu.quanlyhomestay.dao.DonDatPhongDAO;
import vn.edu.quanlyhomestay.dao.PhongDatDAO;
import vn.edu.quanlyhomestay.dao.HoaDonDAO;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import vn.edu.quanlyhomestay.util.ThoiGianUtil;
import java.math.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Dùng chung công thức hoàn cọc và transaction hủy cho các cổng. */
public class HuyDonService {
    private final HoanCocDAO dao = new HoanCocDAO();
    private final DonDatPhongDAO donDAO = new DonDatPhongDAO();
    private final PhongDatDAO phongDAO = new PhongDatDAO();
    private final NhatKyThaoTacService nhatKy = new NhatKyThaoTacService();
    private final DonDatPhongService donDatPhongService = new DonDatPhongService();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    /** Xem trước số tiền hoàn cho Quản lý; không thay đổi dữ liệu. */
    public Map<String, Object> xemTruocHuyQuanLy(long maDon, NguoiThaoTac nguoi) throws SQLException {
        kiemTraQuanLy(nguoi);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> don = donDAO.layDonDayDu(c, maDon);
            if (don.isEmpty())
                throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
            if (Set.of("CHO_COC", "HET_HAN").contains(don.get(0).get("trangThai")))
                throw new NghiepVuException(409, "DON_CHO_COC", "Hãy xử lý đơn chờ cọc trong màn đối soát cọc.");
            List<Map<String, Object>> cacDong = dao.cacDongDon(c, maDon);
            kiemTraDonChoPhepHuy(cacDong);
            return tinhHoanCoc(c, maDon, cacDong);
        }
    }

    /** Xem trước hoàn cọc khi quản lý hủy riêng một phòng trong đơn nhiều phòng. */
    public Map<String, Object> xemTruocHuyChiTietQuanLy(long maDon, long maChiTiet, NguoiThaoTac nguoi)
            throws SQLException {
        kiemTraQuanLy(nguoi);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> don = donDAO.layDonDayDu(c, maDon);
            if (don.isEmpty())
                throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
            if (Set.of("CHO_COC", "HET_HAN").contains(don.get(0).get("trangThai")))
                throw new NghiepVuException(409, "DON_CHO_COC", "Hãy xử lý đơn chờ cọc trong màn đối soát cọc.");
            List<Map<String, Object>> cacDong = dao.cacDongDon(c, maDon);
            Map<String, Object> dong = timDongCoTheHuy(cacDong, maChiTiet);
            return duKienHoanChoDong(c, maDon, cacDong, dong);
        }
    }

    /** Hủy đúng một phòng đã đặt, giữ nguyên các phòng khác và ghi hoàn cọc trong cùng transaction. */
    public Map<String, Object> huyChiTietQuanLy(long maDon, long maChiTiet, String lyDo, NguoiThaoTac nguoi)
            throws SQLException {
        kiemTraQuanLy(nguoi);
        if (lyDo == null || lyDo.isBlank())
            throw new NghiepVuException(422, "THIEU_LY_DO_HUY", "Vui lòng nhập lý do hủy.");
        if (lyDo.length() > 1000)
            throw new NghiepVuException(422, "LY_DO_QUA_DAI", "Lý do hủy tối đa 1000 ký tự.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> don = donDAO.khoaDeSua(c, maDon);
                if (don.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                if (Set.of("CHO_COC", "HET_HAN").contains(don.get(0).get("trangThai")))
                    throw new NghiepVuException(409, "DON_CHO_COC", "Hãy xử lý đơn chờ cọc trong màn đối soát cọc.");
                List<Map<String, Object>> cacDong = dao.cacDongDon(c, maDon);
                Map<String, Object> dong = timDongCoTheHuy(cacDong, maChiTiet);
                Map<String, Object> duKien = duKienHoanChoDong(c, maDon, cacDong, dong);
                if (dao.huyDong(c, maChiTiet, lyDo) != 1)
                    throw new NghiepVuException(409, "DON_VUA_THAY_DOI",
                            "Phòng vừa thay đổi trạng thái. Hãy tải lại rồi thử lại.");
                dao.giaiPhongPhongDat(c, ((Number) dong.get("maPhong")).longValue());
                donDatPhongService.capNhatTrangThaiTongHop(c, maDon);
                BigDecimal tienHoan = (BigDecimal) duKien.get("soTienHoanDuKien");
                if (tienHoan.signum() > 0)
                    hoaDonDAO.themHoanCocDaXacNhan(c, maDon, "CHUYEN_KHOAN", tienHoan,
                            "Quản lý xác nhận hoàn cọc khi hủy phòng: " + lyDo);
                nhatKy.ghi(c, nguoi, maDon, "QUAN_LY_HUY_PHONG",
                        "Hủy phòng trong đơn; hoàn " + tienHoan.toPlainString() + " đồng. Lý do: " + lyDo);
                c.commit();
                Map<String, Object> ketQua = new LinkedHashMap<>(duKien);
                ketQua.put("soTienHoanThucTe", tienHoan);
                ketQua.put("maChiTietDatPhong", maChiTiet);
                return ketQua;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Map<String, Object> timDongCoTheHuy(List<Map<String, Object>> cacDong, long maChiTiet) {
        for (Map<String, Object> dong : cacDong)
            if (((Number) dong.get("maChiTietDatPhong")).longValue() == maChiTiet) {
                if (!"DA_DAT".equals(dong.get("trangThai")))
                    throw new NghiepVuException(409, "PHONG_KHONG_THE_HUY",
                            "Chỉ phòng đang ở trạng thái Đã đặt mới có thể hủy.");
                return dong;
            }
        throw new NghiepVuException(404, "KHONG_TIM_THAY_CHI_TIET", "Không tìm thấy phòng trong đơn này.");
    }

    private Map<String, Object> duKienHoanChoDong(Connection c, long maDon, List<Map<String, Object>> cacDong,
            Map<String, Object> dongCanHuy) throws SQLException {
        BigDecimal coc = ((BigDecimal) dao.tongCocDaNhan(c, maDon).get(0).get("tienCocConLai"))
                .setScale(0, RoundingMode.HALF_UP);
        BigDecimal tong = BigDecimal.ZERO;
        for (Map<String, Object> dong : cacDong)
            if (!"DA_HUY".equals(dong.get("trangThai")))
                tong = tong.add((BigDecimal) dong.get("tienPhong"));
        BigDecimal tienHoan = BigDecimal.ZERO;
        if (coc.signum() > 0 && tong.signum() > 0) {
            int soNgay = (int) ChronoUnit.DAYS.between(ThoiGianUtil.homNay(),
                    LocalDate.parse(String.valueOf(dongCanHuy.get("ngayNhan"))));
            List<Map<String, Object>> bac = dao.chinhSach(c, Math.max(0, soNgay));
            BigDecimal tyLe = bac.isEmpty() ? BigDecimal.ZERO : (BigDecimal) bac.get(0).get("phanTramHoan");
            BigDecimal tiLeDong = ((BigDecimal) dongCanHuy.get("tienPhong")).divide(tong, 8, RoundingMode.HALF_UP);
            tienHoan = coc.multiply(tiLeDong).multiply(tyLe).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP)
                    .min(coc);
        }
        Map<String, Object> ketQua = new LinkedHashMap<>();
        ketQua.put("tienCocConLai", coc);
        ketQua.put("soTienHoanDuKien", tienHoan);
        ketQua.put("maChiTietDatPhong", dongCanHuy.get("maChiTietDatPhong"));
        return ketQua;
    }

    /** Hủy các phòng chưa nhận và ghi hoàn cọc trong cùng transaction. */
    public Map<String, Object> huyDonQuanLy(long maDon, String lyDo, BigDecimal soTienHoan, String phuongThucHoan,
            NguoiThaoTac nguoi) throws SQLException {
        kiemTraQuanLy(nguoi);
        if (lyDo == null || lyDo.isBlank())
            throw new NghiepVuException(422, "THIEU_LY_DO_HUY", "Vui lòng nhập lý do hủy.");
        if (lyDo.length() > 1000)
            throw new NghiepVuException(422, "LY_DO_QUA_DAI", "Lý do hủy tối đa 1000 ký tự.");
        if (phuongThucHoan == null || phuongThucHoan.isBlank())
            phuongThucHoan = "CHUYEN_KHOAN";
        if (!Set.of("TIEN_MAT", "CHUYEN_KHOAN", "VI_DIEN_TU").contains(phuongThucHoan))
            throw new NghiepVuException(422, "PHUONG_THUC_KHONG_HOP_LE", "Phương thức hoàn cọc không hợp lệ.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> don = donDAO.khoaDeSua(c, maDon);
                if (don.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                if (Set.of("CHO_COC", "HET_HAN").contains(don.get(0).get("trangThai")))
                    throw new NghiepVuException(409, "DON_CHO_COC", "Hãy xử lý đơn chờ cọc trong màn đối soát cọc.");
                List<Map<String, Object>> cacDong = dao.cacDongDon(c, maDon);
                kiemTraDonChoPhepHuy(cacDong);
                TreeSet<Long> cacMaPhong = new TreeSet<>();
                for (Map<String, Object> dong : cacDong)
                    if ("DA_DAT".equals(dong.get("trangThai")))
                        cacMaPhong.add(((Number) dong.get("maPhong")).longValue());
                for (long maPhong : cacMaPhong)
                    phongDAO.layDeKhoa(c, maPhong);

                Map<String, Object> duKien = tinhHoanCoc(c, maDon, cacDong);
                BigDecimal cocConGiu = (BigDecimal) duKien.get("tienCocConLai");
                BigDecimal tienHoan = soTienHoan == null ? (BigDecimal) duKien.get("soTienHoanDuKien")
                        : soTienHoan.setScale(0, RoundingMode.HALF_UP);
                if (tienHoan.signum() < 0 || tienHoan.compareTo(cocConGiu) > 0)
                    throw new NghiepVuException(422, "TIEN_HOAN_KHONG_HOP_LE",
                            "Tiền hoàn phải nằm trong khoảng 0 đến tiền cọc còn giữ.");

                int soDongDaHuy = 0;
                for (Map<String, Object> dong : cacDong) {
                    if (!"DA_DAT".equals(dong.get("trangThai")))
                        continue;
                    long maChiTiet = ((Number) dong.get("maChiTietDatPhong")).longValue();
                    long maPhong = ((Number) dong.get("maPhong")).longValue();
                    if (dao.huyDong(c, maChiTiet, lyDo) != 1)
                        throw new NghiepVuException(409, "DON_VUA_THAY_DOI",
                                "Đơn vừa thay đổi trạng thái. Hãy tải lại rồi thử lại.");
                    dao.giaiPhongPhongDat(c, maPhong);
                    soDongDaHuy++;
                }
                donDatPhongService.capNhatTrangThaiTongHop(c, maDon);
                if (tienHoan.signum() > 0)
                    hoaDonDAO.themHoanCocDaXacNhan(c, maDon, phuongThucHoan, tienHoan,
                            "Quản lý xác nhận hoàn cọc khi hủy: " + lyDo);
                nhatKy.ghi(c, nguoi, maDon, "QUAN_LY_HUY_DON",
                        "Hủy " + soDongDaHuy + " phòng; hoàn " + tienHoan.toPlainString() + " đồng. Lý do: " + lyDo);
                c.commit();
                Map<String, Object> ketQua = new LinkedHashMap<>(duKien);
                ketQua.put("soTienHoanThucTe", tienHoan);
                ketQua.put("soDongDaHuy", soDongDaHuy);
                ketQua.put("trangThaiHoanCoc", tienHoan.signum() > 0 ? "DA_XAC_NHAN" : "KHONG_CO_COC");
                return ketQua;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void kiemTraDonChoPhepHuy(List<Map<String, Object>> cacDong) {
        boolean coDongDaDat = false;
        for (Map<String, Object> dong : cacDong) {
            String trangThai = (String) dong.get("trangThai");
            if ("DA_DAT".equals(trangThai))
                coDongDaDat = true;
            else if (!"DA_HUY".equals(trangThai))
                throw new NghiepVuException(409, "KHONG_THE_HUY",
                        "Đơn đã có phòng nhận hoặc trả. Chỉ các phòng chưa nhận mới được hủy.");
        }
        if (!coDongDaDat)
            throw new NghiepVuException(409, "KHONG_THE_HUY", "Đơn không còn phòng đã đặt để hủy.");
    }

    private void kiemTraQuanLy(NguoiThaoTac nguoi) {
        if (nguoi == null || nguoi.getMa() <= 0 || !"QUAN_LY".equals(nguoi.getLoai()))
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Chỉ Quản lý mới được hủy đơn.");
    }

    public Map<String, Object> xemTruocHuyDon(long maTaiKhoan, long maDon) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            Map<String, Object> don = layDonCuaTaiKhoan(c, maTaiKhoan, maDon);
            if ("HET_HAN".equals(don.get("trangThaiDon")))
                throw new NghiepVuException(409, "DON_DA_HET_HAN", "Đơn đã hết hạn giữ chỗ; hãy tìm phòng lại.");
            List<Map<String, Object>> cacDong = dao.cacDongDon(c, maDon);
            kiemTraDuocPhepHuy(cacDong);
            return tinhHoanCoc(c, maDon, cacDong);
        }
    }

    public Map<String, Object> huyDonKhach(long maTaiKhoan, long maDon, String lyDo, NguoiThaoTac nguoi)
            throws SQLException {
        if (nguoi == null || !"KHACH_HANG".equals(nguoi.getLoai()) || nguoi.getMa() != maTaiKhoan)
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Không có quyền hủy đơn này.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> donKhoa = donDAO.donKhoa(c, maDon);
                if (donKhoa.isEmpty() || !donDAO.thuocKhachHang(c, maDon, maTaiKhoan))
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                if ("HET_HAN".equals(donKhoa.get(0).get("trangThai"))
                        || ("CHO_COC".equals(donKhoa.get(0).get("trangThai"))
                                && !Boolean.TRUE.equals(donKhoa.get(0).get("conHan"))))
                    throw new NghiepVuException(409, "DON_DA_HET_HAN", "Đơn đã hết hạn giữ chỗ; hãy tìm phòng lại.");
                List<Map<String, Object>> cacDong = dao.cacDongDon(c, maDon);
                kiemTraDuocPhepHuy(cacDong);
                TreeSet<Long> cacMaPhong = new TreeSet<>();
                for (Map<String, Object> d : cacDong)
                    cacMaPhong.add(((Number) d.get("maPhong")).longValue());
                for (long maPhong : cacMaPhong)
                    phongDAO.layDeKhoa(c, maPhong);
                Map<String, Object> hoan = tinhHoanCoc(c, maDon, cacDong);
                for (Map<String, Object> d : cacDong) {
                    if ("DA_DAT".equals(d.get("trangThai"))) {
                        long maPhong = ((Number) d.get("maPhong")).longValue();
                        if (dao.huyDong(c, ((Number) d.get("maChiTietDatPhong")).longValue(), lyDo) != 1)
                            throw new NghiepVuException(409, "DON_VUA_THAY_DOI",
                                    "Đơn vừa được cập nhật. Hãy tải lại trạng thái mới nhất.");
                        dao.giaiPhongPhongDat(c, maPhong);
                    }
                }
                // BR-12 (Khách): trạng thái đơn được tổng hợp bằng cùng phương thức lõi sau khi cập nhật các dòng.
                donDatPhongService.capNhatTrangThaiTongHop(c, maDon);
                BigDecimal tienHoan = (BigDecimal) hoan.get("soTienHoanDuKien");
                if (tienHoan.signum() > 0)
                    dao.themYeuCauHoanCoc(c, maDon, tienHoan, "Khách yêu cầu hoàn cọc khi hủy đơn.");
                // BR-10 (Khách): nhật ký hủy nằm trong cùng transaction với việc hủy và yêu cầu hoàn cọc.
                nhatKy.ghi(c, nguoi, maDon, "KHACH_HANG_HUY_DON",
                        "Khách hủy đơn; tiền hoàn nếu có đang chờ Quản lý xác nhận.");
                c.commit();
                Map<String, Object> ketQua = new LinkedHashMap<>(hoan);
                ketQua.put("trangThaiDon", "DA_HUY");
                ketQua.put("trangThaiHoanCoc", tienHoan.signum() > 0 ? "CHO_XU_LY" : "KHONG_CO_COC");
                return ketQua;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Map<String, Object> layDonCuaTaiKhoan(Connection c, long maTaiKhoan, long maDon) throws SQLException {
        List<Map<String, Object>> ds = dao.chiTietDonCuaKhach(c, maTaiKhoan, maDon);
        if (ds.isEmpty())
            throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
        Map<String, Object> don = new LinkedHashMap<>();
        don.put("maDonDatPhong", ds.get(0).get("maDonDatPhong"));
        don.put("trangThaiDon", ds.get(0).get("trangThaiDon"));
        return don;
    }

    private void kiemTraDuocPhepHuy(List<Map<String, Object>> cacDong) {
        // Trạng thái từng phòng là nguồn xác định phòng đã nhận/trả hay chưa; trạng thái đơn tổng hợp có thể cũ.
        // BR-07/08 (Khách): cho hủy nếu còn dòng DA_DAT và các dòng khác chỉ có thể là DA_HUY.
        boolean coDaDat = false;
        for (Map<String, Object> d : cacDong) {
            String tt = (String) d.get("trangThai");
            if ("DA_DAT".equals(tt))
                coDaDat = true;
            else if (!"DA_HUY".equals(tt))
                throw new NghiepVuException(409, "KHONG_THE_HUY",
                        "Đơn đã có phòng nhận hoặc đã kết thúc. Vui lòng liên hệ Quản lý.");
        }
        if (!coDaDat)
            throw new NghiepVuException(409, "KHONG_THE_HUY",
                    "Đơn không còn phòng có thể hủy. Vui lòng liên hệ Quản lý.");
    }

    private Map<String, Object> tinhHoanCoc(Connection c, long maDon, List<Map<String, Object>> cacDong)
            throws SQLException {
        // BR-07 (Khách): dùng chính sách và tỷ trọng tiền phòng của Giai đoạn 4; khách không truyền số tiền hoàn.
        BigDecimal coc = ((BigDecimal) dao.tongCocDaNhan(c, maDon).get(0).get("tienCocConLai")).setScale(0,
                RoundingMode.HALF_UP);
        BigDecimal tong = BigDecimal.ZERO;
        for (Map<String, Object> d : cacDong)
            if (!"DA_HUY".equals(d.get("trangThai")))
                tong = tong.add((BigDecimal) d.get("tienPhong"));
        BigDecimal deXuat = BigDecimal.ZERO;
        LocalDate homNay = ThoiGianUtil.homNay();
        if (coc.signum() > 0 && tong.signum() > 0) {
            for (Map<String, Object> d : cacDong)
                if ("DA_DAT".equals(d.get("trangThai"))) {
                    java.time.LocalDate ngayNhan = java.time.LocalDate.parse(String.valueOf(d.get("ngayNhan")));
                    int soNgay = (int) ChronoUnit.DAYS.between(homNay, ngayNhan);
                    BigDecimal tyLe = BigDecimal.ZERO;
                    List<Map<String, Object>> bac = dao.chinhSach(c, Math.max(0, soNgay));
                    if (!bac.isEmpty())
                        tyLe = (BigDecimal) bac.get(0).get("phanTramHoan");
                    BigDecimal tiLeDong = ((BigDecimal) d.get("tienPhong")).divide(tong, 8, RoundingMode.HALF_UP);
                    deXuat = deXuat.add(coc.multiply(tiLeDong).multiply(tyLe).divide(new BigDecimal("100"), 0,
                            RoundingMode.HALF_UP));
                }
        }
        if (deXuat.compareTo(coc) > 0)
            deXuat = coc;
        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("tienCocConLai", coc);
        kq.put("soTienHoanDuKien", deXuat);
        return kq;
    }
}
