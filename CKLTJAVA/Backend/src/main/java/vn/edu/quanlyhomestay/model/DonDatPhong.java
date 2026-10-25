package vn.edu.quanlyhomestay.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DonDatPhong {
    private long maDonDatPhong;
    private String maTraCuuDon;
    private long maKhachHang;
    private String trangThai;
    private BigDecimal tienCocDaNhan;
    private String ghiChu;
    private LocalDateTime taoLuc;

    public long getMaDonDatPhong() {
        return maDonDatPhong;
    }

    public void setMaDonDatPhong(long v) {
        maDonDatPhong = v;
    }

    public String getMaTraCuuDon() {
        return maTraCuuDon;
    }

    public void setMaTraCuuDon(String v) {
        maTraCuuDon = v;
    }

    public long getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(long v) {
        maKhachHang = v;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String v) {
        trangThai = v;
    }

    public BigDecimal getTienCocDaNhan() {
        return tienCocDaNhan;
    }

    public void setTienCocDaNhan(BigDecimal v) {
        tienCocDaNhan = v;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String v) {
        ghiChu = v;
    }

    public LocalDateTime getTaoLuc() {
        return taoLuc;
    }

    public void setTaoLuc(LocalDateTime v) {
        taoLuc = v;
    }
}
