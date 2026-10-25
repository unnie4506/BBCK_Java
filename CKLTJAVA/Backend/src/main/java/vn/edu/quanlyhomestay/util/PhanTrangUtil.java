package vn.edu.quanlyhomestay.util;

public final class PhanTrangUtil {
    private PhanTrangUtil() {
    }

    public static int docSoTrang(String giaTri) {
        try {
            return giaTri == null ? 1 : Math.max(1, Integer.parseInt(giaTri));
        } catch (NumberFormatException loi) {
            return 1;
        }
    }

    public static int docKichThuoc(String giaTri) {
        try {
            return giaTri == null ? 20 : Math.max(1, Math.min(100, Integer.parseInt(giaTri)));
        } catch (NumberFormatException loi) {
            return 20;
        }
    }
}
