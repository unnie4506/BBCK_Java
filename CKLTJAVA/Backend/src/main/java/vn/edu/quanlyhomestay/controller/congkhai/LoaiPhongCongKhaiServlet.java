package vn.edu.quanlyhomestay.controller.congkhai;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.service.*;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = { "/api/cong-khai/loai-phong", "/api/cong-khai/loai-phong/*" })
public class LoaiPhongCongKhaiServlet extends HttpServlet {
    private final LoaiPhongCongKhaiService dichVu = new LoaiPhongCongKhaiService();

    @Override
    protected void doGet(HttpServletRequest q, HttpServletResponse p) throws IOException {
        try {
            List<String> d = DuongDanUtil.tach(q.getPathInfo());
            if (d.isEmpty()) {
                var ds = dichVu.danhSach();
                TienIchJson.ghiPhanHoiJson(p, 200, PhanHoiApi
                        .thanhCong(ds.isEmpty() ? "Chưa có phòng để hiển thị." : "Lấy loại phòng thành công.", ds));
                return;
            }
            if (d.size() == 1) {
                TienIchJson.ghiPhanHoiJson(p, 200,
                        PhanHoiApi.thanhCong("Lấy chi tiết loại phòng thành công.", dichVu.chiTiet(parse(d.get(0)))));
                return;
            }
            TienIchJson.ghiPhanHoiJson(p, 404, PhanHoiApi.thatBai("Không tìm thấy tài nguyên."));
        } catch (Exception e) {
            ghiLoi(p, e);
        }
    }

    private long parse(String s) {
        try {
            long n = Long.parseLong(s);
            if (n > 0)
                return n;
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Mã loại phòng không hợp lệ.");
    }

    private void ghiLoi(HttpServletResponse p, Exception e) throws IOException {
        if (e instanceof NghiepVuException n)
            TienIchJson.ghiPhanHoiJson(p, n.getMaHttp(),
                    new PhanHoiApi(false, n.getMessage(), n.getDuLieu(), n.getMaLoi()));
        else if (e instanceof IllegalArgumentException)
            TienIchJson.ghiPhanHoiJson(p, 400, PhanHoiApi.thatBai(e.getMessage()));
        else {
            getServletContext().log("Lỗi đọc loại phòng công khai.", e);
            TienIchJson.ghiPhanHoiJson(p, 500, PhanHoiApi.thatBai("Đã xảy ra lỗi máy chủ."));
        }
    }
}
