package vn.edu.quanlyhomestay.model;

public class TienNghi {
    private long maTienNghi;
    private String ten;
    private boolean dangHoatDong;

    public long getMaTienNghi() {
        return maTienNghi;
    }

    public void setMaTienNghi(long v) {
        maTienNghi = v;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String v) {
        ten = v;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }

    public void setDangHoatDong(boolean v) {
        dangHoatDong = v;
    }
}
