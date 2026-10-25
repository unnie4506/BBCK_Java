package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import vn.edu.quanlyhomestay.util.PhanTrangUtil;

@WebServlet("/api/quan-ly/khach-hang/*")
public class KhachHangServlet extends ServletDanhMuc {
    {
        dichVu = new vn.edu.quanlyhomestay.service.KhachHangService();
    }

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        List<String> d = duongDan(q);
        try {
            if ("GET".equals(q.getMethod())) {
                if (d.isEmpty())
                    tra(p, 200,
                            dichVu.khachHang(q.getParameter("tuKhoa"),
                                    PhanTrangUtil.docSoTrang(q.getParameter("trang")),
                                    PhanTrangUtil.docKichThuoc(q.getParameter("kichThuoc"))),
                            "Tìm khách hàng thành công.");
                else if (d.size() == 1)
                    tra(p, 200, dichVu.khachTheoMa(ma(d, 0)).get(0), "Lấy hồ sơ khách hàng thành công.");
                else if (d.size() == 2 && "lich-su-luu-tru".equals(d.get(1))) {
                    dichVu.khachTheoMa(ma(d, 0));
                    var ketQua = new vn.edu.quanlyhomestay.service.LichSuLuuTruService().danhSachTheoHoSo(ma(d, 0),
                            PhanTrangUtil.docSoTrang(q.getParameter("trang")),
                            PhanTrangUtil.docKichThuoc(q.getParameter("kichThuoc")));
                    tra(p, 200, ketQua, (String) ketQua.get("thongBao"));
                } else
                    phuongThuc(p);
                return;
            }
            if ("POST".equals(q.getMethod())) {
                if (!json(q, p))
                    return;
                Map<String, Object> x = doc(q);
                if (d.isEmpty()) {
                    long id = dichVu.luuKhach(null, x);
                    tra(p, 201, Map.of("maKhachHang", id), "Thêm khách hàng thành công.");
                } else if (d.size() == 2 && Set.of("ngung-su-dung", "su-dung-lai").contains(d.get(1))) {
                    boolean active = d.get(1).equals("su-dung-lai");
                    dichVu.doiHoatDong("ho_so_khach_hang", "", ma(d, 0), active);
                    tra(p, 200, null,
                            active ? "Đã sử dụng lại hồ sơ khách hàng." : "Đã ngừng sử dụng hồ sơ khách hàng.");
                } else
                    phuongThuc(p);
                return;
            }
            if ("PUT".equals(q.getMethod()) && d.size() == 1) {
                if (!json(q, p))
                    return;
                long id = dichVu.luuKhach(ma(d, 0), doc(q));
                tra(p, 200, Map.of("maKhachHang", id), "Cập nhật hồ sơ khách hàng thành công.");
                return;
            }
            phuongThuc(p);
        } catch (Exception e) {
            loi(p, e);
        }
    }
}
