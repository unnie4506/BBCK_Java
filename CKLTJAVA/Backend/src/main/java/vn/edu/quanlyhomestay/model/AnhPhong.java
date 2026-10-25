package vn.edu.quanlyhomestay.model;

public class AnhPhong {
    private long maAnhPhong;
    private long maPhong;
    private String duongDanAnh;
    private String chuThich;
    private int thuTuHienThi;

    public long getMaAnhPhong() {
        return maAnhPhong;
    }

    public void setMaAnhPhong(long v) {
        maAnhPhong = v;
    }

    public long getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(long v) {
        maPhong = v;
    }

    public String getDuongDanAnh() {
        return duongDanAnh;
    }

    public void setDuongDanAnh(String v) {
        duongDanAnh = v;
    }

    public String getChuThich() {
        return chuThich;
    }

    public void setChuThich(String v) {
        chuThich = v;
    }

    public int getThuTuHienThi() {
        return thuTuHienThi;
    }

    public void setThuTuHienThi(int v) {
        thuTuHienThi = v;
    }
}
