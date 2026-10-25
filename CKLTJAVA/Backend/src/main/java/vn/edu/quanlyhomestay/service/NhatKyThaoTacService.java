package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.NhatKyThaoTacDAO;
import vn.edu.quanlyhomestay.dao.DonDatPhongDAO;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Ghi cùng transaction mà caller truyền vào; không tự mở hay commit kết nối. */
public class NhatKyThaoTacService {
    private final NhatKyThaoTacDAO dao = new NhatKyThaoTacDAO();
    private final DonDatPhongDAO donDAO = new DonDatPhongDAO();

    public void ghi(Connection ketNoi, NguoiThaoTac nguoi, String hanhDong, String moTa) throws SQLException {
        if (nguoi == null || (!"QUAN_LY".equals(nguoi.getLoai()) && !"KHACH_HANG".equals(nguoi.getLoai())))
            throw new IllegalArgumentException("Người thao tác không hợp lệ.");
        Long maQuanLy = "QUAN_LY".equals(nguoi.getLoai()) ? nguoi.getMa() : null;
        Long maTaiKhoan = "KHACH_HANG".equals(nguoi.getLoai()) ? nguoi.getMa() : null;
        dao.ghiNhatKy(ketNoi, nguoi.getLoai(), maQuanLy, maTaiKhoan, hanhDong, moTa,
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    public void ghi(Connection ketNoi, NguoiThaoTac nguoi, long maDon, String hanhDong, String moTa)
            throws SQLException {
        if (nguoi == null || (!"QUAN_LY".equals(nguoi.getLoai()) && !"KHACH_HANG".equals(nguoi.getLoai())))
            throw new IllegalArgumentException("Người thao tác không hợp lệ.");
        Long maQuanLy = "QUAN_LY".equals(nguoi.getLoai()) ? nguoi.getMa() : null;
        Long maTaiKhoan = "KHACH_HANG".equals(nguoi.getLoai()) ? nguoi.getMa() : null;
        dao.ghiNhatKy(ketNoi, nguoi.getLoai(), maQuanLy, maTaiKhoan, maDon, hanhDong, moTa,
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    public void ghiHeThong(Connection ketNoi, long maDon, String hanhDong, String moTa) throws SQLException {
        dao.ghiNhatKyHeThong(ketNoi, maDon, hanhDong, moTa);
    }

    public java.util.Map<String, Object> timTheoDon(long maDon, int trang, int kichThuoc, NguoiThaoTac nguoi)
            throws SQLException {
        if (nguoi == null || maDon <= 0 || trang < 1 || kichThuoc < 1 || kichThuoc > 100)
            throw new NghiepVuException(400, "THAM_SO_KHONG_HOP_LE", "Tham số xem nhật ký không hợp lệ.");
        try (Connection c = vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu.moKetNoi()) {
            if ("KHACH_HANG".equals(nguoi.getLoai()) && !donDAO.thuocKhachHang(c, maDon, nguoi.getMa()))
                throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
            java.util.Map<String, Object> kq = dao.timTheoDon(c, maDon, kichThuoc, (trang - 1) * kichThuoc);
            kq.put("trang", trang);
            kq.put("kichThuoc", kichThuoc);
            long tong = ((Number) kq.get("tongSoBanGhi")).longValue();
            kq.put("tongSoTrang", (tong + kichThuoc - 1) / kichThuoc);
            return kq;
        }
    }
}
