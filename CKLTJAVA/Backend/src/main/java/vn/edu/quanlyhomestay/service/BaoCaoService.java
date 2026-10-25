package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.BaoCaoDAO;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.util.*;
import java.math.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BaoCaoService {
    private final BaoCaoDAO dao = new BaoCaoDAO();

    public Map<String, Object> doanhThu(LocalDate tu, LocalDate den, KyThongKe ky, NguoiThaoTac nguoi)
            throws SQLException {
        // BR-09 (Admin): kỳ NGAY/THANG/NAM chỉ áp dụng cho doanh thu và tỷ lệ lấp đầy.
        LocalDate[] kyHopLe = kiemTra(tu, den, ky, nguoi);
        tu = kyHopLe[0];
        den = kyHopLe[1];
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> raw = dao.doanhThu(c, tu, den);
            Map<String, Map<String, Object>> theoNgay = new HashMap<>();
            for (Map<String, Object> r : raw)
                theoNgay.put(String.valueOf(r.get("ngayLap")), r);
            Map<String, BigDecimal> tienTheoKy = new HashMap<>();
            Map<String, Long> hoaDonTheoKy = new HashMap<>();
            BigDecimal tong = BigDecimal.ZERO;
            long soHoaDon = 0;
            for (LocalDate n = tu; n.isBefore(den.plusDays(1)); n = n.plusDays(1)) {
                Map<String, Object> row = theoNgay.get(n.toString());
                if (row != null) {
                    String k = KyThongKeUtil.khoa(n, ky);
                    BigDecimal v = (BigDecimal) row.get("doanhThu");
                    long dem = ((Number) row.get("soHoaDon")).longValue();
                    tienTheoKy.merge(k, v, BigDecimal::add);
                    hoaDonTheoKy.merge(k, dem, Long::sum);
                    tong = tong.add(v);
                    soHoaDon += dem;
                }
            }
            List<Map<String, Object>> danhSach = new ArrayList<>();
            if (soHoaDon > 0)
                for (LocalDate moc : KyThongKeUtil.cacMoc(tu, den, ky)) {
                    String k = KyThongKeUtil.khoa(moc, ky);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("ky", k);
                    item.put("doanhThu", tienTheoKy.getOrDefault(k, BigDecimal.ZERO));
                    item.put("soHoaDon", hoaDonTheoKy.getOrDefault(k, 0L));
                    danhSach.add(item);
                }
            Map<String, Object> data = khung(tu, den, ky, danhSach);
            data.put("tongDoanhThu", tong);
            data.put("tongSoHoaDon", soHoaDon);
            data.put("doanhThuTrungBinhMoiHoaDon", soHoaDon == 0 ? BigDecimal.ZERO
                    : tong.divide(BigDecimal.valueOf(soHoaDon), 0, RoundingMode.HALF_UP));
            data.put("coDuLieu", soHoaDon > 0);
            return data;
        }
    }

    public Map<String, Object> tyLeLapDay(LocalDate tu, LocalDate den, KyThongKe ky, NguoiThaoTac nguoi)
            throws SQLException {
        // BR-09 (Admin): kỳ NGAY/THANG/NAM chỉ áp dụng cho doanh thu và tỷ lệ lấp đầy.
        LocalDate[] rg = kiemTra(tu, den, ky, nguoi);
        tu = rg[0];
        den = rg[1];
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            // BR-09 (Admin): mẫu số dùng số phòng đang hoạt động hiện tại cho mọi kỳ báo cáo.
            long phong = dao.demPhongHoatDong(c);
            List<Map<String, Object>> raw = dao.cacDemDaBan(c, tu, den);
            Map<String, Long> dem = new HashMap<>();
            // BR-09 (Admin): mỗi đêm thuộc ngày bắt đầu của nó, sau đó được cộng vào kỳ tương ứng.
            for (Map<String, Object> r : raw) {
                LocalDate n = LocalDate.parse(String.valueOf(r.get("ngayNhan"))),
                        x = LocalDate.parse(String.valueOf(r.get("ngayTra")));
                KyThongKeUtil.demDem(n, x, tu, den, ky).forEach((k, v) -> dem.merge(k, v, Long::sum));
            }
            List<Map<String, Object>> ds = new ArrayList<>();
            long tongDem = 0, tongKhaDung = 0;
            if (!dem.isEmpty())
                for (LocalDate moc : KyThongKeUtil.cacMoc(tu, den, ky)) {
                    LocalDate dau = switch (ky) {
                    case NGAY -> moc;
                    case THANG -> moc.withDayOfMonth(1);
                    case NAM -> moc.withDayOfYear(1);
                    };
                    LocalDate cuoi = switch (ky) {
                    case NGAY -> moc;
                    case THANG -> moc.withDayOfMonth(moc.lengthOfMonth());
                    case NAM -> moc.withDayOfYear(moc.lengthOfYear());
                    };
                    if (dau.isBefore(tu))
                        dau = tu;
                    if (cuoi.isAfter(den))
                        cuoi = den;
                    long demKy = dem.getOrDefault(KyThongKeUtil.khoa(moc, ky), 0L);
                    long demNgay = ChronoUnit.DAYS.between(dau, cuoi) + 1;
                    long khaDung = phong * demNgay;
                    Map<String, Object> it = new LinkedHashMap<>();
                    it.put("ky", KyThongKeUtil.khoa(moc, ky));
                    it.put("soDemDaBan", demKy);
                    it.put("soDemKhaDung", khaDung);
                    it.put("tyLe", phanTram(demKy, khaDung));
                    ds.add(it);
                    tongDem += demKy;
                    tongKhaDung += khaDung;
                }
            Map<String, Object> data = khung(tu, den, ky, ds);
            data.put("tongSoDemDaBan", tongDem);
            data.put("tongSoDemKhaDung", tongKhaDung);
            data.put("tyLeChung", phanTram(tongDem, tongKhaDung));
            data.put("coDuLieu", tongDem > 0);
            data.put("soPhongDangHoatDong", phong);
            return data;
        }
    }

    public Map<String, Object> phongDatNhieu(LocalDate tu, LocalDate den, int top, NguoiThaoTac nguoi)
            throws SQLException {
        LocalDate[] rg = kiemTra(tu, den, null, nguoi);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = dao.phongDatNhieu(c, rg[0], rg[1], top);
            Map<String, Object> d = khung(rg[0], rg[1], null, ds);
            d.put("coDuLieu", !ds.isEmpty());
            return d;
        }
    }

    public Map<String, Object> khachQuayLai(LocalDate tu, LocalDate den, NguoiThaoTac nguoi) throws SQLException {
        LocalDate[] rg = kiemTra(tu, den, null, nguoi);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = dao.khachQuayLai(c, rg[0], rg[1]);
            Map<String, Object> d = khung(rg[0], rg[1], null, ds);
            d.put("coDuLieu", !ds.isEmpty());
            return d;
        }
    }

    private LocalDate[] kiemTra(LocalDate tu, LocalDate den, KyThongKe ky, NguoiThaoTac n) {
        if (n == null || !"QUAN_LY".equals(n.getLoai()))
            throw new NghiepVuException(404, "KHONG_TIM_THAY", "Không tìm thấy tài nguyên.");
        if (tu == null || den == null)
            throw loi("THIEU_KHOANG_NGAY", "Cần chọn ngày bắt đầu và ngày kết thúc.");
        if (tu.isAfter(den))
            throw loi("KHOANG_NGAY_KHONG_HOP_LE", "Ngày bắt đầu phải bằng hoặc trước ngày kết thúc.");
        if (tu.isBefore(den.minusYears(5)))
            throw loi("VUOT_QUA_5_NAM", "Khoảng báo cáo tối đa là 5 năm.");
        if (ky == KyThongKe.NGAY && ChronoUnit.DAYS.between(tu, den) > 365)
            throw loi("VUOT_QUA_366_NGAY", "Kỳ ngày tối đa là 366 ngày.");
        LocalDate homNay = ThoiGianUtil.homNay();
        // BR-09 (Admin): giả định đã chốt, nếu khoảng chọn vượt hôm nay thì cắt đến hôm nay và trả daCatDenHomNay.
        if (tu.isAfter(homNay))
            return new LocalDate[] { tu, tu.minusDays(1) };
        return new LocalDate[] { tu, den.isAfter(homNay) ? homNay : den };
    }

    private NghiepVuException loi(String ma, String tb) {
        return new NghiepVuException(400, ma, tb);
    }

    private BigDecimal phanTram(long a, long b) {
        if (b == 0)
            return BigDecimal.ZERO;
        return BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(b), 2,
                RoundingMode.HALF_UP);
    }

    private Map<String, Object> khung(LocalDate tu, LocalDate den, KyThongKe ky, Object ds) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("tuNgay", tu.toString());
        d.put("denNgay", den.toString());
        d.put("kyThongKe", ky == null ? null : ky.name());
        d.put("daCatDenHomNay", den.isBefore(ThoiGianUtil.homNay()));
        d.put("danhSach", ds);
        return d;
    }
}
