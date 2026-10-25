package vn.edu.quanlyhomestay.model;

import java.time.LocalDateTime;

public class KhachHang {
    private final long maTaiKhoanKhachHang;
    private final long maKhachHang;
    private final String hoTen;
    private final String soDienThoai;
    private final String thuDienTu;
    private String soGiayTo;
    private String ghiChu;
    private boolean hoSoDangHoatDong;
    private final transient String matKhauDaBam;
    private final boolean dangHoatDong;
    private final LocalDateTime thoiDiemMoKhoa;

    public KhachHang(long maTaiKhoanKhachHang, long maKhachHang, String hoTen, String soDienThoai, String thuDienTu,
            String matKhauDaBam, boolean dangHoatDong, LocalDateTime thoiDiemMoKhoa) {
        this.maTaiKhoanKhachHang = maTaiKhoanKhachHang;
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.thuDienTu = thuDienTu;
        this.matKhauDaBam = matKhauDaBam;
        this.dangHoatDong = dangHoatDong;
        this.thoiDiemMoKhoa = thoiDiemMoKhoa;
        this.hoSoDangHoatDong = dangHoatDong;
    }

    public long getMaTaiKhoanKhachHang() {
        return maTaiKhoanKhachHang;
    }

    public long getMaKhachHang() {
        return maKhachHang;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getThuDienTu() {
        return thuDienTu;
    }

    public String getMatKhauDaBam() {
        return matKhauDaBam;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public LocalDateTime getThoiDiemMoKhoa() {
        return thoiDiemMoKhoa;
    }

    public String getSoGiayTo() {
        return soGiayTo;
    }

    public void setSoGiayTo(String soGiayTo) {
        this.soGiayTo = soGiayTo;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public boolean isHoSoDangHoatDong() {
        return hoSoDangHoatDong;
    }

    public void setHoSoDangHoatDong(boolean hoSoDangHoatDong) {
        this.hoSoDangHoatDong = hoSoDangHoatDong;
    }
}
