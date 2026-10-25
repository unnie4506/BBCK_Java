package vn.edu.quanlyhomestay.service;

public class LoiXacThuc extends Exception {
    private final int maTrangThai;
    private final long thoiGianChoGiay;

    public LoiXacThuc(int maTrangThai, String thongBao) {
        this(maTrangThai, thongBao, 0);
    }

    public LoiXacThuc(int maTrangThai, String thongBao, long thoiGianChoGiay) {
        super(thongBao);
        this.maTrangThai = maTrangThai;
        this.thoiGianChoGiay = thoiGianChoGiay;
    }

    public int getMaTrangThai() {
        return maTrangThai;
    }

    public long getThoiGianChoGiay() {
        return thoiGianChoGiay;
    }
}
