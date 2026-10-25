package vn.edu.quanlyhomestay.model;

public class NguoiDungDaXacThuc {
    private final long maNguoiDung;
    private final String vaiTro;
    private final String tenHienThi;
    private final String thuDienTu;

    public NguoiDungDaXacThuc(long maNguoiDung, String vaiTro, String tenHienThi, String thuDienTu) {
        this.maNguoiDung = maNguoiDung;
        this.vaiTro = vaiTro;
        this.tenHienThi = tenHienThi;
        this.thuDienTu = thuDienTu;
    }

    public long getMaNguoiDung() {
        return maNguoiDung;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public String getThuDienTu() {
        return thuDienTu;
    }
}
