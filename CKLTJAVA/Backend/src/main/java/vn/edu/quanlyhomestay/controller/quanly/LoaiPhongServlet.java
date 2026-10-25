package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/quan-ly/loai-phong/*")
@MultipartConfig(maxFileSize = 5242880, maxRequestSize = 26214400, fileSizeThreshold = 1048576)
public class LoaiPhongServlet extends ServletDanhMuc {
    {
        dichVu = new vn.edu.quanlyhomestay.service.LoaiPhongService();
    }
    private final AnhLoaiPhongServlet xuLyAnh = new AnhLoaiPhongServlet();

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        List<String> d = duongDan(q);
        try {
            if (xuLyAnh.xuLyAnh(q, p, d))
                return;
            if ("GET".equals(q.getMethod())) {
                if (d.isEmpty())
                    tra(p, 200,
                            dichVu.loaiPhong(q.getParameter("tuKhoa"),
                                    q.getParameter("dangHoatDong") == null ? null
                                            : Boolean.valueOf(q.getParameter("dangHoatDong"))),
                            "Lấy danh sách loại phòng thành công.");
                else if (d.size() == 1)
                    tra(p, 200, dichVu.chiTietLoai(ma(d, 0)).get(0), "Lấy loại phòng thành công.");
                else
                    phuongThuc(p);
                return;
            }
            if ("POST".equals(q.getMethod())) {
                if (!json(q, p))
                    return;
                if (d.isEmpty()) {
                    long id = dichVu.luuLoai(null, doc(q));
                    tra(p, 201, Map.of("maLoaiPhong", id), "Thêm loại phòng thành công.");
                } else if (d.size() == 2 && "ngung-su-dung".equals(d.get(1))) {
                    dichVu.doiHoatDong("loai_phong", "", ma(d, 0), false);
                    tra(p, 200, null, "Đã ngừng sử dụng loại phòng.");
                } else if (d.size() == 2 && "su-dung-lai".equals(d.get(1))) {
                    dichVu.doiHoatDong("loai_phong", "", ma(d, 0), true);
                    tra(p, 200, null, "Đã sử dụng lại loại phòng.");
                } else
                    phuongThuc(p);
                return;
            }
            if ("PUT".equals(q.getMethod()) && d.size() == 1) {
                if (!json(q, p))
                    return;
                long id = dichVu.luuLoai(ma(d, 0), doc(q));
                tra(p, 200, Map.of("maLoaiPhong", id), "Cập nhật loại phòng thành công.");
                return;
            }
            phuongThuc(p);
        } catch (Exception e) {
            loi(p, e);
        }
    }
}
