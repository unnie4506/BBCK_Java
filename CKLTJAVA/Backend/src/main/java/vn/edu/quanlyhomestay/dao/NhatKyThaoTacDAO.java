package vn.edu.quanlyhomestay.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class NhatKyThaoTacDAO {
    public void ghiNhatKy(Connection ketNoi, String loaiNguoiThaoTac, Long maQuanLy, Long maTaiKhoanKhachHang,
            String hanhDong, String moTa, LocalDateTime thoiDiemTao) throws SQLException {
        ghiNhatKy(ketNoi, loaiNguoiThaoTac, maQuanLy, maTaiKhoanKhachHang, null, hanhDong, moTa, thoiDiemTao);
    }

    public void ghiNhatKyHeThong(Connection ketNoi, long maDonDatPhong, String hanhDong, String moTa)
            throws SQLException {
        String sql = "INSERT INTO nhat_ky_thao_tac(loai_nguoi_thao_tac,ma_quan_ly,ma_tai_khoan_khach_hang,ma_don_dat_phong,hanh_dong,mo_ta,tao_luc) VALUES('HE_THONG',NULL,NULL,?,?,?,CURRENT_TIMESTAMP)";
        try (PreparedStatement p = ketNoi.prepareStatement(sql)) {
            p.setLong(1, maDonDatPhong); p.setString(2, hanhDong); p.setString(3, moTa); p.executeUpdate();
        }
    }

    public void ghiNhatKy(Connection ketNoi, String loaiNguoiThaoTac, Long maQuanLy, Long maTaiKhoanKhachHang,
            Long maDonDatPhong, String hanhDong, String moTa, LocalDateTime thoiDiemTao) throws SQLException {
        String sql = "INSERT INTO nhat_ky_thao_tac "
                + "(loai_nguoi_thao_tac, ma_quan_ly, ma_tai_khoan_khach_hang, ma_don_dat_phong, hanh_dong, mo_ta, tao_luc) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, loaiNguoiThaoTac);
            setNullableLong(cauLenh, 2, maQuanLy);
            setNullableLong(cauLenh, 3, maTaiKhoanKhachHang);
            setNullableLong(cauLenh, 4, maDonDatPhong);
            cauLenh.setString(5, hanhDong);
            cauLenh.setString(6, moTa);
            cauLenh.setTimestamp(7, Timestamp.valueOf(thoiDiemTao));
            cauLenh.executeUpdate();
        }
    }

    public java.util.Map<String, Object> timTheoDon(Connection ketNoi, long maDon, int kichThuoc, int boQua)
            throws SQLException {
        long tong;
        try (PreparedStatement p = ketNoi
                .prepareStatement("SELECT COUNT(*) FROM nhat_ky_thao_tac WHERE ma_don_dat_phong=?")) {
            p.setLong(1, maDon);
            try (java.sql.ResultSet r = p.executeQuery()) {
                r.next();
                tong = r.getLong(1);
            }
        }
        String sql = "SELECT ma_nhat_ky_thao_tac,loai_nguoi_thao_tac,ma_quan_ly,ma_tai_khoan_khach_hang,ma_don_dat_phong,hanh_dong,mo_ta,tao_luc FROM nhat_ky_thao_tac WHERE ma_don_dat_phong=? ORDER BY tao_luc DESC,ma_nhat_ky_thao_tac DESC LIMIT ? OFFSET ?";
        java.util.List<java.util.Map<String, Object>> ds;
        try (PreparedStatement p = ketNoi.prepareStatement(sql)) {
            p.setLong(1, maDon);
            p.setInt(2, kichThuoc);
            p.setInt(3, boQua);
            try (java.sql.ResultSet r = p.executeQuery()) {
                ds = vn.edu.quanlyhomestay.util.DocBanGhiUtil.docDanhSach(r);
            }
        }
        java.util.Map<String, Object> kq = new java.util.LinkedHashMap<>();
        kq.put("danhSach", ds);
        kq.put("tongSoBanGhi", tong);
        return kq;
    }

    private void setNullableLong(PreparedStatement cauLenh, int chiSo, Long giaTri) throws SQLException {
        if (giaTri == null) {
            cauLenh.setNull(chiSo, java.sql.Types.BIGINT);
        } else {
            cauLenh.setLong(chiSo, giaTri);
        }
    }
}
