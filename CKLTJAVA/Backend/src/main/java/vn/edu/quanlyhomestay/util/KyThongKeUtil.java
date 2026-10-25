package vn.edu.quanlyhomestay.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Chia đêm theo ngày bắt đầu, độc lập với MySQL. // BR-09 (Admin) */
public final class KyThongKeUtil {
    private KyThongKeUtil() {
    }

    public static String khoa(LocalDate ngay, KyThongKe ky) {
        return switch (ky) {
        case NGAY -> ngay.toString();
        case THANG -> YearMonth.from(ngay).toString();
        case NAM -> String.valueOf(ngay.getYear());
        };
    }

    public static Map<String, Long> demDem(LocalDate ngayNhan, LocalDate ngayTra, LocalDate tuNgay, LocalDate denNgay,
            KyThongKe ky) {
        Map<String, Long> ketQua = new LinkedHashMap<>();
        LocalDate batDau = ngayNhan.isAfter(tuNgay) ? ngayNhan : tuNgay;
        LocalDate ketThuc = ngayTra.isBefore(denNgay.plusDays(1)) ? ngayTra : denNgay.plusDays(1);
        for (LocalDate ngay = batDau; ngay.isBefore(ketThuc); ngay = ngay.plusDays(1))
            ketQua.merge(khoa(ngay, ky), 1L, Long::sum);
        return ketQua;
    }

    public static List<LocalDate> cacMoc(LocalDate tuNgay, LocalDate denNgay, KyThongKe ky) {
        List<LocalDate> ketQua = new ArrayList<>();
        LocalDate moc = switch (ky) {
        case NGAY -> tuNgay;
        case THANG -> tuNgay.withDayOfMonth(1);
        case NAM -> tuNgay.withDayOfYear(1);
        };
        while (!moc.isAfter(denNgay)) {
            ketQua.add(moc);
            moc = switch (ky) {
            case NGAY -> moc.plusDays(1);
            case THANG -> moc.plusMonths(1);
            case NAM -> moc.plusYears(1);
            };
        }
        return ketQua;
    }

    /** Chạy độc lập bằng java KyThongKeUtil để rà các biên ngày/tháng/năm. */
    public static void main(String[] args) {
        kiemTra(demDem(LocalDate.parse("2026-01-31"), LocalDate.parse("2026-02-03"), LocalDate.parse("2026-01-31"),
                LocalDate.parse("2026-02-02"), KyThongKe.THANG).equals(Map.of("2026-01", 1L, "2026-02", 2L)),
                "Vắt qua tháng");
        kiemTra(demDem(LocalDate.parse("2025-12-31"), LocalDate.parse("2026-01-03"), LocalDate.parse("2025-12-31"),
                LocalDate.parse("2026-01-02"), KyThongKe.NAM).equals(Map.of("2025", 1L, "2026", 2L)), "Vắt qua năm");
        kiemTra(demDem(LocalDate.parse("2026-05-01"), LocalDate.parse("2026-05-02"), LocalDate.parse("2026-05-01"),
                LocalDate.parse("2026-05-02"), KyThongKe.NGAY).size() == 1, "Một đêm");
        kiemTra(demDem(LocalDate.parse("2026-04-01"), LocalDate.parse("2026-04-05"), LocalDate.parse("2026-04-03"),
                LocalDate.parse("2026-04-04"), KyThongKe.NGAY).equals(Map.of("2026-04-03", 1L, "2026-04-04", 1L)),
                "Cắt theo kỳ");
        kiemTra(cacMoc(LocalDate.parse("2026-05-12"), LocalDate.parse("2026-05-12"), KyThongKe.NGAY).size() == 1,
                "Kỳ một ngày");
        kiemTra(ChronoUnit.DAYS.between(LocalDate.parse("2024-02-28"), LocalDate.parse("2024-03-01")) == 2,
                "Năm nhuận");
        System.out.println("Hoàn tất 6 kiểm tra KyThongKeUtil.");
    }

    private static void kiemTra(boolean dung, String ten) {
        if (!dung)
            throw new AssertionError("FAIL: " + ten);
        System.out.println("PASS: " + ten);
    }
}
