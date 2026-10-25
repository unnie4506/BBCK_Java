package vn.edu.quanlyhomestay.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DonDatPhongYeuCau {
    private Long maKhachHang;
    private KhachHangVangLaiYeuCau khachHangVangLai;
    private String ghiChu;
    private BigDecimal tienCoc;
    private List<ChiTietDatPhongYeuCau> danhSachPhong = new ArrayList<>();

    public Long getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(Long v) {
        maKhachHang = v;
    }

    public KhachHangVangLaiYeuCau getKhachHangVangLai() {
        return khachHangVangLai;
    }

    public void setKhachHangVangLai(KhachHangVangLaiYeuCau v) {
        khachHangVangLai = v;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String v) {
        ghiChu = v;
    }

    public BigDecimal getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(BigDecimal v) {
        tienCoc = v;
    }

    public List<ChiTietDatPhongYeuCau> getDanhSachPhong() {
        return danhSachPhong;
    }

    public void setDanhSachPhong(List<ChiTietDatPhongYeuCau> v) {
        danhSachPhong = v;
    }
}
