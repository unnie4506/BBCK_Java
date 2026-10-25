package vn.edu.quanlyhomestay.model.dto;

import java.math.BigDecimal;

public class PhongTrongCongKhaiDto {
    private long maPhong;
    private String soPhong;
    private String tenLoaiPhong;
    private int sucChua;
    private String anhDaiDien;
    private int soDem;
    private int soDemNgayThuong;
    private int soDemCuoiTuan;
    private BigDecimal tienPhongDuKien;

    public long getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(long v) {
        maPhong = v;
    }

    public String getSoPhong() {
        return soPhong;
    }

    public void setSoPhong(String v) {
        soPhong = v;
    }

    public String getTenLoaiPhong() {
        return tenLoaiPhong;
    }

    public void setTenLoaiPhong(String v) {
        tenLoaiPhong = v;
    }

    public int getSucChua() {
        return sucChua;
    }

    public void setSucChua(int v) {
        sucChua = v;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String v) {
        anhDaiDien = v;
    }

    public int getSoDem() {
        return soDem;
    }

    public void setSoDem(int v) {
        soDem = v;
    }

    public int getSoDemNgayThuong() {
        return soDemNgayThuong;
    }

    public void setSoDemNgayThuong(int v) {
        soDemNgayThuong = v;
    }

    public int getSoDemCuoiTuan() {
        return soDemCuoiTuan;
    }

    public void setSoDemCuoiTuan(int v) {
        soDemCuoiTuan = v;
    }

    public BigDecimal getTienPhongDuKien() {
        return tienPhongDuKien;
    }

    public void setTienPhongDuKien(BigDecimal v) {
        tienPhongDuKien = v;
    }
}
