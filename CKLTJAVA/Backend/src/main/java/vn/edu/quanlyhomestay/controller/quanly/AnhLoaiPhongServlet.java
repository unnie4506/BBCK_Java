package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.service.*;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/* Handler cho ảnh loại phòng, dùng chung URL wildcard với LoaiPhongServlet. */
public class AnhLoaiPhongServlet extends ServletDanhMuc {
    private final AnhLoaiPhongService dichVuAnh = new AnhLoaiPhongService();

    public boolean xuLyAnh(HttpServletRequest q, HttpServletResponse p, List<String> d) throws IOException {
        if (d.size() < 2 || !"anh".equals(d.get(1))) return false;
        try {
            if (d.size() == 2 && "GET".equals(q.getMethod())) {
                tra(p, 200, dichVuAnh.danhSach(ma(d, 0)), "Lấy danh sách ảnh loại phòng thành công.");
                return true;
            }
            if (d.size() == 2 && "POST".equals(q.getMethod())) {
                if (q.getContentType() == null
                        || !q.getContentType().toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
                    TienIchJson.ghiPhanHoiJson(p, 415, PhanHoiApi.thatBai("Yêu cầu multipart/form-data."));
                    return true;
                }
                List<String> names = new ArrayList<>();
                List<byte[]> bytes = new ArrayList<>();
                for (Part part : q.getParts()) {
                    if (!"anh".equals(part.getName()) || part.getSize() == 0) continue;
                    if (part.getSize() > TepAnhUtil.TOI_DA_BYTE) {
                        TienIchJson.ghiPhanHoiJson(p, 413, PhanHoiApi.thatBai("Mỗi ảnh tối đa 5 MB."));
                        return true;
                    }
                    String ten = part.getSubmittedFileName();
                    names.add(ten == null ? "" : Paths.get(ten).getFileName().toString());
                    bytes.add(part.getInputStream().readAllBytes());
                }
                List<Long> ids = dichVuAnh.themNhieu(ma(d, 0), names, bytes);
                tra(p, 201, Map.of("danhSachMaAnhLoaiPhong", ids), "Tải ảnh loại phòng lên thành công.");
                return true;
            }
            if (d.size() == 3 && "DELETE".equals(q.getMethod())) {
                if (!json(q, p)) return true;
                dichVuAnh.xoa(ma(d, 0), ma(d, 2));
                tra(p, 200, null, "Đã xóa ảnh loại phòng.");
                return true;
            }
            if (d.size() == 4 && "dai-dien".equals(d.get(3)) && "PUT".equals(q.getMethod())) {
                if (!json(q, p)) return true;
                dichVuAnh.datDaiDien(ma(d, 0), ma(d, 2));
                tra(p, 200, null, "Đã đặt ảnh đại diện loại phòng.");
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
                q.getServletContext().log("Lỗi xử lý ảnh loại phòng.", e);
                TienIchJson.ghiPhanHoiJson(p, 500, PhanHoiApi.thatBai("Đã xảy ra lỗi máy chủ. Vui lòng thử lại."));
            }
            return true;
        }
    }
}
