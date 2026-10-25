package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.util.DocBanGhiUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class BaoCaoDAO {
    public List<Map<String, Object>> doanhThu(Connection c, LocalDate tu, LocalDate den) throws SQLException {
        // BR-09 (Admin): ngày ghi nhận lấy từ ngày lập hóa đơn; tiền doanh thu là tổng tiền phòng của đơn HOAN_TAT, không cộng tiền cọc giữ lại.
        String sql = "SELECT DATE(h.lap_luc) AS ngay_lap, COUNT(*) AS so_hoa_don, SUM(h.tien_phong) AS doanh_thu FROM hoa_don h JOIN don_dat_phong d ON d.ma_don_dat_phong=h.ma_don_dat_phong WHERE h.trang_thai<>'DA_HUY' AND d.trang_thai='HOAN_TAT' AND h.lap_luc>=? AND h.lap_luc<? GROUP BY DATE(h.lap_luc) ORDER BY ngay_lap";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setTimestamp(1, Timestamp.valueOf(tu.atStartOfDay()));
            p.setTimestamp(2, Timestamp.valueOf(den.plusDays(1).atStartOfDay()));
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public long demPhongHoatDong(Connection c) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM phong WHERE dang_hoat_dong=TRUE")) {
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getLong(1);
            }
        }
    }

    public List<Map<String, Object>> cacDemDaBan(Connection c, LocalDate tu, LocalDate den) throws SQLException {
        String sql = "SELECT ct.ngay_nhan,ct.ngay_tra FROM chi_tiet_dat_phong ct JOIN don_dat_phong d ON d.ma_don_dat_phong=ct.ma_don_dat_phong WHERE ct.trang_thai='DA_TRA' AND d.trang_thai='HOAN_TAT' AND ct.ngay_nhan<=? AND ct.ngay_tra>?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, java.sql.Date.valueOf(den.plusDays(1)));
            p.setDate(2, java.sql.Date.valueOf(tu));
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> phongDatNhieu(Connection c, LocalDate tu, LocalDate den, int top)
            throws SQLException {
        // BR-09 (Admin): cắt khoảng ở theo [ngày nhận, ngày trả), chỉ cộng các đêm thực sự nằm trong khoảng báo cáo.
        String sql = "SELECT p.so_phong,l.ten AS loai_phong,p.tang,SUM(DATEDIFF(LEAST(ct.ngay_tra,?),GREATEST(ct.ngay_nhan,?))) AS so_dem,COUNT(DISTINCT ct.ma_don_dat_phong) AS so_luot_dat FROM chi_tiet_dat_phong ct JOIN don_dat_phong d ON d.ma_don_dat_phong=ct.ma_don_dat_phong JOIN phong p ON p.ma_phong=ct.ma_phong JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE ct.trang_thai='DA_TRA' AND d.trang_thai='HOAN_TAT' AND ct.ngay_nhan<? AND ct.ngay_tra>? GROUP BY p.ma_phong,p.so_phong,l.ten,p.tang ORDER BY so_dem DESC,so_luot_dat DESC,p.so_phong ASC LIMIT ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, java.sql.Date.valueOf(den.plusDays(1)));
            p.setDate(2, java.sql.Date.valueOf(tu));
            p.setDate(3, java.sql.Date.valueOf(den.plusDays(1)));
            p.setDate(4, java.sql.Date.valueOf(tu));
            p.setInt(5, top);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> khachQuayLai(Connection c, LocalDate tu, LocalDate den) throws SQLException {
        // BR-09 (Admin): khách quay lại có ít nhất hai đơn hoàn tất, tính theo ngày lập hóa đơn trong khoảng chọn.
        String sql = "SELECT k.ma_khach_hang,k.ho_ten,k.so_dien_thoai,COUNT(DISTINCT d.ma_don_dat_phong) AS so_don_hoan_tat_trong_ky,SUM(h.tien_phong) AS tong_chi_tieu_trong_ky,(SELECT MAX(ct.ngay_tra) FROM chi_tiet_dat_phong ct JOIN don_dat_phong dc ON dc.ma_don_dat_phong=ct.ma_don_dat_phong JOIN hoa_don hc ON hc.ma_don_dat_phong=dc.ma_don_dat_phong WHERE dc.ma_khach_hang=k.ma_khach_hang AND dc.trang_thai='HOAN_TAT' AND hc.trang_thai<>'DA_HUY' AND hc.lap_luc>=? AND hc.lap_luc<?) AS lan_luu_tru_gan_nhat,(SELECT COUNT(*) FROM don_dat_phong da JOIN hoa_don ha ON ha.ma_don_dat_phong=da.ma_don_dat_phong WHERE da.ma_khach_hang=k.ma_khach_hang AND da.trang_thai='HOAN_TAT' AND ha.trang_thai<>'DA_HUY') AS tong_so_don_hoan_tat FROM don_dat_phong d JOIN hoa_don h ON h.ma_don_dat_phong=d.ma_don_dat_phong JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang WHERE d.trang_thai='HOAN_TAT' AND h.trang_thai<>'DA_HUY' AND h.lap_luc>=? AND h.lap_luc<? GROUP BY k.ma_khach_hang,k.ho_ten,k.so_dien_thoai HAVING COUNT(DISTINCT d.ma_don_dat_phong)>=2 ORDER BY so_don_hoan_tat_trong_ky DESC,tong_chi_tieu_trong_ky DESC,k.ho_ten";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            Timestamp dau = Timestamp.valueOf(tu.atStartOfDay()),
                    cuoi = Timestamp.valueOf(den.plusDays(1).atStartOfDay());
            p.setTimestamp(1, dau);
            p.setTimestamp(2, cuoi);
            p.setTimestamp(3, dau);
            p.setTimestamp(4, cuoi);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }
}
