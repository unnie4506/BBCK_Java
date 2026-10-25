package vn.edu.quanlyhomestay.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class ThoiGianUtil {
    private static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");

    private ThoiGianUtil() {
    }

    public static LocalDate homNay() {
        return ZonedDateTime.now(MUI_GIO).toLocalDate();
    }
}
