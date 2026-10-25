package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.CongKhaiDAO;
import vn.edu.quanlyhomestay.model.dto.LoaiPhongCongKhaiDto;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class LoaiPhongCongKhaiService {
    private final CongKhaiDAO dao = new CongKhaiDAO();

    public List<LoaiPhongCongKhaiDto> danhSach() throws SQLException {
        // BR-03 (Khách): chỉ lấy loại phòng đang hoạt động; dữ liệu trả về không chứa thông tin đặt phòng.
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<LoaiPhongCongKhaiDto> ketQua = new ArrayList<>();
            for (Map<String, Object> dong : dao.danhSachLoaiPhong(c))
                ketQua.add(doc(dong));
            return ketQua;
        }
    }

    public LoaiPhongCongKhaiDto chiTiet(long ma) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = dao.chiTietLoaiPhong(c, ma);
            if (ds.isEmpty())
                throw new NghiepVuException(404, "KHONG_TIM_THAY_LOAI_PHONG", "Không tìm thấy loại phòng.");
            LoaiPhongCongKhaiDto dto = doc(ds.get(0));
            // Giả định 1 (Khách): tiện nghi và ảnh được gộp, bỏ trùng, từ các phòng đang hoạt động.
            dto.setTienNghi(
                    dao.tienNghiTheoLoai(c, ma).stream().map(x -> String.valueOf(x.get("ten"))).distinct().toList());
            dto.setDanhSachAnh(dao.anhTheoLoai(c, ma).stream()
                    .map(x -> taoDuongDanAnh(String.valueOf(x.get("duongDanAnh")))).distinct().toList());
            return dto;
        }
    }

    private LoaiPhongCongKhaiDto doc(Map<String, Object> d) {
        LoaiPhongCongKhaiDto x = new LoaiPhongCongKhaiDto();
        x.setMaLoaiPhong(((Number) d.get("maLoaiPhong")).longValue());
        x.setTen((String) d.get("ten"));
        x.setSucChua(((Number) d.get("sucChua")).intValue());
        x.setMoTa((String) d.get("moTa"));
        x.setGiaNgayThuong((java.math.BigDecimal) d.get("giaNgayThuong"));
        x.setGiaCuoiTuan((java.math.BigDecimal) d.get("giaCuoiTuan"));
        Object a = d.get("anhDaiDien");
        x.setAnhDaiDien(a == null ? null : taoDuongDanAnh(String.valueOf(a)));
        return x;
    }

    private String taoDuongDanAnh(String duongDan) {
        String ten = duongDan.substring(duongDan.lastIndexOf('/') + 1);
        return "/api/cong-khai/anh-phong/" + ten;
    }
}
