package vn.edu.quanlyhomestay.controller.khachhang;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.controller.quanly.ServletNghiepVu;
import vn.edu.quanlyhomestay.service.LichSuLuuTruService;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/khach-hang/lich-su-luu-tru/*")
public class LichSuLuuTruKhachHangServlet extends ServletNghiepVu {
    private final LichSuLuuTruService dichVu = new LichSuLuuTruService();

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        try {
            Long ma = TienIchPhien.getMaNguoiDung(q);
            if (ma == null)
                throw new vn.edu.quanlyhomestay.service.NghiepVuException(401, "CHUA_DANG_NHAP", "Vui lòng đăng nhập.");
            List<String> d = DuongDanUtil.tach(q.getPathInfo());
            if (!"GET".equals(q.getMethod())) {
                phuongThuc(p);
                return;
            }
            if (d.isEmpty()) {
                int trang = PhanTrangUtil.docSoTrang(q.getParameter("trang"));
                var kq = dichVu.danhSach(ma, trang, PhanTrangUtil.docKichThuoc(q.getParameter("kichThuoc")));
                thanhCong(p, 200, (String) kq.get("thongBao"), kq);
                return;
            }
            if (d.size() == 1) {
                thanhCong(p, 200, "Lấy chi tiết lịch sử và hóa đơn thành công.", dichVu.chiTiet(ma, maLong(d.get(0))));
                return;
            }
            thanhCong(p, 404, "Không tìm thấy tài nguyên.", null);
        } catch (Exception e) {
            thatBai(p, e);
        }
    }
}
