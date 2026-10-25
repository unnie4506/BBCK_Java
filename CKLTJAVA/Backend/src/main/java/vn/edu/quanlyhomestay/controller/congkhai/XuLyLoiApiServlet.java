package vn.edu.quanlyhomestay.controller.congkhai;

import vn.edu.quanlyhomestay.util.PhanHoiApi;
import vn.edu.quanlyhomestay.util.TienIchJson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class XuLyLoiApiServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        Object giaTriTrangThai = yeuCau.getAttribute("jakarta.servlet.error.status_code");
        int trangThai = giaTriTrangThai instanceof Integer ? (Integer) giaTriTrangThai
                : HttpServletResponse.SC_NOT_FOUND;
        String thongBao;
        if (trangThai == HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
            thongBao = "Phương thức HTTP không được hỗ trợ.";
        } else if (trangThai == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            thongBao = "Đã xảy ra lỗi máy chủ.";
        } else {
            thongBao = "Không tìm thấy tài nguyên.";
        }
        TienIchJson.ghiPhanHoiJson(phanHoi, trangThai, PhanHoiApi.thatBai(thongBao));
    }
}
