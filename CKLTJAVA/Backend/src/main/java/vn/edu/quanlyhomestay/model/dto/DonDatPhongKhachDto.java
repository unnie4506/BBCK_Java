package vn.edu.quanlyhomestay.model.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DonDatPhongKhachDto {
    private long maDonDatPhong;
    private String maTraCuuDon;
    private String trangThai;
    private int soKhach;
    private BigDecimal tienCoc;
    private BigDecimal tienCocYeuCau;
    private Object hanGiuCho;
    private long hanGiuChoMs;
    private long thoiGianMayChuMs;
    private boolean daBaoDaChuyenKhoan;
    private String nganHang;
    private String soTaiKhoan;
    private String chuTaiKhoan;
    private String noiDungChuyenKhoan;
    private List<Map<String, Object>> danhSachTienCoc;
    private BigDecimal tongTienPhong;
    private boolean choPhepHuy;
    private List<Map<String, Object>> danhSachPhong;

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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String v) {
        trangThai = v;
    }

    public int getSoKhach() {
        return soKhach;
    }

    public void setSoKhach(int v) {
        soKhach = v;
    }

    public BigDecimal getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(BigDecimal v) {
        tienCoc = v;
    }

    public BigDecimal getTienCocYeuCau() { return tienCocYeuCau; }
    public void setTienCocYeuCau(BigDecimal v) { tienCocYeuCau = v; }
    public Object getHanGiuCho() { return hanGiuCho; }
    public void setHanGiuCho(Object v) { hanGiuCho = v; }
    public long getHanGiuChoMs() { return hanGiuChoMs; }
    public void setHanGiuChoMs(long v) { hanGiuChoMs = v; }
    public long getThoiGianMayChuMs() { return thoiGianMayChuMs; }
    public void setThoiGianMayChuMs(long v) { thoiGianMayChuMs = v; }
    public boolean isDaBaoDaChuyenKhoan() { return daBaoDaChuyenKhoan; }
    public void setDaBaoDaChuyenKhoan(boolean v) { daBaoDaChuyenKhoan = v; }
    public String getNganHang() { return nganHang; }
    public void setNganHang(String v) { nganHang = v; }
    public String getSoTaiKhoan() { return soTaiKhoan; }
    public void setSoTaiKhoan(String v) { soTaiKhoan = v; }
    public String getChuTaiKhoan() { return chuTaiKhoan; }
    public void setChuTaiKhoan(String v) { chuTaiKhoan = v; }
    public String getNoiDungChuyenKhoan() { return noiDungChuyenKhoan; }
    public void setNoiDungChuyenKhoan(String v) { noiDungChuyenKhoan = v; }
    public List<Map<String, Object>> getDanhSachTienCoc() { return danhSachTienCoc; }
    public void setDanhSachTienCoc(List<Map<String, Object>> v) { danhSachTienCoc = v; }

    public BigDecimal getTongTienPhong() {
        return tongTienPhong;
    }

    public void setTongTienPhong(BigDecimal v) {
        tongTienPhong = v;
    }

    public boolean isChoPhepHuy() {
        return choPhepHuy;
    }

    public void setChoPhepHuy(boolean v) {
        choPhepHuy = v;
    }

    public List<Map<String, Object>> getDanhSachPhong() {
        return danhSachPhong;
    }

    public void setDanhSachPhong(List<Map<String, Object>> v) {
        danhSachPhong = v;
    }
}
