package vn.edu.quanlyhomestay.controller.congkhai;

import vn.edu.quanlyhomestay.util.TepAnhUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;

@WebServlet("/api/cong-khai/anh-phong/*")
public class AnhCongKhaiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest q, HttpServletResponse p) throws IOException {
        String ten = q.getPathInfo();
        if (ten != null && ten.startsWith("/"))
            ten = ten.substring(1);
        try {
            Path f = TepAnhUtil.duongDanAnToan(ten);
            p.setContentType(ten.endsWith(".png") ? "image/png" : "image/jpeg");
            p.setHeader("X-Content-Type-Options", "nosniff");
            p.setHeader("Cache-Control", "public, max-age=86400");
            Files.copy(f, p.getOutputStream());
        } catch (FileNotFoundException e) {
            p.sendError(404);
        } catch (Exception e) {
            getServletContext().log("Không thể tải ảnh phòng.", e);
            p.sendError(404);
        }
    }
}
