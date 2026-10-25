package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@WebServlet("/api/quan-ly/so-do-phong")
public class SoDoPhongServlet extends ServletDanhMuc {
    {
        dichVu = new vn.edu.quanlyhomestay.service.SoDoPhongService();
    }

    @Override
    protected void doGet(HttpServletRequest q, HttpServletResponse p) throws IOException {
        try {
            String ngay = q.getParameter("ngay");
            LocalDate d = ngay == null ? LocalDate.now() : LocalDate.parse(ngay);
            tra(p, 200, dichVu.soDo(d).get(0), "Lấy sơ đồ phòng thành công.");
        } catch (java.time.format.DateTimeParseException e) {
            loi(p, new IllegalArgumentException("Ngày phải theo định dạng yyyy-MM-dd."));
        } catch (Exception e) {
            loi(p, e);
        }
    }
}
