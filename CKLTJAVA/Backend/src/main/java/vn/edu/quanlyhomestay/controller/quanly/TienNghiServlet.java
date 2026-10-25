package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/quan-ly/tien-nghi/*")
public class TienNghiServlet extends ServletDanhMuc {
    {
        dichVu = new vn.edu.quanlyhomestay.service.TienNghiService();
    }

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        List<String> d = duongDan(q);
        try {
            if ("GET".equals(q.getMethod()) && d.isEmpty()) {
                tra(p, 200, dichVu.tienNghi(), "Lấy danh sách tiện nghi thành công.");
                return;
            }
            if ("POST".equals(q.getMethod()) && d.isEmpty()) {
                if (!json(q, p))
                    return;
                long id = dichVu.luuTienNghi(null, (String) doc(q).get("ten"));
                tra(p, 201, Map.of("maTienNghi", id), "Thêm tiện nghi thành công.");
                return;
            }
            if ("PUT".equals(q.getMethod()) && d.size() == 1) {
                if (!json(q, p))
                    return;
                long id = dichVu.luuTienNghi(ma(d, 0), (String) doc(q).get("ten"));
                tra(p, 200, Map.of("maTienNghi", id), "Cập nhật tiện nghi thành công.");
                return;
            }
            if ("POST".equals(q.getMethod()) && d.size() == 2
                    && Set.of("ngung-su-dung", "su-dung-lai").contains(d.get(1))) {
                if (!json(q, p))
                    return;
                boolean hoatDong = "su-dung-lai".equals(d.get(1));
                dichVu.doiHoatDong("tien_nghi", "", ma(d, 0), hoatDong);
                tra(p, 200, null, hoatDong ? "Đã sử dụng lại tiện nghi." : "Đã ngừng sử dụng tiện nghi.");
                return;
            }
            if ("DELETE".equals(q.getMethod()) && d.size() == 1) {
                if (!json(q, p))
                    return;
                dichVu.xoaTienNghi(ma(d, 0));
                tra(p, 200, null, "Xóa tiện nghi thành công.");
                return;
            }
            phuongThuc(p);
        } catch (Exception e) {
            loi(p, e);
        }
    }
}
