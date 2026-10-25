package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.service.HoanCocService;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@WebServlet("/api/quan-ly/hoan-coc/*")
public class HoanCocServlet extends ServletNghiepVu {
    private final HoanCocService dichVu = new HoanCocService();

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        List<String> d = DuongDanUtil.tach(q.getPathInfo());
        try {
            if ("GET".equals(q.getMethod()) && d.isEmpty()) {
                var ds = dichVu.danhSach(q.getParameter("trangThai"),
                        PhanTrangUtil.docKichThuoc(q.getParameter("kichThuoc")),
                        PhanTrangUtil.docSoTrang(q.getParameter("trang")), nguoi(q));
                thanhCong(p, 200, "Lấy danh sách hoàn cọc thành công.", ds);
                return;
            }
            if ("POST".equals(q.getMethod()) && d.size() == 2 && "xac-nhan".equals(d.get(1))) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                YeuCauXacNhan y = doc(q, YeuCauXacNhan.class);
                thanhCong(p, 200, "Đã xác nhận khoản hoàn cọc.",
                        dichVu.xacNhan(maLong(d.get(0)), y.soTienThucHoan, y.phuongThucHoan, nguoi(q)));
                return;
            }
            phuongThuc(p);
        } catch (Exception e) {
            thatBai(p, e);
        }
    }

    private static class YeuCauXacNhan {
        private BigDecimal soTienThucHoan;
        private String phuongThucHoan;
    }
}
