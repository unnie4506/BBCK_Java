package vn.edu.quanlyhomestay.controller.quanly;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import vn.edu.quanlyhomestay.service.DanhMucService;
import vn.edu.quanlyhomestay.service.NghiepVuException;
import vn.edu.quanlyhomestay.util.DuongDanUtil;
import vn.edu.quanlyhomestay.util.PhanHoiApi;
import vn.edu.quanlyhomestay.util.TienIchJson;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;

/** Servlet định tuyến các API danh mục; không chứa SQL hoặc quy tắc nghiệp vụ. */
public class ServletDanhMuc extends HttpServlet {
    private static final Type KIEU_DOI_TUONG = new TypeToken<Map<String, Object>>() {
    }.getType();
    protected DanhMucService dichVu = new DanhMucService();
    protected final Gson gson = TienIchJson.taoGson();

    protected Map<String, Object> doc(HttpServletRequest q) throws IOException {
        try {
            Map<String, Object> duLieu = gson.fromJson(q.getReader(), KIEU_DOI_TUONG);
            if (duLieu == null)
                throw new IllegalArgumentException("Nội dung JSON không được để trống.");
            return duLieu;
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException i)
                throw i;
            throw new IllegalArgumentException("Nội dung JSON không hợp lệ.", e);
        }
    }

    protected void tra(HttpServletResponse p, int ma, Object duLieu, String tb) throws IOException {
        TienIchJson.ghiPhanHoiJson(p, ma, new PhanHoiApi(true, tb, duLieu));
    }

    protected void loi(HttpServletResponse p, Exception e) throws IOException {
        if (e instanceof NghiepVuException n) {
            TienIchJson.ghiPhanHoiJson(p, n.getMaHttp(),
                    new PhanHoiApi(false, n.getMessage(), n.getDuLieu(), n.getMaLoi()));
            return;
        }
        if (e instanceof IllegalArgumentException) {
            TienIchJson.ghiPhanHoiJson(p, 400, PhanHoiApi.thatBai(e.getMessage()));
            return;
        }
        getServletContext().log("Lỗi xử lý API danh mục.", e);
        TienIchJson.ghiPhanHoiJson(p, 500, PhanHoiApi.thatBai("Đã xảy ra lỗi máy chủ. Vui lòng thử lại."));
    }

    protected long ma(List<String> d, int viTri) {
        if (d.size() <= viTri)
            throw new IllegalArgumentException("Thiếu mã tài nguyên.");
        return DuongDanUtil.docMa(d.get(viTri));
    }

    protected boolean json(HttpServletRequest q, HttpServletResponse p) throws IOException {
        return TienIchJson.yeuCauNoiDungJson(q, p);
    }

    protected List<String> duongDan(HttpServletRequest q) {
        return DuongDanUtil.tach(q.getPathInfo());
    }

    protected void phuongThuc(HttpServletResponse p) throws IOException {
        TienIchJson.ghiPhanHoiJson(p, 405, PhanHoiApi.thatBai("Phương thức HTTP không được hỗ trợ."));
    }
}
