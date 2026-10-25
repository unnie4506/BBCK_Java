package vn.edu.quanlyhomestay.model;

import java.math.BigDecimal;

public class LoaiPhong {
    private long maLoaiPhong;
    private String ten;
    private int sucChua;
    private String moTa;
    private BigDecimal giaNgayThuong;
    private BigDecimal giaCuoiTuan;
    private boolean dangHoatDong;

    public long getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(long v) {
        maLoaiPhong = v;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String v) {
        ten = v;
    }

    public int getSucChua() {
        return sucChua;
    }

    public void setSucChua(int v) {
        sucChua = v;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String v) {
        moTa = v;
    }

    public BigDecimal getGiaNgayThuong() {
        return giaNgayThuong;
    }

    public void setGiaNgayThuong(BigDecimal v) {
        giaNgayThuong = v;
    }

    public BigDecimal getGiaCuoiTuan() {
        return giaCuoiTuan;
    }

    public void setGiaCuoiTuan(BigDecimal v) {
        giaCuoiTuan = v;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(boolean v) {
        dangHoatDong = v;
    }
}
