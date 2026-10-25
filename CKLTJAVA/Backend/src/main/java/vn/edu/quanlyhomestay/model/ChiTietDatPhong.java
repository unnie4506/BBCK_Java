package vn.edu.quanlyhomestay.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ChiTietDatPhong {
    private long maChiTietDatPhong;
    private long maDonDatPhong;
    private long maPhong;
    private LocalDate ngayNhan;
    private LocalDate ngayTra;
    private BigDecimal giaNgayThuongLucDat;
    private BigDecimal giaCuoiTuanLucDat;
    private BigDecimal tienPhong;
    private String trangThai;

    public long getMaChiTietDatPhong() {
        return maChiTietDatPhong;
    }

    public void setMaChiTietDatPhong(long v) {
        maChiTietDatPhong = v;
    }

    public long getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public void setMaDonDatPhong(long v) {
        maDonDatPhong = v;
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

    public BigDecimal getGiaNgayThuongLucDat() {
        return giaNgayThuongLucDat;
    }

    public void setGiaNgayThuongLucDat(BigDecimal v) {
        giaNgayThuongLucDat = v;
    }

    public BigDecimal getGiaCuoiTuanLucDat() {
        return giaCuoiTuanLucDat;
    }

    public void setGiaCuoiTuanLucDat(BigDecimal v) {
        giaCuoiTuanLucDat = v;
    }

    public BigDecimal getTienPhong() {
        return tienPhong;
    }

    public void setTienPhong(BigDecimal v) {
        tienPhong = v;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String v) {
        trangThai = v;
    }
}
