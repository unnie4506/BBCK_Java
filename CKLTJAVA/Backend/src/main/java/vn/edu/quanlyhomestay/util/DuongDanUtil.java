package vn.edu.quanlyhomestay.util;

import java.util.ArrayList;
import java.util.List;

public final class DuongDanUtil {
    private DuongDanUtil() {
    }

    public static List<String> tach(String duongDan) {
        List<String> ketQua = new ArrayList<>();
        if (duongDan == null || duongDan.isBlank())
            return ketQua;
        for (String doan : duongDan.split("/"))
            if (!doan.isBlank())
                ketQua.add(doan);
        return ketQua;
    }

    public static long docMa(String giaTri) {
        try {
            long ma = Long.parseLong(giaTri);
            if (ma > 0)
                return ma;
        } catch (NumberFormatException boQua) {
        }
        throw new IllegalArgumentException("Mã phải là số nguyên dương.");
    }
}
