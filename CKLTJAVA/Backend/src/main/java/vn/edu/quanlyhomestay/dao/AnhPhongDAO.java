package vn.edu.quanlyhomestay.dao;

import java.sql.*;
import java.util.*;

public class AnhPhongDAO {
    public List<Map<String, Object>> timTheoPhong(Connection c, long maPhong) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT * FROM anh_phong WHERE ma_phong=? AND dang_hoat_dong=TRUE AND duong_dan_anh LIKE '/api/cong-khai/anh-phong/%' ORDER BY thu_tu_hien_thi,ma_anh_phong")) {
            p.setLong(1, maPhong);
            try (ResultSet r = p.executeQuery()) {
                List<Map<String, Object>> l = new ArrayList<>();
                while (r.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("maAnhPhong", r.getLong("ma_anh_phong"));
                    m.put("maPhong", r.getLong("ma_phong"));
                    m.put("duongDanAnh", r.getString("duong_dan_anh"));
                    m.put("chuThich", r.getString("chu_thich"));
                    m.put("thuTuHienThi", r.getInt("thu_tu_hien_thi"));
                    l.add(m);
                }
                return l;
            }
        }
    }

    public long them(Connection c, long maPhong, String duongDan) throws SQLException {
        int thuTu = 0;
        try (PreparedStatement p = c.prepareStatement(
                "SELECT COALESCE(MAX(thu_tu_hien_thi),-1)+1 FROM anh_phong WHERE ma_phong=? AND dang_hoat_dong=TRUE")) {
            p.setLong(1, maPhong);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) thuTu = r.getInt(1);
            }
        }
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO anh_phong(ma_phong,duong_dan_anh,thu_tu_hien_thi) VALUES(?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, maPhong);
            p.setString(2, duongDan);
            p.setInt(3, thuTu);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next())
                    throw new SQLException("Không lấy được mã ảnh.");
                return r.getLong(1);
            }
        }
    }

    public boolean thuocPhong(Connection c, long maAnh, long maPhong) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT 1 FROM anh_phong WHERE ma_anh_phong=? AND ma_phong=? AND dang_hoat_dong=TRUE")) {
            p.setLong(1, maAnh);
            p.setLong(2, maPhong);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public void datAnhDaiDien(Connection c, long maPhong, long maAnh) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE anh_phong SET thu_tu_hien_thi=CASE WHEN ma_anh_phong=? THEN 0 ELSE thu_tu_hien_thi+1 END WHERE ma_phong=? AND dang_hoat_dong=TRUE")) {
            p.setLong(1, maAnh);
            p.setLong(2, maPhong);
            p.executeUpdate();
        }
    }

    public String voHieuHoa(Connection c, long maPhong, long maAnh) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE anh_phong SET dang_hoat_dong=FALSE WHERE ma_anh_phong=? AND ma_phong=? AND dang_hoat_dong=TRUE")) {
            p.setLong(1, maAnh);
            p.setLong(2, maPhong);
            if (p.executeUpdate() != 1)
                return null;
        }
        try (PreparedStatement p = c.prepareStatement("SELECT duong_dan_anh FROM anh_phong WHERE ma_anh_phong=?")) {
            p.setLong(1, maAnh);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString(1) : null;
            }
        }
    }
}
