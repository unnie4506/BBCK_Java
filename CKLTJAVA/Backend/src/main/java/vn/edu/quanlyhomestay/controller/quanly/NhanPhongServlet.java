package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.service.NhanTraPhongService;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;

@WebServlet("/api/quan-ly/chi-tiet/*")
public class NhanPhongServlet extends ServletNghiepVu {
    private final NhanTraPhongService dichVu = new NhanTraPhongService();

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        var d = DuongDanUtil.tach(q.getPathInfo());
        try {
            if (!"POST".equals(q.getMethod()) || d.size() != 2) {
                phuongThuc(p);
                return;
            }
            if (!TienIchJson.yeuCauNoiDungJson(q, p))
                return;
            if ("check-in".equals(d.get(1))) {
                YeuCauNhanPhong y = doc(q, YeuCauNhanPhong.class);
                thanhCong(p, 200, "Nhận phòng thành công.",
                        dichVu.nhanPhong(maLong(d.get(0)), y.xacNhanNhanSom, nguoi(q)));
                return;
            }
            if ("gia-han".equals(d.get(1))) {
                YeuCauGiaHan y = doc(q, YeuCauGiaHan.class);
                thanhCong(p, 200, "Gia hạn phòng thành công.",
                        dichVu.giaHan(maLong(d.get(0)), docNgay(y.ngayTra), nguoi(q)));
                return;
            }
            if ("xem-truoc-tra".equals(d.get(1))) {
                YeuCauTraPhong y = doc(q, YeuCauTraPhong.class);
                thanhCong(p, 200, "Tính tiền trả phòng thành công.",
                        dichVu.xemTruocTraPhong(maLong(d.get(0)), docNgayTra(y.ngayTraThucTe), nguoi(q)));
                return;
            }
            if ("check-out".equals(d.get(1))) {
                YeuCauTraPhong y = doc(q, YeuCauTraPhong.class);
                thanhCong(p, 200, "Đã xử lý trả phòng.",
                        dichVu.traPhong(maLong(d.get(0)), docNgayTra(y.ngayTraThucTe), nguoi(q)));
                return;
            }
            phuongThuc(p);
        } catch (Exception e) {
            thatBai(p, e);
        }
    }

    private java.time.LocalDate docNgayTra(String s) {
        try {
            return s == null || s.isBlank() ? null : java.time.LocalDate.parse(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày phải theo định dạng yyyy-MM-dd.");
        }
    }

    private java.time.LocalDate docNgay(String s) {
        try {
            return s == null || s.isBlank() ? null : java.time.LocalDate.parse(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày phải theo định dạng yyyy-MM-dd.");
        }
    }

    private static class YeuCauNhanPhong {
        private boolean xacNhanNhanSom;
    }

    private static class YeuCauTraPhong {
        private String ngayTraThucTe;
    }

    private static class YeuCauGiaHan {
        private String ngayTra;
    }
}
