package vn.edu.quanlyhomestay.filter;

import vn.edu.quanlyhomestay.util.PhanHoiApi;
import vn.edu.quanlyhomestay.util.TienIchJson;
import vn.edu.quanlyhomestay.util.TienIchPhien;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class QuanLyFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest yeuCau, ServletResponse phanHoi, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) yeuCau;
        HttpServletResponse httpResponse = (HttpServletResponse) phanHoi;
        String duongDan = httpRequest.getServletPath();

        if ("/api/quan-ly/xac-thuc/dang-nhap".equals(duongDan)) {
            chain.doFilter(yeuCau, phanHoi);
            return;
        }

        if (TienIchPhien.VAI_TRO_QUAN_LY.equals(TienIchPhien.getVaiTro(httpRequest))) {
            chain.doFilter(yeuCau, phanHoi);
            return;
        }

        // Ẩn cả trường hợp chưa đăng nhập và sai role như một đường dẫn không tồn tại.
        TienIchJson.ghiPhanHoiJson(httpResponse, HttpServletResponse.SC_NOT_FOUND,
                PhanHoiApi.thatBai("Không tìm thấy tài nguyên."));
    }

    @Override
    public void destroy() {
    }
}
