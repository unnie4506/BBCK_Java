package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.CongKhaiDAO;
import java.sql.Connection;
import java.sql.SQLException;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import java.util.*;

public class LichSuLuuTruService {
    private final CongKhaiDAO dao = new CongKhaiDAO();
    private final DatPhongTrucTuyenService datPhong = new DatPhongTrucTuyenService();

    public Map<String, Object> danhSach(long maTaiKhoan, int trang, int kichThuoc) throws SQLException {
        // BR-09 (Khách): chỉ đọc đơn HOAN_TAT thuộc tài khoản khách và phân trang mới nhất trước.
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = dao.cacDonHoanTat(c, maTaiKhoan, kichThuoc, (trang - 1) * kichThuoc);
            long tong = ((Number) dao.demDonCuaKhach(c, maTaiKhoan, "HOAN_TAT").get(0).get("tong")).longValue();
            Map<String, Object> kq = new LinkedHashMap<>();
            kq.put("danhSach", ds);
            kq.put("trang", trang);
            kq.put("kichThuoc", kichThuoc);
            kq.put("tongSoBanGhi", tong);
            kq.put("tongSoTrang", (tong + kichThuoc - 1) / kichThuoc);
            kq.put("thongKe", dao.thongKeLichSu(c, maTaiKhoan).get(0));
            kq.put("thongBao", tong == 0 ? "Chưa có lịch sử lưu trú." : "Lấy lịch sử lưu trú thành công.");
            return kq;
        }
    }

    public Map<String, Object> danhSachTheoHoSo(long maKhach, int trang, int kichThuoc) throws SQLException {
        // UC13 (Admin): lịch sử tra theo hồ sơ khách để Quản lý xem cả khách không có tài khoản.
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = dao.lichSuCuaHoSo(c, maKhach, kichThuoc, (trang - 1) * kichThuoc);
            Map<String, Object> thongKe = dao.thongKeLichSuTheoHoSo(c, maKhach).get(0);
            long tong = ((Number) thongKe.get("tongSoLanLuuTru")).longValue();
            Map<String, Object> kq = new LinkedHashMap<>();
            kq.put("danhSach", ds);
            kq.put("trang", trang);
            kq.put("kichThuoc", kichThuoc);
            kq.put("tongSoBanGhi", tong);
            kq.put("tongSoTrang", (tong + kichThuoc - 1) / kichThuoc);
            kq.put("thongKe", thongKe);
            kq.put("thongBao", tong == 0 ? "Chưa có lịch sử lưu trú." : "Lấy lịch sử lưu trú thành công.");
            return kq;
        }
    }

    public Map<String, Object> chiTiet(long maTaiKhoan, long maDon) throws Exception {
        var hoaDon = datPhong.hoaDon(maTaiKhoan, maDon);
        Map<String, Object> ketQua = new LinkedHashMap<>();
        ketQua.put("hoaDon", hoaDon.getHoaDon());
        ketQua.put("danhSachPhong", hoaDon.getDanhSachPhong());
        ketQua.put("danhSachThanhToan", hoaDon.getDanhSachThanhToan());
        return ketQua;
    }
}
