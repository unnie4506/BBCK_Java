package vn.edu.quanlyhomestay.controller.khachhang;

import com.google.gson.JsonParseException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.service.*;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/khach-hang/ho-so")
public class HoSoKhachHangServlet extends HttpServlet {
    private final HoSoKhachHangService dichVu = new HoSoKhachHangService();

    @Override
    protected void doGet(HttpServletRequest q, HttpServletResponse p) throws IOException {
        try {
            long ma = maKhach(q);
            TienIchJson.ghiPhanHoiJson(p, 200, PhanHoiApi.thanhCong("Lấy hồ sơ thành công.", dichVu.xem(ma)));
        } catch (Exception e) {
            ghiLoi(p, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest q, HttpServletResponse p) throws IOException {
        if (!TienIchJson.yeuCauNoiDungJson(q, p))
            return;
        try {
            YeuCauHoSo y = TienIchJson.docThanJson(q, YeuCauHoSo.class);
            if (y == null)
                throw new IllegalArgumentException("Nội dung JSON không hợp lệ.");
            long ma = maKhach(q);
            Map<String, Object> data = dichVu.capNhat(ma, y.hoTen, y.soDienThoai, y.soGiayTo);
            q.getSession(false).setAttribute(TienIchPhien.TEN_HIEN_THI, data.get("hoTen"));
            TienIchJson.ghiPhanHoiJson(p, 200, PhanHoiApi.thanhCong("Cập nhật hồ sơ thành công.", data));
        } catch (JsonParseException e) {
            ghiLoi(p, new IllegalArgumentException("Nội dung JSON không hợp lệ."));
        } catch (Exception e) {
            ghiLoi(p, e);
        }
    }

    private long maKhach(HttpServletRequest q) {
        Long ma = TienIchPhien.getMaNguoiDung(q);
        if (ma == null)
            throw new NghiepVuException(401, "CHUA_DANG_NHAP", "Vui lòng đăng nhập.");
        return ma;
    }

    private void ghiLoi(HttpServletResponse p, Exception e) throws IOException {
        if (e instanceof NghiepVuException n)
            TienIchJson.ghiPhanHoiJson(p, n.getMaHttp(),
                    new PhanHoiApi(false, n.getMessage(), n.getDuLieu(), n.getMaLoi()));
        else if (e instanceof IllegalArgumentException)
            TienIchJson.ghiPhanHoiJson(p, 400, PhanHoiApi.thatBai(e.getMessage()));
        else {
            getServletContext().log("Lỗi hồ sơ khách hàng.", e);
            TienIchJson.ghiPhanHoiJson(p, 500, PhanHoiApi.thatBai("Đã xảy ra lỗi máy chủ."));
        }
    }

    private static class YeuCauHoSo {
        private String hoTen;
        private String soDienThoai;
        private String soGiayTo;
        private String thuDienTu;
    }
}
