package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.util.DocBanGhiUtil;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Các câu SQL về hóa đơn và thanh toán; kết nối do service quản lý. */
public class HoaDonDAO {
    public List<Map<String, Object>> theoDon(Connection c, long maDon, boolean khoa) throws SQLException {
        String sql = "SELECT * FROM hoa_don WHERE ma_don_dat_phong=?" + (khoa ? " FOR UPDATE" : "");
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> theoMa(Connection c, long maHoaDon, boolean khoa) throws SQLException {
        String sql = "SELECT h.*,d.ma_tra_cuu_don,d.trang_thai AS trang_thai_don,d.tien_coc_da_nhan,k.ho_ten,k.so_dien_thoai,k.thu_dien_tu FROM hoa_don h JOIN don_dat_phong d ON d.ma_don_dat_phong=h.ma_don_dat_phong JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang WHERE h.ma_hoa_don=?"
                + (khoa ? " FOR UPDATE" : "");
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maHoaDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public long them(Connection c, long maDon, String soHoaDon, BigDecimal tienPhong, BigDecimal cocTru,
            BigDecimal conPhaiThu, boolean daThanhToan) throws SQLException {
        String sql = "INSERT INTO hoa_don(ma_don_dat_phong,so_hoa_don,tien_phong,tien_coc_duoc_tru,so_tien_con_phai_thu,trang_thai,chot_luc) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, maDon);
            p.setString(2, soHoaDon);
            p.setBigDecimal(3, tienPhong);
            p.setBigDecimal(4, cocTru);
            p.setBigDecimal(5, conPhaiThu);
            p.setString(6, daThanhToan ? "DA_THANH_TOAN" : "CHUA_THANH_TOAN");
            if (daThanhToan)
                p.setTimestamp(7,
                        Timestamp.valueOf(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))));
            else
                p.setNull(7, Types.TIMESTAMP);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next())
                    throw new SQLException("Không tạo được mã hóa đơn.");
                return r.getLong(1);
            }
        }
    }

    public int themThanhToan(Connection c, long maDon, long maHoaDon, String phuongThuc, BigDecimal soTien,
            long maQuanLy, String ghiChu) throws SQLException {
        String sql = "INSERT INTO thanh_toan(ma_don_dat_phong,ma_hoa_don,loai_thanh_toan,phuong_thuc,so_tien,ma_quan_ly_ghi_nhan,ghi_chu) VALUES(?,?,'THANH_TOAN',?,?,?,?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon);
            p.setLong(2, maHoaDon);
            p.setString(3, phuongThuc);
            p.setBigDecimal(4, soTien);
            p.setLong(5, maQuanLy);
            p.setString(6, ghiChu);
            return p.executeUpdate();
        }
    }

    public int themTienCoc(Connection c, long maDon, String phuongThuc, BigDecimal soTien, Long maQuanLy,
            String maGiaoDich, String ghiChu) throws SQLException {
        String sql = "INSERT INTO thanh_toan(ma_don_dat_phong,loai_thanh_toan,phuong_thuc,so_tien,ma_giao_dich,ma_quan_ly_ghi_nhan,ghi_chu) VALUES(?,'TIEN_COC',?,?,?,?,?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon); p.setString(2, phuongThuc); p.setBigDecimal(3, soTien);
            p.setString(4, maGiaoDich); if (maQuanLy == null) p.setNull(5, java.sql.Types.BIGINT); else p.setLong(5, maQuanLy);
            p.setString(6, ghiChu); return p.executeUpdate();
        }
    }

    public List<Map<String, Object>> tienCocCuaDon(Connection c, long maDon) throws SQLException {
        String sql = "SELECT ma_thanh_toan,so_tien,phuong_thuc,ma_giao_dich,ghi_chu,ma_quan_ly_ghi_nhan,tao_luc FROM thanh_toan WHERE ma_don_dat_phong=? AND loai_thanh_toan='TIEN_COC' ORDER BY tao_luc,ma_thanh_toan";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon); try (ResultSet r = p.executeQuery()) { return DocBanGhiUtil.docDanhSach(r); }
        }
    }

    public BigDecimal tongDaThanhToan(Connection c, long maHoaDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT COALESCE(SUM(so_tien),0) FROM thanh_toan WHERE ma_hoa_don=? AND loai_thanh_toan='THANH_TOAN'")) {
            p.setLong(1, maHoaDon);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getBigDecimal(1);
            }
        }
    }

    public int capNhatSoTienConPhaiThu(Connection c, long maHoaDon, BigDecimal conPhaiThu, boolean daThanhToan)
            throws SQLException {
        String sql = daThanhToan
                ? "UPDATE hoa_don SET so_tien_con_phai_thu=?,trang_thai='DA_THANH_TOAN',chot_luc=NOW() WHERE ma_hoa_don=?"
                : "UPDATE hoa_don SET so_tien_con_phai_thu=? WHERE ma_hoa_don=? AND trang_thai='CHUA_THANH_TOAN'";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setBigDecimal(1, conPhaiThu);
            p.setLong(2, maHoaDon);
            return p.executeUpdate();
        }
    }

    public List<Map<String, Object>> cacDongHoaDon(Connection c, long maDon) throws SQLException {
        String sql = "SELECT x.ma_chi_tiet_dat_phong,x.ma_phong,p.so_phong,l.ten AS ten_loai_phong,x.ngay_nhan,x.ngay_tra,x.tien_phong,x.trang_thai FROM chi_tiet_dat_phong x JOIN phong p ON p.ma_phong=x.ma_phong JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE x.ma_don_dat_phong=? AND x.trang_thai<>'DA_HUY' ORDER BY x.ma_chi_tiet_dat_phong";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> cacKhoanThanhToan(Connection c, long maDon, long maHoaDon) throws SQLException {
        String sql = "SELECT ma_thanh_toan,loai_thanh_toan,phuong_thuc,so_tien,tao_luc,ghi_chu FROM thanh_toan WHERE ma_don_dat_phong=? AND (ma_hoa_don=? OR loai_thanh_toan='TIEN_COC') ORDER BY tao_luc,ma_thanh_toan";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon);
            p.setLong(2, maHoaDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public int themHoanCocDaXacNhan(Connection c, long maDon, String phuongThuc, BigDecimal soTien, String ghiChu)
            throws SQLException {
        String sql = "INSERT INTO thanh_toan(ma_don_dat_phong,loai_thanh_toan,phuong_thuc,so_tien,ghi_chu,trang_thai_hoan_coc,so_tien_thuc_hoan,phuong_thuc_hoan) VALUES(?,'HOAN_COC',?,?,?,'DA_XAC_NHAN',?,?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon);
            p.setString(2, phuongThuc);
            p.setBigDecimal(3, soTien);
            p.setString(4, ghiChu);
            p.setBigDecimal(5, soTien);
            p.setString(6, phuongThuc);
            return p.executeUpdate();
        }
    }
}
