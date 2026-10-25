package vn.edu.quanlyhomestay.model;

import java.time.LocalDate;

public class ChiTietDatPhongYeuCau {
    private Long maChiTiet;
    private long maPhong;
    private LocalDate ngayNhan;
    private LocalDate ngayTra;

    public Long getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(Long v) {
        maChiTiet = v;
    }

    public long getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(long v) {
        maPhong = v;
    }

    public LocalDate getNgayNhan() {
        return ngayNhan;
    }

    public void setNgayNhan(LocalDate v) {
        ngayNhan = v;
    }

    public LocalDate getNgayTra() {
        return ngayTra;
    }

    public void setNgayTra(LocalDate v) {
        ngayTra = v;
    }
}
