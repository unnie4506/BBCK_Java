package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.model.KhachHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class KhachHangDAO {
    public boolean hoSoDangHoatDong(Connection ketNoi, long maKhachHang) throws SQLException {
        String sql = "SELECT 1 FROM ho_so_khach_hang WHERE ma_khach_hang=? AND dang_hoat_dong=TRUE";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setLong(1, maKhachHang);
            try (ResultSet ketQua = cauLenh.executeQuery()) {
                return ketQua.next();
            }
        }
    }

    public boolean taiKhoanThuocHoSo(Connection ketNoi, long maTaiKhoan, long maKhachHang) throws SQLException {
        String sql = "SELECT 1 FROM tai_khoan_khach_hang WHERE ma_tai_khoan_khach_hang=? AND ma_khach_hang=? AND dang_hoat_dong=TRUE";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setLong(1, maTaiKhoan);
            cauLenh.setLong(2, maKhachHang);
            try (ResultSet ketQua = cauLenh.executeQuery()) {
                return ketQua.next();
            }
        }
    }

    public KhachHang timTheoThuDienTuKhoaDong(Connection ketNoi, String thuDienTu) throws SQLException {
        String sql = "SELECT a.ma_tai_khoan_khach_hang, a.ma_khach_hang, p.ho_ten, p.so_dien_thoai, "
                + "a.thu_dien_tu, a.mat_khau_da_bam, (a.dang_hoat_dong AND p.dang_hoat_dong) AS dang_hoat_dong, a.thoi_diem_mo_khoa "
                + "FROM tai_khoan_khach_hang a JOIN ho_so_khach_hang p "
                + "ON p.ma_khach_hang = a.ma_khach_hang WHERE a.thu_dien_tu = ? FOR UPDATE";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, thuDienTu);
            try (ResultSet ketQua = cauLenh.executeQuery()) {
                return ketQua.next() ? docKhachHang(ketQua) : null;
            }
        }
    }

    public KhachHang timTheoMaTaiKhoan(Connection ketNoi, long maTaiKhoan) throws SQLException {
        String sql = "SELECT a.ma_tai_khoan_khach_hang, a.ma_khach_hang, p.ho_ten, p.so_dien_thoai, "
                + "a.thu_dien_tu, a.mat_khau_da_bam, (a.dang_hoat_dong AND p.dang_hoat_dong) AS dang_hoat_dong, a.thoi_diem_mo_khoa "
                + "FROM tai_khoan_khach_hang a JOIN ho_so_khach_hang p " + "ON p.ma_khach_hang = a.ma_khach_hang "
                + "WHERE a.ma_tai_khoan_khach_hang = ? AND a.dang_hoat_dong = TRUE AND p.dang_hoat_dong = TRUE";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setLong(1, maTaiKhoan);
            try (ResultSet ketQua = cauLenh.executeQuery()) {
                return ketQua.next() ? docKhachHang(ketQua) : null;
            }
        }
    }

    public boolean thuDienTuDaTonTai(Connection ketNoi, String thuDienTu) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM tai_khoan_khach_hang WHERE thu_dien_tu = ?) "
                + "OR EXISTS(SELECT 1 FROM ho_so_khach_hang WHERE thu_dien_tu = ?)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, thuDienTu);
            cauLenh.setString(2, thuDienTu);
            try (ResultSet ketQua = cauLenh.executeQuery()) {
                ketQua.next();
                return ketQua.getBoolean(1);
            }
        }
    }

    public boolean thuDienTuDaTonTaiVoiKhachHangKhac(Connection ketNoi, String thuDienTu, long maTaiKhoan,
            long maKhachHang) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM tai_khoan_khach_hang "
                + "WHERE thu_dien_tu = ? AND ma_tai_khoan_khach_hang <> ?) "
                + "OR EXISTS(SELECT 1 FROM ho_so_khach_hang " + "WHERE thu_dien_tu = ? AND ma_khach_hang <> ?)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, thuDienTu);
            cauLenh.setLong(2, maTaiKhoan);
            cauLenh.setString(3, thuDienTu);
            cauLenh.setLong(4, maKhachHang);
            try (ResultSet ketQua = cauLenh.executeQuery()) {
                ketQua.next();
                return ketQua.getBoolean(1);
            }
        }
    }

    public long themHoSo(Connection ketNoi, String hoTen, String soDienThoai, String thuDienTu) throws SQLException {
        String sql = "INSERT INTO ho_so_khach_hang (ho_ten, so_dien_thoai, thu_dien_tu) VALUES (?, ?, ?)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cauLenh.setString(1, hoTen);
            cauLenh.setString(2, soDienThoai);
            cauLenh.setString(3, thuDienTu);
            cauLenh.executeUpdate();
            try (ResultSet keys = cauLenh.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Không nhận được mã hồ sơ khách hàng mới.");
                }
                return keys.getLong(1);
            }
        }
    }

    public boolean hoSoTrungThongTin(Connection ketNoi, String soDienThoai, String soGiayTo) throws SQLException {
        String sql = "SELECT 1 FROM ho_so_khach_hang WHERE so_dien_thoai=? OR (?<>'' AND so_giay_to=?) LIMIT 1";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, soDienThoai);
            cauLenh.setString(2, soGiayTo == null ? "" : soGiayTo);
            cauLenh.setString(3, soGiayTo == null ? "" : soGiayTo);
            try (ResultSet ketQua = cauLenh.executeQuery()) { return ketQua.next(); }
        }
    }

    public long themHoSoVangLai(Connection ketNoi, String hoTen, String soDienThoai, String thuDienTu,
            String soGiayTo) throws SQLException {
        String sql = "INSERT INTO ho_so_khach_hang(ho_ten,so_dien_thoai,thu_dien_tu,so_giay_to) VALUES(?,?,?,?)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cauLenh.setString(1, hoTen);
            cauLenh.setString(2, soDienThoai);
            cauLenh.setString(3, thuDienTu);
            cauLenh.setString(4, soGiayTo);
            cauLenh.executeUpdate();
            try (ResultSet keys = cauLenh.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không nhận được mã hồ sơ khách vãng lai.");
                return keys.getLong(1);
            }
        }
    }

    public long themTaiKhoan(Connection ketNoi, long maKhachHang, String thuDienTu, String matKhauDaBam)
            throws SQLException {
        String sql = "INSERT INTO tai_khoan_khach_hang "
                + "(ma_khach_hang, thu_dien_tu, mat_khau_da_bam) VALUES (?, ?, ?)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cauLenh.setLong(1, maKhachHang);
            cauLenh.setString(2, thuDienTu);
            cauLenh.setString(3, matKhauDaBam);
            cauLenh.executeUpdate();
            try (ResultSet keys = cauLenh.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Không nhận được mã tài khoản khách hàng mới.");
                }
                return keys.getLong(1);
            }
        }
    }

    public void capNhatHoSo(Connection ketNoi, long maKhachHang, String hoTen, String soDienThoai, String thuDienTu)
            throws SQLException {
        String capNhatHoSo = "UPDATE ho_so_khach_hang SET ho_ten = ?, so_dien_thoai = ?, thu_dien_tu = ? "
                + "WHERE ma_khach_hang = ?";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(capNhatHoSo)) {
            cauLenh.setString(1, hoTen);
            cauLenh.setString(2, soDienThoai);
            cauLenh.setString(3, thuDienTu);
            cauLenh.setLong(4, maKhachHang);
            if (cauLenh.executeUpdate() != 1) {
                throw new SQLException("Không tìm thấy hồ sơ khách hàng cần cập nhật.");
            }
        }

        String updateAccount = "UPDATE tai_khoan_khach_hang SET thu_dien_tu = ? WHERE ma_khach_hang = ?";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(updateAccount)) {
            cauLenh.setString(1, thuDienTu);
            cauLenh.setLong(2, maKhachHang);
            if (cauLenh.executeUpdate() != 1) {
                throw new SQLException("Không tìm thấy tài khoản khách hàng cần cập nhật.");
            }
        }
    }

    public void capNhatThoiDiemMoKhoa(Connection ketNoi, long maTaiKhoan, LocalDateTime thoiDiemMoKhoa)
            throws SQLException {
        String sql = "UPDATE tai_khoan_khach_hang SET thoi_diem_mo_khoa = ? WHERE ma_tai_khoan_khach_hang = ?";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            if (thoiDiemMoKhoa == null) {
                cauLenh.setNull(1, java.sql.Types.TIMESTAMP);
            } else {
                cauLenh.setTimestamp(1, Timestamp.valueOf(thoiDiemMoKhoa));
            }
            cauLenh.setLong(2, maTaiKhoan);
            cauLenh.executeUpdate();
        }
    }

    private KhachHang docKhachHang(ResultSet ketQua) throws SQLException {
        Timestamp lockedTimestamp = ketQua.getTimestamp("thoi_diem_mo_khoa");
        LocalDateTime thoiDiemMoKhoa = lockedTimestamp == null ? null : lockedTimestamp.toLocalDateTime();
        return new KhachHang(ketQua.getLong("ma_tai_khoan_khach_hang"), ketQua.getLong("ma_khach_hang"),
                ketQua.getString("ho_ten"), ketQua.getString("so_dien_thoai"), ketQua.getString("thu_dien_tu"),
                ketQua.getString("mat_khau_da_bam"), ketQua.getBoolean("dang_hoat_dong"), thoiDiemMoKhoa);
    }
}
