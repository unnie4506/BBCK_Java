package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.*;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Tạo hóa đơn, ghi nhận thanh toán và hoàn tất lần trả phòng cuối. */
public class HoaDonService {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final DonDatPhongDAO donDAO = new DonDatPhongDAO();
    private final ChiTietDatPhongDAO chiTietDAO = new ChiTietDatPhongDAO();
    private final CongKhaiDAO congKhaiDAO = new CongKhaiDAO();
    private final PhongDatDAO phongDAO = new PhongDatDAO();
    private final DonDatPhongService donService = new DonDatPhongService();
    private final NhatKyThaoTacService nhatKy = new NhatKyThaoTacService();

    public Map<String, Object> layTheoMa(long maHoaDon, NguoiThaoTac actor) throws SQLException {
        kiemTraQuanLy(actor);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = hoaDonDAO.theoMa(c, maHoaDon, false);
            if (ds.isEmpty())
                throw loi(404, "KHONG_TIM_THAY_HOA_DON", "Không tìm thấy hóa đơn.");
            return taoDuLieuHoaDon(c, ds.get(0));
        }
    }

    public Map<String, Object> layTheoDon(long maDon, NguoiThaoTac actor) throws SQLException {
        kiemTraQuanLy(actor);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = hoaDonDAO.theoDon(c, maDon, false);
            if (ds.isEmpty())
                throw loi(404, "KHONG_TIM_THAY_HOA_DON", "Đơn này chưa có hóa đơn.");
            return taoDuLieuHoaDon(c, hoaDonDAO.theoMa(c, soLong(ds.get(0).get("maHoaDon")), false).get(0));
        }
    }

    public Map<String, Object> thanhToan(long maHoaDon, String phuongThuc, BigDecimal soTien, NguoiThaoTac actor)
            throws SQLException {
        kiemTraQuanLy(actor);
        if (soTien == null || soTien.signum() <= 0)
            throw loi(422, "SO_TIEN_KHONG_HOP_LE", "Số tiền thanh toán phải lớn hơn 0.");
        if (!Set.of("TIEN_MAT", "CHUYEN_KHOAN", "VI_DIEN_TU").contains(phuongThuc))
            throw loi(422, "PHUONG_THUC_KHONG_HOP_LE", "Phương thức thanh toán không hợp lệ.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> h = hoaDonDAO.theoMa(c, maHoaDon, false);
                if (h.isEmpty())
                    throw loi(404, "KHONG_TIM_THAY_HOA_DON", "Không tìm thấy hóa đơn.");
                long maDon = soLong(h.get(0).get("maDonDatPhong"));
                List<Map<String, Object>> don = donDAO.khoaDeSua(c, maDon);
                if (don.isEmpty())
                    throw loi(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                h = hoaDonDAO.theoMa(c, maHoaDon, true);
                if (h.isEmpty())
                    throw loi(404, "KHONG_TIM_THAY_HOA_DON", "Không tìm thấy hóa đơn.");
                Map<String, Object> hoaDon = h.get(0);
                List<Map<String, Object>> dong = chiTietDAO.khoaTheoDon(c, maDon);
                if (!"CHUA_THANH_TOAN".equals(hoaDon.get("trangThai")))
                    throw loi(409, "HOA_DON_DA_THANH_TOAN", "Hóa đơn đã thanh toán hoặc đã hủy.");
                BigDecimal daThu = hoaDonDAO.tongDaThanhToan(c, maHoaDon);
                BigDecimal conPhaiThu = ((BigDecimal) hoaDon.get("tienPhong"))
                        .subtract((BigDecimal) hoaDon.get("tienCocDuocTru")).subtract(daThu).max(BigDecimal.ZERO);
                if (soTien.compareTo(conPhaiThu) > 0)
                    throw loi(422, "VUOT_SO_TIEN_CON_NO",
                            "Số tiền không được vượt số tiền còn phải thu là " + conPhaiThu.toPlainString() + " đồng.");
                hoaDonDAO.themThanhToan(c, maDon, maHoaDon, phuongThuc, soTien, actor.getMa(), "Thanh toán hóa đơn");
                BigDecimal conLai = conPhaiThu.subtract(soTien);
                boolean daThuDu = conLai.signum() == 0;
                if (hoaDonDAO.capNhatSoTienConPhaiThu(c, maHoaDon, conLai, daThuDu) != 1)
                    throw loi(409, "HOA_DON_VUA_THAY_DOI", "Hóa đơn vừa được cập nhật, vui lòng tải lại.");
                if (daThuDu) {
                    Map<String, Object> phongCuoi = null;
                    for (Map<String, Object> x : dong)
                        if ("DANG_O".equals(x.get("trangThai"))) {
                            if (phongCuoi != null)
                                throw loi(409, "HOA_DON_KHONG_HOP_LE",
                                        "Hóa đơn còn nhiều phòng chưa hoàn tất trả phòng.");
                            phongCuoi = x;
                        }
                    if (phongCuoi == null)
                        throw loi(409, "KHONG_CON_PHONG_CHO_TRA", "Không còn phòng đang ở để hoàn tất check-out.");
                    long maPhong = soLong(phongCuoi.get("maPhong"));
                    List<Map<String, Object>> p = phongDAO.layDeKhoa(c, maPhong);
                    if (p.isEmpty() || daoCapNhatTra(c, soLong(phongCuoi.get("maChiTietDatPhong")), maPhong) != 1)
                        throw loi(409, "PHONG_VUA_THAY_DOI", "Không thể hoàn tất trả phòng, vui lòng tải lại.");
                    donService.capNhatTrangThaiTongHop(c, maDon);
                }
                // BR-10 (Admin): nhật ký thanh toán ở cùng transaction với khoản thu và cập nhật hóa đơn.
                nhatKy.ghi(c, actor, maDon, "QUAN_LY_THANH_TOAN_HOA_DON", "Thu " + soTien.toPlainString()
                        + " đồng bằng " + phuongThuc + "; còn phải thu " + conLai.toPlainString() + " đồng.");
                c.commit();
                Map<String, Object> kq = new LinkedHashMap<>();
                kq.put("maHoaDon", maHoaDon);
                kq.put("soTienThanhToan", soTien);
                kq.put("tongDaThanhToan", daThu.add(soTien));
                kq.put("soTienConPhaiThu", conLai);
                kq.put("daHoanTatCheckOut", daThuDu);
                kq.put("trangThaiHoaDon", daThuDu ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN");
                kq.put("trangThaiDon", daThuDu ? "HOAN_TAT" : "DANG_O");
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Map<String, Object> taoDuLieuHoaDon(Connection c, Map<String, Object> hoaDon) throws SQLException {
        long maDon = soLong(hoaDon.get("maDonDatPhong")), maHoaDon = soLong(hoaDon.get("maHoaDon"));
        hoaDon.put("danhSachPhong", hoaDonDAO.cacDongHoaDon(c, maDon));
        hoaDon.put("danhSachThanhToan", hoaDonDAO.cacKhoanThanhToan(c, maDon, maHoaDon));
        BigDecimal daThu = hoaDonDAO.tongDaThanhToan(c, maHoaDon);
        hoaDon.put("tongDaThanhToan", daThu);
        hoaDon.put("soTienConPhaiThu", ((BigDecimal) hoaDon.get("tienPhong"))
                .subtract((BigDecimal) hoaDon.get("tienCocDuocTru")).subtract(daThu).max(BigDecimal.ZERO));
        return hoaDon;
    }

    private int daoCapNhatTra(Connection c, long maChiTiet, long maPhong) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE chi_tiet_dat_phong SET trang_thai='DA_TRA',thoi_diem_tra_thuc_te=NOW() WHERE ma_chi_tiet_dat_phong=? AND trang_thai='DANG_O'")) {
            p.setLong(1, maChiTiet);
            int n = p.executeUpdate();
            if (n == 1)
                try (PreparedStatement q = c.prepareStatement(
                        "UPDATE phong SET trang_thai='DANG_DON' WHERE ma_phong=? AND trang_thai='DANG_O' AND dang_hoat_dong=TRUE")) {
                    q.setLong(1, maPhong);
                    if (q.executeUpdate() != 1)
                        throw loi(409, "PHONG_VUA_THAY_DOI", "Trạng thái phòng vừa thay đổi.");
                }
            return n;
        }
    }

    private long soLong(Object x) {
        return ((Number) x).longValue();
    }

    private void kiemTraQuanLy(NguoiThaoTac a) {
        if (a == null || a.getMa() <= 0 || !"QUAN_LY".equals(a.getLoai()))
            throw loi(403, "KHONG_CO_QUYEN", "Chỉ Quản lý mới được xử lý hóa đơn.");
    }

    private NghiepVuException loi(int http, String ma, String noiDung) {
        return new NghiepVuException(http, ma, noiDung);
    }
}
