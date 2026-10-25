package vn.edu.quanlyhomestay.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class KetNoiCoSoDuLieu {
    private KetNoiCoSoDuLieu() {
    }

    private static Properties taiCauHinh() throws SQLException {
        Properties thuocTinh = new Properties();
        try (InputStream dauVao = KetNoiCoSoDuLieu.class.getClassLoader()
                .getResourceAsStream("cau-hinh-co-so-du-lieu.properties")) {
            if (dauVao == null) {
                throw new SQLException(
                        "Không tìm thấy cau-hinh-co-so-du-lieu.properties. Hãy tạo file cấu hình từ cau-hinh-co-so-du-lieu.properties.example.");
            }
            thuocTinh.load(dauVao);
            return thuocTinh;
        } catch (IOException ngoaiLe) {
            throw new SQLException("Không đọc được cau-hinh-co-so-du-lieu.properties.", ngoaiLe);
        }
    }

    public static Connection moKetNoi() throws SQLException {
        Properties cauHinh = taiCauHinh();
        String diaChi = cauHinh.getProperty("coSoDuLieu.diaChi");
        String tenDangNhap = cauHinh.getProperty("coSoDuLieu.tenDangNhap");
        String matKhau = cauHinh.getProperty("coSoDuLieu.matKhau");

        if (diaChi == null || tenDangNhap == null || matKhau == null) {
            throw new SQLException(
                    "cau-hinh-co-so-du-lieu.properties thiếu coSoDuLieu.diaChi, coSoDuLieu.tenDangNhap hoặc coSoDuLieu.matKhau.");
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ngoaiLe) {
            throw new SQLException("Không tìm thấy trình điều khiển MySQL JDBC.", ngoaiLe);
        }
        return DriverManager.getConnection(diaChi, tenDangNhap, matKhau);
    }
}
