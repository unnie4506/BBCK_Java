package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.model.TaiKhoanQuanLy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TaiKhoanQuanLyDAO {
    public TaiKhoanQuanLy timTheoTenDangNhapKhoaDong(Connection ketNoi, String tenDangNhap) throws SQLException {
        String sql = "SELECT ma_quan_ly, ten_dang_nhap, mat_khau_da_bam, ho_ten, dang_hoat_dong, thoi_diem_mo_khoa "
                + "FROM tai_khoan_quan_ly WHERE ten_dang_nhap = ? FOR UPDATE";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, tenDangNhap);
            try (ResultSet ketQua = cauLenh.executeQuery()) {
                if (!ketQua.next()) {
                    return null;
                }
                Timestamp lockedTimestamp = ketQua.getTimestamp("thoi_diem_mo_khoa");
                LocalDateTime thoiDiemMoKhoa = lockedTimestamp == null ? null : lockedTimestamp.toLocalDateTime();
                return new TaiKhoanQuanLy(ketQua.getLong("ma_quan_ly"), ketQua.getString("ten_dang_nhap"),
                        ketQua.getString("mat_khau_da_bam"), ketQua.getString("ho_ten"),
                        ketQua.getBoolean("dang_hoat_dong"), thoiDiemMoKhoa);
            }
        }
    }

    public void capNhatThoiDiemMoKhoa(Connection ketNoi, long maQuanLy, LocalDateTime thoiDiemMoKhoa)
            throws SQLException {
        String sql = "UPDATE tai_khoan_quan_ly SET thoi_diem_mo_khoa = ? WHERE ma_quan_ly = ?";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            if (thoiDiemMoKhoa == null) {
                cauLenh.setNull(1, java.sql.Types.TIMESTAMP);
            } else {
                cauLenh.setTimestamp(1, Timestamp.valueOf(thoiDiemMoKhoa));
            }
            cauLenh.setLong(2, maQuanLy);
            cauLenh.executeUpdate();
        }
    }
}
