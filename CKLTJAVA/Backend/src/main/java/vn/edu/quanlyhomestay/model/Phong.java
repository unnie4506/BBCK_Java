package vn.edu.quanlyhomestay.model;

import java.util.*;

public class Phong {
    private long maPhong;
    private String soPhong;
    private int tang;
    private long maLoaiPhong;
    private String moTa;
    private TrangThaiPhong trangThai;
    private boolean dangHoatDong;
    private List<Long> danhSachMaTienNghi = new ArrayList<>();

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

    public int getTang() {
        return tang;
    }

    public void setTang(int v) {
        tang = v;
    }

    public long getMaLoaiPhong() {
        return maLoaiPhong;
    }

    public void setMaLoaiPhong(long v) {
        maLoaiPhong = v;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String v) {
        moTa = v;
    }

    public TrangThaiPhong getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiPhong v) {
        trangThai = v;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(boolean v) {
        dangHoatDong = v;
    }

    public List<Long> getDanhSachMaTienNghi() {
        return danhSachMaTienNghi;
    }

    public void setDanhSachMaTienNghi(List<Long> v) {
        danhSachMaTienNghi = v;
    }
}
