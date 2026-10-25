package vn.edu.quanlyhomestay.controller.khachhang;

import com.google.gson.JsonParseException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.controller.quanly.ServletNghiepVu;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.model.dto.DatPhongTrucTuyenYeuCau;
import vn.edu.quanlyhomestay.service.*;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/api/khach-hang/don-dat-phong/*")
public class DonDatPhongKhachHangServlet extends ServletNghiepVu {
    private final DatPhongTrucTuyenService dichVu = new DatPhongTrucTuyenService();

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        List<String> d = DuongDanUtil.tach(q.getPathInfo());
        try {
            Long ma = TienIchPhien.getMaNguoiDung(q);
            if (ma == null)
                throw new NghiepVuException(401, "CHUA_DANG_NHAP", "Vui lòng đăng nhập.");
            NguoiThaoTac actor = new NguoiThaoTac("KHACH_HANG", ma);
            if ("POST".equals(q.getMethod()) && d.equals(List.of("xem-truoc"))) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                var kq = dichVu.xemTruoc(doc(q, DatPhongTrucTuyenYeuCau.class));
                thanhCong(p, 200, "Xem trước giá và sức chứa thành công.", kq);
                return;
            }
            if ("POST".equals(q.getMethod()) && d.isEmpty()) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                var kq = dichVu.datPhong(ma, doc(q, DatPhongTrucTuyenYeuCau.class), actor);
                thanhCong(p, 201, "Đặt phòng thành công.", kq);
                return;
            }
            if ("GET".equals(q.getMethod()) && d.isEmpty()) {
                int trang = PhanTrangUtil.docSoTrang(q.getParameter("trang")),
                        kichThuoc = PhanTrangUtil.docKichThuoc(q.getParameter("kichThuoc"));
                var kq = dichVu.danhSachDon(ma, q.getParameter("trangThai"), trang, kichThuoc);
                long so = ((Number) kq.get("tongSoBanGhi")).longValue();
                thanhCong(p, 200, so == 0 ? "Bạn chưa có đơn đặt phòng." : "Lấy danh sách đơn của tôi thành công.", kq);
                return;
            }
            if (d.size() == 1 && "GET".equals(q.getMethod())) {
                thanhCong(p, 200, "Lấy chi tiết đơn thành công.", dichVu.chiTietDon(ma, maLong(d.get(0))));
                return;
            }
            if (d.size() == 2 && "xem-truoc-huy".equals(d.get(1)) && "GET".equals(q.getMethod())) {
                thanhCong(p, 200, "Xem trước khoản hoàn cọc.", dichVu.xemTruocHuy(ma, maLong(d.get(0))));
                return;
            }
            if (d.size() == 2 && "huy".equals(d.get(1)) && "POST".equals(q.getMethod())) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                YeuCauHuy y = doc(q, YeuCauHuy.class);
                if (y.lyDo != null && y.lyDo.length() > 1000)
                    throw new IllegalArgumentException("Lý do hủy tối đa 1000 ký tự.");
                thanhCong(p, 200, "Hủy đơn thành công.", dichVu.huy(ma, maLong(d.get(0)), y.lyDo, actor));
                return;
            }
            if (d.size() == 2 && "bao-da-chuyen-khoan".equals(d.get(1)) && "POST".equals(q.getMethod())) {
                thanhCong(p, 200, "Đã ghi nhận thông báo chuyển khoản; đơn vẫn chờ Quản lý đối soát.",
                        new CocDatPhongService().baoDaChuyen(maLong(d.get(0)), ma));
                return;
            }
            phuongThuc(p);
        } catch (JsonParseException e) {
            thatBai(p, new IllegalArgumentException("Nội dung JSON không hợp lệ."));
        } catch (Exception e) {
            thatBai(p, e);
        }
    }

    private static class YeuCauHuy {
        private String lyDo;
    }
}
