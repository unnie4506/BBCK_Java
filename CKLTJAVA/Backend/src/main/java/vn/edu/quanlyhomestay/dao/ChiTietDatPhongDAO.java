package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.util.DocBanGhiUtil;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class ChiTietDatPhongDAO {
    public List<Map<String, Object>> khoaTheoDon(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT * FROM chi_tiet_dat_phong WHERE ma_don_dat_phong=? ORDER BY ma_phong,ma_chi_tiet_dat_phong FOR UPDATE")) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> trungLich(Connection c, long maPhong, LocalDate nhan, LocalDate tra,
            Long boQuaChiTiet) throws SQLException {
        String sql = "SELECT d.ma_chi_tiet_dat_phong,d.ma_don_dat_phong,d.ngay_nhan,d.ngay_tra FROM chi_tiet_dat_phong d JOIN don_dat_phong don ON don.ma_don_dat_phong=d.ma_don_dat_phong WHERE d.ma_phong=? AND d.trang_thai<>'DA_HUY' AND (don.trang_thai IN ('DA_DAT','DANG_O') OR (don.trang_thai='CHO_COC' AND don.han_giu_cho>CURRENT_TIMESTAMP(3))) AND d.ngay_nhan<? AND d.ngay_tra>? AND (? IS NULL OR d.ma_chi_tiet_dat_phong<>?) LIMIT 1";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maPhong);
            p.setDate(2, java.sql.Date.valueOf(tra));
            p.setDate(3, java.sql.Date.valueOf(nhan));
            p.setObject(4, boQuaChiTiet);
            p.setObject(5, boQuaChiTiet);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> trungLichNgoaiDon(Connection c, long maPhong, LocalDate nhan, LocalDate tra,
            long boQuaDon) throws SQLException {
        String sql = "SELECT d.ma_chi_tiet_dat_phong,d.ma_don_dat_phong FROM chi_tiet_dat_phong d JOIN don_dat_phong don ON don.ma_don_dat_phong=d.ma_don_dat_phong WHERE d.ma_phong=? AND d.trang_thai<>'DA_HUY' AND (don.trang_thai IN ('DA_DAT','DANG_O') OR (don.trang_thai='CHO_COC' AND don.han_giu_cho>CURRENT_TIMESTAMP(3))) AND d.ma_don_dat_phong<>? AND d.ngay_nhan<? AND d.ngay_tra>? LIMIT 1";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maPhong);
            p.setLong(2, boQuaDon);
            p.setDate(3, java.sql.Date.valueOf(tra));
            p.setDate(4, java.sql.Date.valueOf(nhan));
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public long them(Connection c, long maDon, long maPhong, LocalDate nhan, LocalDate tra, BigDecimal giaThuong,
            BigDecimal giaCuoiTuan, BigDecimal tienPhong) throws SQLException {
        String sql = "INSERT INTO chi_tiet_dat_phong(ma_don_dat_phong,ma_phong,ngay_nhan,ngay_tra,gia_ngay_thuong_luc_dat,gia_cuoi_tuan_luc_dat,tien_phong,trang_thai) VALUES(?,?,?,?,?,?,?,'DA_DAT')";
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, maDon);
            p.setLong(2, maPhong);
            p.setDate(3, java.sql.Date.valueOf(nhan));
            p.setDate(4, java.sql.Date.valueOf(tra));
            p.setBigDecimal(5, giaThuong);
            p.setBigDecimal(6, giaCuoiTuan);
            p.setBigDecimal(7, tienPhong);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next())
                    throw new SQLException("Không nhận được mã chi tiết đặt phòng.");
                long ma = r.getLong(1);
                ghiGiaTungDem(c, ma, nhan, tra, giaThuong, giaCuoiTuan);
                return ma;
            }
        }
    }

    public int capNhatDatPhong(Connection c, long maChiTiet, long maPhong, LocalDate nhan, LocalDate tra,
            BigDecimal giaThuong, BigDecimal giaCuoiTuan, BigDecimal tienPhong) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE chi_tiet_dat_phong SET ma_phong=?,ngay_nhan=?,ngay_tra=?,gia_ngay_thuong_luc_dat=?,gia_cuoi_tuan_luc_dat=?,tien_phong=? WHERE ma_chi_tiet_dat_phong=? AND trang_thai='DA_DAT'")) {
            p.setLong(1, maPhong);
            p.setDate(2, java.sql.Date.valueOf(nhan));
            p.setDate(3, java.sql.Date.valueOf(tra));
            p.setBigDecimal(4, giaThuong);
            p.setBigDecimal(5, giaCuoiTuan);
            p.setBigDecimal(6, tienPhong);
            p.setLong(7, maChiTiet);
            int n = p.executeUpdate();
            if (n == 1)
                ghiGiaTungDem(c, maChiTiet, nhan, tra, giaThuong, giaCuoiTuan);
            return n;
        }
    }

    public int giaHan(Connection c, long maChiTiet, LocalDate ngayNhan, LocalDate ngayTra, BigDecimal giaThuong,
            BigDecimal giaCuoiTuan, BigDecimal tienPhong) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE chi_tiet_dat_phong SET ngay_tra=?,tien_phong=? WHERE ma_chi_tiet_dat_phong=? AND trang_thai='DANG_O'")) {
            p.setDate(1, java.sql.Date.valueOf(ngayTra));
            p.setBigDecimal(2, tienPhong);
            p.setLong(3, maChiTiet);
            int n = p.executeUpdate();
            if (n == 1)
                ghiGiaTungDem(c, maChiTiet, ngayNhan, ngayTra, giaThuong, giaCuoiTuan);
            return n;
        }
    }

    public int traPhong(Connection c, long maChiTiet, LocalDate ngayTra, BigDecimal tienPhong, boolean capNhatNgay)
            throws SQLException {
        String sql = capNhatNgay
                ? "UPDATE chi_tiet_dat_phong SET ngay_tra=?,tien_phong=?,trang_thai='DA_TRA',thoi_diem_tra_thuc_te=NOW() WHERE ma_chi_tiet_dat_phong=? AND trang_thai='DANG_O'"
                : "UPDATE chi_tiet_dat_phong SET trang_thai='DA_TRA',thoi_diem_tra_thuc_te=NOW() WHERE ma_chi_tiet_dat_phong=? AND trang_thai='DANG_O'";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            if (capNhatNgay) {
                p.setDate(1, java.sql.Date.valueOf(ngayTra));
                p.setBigDecimal(2, tienPhong);
                p.setLong(3, maChiTiet);
            } else
                p.setLong(1, maChiTiet);
            int n = p.executeUpdate();
            if (n == 1 && capNhatNgay) {
                try (PreparedStatement d = c.prepareStatement(
                        "SELECT ngay_nhan,gia_ngay_thuong_luc_dat,gia_cuoi_tuan_luc_dat FROM chi_tiet_dat_phong WHERE ma_chi_tiet_dat_phong=?")) {
                    d.setLong(1, maChiTiet);
                    try (ResultSet r = d.executeQuery()) {
                        if (r.next())
                            ghiGiaTungDem(c, maChiTiet, r.getDate(1).toLocalDate(), ngayTra, r.getBigDecimal(2),
                                    r.getBigDecimal(3));
                    }
                }
            }
            return n;
        }
    }

    public int boPhong(Connection c, long maChiTiet, String lyDo) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE chi_tiet_dat_phong SET trang_thai='DA_HUY',huy_luc=NOW(),ly_do_huy=? WHERE ma_chi_tiet_dat_phong=? AND trang_thai='DA_DAT'")) {
            p.setString(1, lyDo);
            p.setLong(2, maChiTiet);
            return p.executeUpdate();
        }
    }

    public void ghiGiaTungDem(Connection c, long maChiTiet, LocalDate nhan, LocalDate tra, BigDecimal giaThuong,
            BigDecimal giaCuoiTuan) throws SQLException {
        try (PreparedStatement d = c
                .prepareStatement("DELETE FROM gia_tung_dem_dat_phong WHERE ma_chi_tiet_dat_phong=?")) {
            d.setLong(1, maChiTiet);
            d.executeUpdate();
        }
        String sql = "INSERT INTO gia_tung_dem_dat_phong(ma_chi_tiet_dat_phong,ngay_luu_tru,gia_dem) VALUES(?,?,?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            for (LocalDate ngay = nhan; ngay.isBefore(tra); ngay = ngay.plusDays(1)) {
                p.setLong(1, maChiTiet);
                p.setDate(2, java.sql.Date.valueOf(ngay));
                java.time.DayOfWeek thu = ngay.getDayOfWeek();
                p.setBigDecimal(3,
                        thu == java.time.DayOfWeek.FRIDAY || thu == java.time.DayOfWeek.SATURDAY ? giaCuoiTuan
                                : giaThuong);
                p.addBatch();
            }
            p.executeBatch();
        }
    }
}
