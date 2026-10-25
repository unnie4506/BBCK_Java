package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.service.PhongTrongService;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/quan-ly/phong-trong")
public class PhongTrongServlet extends ServletNghiepVu {
    private final PhongTrongService dichVu = new PhongTrongService();

    @Override
    protected void doGet(HttpServletRequest q, HttpServletResponse p) throws IOException {
        try {
            Long maLoai = maLong(q.getParameter("maLoaiPhong")), boQua = maLong(q.getParameter("boQuaMaDon"));
            Integer tang = null;
            try {
                String t = q.getParameter("tang");
                if (t != null && !t.isBlank()) {
                    tang = Integer.valueOf(t);
                    if (tang < 1)
                        throw new NumberFormatException();
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Tầng phải là số nguyên dương.");
            }
            List<Map<String, Object>> ds = dichVu.timPhong(ngay(q.getParameter("ngayNhan")),
                    ngay(q.getParameter("ngayTra")), maLoai, tang, boQua, nguoi(q));
            String tb = ds.isEmpty() ? "Không tìm thấy phòng trống. Hãy thử đổi ngày hoặc bỏ bớt bộ lọc."
                    : "Tìm phòng trống thành công.";
            thanhCong(p, 200, tb, ds);
        } catch (Exception e) {
            thatBai(p, e);
        }
    }
}
