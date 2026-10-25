package vn.edu.quanlyhomestay.util;

import java.util.regex.Pattern;

public final class TienIchKiemTraDuLieu {
    private static final Pattern EMAIL_PATTERN = Pattern
            .compile("^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?"
                    + "(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$", Pattern.CASE_INSENSITIVE);

    private TienIchKiemTraDuLieu() {
    }

    public static String lamSach(String giaTri) {
        return giaTri == null ? null : giaTri.trim();
    }

    public static boolean laThuDienTu(String giaTri) {
        return giaTri != null && giaTri.length() <= 254 && EMAIL_PATTERN.matcher(giaTri).matches();
    }

    public static boolean laSoDienThoai(String giaTri) {
        return chuanHoaSoDienThoaiVietNam(giaTri) != null;
    }

    /** Chuẩn hóa điện thoại theo quy tắc danh mục Giai đoạn 3; số sai định dạng trả về null. */
    public static String chuanHoaSoDienThoaiVietNam(String giaTri) {
        if (giaTri == null)
            return null;
        String so = giaTri.trim().replaceAll("[ .-]", "");
        if (so.startsWith("+84"))
            so = "0" + so.substring(3);
        return so.matches("0(3|5|7|8|9)[0-9]{8}") ? so : null;
    }
}
