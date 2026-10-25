package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.PhongDatDAO;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import vn.edu.quanlyhomestay.util.ThoiGianUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.*;

public class PhongTrongService {
    private final PhongDatDAO phongDAO = new PhongDatDAO();
    private final TinhTienService tinhTien = new TinhTienService();

    public List<Map<String, Object>> timPhong(LocalDate ngayNhan, LocalDate ngayTra, Long maLoaiPhong, Integer tang,
            Long boQuaMaDon, NguoiThaoTac nguoi) throws Exception {
        kiemTraNguoi(nguoi);
        return timPhongNoiBo(ngayNhan, ngayTra, maLoaiPhong, tang, boQuaMaDon);
    }

    public List<Map<String, Object>> timPhongCongKhai(LocalDate ngayNhan, LocalDate ngayTra, Long maLoaiPhong)
            throws Exception {
        return timPhongNoiBo(ngayNhan, ngayTra, maLoaiPhong, null, null);
    }

    private List<Map<String, Object>> timPhongNoiBo(LocalDate ngayNhan, LocalDate ngayTra, Long maLoaiPhong,
            Integer tang, Long boQuaMaDon) throws Exception {
        if (ngayNhan == null || ngayTra == null || !ngayTra.isAfter(ngayNhan))
            throw new NghiepVuException(422, "NGAY_KHONG_HOP_LE", "Ngày trả phải sau ngày nhận.");
        TinhTienService.KetQuaTinhTien soDem = tinhTien.tinhTienPhong(ngayNhan, ngayTra, new BigDecimal("1"),
                new BigDecimal("1"));
        try (Connection ketNoi = KetNoiCoSoDuLieu.moKetNoi()) {
            // BR-01 (Admin): khoảng giao nhau bị loại; trả phòng cùng ngày nhận của đơn khác vẫn hợp lệ.
            // Chỉ phòng ở trạng thái TRONG được đưa vào danh sách tìm kiếm; lịch đặt vẫn được kiểm tra bên dưới.
            List<Map<String, Object>> phong = phongDAO.timPhongTrong(ketNoi, ngayNhan, ngayTra, maLoaiPhong, tang,
                    boQuaMaDon);
            List<Map<String, Object>> ketQua = new ArrayList<>();
            for (Map<String, Object> p : phong) {
                BigDecimal giaThuong = (BigDecimal) p.get("giaNgayThuong"),
                        giaCuoiTuan = (BigDecimal) p.get("giaCuoiTuan");
                TinhTienService.KetQuaTinhTien tien = tinhTien.tinhTienPhong(ngayNhan, ngayTra, giaThuong, giaCuoiTuan);
                Map<String, Object> dong = new LinkedHashMap<>();
                dong.put("maPhong", p.get("maPhong"));
                dong.put("soPhong", p.get("soPhong"));
                dong.put("loai", p.get("loai"));
                dong.put("tang", p.get("tang"));
                dong.put("giaNgayThuong", giaThuong);
                dong.put("giaCuoiTuan", giaCuoiTuan);
                dong.put("soDemNgayThuong", tien.getSoDemNgayThuong());
                dong.put("soDemCuoiTuan", tien.getSoDemCuoiTuan());
                dong.put("tienPhongDuKien", tien.getTienPhong());
                ketQua.add(dong);
            }
            return ketQua;
        }
    }

    private void kiemTraNguoi(NguoiThaoTac n) {
        if (n == null || n.getMa() <= 0 || (!"QUAN_LY".equals(n.getLoai()) && !"KHACH_HANG".equals(n.getLoai())))
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Người thao tác không hợp lệ.");
    }
}
