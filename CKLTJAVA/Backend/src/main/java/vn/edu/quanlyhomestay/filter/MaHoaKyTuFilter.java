package vn.edu.quanlyhomestay.filter;

import vn.edu.quanlyhomestay.util.PhanHoiApi;
import vn.edu.quanlyhomestay.util.TienIchJson;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MaHoaKyTuFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest yeuCau, ServletResponse phanHoi, FilterChain chain)
            throws IOException, ServletException {
        yeuCau.setCharacterEncoding("UTF-8");
        phanHoi.setCharacterEncoding("UTF-8");
        phanHoi.setContentType("application/json;charset=UTF-8");

        HttpServletRequest httpRequest = (HttpServletRequest) yeuCau;
        HttpServletResponse httpResponse = (HttpServletResponse) phanHoi;
        String phuong_thuc = httpRequest.getMethod();
        if ("POST".equalsIgnoreCase(phuong_thuc) || "PUT".equalsIgnoreCase(phuong_thuc)
                || "DELETE".equalsIgnoreCase(phuong_thuc)) {
            // Chống CSRF cơ bản: API chỉ nhận request thay đổi dữ liệu dưới dạng JSON.
            if (!TienIchJson.yeuCauNoiDungJson(httpRequest, httpResponse)) {
                return;
            }
        }
        chain.doFilter(yeuCau, phanHoi);
        // Phiên chỉ thêm Secure khi container xác nhận request đang dùng HTTPS.
        if (httpRequest.isSecure() && !httpResponse.isCommitted()) {
            List<String> cookies = new ArrayList<>(httpResponse.getHeaders("Set-Cookie"));
            boolean daDatCookie = false;
            httpResponse.setHeader("Set-Cookie", null);
            for (String cookie : cookies) {
                String capNhat = cookie;
                if (cookie.startsWith("JSESSIONID=")
                        && !cookie.toLowerCase(java.util.Locale.ROOT).contains("; secure")) {
                    capNhat = cookie + "; Secure";
                }
                if (!daDatCookie) {
                    httpResponse.setHeader("Set-Cookie", capNhat);
                    daDatCookie = true;
                } else {
                    httpResponse.addHeader("Set-Cookie", capNhat);
                }
            }
        }
    }

    @Override
    public void destroy() {
    }
}
