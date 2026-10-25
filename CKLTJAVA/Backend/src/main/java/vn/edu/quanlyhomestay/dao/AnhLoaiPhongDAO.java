package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.util.DocBanGhiUtil;
import java.sql.*;
import java.util.*;

public class AnhLoaiPhongDAO {
    public List<Map<String, Object>> timTheoLoai(Connection c, long maLoai) throws SQLException {
        String sql = "SELECT * FROM anh_loai_phong WHERE ma_loai_phong=? AND dang_hoat_dong=TRUE "
                + "ORDER BY thu_tu_hien_thi,ma_anh_loai_phong";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maLoai);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public long them(Connection c, long maLoai, String duongDan) throws SQLException {
        int thuTu = 0;
        try (PreparedStatement p = c.prepareStatement(
                "SELECT COALESCE(MAX(thu_tu_hien_thi),-1)+1 FROM anh_loai_phong WHERE ma_loai_phong=? AND dang_hoat_dong=TRUE")) {
            p.setLong(1, maLoai);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) thuTu = r.getInt(1);
            }
        }
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO anh_loai_phong(ma_loai_phong,duong_dan_anh,thu_tu_hien_thi) VALUES(?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, maLoai);
            p.setString(2, duongDan);
            p.setInt(3, thuTu);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("Không lấy được mã ảnh loại phòng.");
                return r.getLong(1);
            }
        }
    }

    public boolean thuocLoai(Connection c, long maAnh, long maLoai) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT 1 FROM anh_loai_phong WHERE ma_anh_loai_phong=? AND ma_loai_phong=? AND dang_hoat_dong=TRUE")) {
            p.setLong(1, maAnh);
            p.setLong(2, maLoai);
            try (ResultSet r = p.executeQuery()) { return r.next(); }
        }
    }

    public void datAnhDaiDien(Connection c, long maLoai, long maAnh) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE anh_loai_phong SET thu_tu_hien_thi=CASE WHEN ma_anh_loai_phong=? THEN 0 ELSE thu_tu_hien_thi+1 END WHERE ma_loai_phong=? AND dang_hoat_dong=TRUE")) {
            p.setLong(1, maAnh);
            p.setLong(2, maLoai);
            p.executeUpdate();
        }
    }

    public String voHieuHoa(Connection c, long maLoai, long maAnh) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE anh_loai_phong SET dang_hoat_dong=FALSE WHERE ma_anh_loai_phong=? AND ma_loai_phong=? AND dang_hoat_dong=TRUE")) {
            p.setLong(1, maAnh);
            p.setLong(2, maLoai);
            if (p.executeUpdate() != 1) return null;
        }
        try (PreparedStatement p = c.prepareStatement(
                "SELECT duong_dan_anh FROM anh_loai_phong WHERE ma_anh_loai_phong=?")) {
            p.setLong(1, maAnh);
            try (ResultSet r = p.executeQuery()) { return r.next() ? r.getString(1) : null; }
        }
    }
}
