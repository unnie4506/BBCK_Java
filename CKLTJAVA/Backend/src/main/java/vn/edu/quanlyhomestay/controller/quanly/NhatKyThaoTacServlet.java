package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.quanlyhomestay.service.NghiepVuException;
import vn.edu.quanlyhomestay.service.NhatKyThaoTacService;
import vn.edu.quanlyhomestay.util.PhanTrangUtil;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/quan-ly/nhat-ky-thao-tac/*")
public class NhatKyThaoTacServlet extends ServletNghiepVu {
    private final NhatKyThaoTacService dichVu = new NhatKyThaoTacService();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            if (!"GET".equals(request.getMethod())) {
                phuongThuc(response);
                return;
            }
            List<String> phanDoan = vn.edu.quanlyhomestay.util.DuongDanUtil.tach(request.getPathInfo());
            if (phanDoan.size() != 1) {
                throw new NghiepVuException(400, "THIEU_MA_DON", "Cần cung cấp mã đơn đặt phòng.");
            }
            int trang = PhanTrangUtil.docSoTrang(request.getParameter("trang"));
            int kichThuoc = PhanTrangUtil.docKichThuoc(request.getParameter("kichThuoc"));
            thanhCong(response, 200, "Lấy nhật ký thao tác thành công.",
                    dichVu.timTheoDon(maLong(phanDoan.get(0)), trang, kichThuoc, nguoi(request)));
        } catch (Exception e) {
            thatBai(response, e);
        }
    }
}
