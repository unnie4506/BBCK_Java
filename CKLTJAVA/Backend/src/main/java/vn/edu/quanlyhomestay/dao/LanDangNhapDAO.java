package vn.edu.quanlyhomestay.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LanDangNhapDAO {
    public static class TrangThaiKhoaTam {
        private final int soLanThatBai;
        private final LocalDateTime lanThatBaiGanNhat;

        public TrangThaiKhoaTam(int soLanThatBai, LocalDateTime lanThatBaiGanNhat) {
            this.soLanThatBai = soLanThatBai;
            this.lanThatBaiGanNhat = lanThatBaiGanNhat;
        }

        public int getSoLanThatBai() {
            return soLanThatBai;
        }

        public LocalDateTime getLanThatBaiGanNhat() {
            return lanThatBaiGanNhat;
        }
    }

    public void ghiNhanLanDangNhap(Connection ketNoi, String loaiTaiKhoan, Long maQuanLy, Long maTaiKhoanKhachHang,
            String dinhDanh, boolean dangNhapThanhCong, String diaChiIp, LocalDateTime thoiDiemThu)
            throws SQLException {
        String sql = "INSERT INTO lan_dang_nhap "
                + "(loai_tai_khoan, ma_quan_ly, ma_tai_khoan_khach_hang, dinh_danh_da_nhap, "
                + "dang_nhap_thanh_cong, dia_chi_ip, thoi_diem_thu) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, loaiTaiKhoan);
            setNullableLong(cauLenh, 2, maQuanLy);
            setNullableLong(cauLenh, 3, maTaiKhoanKhachHang);
            cauLenh.setString(4, dinhDanh);
            cauLenh.setBoolean(5, dangNhapThanhCong);
            cauLenh.setString(6, diaChiIp);
            cauLenh.setTimestamp(7, Timestamp.valueOf(thoiDiemThu));
            cauLenh.executeUpdate();
        }
    }

    public boolean khoaDinhDanhDangNhap(Connection ketNoi, String loaiTaiKhoan, String dinhDanh) throws SQLException {
        String sql = "SELECT GET_LOCK(?, 5)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, taoTenKhoaDangNhap(loaiTaiKhoan, dinhDanh));
            try (ResultSet ketQua = cauLenh.executeQuery()) {
                return ketQua.next() && ketQua.getInt(1) == 1;
            }
        }
    }

    public void moKhoaDinhDanhDangNhap(Connection ketNoi, String loaiTaiKhoan, String dinhDanh) throws SQLException {
        String sql = "SELECT RELEASE_LOCK(?)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, taoTenKhoaDangNhap(loaiTaiKhoan, dinhDanh));
            cauLenh.executeQuery().close();
        }
    }

    public TrangThaiKhoaTam layTrangThaiKhoaTam(Connection ketNoi, String loaiTaiKhoan, String dinhDanh,
            LocalDateTime batDauCuaSo) throws SQLException {
        String sql = "SELECT COUNT(*) AS so_lan_that_bai, MAX(thoi_diem_thu) AS lan_that_bai_gan_nhat "
                + "FROM lan_dang_nhap la WHERE la.loai_tai_khoan = ? AND la.dinh_danh_da_nhap = ? "
                + "AND la.dang_nhap_thanh_cong = FALSE AND la.thoi_diem_thu >= ? "
                + "AND la.ma_lan_dang_nhap > COALESCE((SELECT MAX(ok.ma_lan_dang_nhap) "
                + "FROM lan_dang_nhap ok WHERE ok.loai_tai_khoan = la.loai_tai_khoan "
                + "AND ok.dinh_danh_da_nhap = la.dinh_danh_da_nhap AND ok.dang_nhap_thanh_cong = TRUE), 0)";
        try (PreparedStatement cauLenh = ketNoi.prepareStatement(sql)) {
            cauLenh.setString(1, loaiTaiKhoan);
            cauLenh.setString(2, dinhDanh);
            cauLenh.setTimestamp(3, Timestamp.valueOf(batDauCuaSo));
            try (ResultSet ketQua = cauLenh.executeQuery()) {
                ketQua.next();
                Timestamp thoiDiem = ketQua.getTimestamp("lan_that_bai_gan_nhat");
                return new TrangThaiKhoaTam(ketQua.getInt("so_lan_that_bai"),
                        thoiDiem == null ? null : thoiDiem.toLocalDateTime());
            }
        }
    }

    private void setNullableLong(PreparedStatement cauLenh, int chiSo, Long giaTri) throws SQLException {
        if (giaTri == null) {
            cauLenh.setNull(chiSo, java.sql.Types.BIGINT);
        } else {
            cauLenh.setLong(chiSo, giaTri);
        }
    }

    private String taoTenKhoaDangNhap(String loaiTaiKhoan, String dinhDanh) throws SQLException {
        try {
            MessageDigest boBam = MessageDigest.getInstance("SHA-256");
            byte[] bam = boBam.digest((loaiTaiKhoan + ":" + dinhDanh).getBytes(StandardCharsets.UTF_8));
            StringBuilder chuoiThapLucPhan = new StringBuilder();
            for (int chiSo = 0; chiSo < 20; chiSo++) {
                chuoiThapLucPhan.append(String.format("%02x", bam[chiSo] & 0xff));
            }
            return "hs-dangNhap-" + chuoiThapLucPhan;
        } catch (NoSuchAlgorithmException ngoaiLe) {
            throw new SQLException("Không tạo được khóa chống đăng nhập đồng thời.", ngoaiLe);
        }
    }
}
