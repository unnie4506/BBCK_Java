package vn.edu.quanlyhomestay.util;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

/** Cấu hình màn chuyển khoản và thời gian giữ chỗ; không chứa thông tin tài khoản mặc định. */
public final class CauHinhDatCoc {
    private static final Properties P = nap();
    private CauHinhDatCoc() {}

    private static Properties nap() {
        Properties p = new Properties();
        try (InputStream in = CauHinhDatCoc.class.getClassLoader()
                .getResourceAsStream("cau-hinh-co-so-du-lieu.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) { }
        return p;
    }

    private static int so(String key, int macDinh, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(P.getProperty(key, "" + macDinh).trim()))); }
        catch (Exception ignored) { return macDinh; }
    }
    public static BigDecimal tyLeCoc() {
        try {
            BigDecimal n = new BigDecimal(P.getProperty("datCoc.tyLePhanTram", "30").trim());
            if (n.signum() >= 0 && n.compareTo(new BigDecimal("100")) <= 0) return n;
        } catch (Exception ignored) { }
        return new BigDecimal("30");
    }
    public static int thoiGianGiuChoPhut() { return so("datCoc.thoiGianGiuChoPhut", 15, 1, 1440); }
    public static boolean demoBat() { return Boolean.parseBoolean(P.getProperty("datCoc.demo", "false").trim()); }
    public static int demoSauGiay() { return so("datCoc.demoSauGiay", 0, 0, 86400); }
    public static String nganHang() { return P.getProperty("datCoc.nganHang", "").trim(); }
    public static String soTaiKhoan() { return P.getProperty("datCoc.soTaiKhoan", "").trim(); }
    public static String chuTaiKhoan() { return P.getProperty("datCoc.chuTaiKhoan", "").trim(); }
}
