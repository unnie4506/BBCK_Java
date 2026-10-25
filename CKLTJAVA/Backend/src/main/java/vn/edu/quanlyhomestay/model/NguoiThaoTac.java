package vn.edu.quanlyhomestay.model;

public class NguoiThaoTac {
    private String loai;
    private long ma;

    public NguoiThaoTac() {
    }

    public NguoiThaoTac(String loai, long ma) {
        this.loai = loai;
        this.ma = ma;
    }

    public String getLoai() {
        return loai;
    }

    public void setLoai(String loai) {
        this.loai = loai;
    }

    public long getMa() {
        return ma;
    }

    public void setMa(long ma) {
        this.ma = ma;
    }
}
