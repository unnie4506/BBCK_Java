package vn.edu.quanlyhomestay.util;

public class PhanHoiApi {
    private final boolean thanhCong;
    private final String thongBao;
    private final Object duLieu;
    private final String maLoi;

    public PhanHoiApi(boolean thanhCong, String thongBao, Object duLieu) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
        this.duLieu = duLieu;
        this.maLoi = null;
    }

    public PhanHoiApi(boolean thanhCong, String thongBao, Object duLieu, String maLoi) {
        this.thanhCong = thanhCong;
        this.thongBao = thongBao;
        this.duLieu = duLieu;
        this.maLoi = maLoi;
    }

    public static PhanHoiApi thanhCong(String thongBao, Object duLieu) {
        return new PhanHoiApi(true, thongBao, duLieu);
    }

    public static PhanHoiApi thatBai(String thongBao) {
        return new PhanHoiApi(false, thongBao, null);
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public String getThongBao() {
        return thongBao;
    }

    public Object getDuLieu() {
        return duLieu;
    }

    public String getMaLoi() {
        return maLoi;
    }
}
