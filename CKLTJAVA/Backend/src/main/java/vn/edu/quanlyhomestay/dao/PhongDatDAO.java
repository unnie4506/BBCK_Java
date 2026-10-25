package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.util.DocBanGhiUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** DAO đọc phòng và mức giá để phục vụ tìm phòng, tạo đơn. */
public class PhongDatDAO {
    public List<Map<String, Object>> timPhongTrong(Connection c, LocalDate ngayNhan, LocalDate ngayTra, Long maLoai,
            Integer tang, Long boQuaMaDon) throws SQLException {
        String sql = "SELECT p.ma_phong,p.so_phong,p.tang,l.ten AS loai,l.suc_chua,l.gia_ngay_thuong,l.gia_cuoi_tuan FROM phong p JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE p.dang_hoat_dong=TRUE AND l.dang_hoat_dong=TRUE AND p.trang_thai='TRONG' AND (? IS NULL OR p.ma_loai_phong=?) AND (? IS NULL OR p.tang=?) AND NOT EXISTS (SELECT 1 FROM chi_tiet_dat_phong d JOIN don_dat_phong don ON don.ma_don_dat_phong=d.ma_don_dat_phong WHERE d.ma_phong=p.ma_phong AND d.trang_thai<>'DA_HUY' AND (don.trang_thai IN ('DA_DAT','DANG_O') OR (don.trang_thai='CHO_COC' AND don.han_giu_cho>CURRENT_TIMESTAMP(3))) AND d.ngay_nhan<? AND d.ngay_tra>? AND (? IS NULL OR d.ma_don_dat_phong<>?)) ORDER BY p.tang,p.so_phong";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setObject(1, maLoai);
            p.setObject(2, maLoai);
            p.setObject(3, tang);
            p.setObject(4, tang);
            p.setDate(5, java.sql.Date.valueOf(ngayTra));
            p.setDate(6, java.sql.Date.valueOf(ngayNhan));
            p.setObject(7, boQuaMaDon);
            p.setObject(8, boQuaMaDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> layDeKhoa(Connection c, long maPhong) throws SQLException {
        String sql = "SELECT p.ma_phong,p.so_phong,p.trang_thai,p.dang_hoat_dong,l.dang_hoat_dong AS loai_dang_hoat_dong,l.suc_chua,l.gia_ngay_thuong,l.gia_cuoi_tuan FROM phong p JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE p.ma_phong=? FOR UPDATE";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maPhong);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }
}
