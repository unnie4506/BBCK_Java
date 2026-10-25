package vn.edu.quanlyhomestay.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class LoaiPhongCongKhaiDto {
    private long maLoaiPhong;
    private String ten;
    private int sucChua;
    private String moTa;
    private BigDecimal giaNgayThuong;
    private BigDecimal giaCuoiTuan;
    private String anhDaiDien;
    private List<String> tienNghi;
    private List<String> danhSachAnh;

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

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String v) {
        anhDaiDien = v;
    }

    public List<String> getTienNghi() {
        return tienNghi;
    }

    public void setTienNghi(List<String> v) {
        tienNghi = v;
    }

    public List<String> getDanhSachAnh() {
        return danhSachAnh;
    }

    public void setDanhSachAnh(List<String> v) {
        danhSachAnh = v;
    }
}
