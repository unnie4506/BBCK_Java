package vn.edu.quanlyhomestay.dao;

import java.sql.*;
import java.util.*;

/** DAO tập hợp các truy vấn danh mục của giai đoạn 3. Mọi câu SQL ở tầng DAO. */
public class DanhMucDAO {
    public List<Map<String, Object>> truyVan(Connection c, String sql, Object... thamSo) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            gan(p, thamSo);
            try (ResultSet r = p.executeQuery()) {
                return doc(r);
            }
        }
    }

    public int capNhat(Connection c, String sql, Object... thamSo) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            gan(p, thamSo);
            return p.executeUpdate();
        }
    }

    public long them(Connection c, String sql, Object... thamSo) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            gan(p, thamSo);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next())
                    throw new SQLException("Không lấy được mã bản ghi.");
                return r.getLong(1);
            }
        }
    }

    private void gan(PreparedStatement p, Object... thamSo) throws SQLException {
        for (int i = 0; i < thamSo.length; i++)
            p.setObject(i + 1, thamSo[i]);
    }

    private List<Map<String, Object>> doc(ResultSet r) throws SQLException {
        List<Map<String, Object>> ds = new ArrayList<>();
        ResultSetMetaData md = r.getMetaData();
        while (r.next()) {
            Map<String, Object> dong = new LinkedHashMap<>();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                Object v = r.getObject(i);
                if (v instanceof java.sql.Date)
                    v = ((java.sql.Date) v).toLocalDate().toString();
                else if (v instanceof java.sql.Timestamp)
                    v = ((java.sql.Timestamp) v).toLocalDateTime().toString();
                dong.put(chuyenCamel(md.getColumnLabel(i)), v);
            }
            ds.add(dong);
        }
        return ds;
    }

    private String chuyenCamel(String tenCot) {
        StringBuilder s = new StringBuilder();
        boolean vietHoa = false;
        for (char k : tenCot.toCharArray()) {
            if (k == '_') {
                vietHoa = true;
                continue;
            }
            s.append(vietHoa ? Character.toUpperCase(k) : k);
            vietHoa = false;
        }
        return s.toString();
    }
}
