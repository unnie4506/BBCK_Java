package vn.edu.quanlyhomestay.controller.congkhai;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.dao.CongKhaiDAO;
import vn.edu.quanlyhomestay.model.dto.PhongTrongCongKhaiDto;
import vn.edu.quanlyhomestay.service.*;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;
import java.sql.Connection;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@WebServlet("/api/cong-khai/phong-trong")
public class PhongTrongCongKhaiServlet extends HttpServlet {
    private final PhongTrongService dichVu = new PhongTrongService();
    private final CongKhaiDAO dao = new CongKhaiDAO();

    @Override
    protected void doGet(HttpServletRequest q, HttpServletResponse p) throws IOException {
        try {
            LocalDate nhan = ngay(q.getParameter("ngayNhan")), tra = ngay(q.getParameter("ngayTra"));
            int soKhach = soKhach(q.getParameter("soKhach"));
            Long maLoai = ma(q.getParameter("maLoaiPhong"));
            if (nhan == null || tra == null || !tra.isAfter(nhan))
                throw new NghiepVuException(422, "NGAY_KHONG_HOP_LE", "Ngày trả phải sau ngày nhận.");
            if (nhan.isBefore(ThoiGianUtil.homNay()))
                throw new NghiepVuException(422, "NGAY_NHAN_QUA_KHU", "Ngày nhận không được ở quá khứ.");
            if (ChronoUnit.DAYS.between(nhan, tra) > 30)
                throw new NghiepVuException(422, "VUOT_QUA_30_DEM", "Mỗi phòng được đặt tối đa 30 đêm.");
            // BR-04/05 (Khách): không lọc theo sức chứa riêng; chỉ trả phòng hoạt động, đang TRONG và không trùng lịch.
            List<Map<String, Object>> rows = dichVu.timPhongCongKhai(nhan, tra, maLoai);
            List<Long> ids = rows.stream().map(x -> ((Number) x.get("maPhong")).longValue()).toList();
            List<Map<String, Object>> meta;
            try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
                meta = dao.phongTheoMa(c, ids);
            }
            Map<Long, Map<String, Object>> byId = new HashMap<>();
            for (Map<String, Object> x : meta)
                byId.put(((Number) x.get("maPhong")).longValue(), x);
            List<PhongTrongCongKhaiDto> ds = new ArrayList<>();
            for (Map<String, Object> x : rows) {
                long id = ((Number) x.get("maPhong")).longValue();
                Map<String, Object> m = byId.get(id);
                if (m == null)
                    continue;
                PhongTrongCongKhaiDto d = new PhongTrongCongKhaiDto();
                d.setMaPhong(id);
                d.setSoPhong((String) x.get("soPhong"));
                d.setTenLoaiPhong((String) m.get("tenLoaiPhong"));
                d.setSucChua(((Number) m.get("sucChua")).intValue());
                Object file = m.get("anhDaiDien");
                d.setAnhDaiDien(file == null ? null : taoAnh(String.valueOf(file)));
                d.setSoDem((int) ChronoUnit.DAYS.between(nhan, tra));
                d.setSoDemNgayThuong(((Number) x.get("soDemNgayThuong")).intValue());
                d.setSoDemCuoiTuan(((Number) x.get("soDemCuoiTuan")).intValue());
                d.setTienPhongDuKien((java.math.BigDecimal) x.get("tienPhongDuKien"));
                ds.add(d);
            }
            if (ds.size() > 100)
                ds = ds.subList(0, 100);
            TienIchJson.ghiPhanHoiJson(p, 200,
                    PhanHoiApi.thanhCong(ds.isEmpty() ? "Không có phòng trống phù hợp; hãy đổi ngày hoặc bỏ bớt bộ lọc."
                            : "Tìm phòng trống thành công.", ds));
        } catch (Exception e) {
            ghiLoi(p, e);
        }
    }

    private String taoAnh(String duongDan) {
        return "/api/cong-khai/anh-phong/" + duongDan.substring(duongDan.lastIndexOf('/') + 1);
    }

    private LocalDate ngay(String s) {
        try {
            return s == null ? null : LocalDate.parse(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày phải theo định dạng yyyy-MM-dd.");
        }
    }

    private Long ma(String s) {
        try {
            return s == null || s.isBlank() ? null : Long.valueOf(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Mã loại phòng không hợp lệ.");
        }
    }

    private int soKhach(String s) {
        try {
            int n = Integer.parseInt(s);
            if (n < 1 || n > 20)
                throw new NumberFormatException();
            return n;
        } catch (Exception e) {
            throw new NghiepVuException(422, "SO_KHACH_KHONG_HOP_LE", "Số khách phải là số nguyên từ 1 đến 20.");
        }
    }

    private void ghiLoi(HttpServletResponse p, Exception e) throws IOException {
        if (e instanceof NghiepVuException n)
            TienIchJson.ghiPhanHoiJson(p, n.getMaHttp(),
                    new PhanHoiApi(false, n.getMessage(), n.getDuLieu(), n.getMaLoi()));
        else if (e instanceof IllegalArgumentException)
            TienIchJson.ghiPhanHoiJson(p, 400, PhanHoiApi.thatBai(e.getMessage()));
        else {
            getServletContext().log("Lỗi tìm phòng công khai.", e);
            TienIchJson.ghiPhanHoiJson(p, 500, PhanHoiApi.thatBai("Đã xảy ra lỗi máy chủ."));
        }
    }
}
