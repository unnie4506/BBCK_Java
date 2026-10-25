package vn.edu.quanlyhomestay.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Dịch vụ thuần để tính tiền theo từng đêm, không truy cập CSDL hay HTTP. */
public class TinhTienService {
    public KetQuaTinhTien tinhTienPhong(LocalDate ngayNhan, LocalDate ngayTra, BigDecimal giaNgayThuong,
            BigDecimal giaCuoiTuan) {
        if (ngayNhan == null || ngayTra == null || !ngayTra.isAfter(ngayNhan))
            throw new NghiepVuException(422, "NGAY_KHONG_HOP_LE", "Ngày trả phải sau ngày nhận.");
        long soDem = java.time.temporal.ChronoUnit.DAYS.between(ngayNhan, ngayTra);
        if (soDem > 30)
            throw new NghiepVuException(422, "VUOT_QUA_30_DEM", "Mỗi phòng được đặt tối đa 30 đêm.");
        int demThuong = 0, demCuoiTuan = 0;
        BigDecimal tong = BigDecimal.ZERO;
        for (LocalDate dem = ngayNhan; dem.isBefore(ngayTra); dem = dem.plusDays(1)) {
            DayOfWeek thu = dem.getDayOfWeek();
            // BR-03 (Admin): giá đêm xác định theo ngày bắt đầu đêm; Thứ Sáu và Thứ Bảy áp giá cuối tuần.
            if (thu == DayOfWeek.FRIDAY || thu == DayOfWeek.SATURDAY) {
                demCuoiTuan++;
                tong = tong.add(giaCuoiTuan);
            } else {
                demThuong++;
                tong = tong.add(giaNgayThuong);
            }
        }
        return new KetQuaTinhTien(soDem, demThuong, demCuoiTuan, tong.setScale(0, RoundingMode.HALF_UP));
    }

    public static class KetQuaTinhTien {
        private final long soDem;
        private final int soDemNgayThuong;
        private final int soDemCuoiTuan;
        private final BigDecimal tienPhong;

        public KetQuaTinhTien(long n, int t, int c, BigDecimal p) {
            soDem = n;
            soDemNgayThuong = t;
            soDemCuoiTuan = c;
            tienPhong = p;
        }

        public long getSoDem() {
            return soDem;
        }

        public int getSoDemNgayThuong() {
            return soDemNgayThuong;
        }

        public int getSoDemCuoiTuan() {
            return soDemCuoiTuan;
        }

        public BigDecimal getTienPhong() {
            return tienPhong;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("soDem", soDem);
            m.put("soDemNgayThuong", soDemNgayThuong);
            m.put("soDemCuoiTuan", soDemCuoiTuan);
            m.put("tienPhong", tienPhong);
            return m;
        }
    }
}
