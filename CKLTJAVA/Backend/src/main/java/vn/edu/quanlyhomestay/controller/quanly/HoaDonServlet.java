package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.service.HoaDonService;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/api/quan-ly/hoa-don/*")
public class HoaDonServlet extends ServletNghiepVu {
    private final HoaDonService dichVu = new HoaDonService();

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        List<String> duongDan = DuongDanUtil.tach(q.getPathInfo());
        try {
            if ("GET".equals(q.getMethod()) && duongDan.size() == 1) {
                thanhCong(p, 200, "Lấy hóa đơn thành công.", dichVu.layTheoMa(maLong(duongDan.get(0)), nguoi(q)));
                return;
            }
            if ("POST".equals(q.getMethod()) && duongDan.size() == 2 && "thanh-toan".equals(duongDan.get(1))) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                YeuCauThanhToan y = doc(q, YeuCauThanhToan.class);
                thanhCong(p, 200, "Ghi nhận thanh toán thành công.",
                        dichVu.thanhToan(maLong(duongDan.get(0)), y.phuongThuc, y.soTien, nguoi(q)));
                return;
            }
            phuongThuc(p);
        } catch (Exception e) {
            thatBai(p, e);
        }
    }

    private static class YeuCauThanhToan {
        private String phuongThuc;
        private BigDecimal soTien;
    }
}
