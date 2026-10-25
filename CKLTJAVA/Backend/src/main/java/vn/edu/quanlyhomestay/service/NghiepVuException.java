package vn.edu.quanlyhomestay.service;

public class NghiepVuException extends RuntimeException {
    private final int maHttp;
    private final Object duLieu;
    private final String maLoi;

    public NghiepVuException(int maHttp, String thongBao) {
        super(thongBao);
        this.maHttp = maHttp;
        this.duLieu = null;
        this.maLoi = null;
    }

    public NghiepVuException(int maHttp, String thongBao, Object duLieu) {
        super(thongBao);
        this.maHttp = maHttp;
        this.duLieu = duLieu;
        this.maLoi = null;
    }

    public NghiepVuException(int maHttp, String maLoi, String thongBao) {
        super(thongBao);
        this.maHttp = maHttp;
        this.maLoi = maLoi;
        this.duLieu = null;
    }

    public NghiepVuException(int maHttp, String maLoi, String thongBao, Object duLieu) {
        super(thongBao);
        this.maHttp = maHttp;
        this.maLoi = maLoi;
        this.duLieu = duLieu;
    }

    public int getMaHttp() {
        return maHttp;
    }

    public Object getDuLieu() {
        return duLieu;
    }

    public String getMaLoi() {
        return maLoi;
    }
}
