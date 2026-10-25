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

public class KhachHangFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest yeuCau, ServletResponse phanHoi, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) yeuCau;
        HttpServletResponse httpResponse = (HttpServletResponse) phanHoi;
        String vaiTro = TienIchPhien.getVaiTro(httpRequest);

        if (vaiTro == null) {
            TienIchJson.ghiPhanHoiJson(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    PhanHoiApi.thatBai("Vui lòng đăng nhập để tiếp tục."));
            return;
        }
        if (!TienIchPhien.VAI_TRO_KHACH_HANG.equals(vaiTro)) {
            TienIchJson.ghiPhanHoiJson(httpResponse, HttpServletResponse.SC_FORBIDDEN,
                    PhanHoiApi.thatBai("Bạn không có quyền thực hiện thao tác này."));
            return;
        }
        chain.doFilter(yeuCau, phanHoi);
    }

    @Override
    public void destroy() {
    }
}
