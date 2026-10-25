package vn.edu.quanlyhomestay.controller.congkhai;

import com.google.gson.Gson;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/cong-khai/ping")
public class KiemTraKetNoiServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        phanHoi.setCharacterEncoding("UTF-8");
        phanHoi.setContentType("application/json;charset=UTF-8");

        try (Connection ketNoi = KetNoiCoSoDuLieu.moKetNoi()) {
            boolean connected = ketNoi.isValid(3);
            phanHoi.setStatus(connected ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            phanHoi.getWriter().write(gson.toJson(Map.of("thanhCong", connected, "thongBao",
                    connected ? "Kết nối cơ sở dữ liệu thành công." : "Cơ sở dữ liệu chưa sẵn sàng.")));
        } catch (SQLException | IllegalStateException ngoaiLe) {
            getServletContext().log("Không thể kiểm tra kết nối cơ sở dữ liệu.", ngoaiLe);
            phanHoi.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            phanHoi.getWriter().write(gson.toJson(Map.of("thanhCong", false, "thongBao",
                    "Không thể kết nối cơ sở dữ liệu. Hãy kiểm tra cấu hình máy chủ.")));
        }
    }
}
