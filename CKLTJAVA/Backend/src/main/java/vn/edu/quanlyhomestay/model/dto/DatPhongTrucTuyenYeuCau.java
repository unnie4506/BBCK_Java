package vn.edu.quanlyhomestay.model.dto;

import java.time.LocalDate;
import java.util.List;

public class DatPhongTrucTuyenYeuCau {
    private LocalDate ngayNhan;
    private LocalDate ngayTra;
    private Integer soKhach;
    private List<Long> danhSachMaPhong;
    private String ghiChu;

    public LocalDate getNgayNhan() {
        return ngayNhan;
    }

    public void setNgayNhan(LocalDate v) {
        ngayNhan = v;
    }

    public LocalDate getNgayTra() {
        return ngayTra;
    }

    public void setNgayTra(LocalDate v) {
        ngayTra = v;
    }

    public Integer getSoKhach() {
        return soKhach;
    }

    public void setSoKhach(Integer v) {
        soKhach = v;
    }

    public List<Long> getDanhSachMaPhong() {
        return danhSachMaPhong;
    }

    public void setDanhSachMaPhong(List<Long> v) {
        danhSachMaPhong = v;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String v) {
        ghiChu = v;
    }
}
