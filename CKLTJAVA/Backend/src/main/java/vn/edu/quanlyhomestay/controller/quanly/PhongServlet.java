package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/quan-ly/phong/*")
@MultipartConfig(maxFileSize = 5242880, maxRequestSize = 26214400, fileSizeThreshold = 1048576)
public class PhongServlet extends ServletDanhMuc {
    {
        dichVu = new vn.edu.quanlyhomestay.service.PhongService();
    }
    private final AnhPhongServlet xuLyAnh = new AnhPhongServlet();

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        List<String> d = duongDan(q);
        try {
            if (xuLyAnh.xuLyAnh(q, p, d))
                return;
            if ("GET".equals(q.getMethod())) {
                if (d.isEmpty()) {
                    Map<String, String> f = new HashMap<>();
                    for (String k : List.of("tuKhoa", "tang", "maLoaiPhong", "trangThai", "dangHoatDong"))
                        f.put(k, q.getParameter(k));
                    tra(p, 200, dichVu.phong(f), "Lấy danh sách phòng thành công.");
                } else if (d.size() == 1)
                    tra(p, 200, dichVu.chiTietPhong(ma(d, 0)).get(0), "Lấy thông tin phòng thành công.");
                else
                    phuongThuc(p);
                return;
            }
            if ("POST".equals(q.getMethod())) {
                if (!json(q, p))
                    return;
                if (d.isEmpty()) {
                    long id = dichVu.luuPhong(null, doc(q));
                    tra(p, 201, Map.of("maPhong", id), "Thêm phòng thành công.");
                } else if (d.size() == 2 && Set.of("ngung-su-dung", "su-dung-lai").contains(d.get(1))) {
                    boolean active = d.get(1).equals("su-dung-lai");
                    dichVu.doiHoatDong("phong", "", ma(d, 0), active);
                    tra(p, 200, null, active ? "Đã sử dụng lại phòng." : "Đã ngừng sử dụng phòng.");
                } else
                    phuongThuc(p);
                return;
            }
            if ("PUT".equals(q.getMethod())) {
                if (!json(q, p))
                    return;
                if (d.size() == 1) {
                    long id = dichVu.luuPhong(ma(d, 0), doc(q));
                    tra(p, 200, Map.of("maPhong", id), "Cập nhật phòng thành công.");
                } else if (d.size() == 2 && "trang-thai".equals(d.get(1))) {
                    dichVu.doiTrangThaiPhong(ma(d, 0), (String) doc(q).get("trangThai"));
                    tra(p, 200, null, "Cập nhật trạng thái phòng thành công.");
                } else
                    phuongThuc(p);
                return;
            }
            phuongThuc(p);
        } catch (IllegalStateException e) {
            vn.edu.quanlyhomestay.util.TienIchJson.ghiPhanHoiJson(p, 413,
                    vn.edu.quanlyhomestay.util.PhanHoiApi.thatBai("Tổng dung lượng tệp gửi lên quá lớn."));
        } catch (Exception e) {
            loi(p, e);
        }
    }
}
