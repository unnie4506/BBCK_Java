package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.model.YeuCauChinhSachHoanHuy;
import vn.edu.quanlyhomestay.service.ChinhSachHoanHuyService;
import vn.edu.quanlyhomestay.util.TienIchJson;
import java.io.IOException;

@WebServlet("/api/quan-ly/chinh-sach-hoan-huy")
public class ChinhSachHoanHuyServlet extends ServletNghiepVu {
    private final ChinhSachHoanHuyService dichVu = new ChinhSachHoanHuyService();

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        try {
            if ("GET".equals(q.getMethod())) {
                thanhCong(p, 200, "Lấy chính sách hoàn hủy thành công.", dichVu.xem(nguoi(q)));
                return;
            }
            if ("PUT".equals(q.getMethod())) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                YeuCauChinhSachHoanHuy yeuCau = doc(q, YeuCauChinhSachHoanHuy.class);
                dichVu.capNhat(yeuCau.getDanhSachBac(), nguoi(q));
                thanhCong(p, 200, "Cập nhật chính sách hoàn hủy thành công.", dichVu.xem(nguoi(q)));
                return;
            }
            phuongThuc(p);
        } catch (Exception e) {
            thatBai(p, e);
        }
    }
}
