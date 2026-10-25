package vn.edu.quanlyhomestay.controller.quanly;

import com.google.gson.Gson;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.service.NghiepVuException;
import vn.edu.quanlyhomestay.util.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public abstract class ServletNghiepVu extends HttpServlet {
    protected final Gson gson = TienIchJson.taoGson();

    protected NguoiThaoTac nguoi(HttpServletRequest q) {
        Long ma = TienIchPhien.getMaNguoiDung(q);
        String vaiTro = TienIchPhien.getVaiTro(q);
        if (ma == null || vaiTro == null)
            throw new NghiepVuException(401, "CHUA_DANG_NHAP", "Vui lòng đăng nhập.");
        return new NguoiThaoTac(vaiTro, ma);
    }

    protected <T> T doc(HttpServletRequest q, Class<T> loai) throws IOException {
        try {
            T x = TienIchJson.docThanJson(q, loai);
            if (x == null)
                throw new IllegalArgumentException("Nội dung JSON không được để trống.");
            return x;
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException i)
                throw i;
            throw new IllegalArgumentException("Nội dung JSON không hợp lệ.", e);
        }
    }

    protected void thanhCong(HttpServletResponse p, int ma, String thongBao, Object duLieu) throws IOException {
        TienIchJson.ghiPhanHoiJson(p, ma, PhanHoiApi.thanhCong(thongBao, duLieu));
    }

    protected void thatBai(HttpServletResponse p, Exception e) throws IOException {
        if (e instanceof NghiepVuException n) {
            TienIchJson.ghiPhanHoiJson(p, n.getMaHttp(),
                    new PhanHoiApi(false, n.getMessage(), n.getDuLieu(), n.getMaLoi()));
            return;
        }
        if (e instanceof IllegalArgumentException) {
            TienIchJson.ghiPhanHoiJson(p, 400, new PhanHoiApi(false, e.getMessage(), null, "DU_LIEU_KHONG_HOP_LE"));
            return;
        }
        getServletContext().log("Lỗi xử lý nghiệp vụ.", e);
        TienIchJson.ghiPhanHoiJson(p, 500,
                new PhanHoiApi(false, "Đã xảy ra lỗi máy chủ. Vui lòng thử lại.", null, "LOI_HE_THONG"));
    }

    protected Long maLong(String s) {
        try {
            if (s == null)
                return null;
            long n = Long.parseLong(s);
            if (n > 0)
                return n;
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("Mã phải là số nguyên dương.");
    }

    protected java.time.LocalDate ngay(String s) {
        try {
            return s == null || s.isBlank() ? null : java.time.LocalDate.parse(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ngày phải theo định dạng yyyy-MM-dd.");
        }
    }

    protected void phuongThuc(HttpServletResponse p) throws IOException {
        TienIchJson.ghiPhanHoiJson(p, 405,
                new PhanHoiApi(false, "Phương thức HTTP không được hỗ trợ.", null, "PHUONG_THUC_KHONG_HO_TRO"));
    }
}
