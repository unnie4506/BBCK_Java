package vn.edu.quanlyhomestay.model;

import java.time.LocalDateTime;

public class TaiKhoanQuanLy {
    private final long maQuanLy;
    private final String tenDangNhap;
    private final transient String matKhauDaBam;
    private final String hoTen;
    private final boolean dangHoatDong;
    private final LocalDateTime thoiDiemMoKhoa;

    public TaiKhoanQuanLy(long maQuanLy, String tenDangNhap, String matKhauDaBam, String hoTen, boolean dangHoatDong,
            LocalDateTime thoiDiemMoKhoa) {
        this.maQuanLy = maQuanLy;
        this.tenDangNhap = tenDangNhap;
        this.matKhauDaBam = matKhauDaBam;
        this.hoTen = hoTen;
        this.dangHoatDong = dangHoatDong;
        this.thoiDiemMoKhoa = thoiDiemMoKhoa;
    }

    public long getMaQuanLy() {
        return maQuanLy;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getMatKhauDaBam() {
        return matKhauDaBam;
    }

    public String getHoTen() {
        return hoTen;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public LocalDateTime getThoiDiemMoKhoa() {
        return thoiDiemMoKhoa;
    }
}
