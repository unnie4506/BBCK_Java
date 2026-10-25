package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.service.*;
import vn.edu.quanlyhomestay.util.*;
import java.io.*;
import java.util.*;
import java.nio.file.Paths;

/* Handler controller được PhongServlet gọi vì cả phòng và ảnh dùng chung URL wildcard /phong/*. */
public class AnhPhongServlet extends ServletDanhMuc {
    private final AnhPhongService dichVuAnh = new AnhPhongService();

    public boolean xuLyAnh(HttpServletRequest q, HttpServletResponse p, List<String> d) throws IOException {
        boolean laDuongDan = d.size() >= 2 && "anh".equals(d.get(1));
        if (!laDuongDan)
            return false;
        try {
            if (d.size() == 2 && "POST".equals(q.getMethod())) {
                if (q.getContentType() == null
                        || !q.getContentType().toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
                    TienIchJson.ghiPhanHoiJson(p, 415, PhanHoiApi.thatBai("Yêu cầu multipart/form-data."));
                    return true;
                }
                List<String> names = new ArrayList<>();
                List<byte[]> bytes = new ArrayList<>();
                for (Part part : q.getParts())
                    if ("anh".equals(part.getName()) && part.getSize() > 0) {
                        if (part.getSize() > TepAnhUtil.TOI_DA_BYTE) {
                            TienIchJson.ghiPhanHoiJson(p, 413, PhanHoiApi.thatBai("Mỗi ảnh tối đa 5 MB."));
                            return true;
                        }
                        String ten = part.getSubmittedFileName();
                        names.add(ten == null ? "" : Paths.get(ten).getFileName().toString());
                        bytes.add(part.getInputStream().readAllBytes());
                    }
                List<Long> ids = dichVuAnh.themNhieu(ma(d, 0), names, bytes);
                tra(p, 201, Map.of("danhSachMaAnhPhong", ids), "Tải ảnh lên thành công.");
                return true;
            }
            if (d.size() == 3 && "DELETE".equals(q.getMethod())) {
                if (!json(q, p))
                    return true;
                dichVuAnh.xoa(ma(d, 0), ma(d, 2));
                tra(p, 200, null, "Đã ngừng sử dụng ảnh.");
                return true;
            }
            if (d.size() == 4 && "dai-dien".equals(d.get(3)) && "PUT".equals(q.getMethod())) {
                if (!json(q, p))
                    return true;
                dichVuAnh.datDaiDien(ma(d, 0), ma(d, 2));
                tra(p, 200, null, "Đã đặt ảnh đại diện.");
                return true;
            }
            TienIchJson.ghiPhanHoiJson(p, 404, PhanHoiApi.thatBai("Không tìm thấy tài nguyên."));
            return true;
        } catch (IllegalStateException e) {
            TienIchJson.ghiPhanHoiJson(p, 413, PhanHoiApi.thatBai("Tổng dung lượng tệp gửi lên quá lớn."));
            return true;
        } catch (Exception e) {
            if (e instanceof NghiepVuException n)
                TienIchJson.ghiPhanHoiJson(p, n.getMaHttp(), new PhanHoiApi(false, n.getMessage(), n.getDuLieu()));
            else if (e instanceof IllegalArgumentException)
                TienIchJson.ghiPhanHoiJson(p, 400, PhanHoiApi.thatBai(e.getMessage()));
            else {
                q.getServletContext().log("Lỗi xử lý ảnh phòng.", e);
                TienIchJson.ghiPhanHoiJson(p, 500, PhanHoiApi.thatBai("Đã xảy ra lỗi máy chủ. Vui lòng thử lại."));
            }
            return true;
        }
    }
}
