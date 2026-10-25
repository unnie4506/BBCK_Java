package vn.edu.quanlyhomestay.util;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public final class DocBanGhiUtil {
    private DocBanGhiUtil() {
    }

    public static Map<String, Object> doc(ResultSet r) throws SQLException {
        ResultSetMetaData md = r.getMetaData();
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            Object v = r.getObject(i);
            if (v instanceof java.sql.Date d) {
                v = d.toLocalDate().toString();
            } else if (v instanceof Timestamp t) {
                v = t.toLocalDateTime().toString();
            } else if (v instanceof LocalDateTime thoiDiem) {
                v = thoiDiem.toString();
            } else if (v instanceof LocalDate ngay) {
                v = ngay.toString();
            }
            m.put(camel(md.getColumnLabel(i)), v);
        }
        return m;
    }

    public static List<Map<String, Object>> docDanhSach(ResultSet r) throws SQLException {
        List<Map<String, Object>> k = new ArrayList<>();
        while (r.next())
            k.add(doc(r));
        return k;
    }

    private static String camel(String t) {
        StringBuilder b = new StringBuilder();
        boolean hoa = false;
        for (char c : t.toCharArray()) {
            if (c == '_') {
                hoa = true;
                continue;
            }
            b.append(hoa ? Character.toUpperCase(c) : c);
            hoa = false;
        }
        return b.toString();
    }
}
