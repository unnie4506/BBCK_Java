package vn.edu.quanlyhomestay.model.dto;

import java.util.List;
import java.util.Map;

public class HoaDonKhachDto {
    private Map<String, Object> hoaDon;
    private List<Map<String, Object>> danhSachPhong;
    private List<Map<String, Object>> danhSachThanhToan;

    public Map<String, Object> getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(Map<String, Object> v) {
        hoaDon = v;
    }

    public List<Map<String, Object>> getDanhSachPhong() {
        return danhSachPhong;
    }

    public void setDanhSachPhong(List<Map<String, Object>> v) {
        danhSachPhong = v;
    }

    public List<Map<String, Object>> getDanhSachThanhToan() {
        return danhSachThanhToan;
    }

    public void setDanhSachThanhToan(List<Map<String, Object>> v) {
        danhSachThanhToan = v;
    }
}
