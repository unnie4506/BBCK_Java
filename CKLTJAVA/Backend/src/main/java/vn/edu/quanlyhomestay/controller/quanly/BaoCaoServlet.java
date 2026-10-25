package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.service.BaoCaoService;
import vn.edu.quanlyhomestay.util.KyThongKe;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/quan-ly/bao-cao/*")
public class BaoCaoServlet extends ServletNghiepVu {
    private final BaoCaoService dichVu = new BaoCaoService();

    @Override
    protected void doGet(HttpServletRequest q, HttpServletResponse p) throws IOException {
        try {
            List<String> d = vn.edu.quanlyhomestay.util.DuongDanUtil.tach(q.getPathInfo());
            java.time.LocalDate tu = ngay(q.getParameter("tuNgay")), den = ngay(q.getParameter("denNgay"));
            Object data;
            String msg;
            if (d.size() != 1) {
                thanhCong(p, 404, "Không tìm thấy báo cáo.", null);
                return;
            }
            switch (d.get(0)) {
            case "doanh-thu" -> {
                data = dichVu.doanhThu(tu, den, ky(q.getParameter("kyThongKe")), nguoi(q));
                msg = "Báo cáo doanh thu thành công.";
            }
            case "ty-le-lap-day" -> {
                data = dichVu.tyLeLapDay(tu, den, ky(q.getParameter("kyThongKe")), nguoi(q));
                msg = "Báo cáo tỷ lệ lấp đầy thành công.";
            }
            case "phong-dat-nhieu" -> {
                int top = 10;
                try {
                    String x = q.getParameter("top");
                    if (x != null)
                        top = Integer.parseInt(x);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Top phải là số nguyên.");
                }
                if (top < 1 || top > 100)
                    throw new IllegalArgumentException("Top phải từ 1 đến 100.");
                data = dichVu.phongDatNhieu(tu, den, top, nguoi(q));
                msg = "Báo cáo phòng được đặt nhiều thành công.";
            }
            case "khach-quay-lai" -> {
                data = dichVu.khachQuayLai(tu, den, nguoi(q));
                msg = "Báo cáo khách quay lại thành công.";
            }
            default -> {
                thanhCong(p, 404, "Không tìm thấy báo cáo.", null);
                return;
            }
            }
            Map<?, ?> m = (Map<?, ?>) data;
            boolean co = Boolean.TRUE.equals(m.get("coDuLieu"));
            thanhCong(p, 200, co ? msg : "Không có dữ liệu trong khoảng thời gian đã chọn.", data);
        } catch (Exception e) {
            thatBai(p, e);
        }
    }

    private KyThongKe ky(String s) {
        try {
            return KyThongKe.valueOf(s == null ? "" : s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Kỳ thống kê phải là NGAY, THANG hoặc NAM.");
        }
    }
}
