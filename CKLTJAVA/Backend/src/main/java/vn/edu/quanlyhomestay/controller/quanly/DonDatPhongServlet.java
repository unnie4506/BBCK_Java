package vn.edu.quanlyhomestay.controller.quanly;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.quanlyhomestay.model.DonDatPhongYeuCau;
import vn.edu.quanlyhomestay.service.DonDatPhongService;
import vn.edu.quanlyhomestay.util.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@WebServlet("/api/quan-ly/don-dat-phong/*")
public class DonDatPhongServlet extends ServletNghiepVu {
    private final DonDatPhongService dichVu = new DonDatPhongService();
    private final vn.edu.quanlyhomestay.service.HoaDonService hoaDonDichVu = new vn.edu.quanlyhomestay.service.HoaDonService();

    @Override
    protected void service(HttpServletRequest q, HttpServletResponse p) throws IOException {
        List<String> d = DuongDanUtil.tach(q.getPathInfo());
        try {
            if ("GET".equals(q.getMethod())) {
                if (d.equals(List.of("cho-xac-nhan-coc"))) {
                    thanhCong(p, 200, "Lấy danh sách đơn chờ xác nhận cọc thành công.",
                            new vn.edu.quanlyhomestay.service.CocDatPhongService().danhSach(nguoi(q)));
                    return;
                }
                if (d.isEmpty()) {
                    int trang = PhanTrangUtil.docSoTrang(q.getParameter("trang")),
                            kichThuoc = PhanTrangUtil.docKichThuoc(q.getParameter("kichThuoc"));
                    thanhCong(p, 200, "Tìm đơn đặt phòng thành công.",
                            dichVu.timDon(q.getParameter("tuKhoa"), q.getParameter("trangThai"),
                                    ngay(q.getParameter("tuNgay")), ngay(q.getParameter("denNgay")),
                                    q.getParameter("nguonDat"), trang, kichThuoc, nguoi(q)));
                    return;
                }
                if (d.size() == 1 && "lich".equals(d.get(0))) {
                    thanhCong(p, 200, "Lấy lịch đặt phòng thành công.",
                            dichVu.lich(ngay(q.getParameter("tuNgay")), ngay(q.getParameter("denNgay")), nguoi(q)));
                    return;
                }
                if (d.size() == 2 && "hoa-don".equals(d.get(1))) {
                    thanhCong(p, 200, "Lấy hóa đơn của đơn thành công.",
                            hoaDonDichVu.layTheoDon(maLong(d.get(0)), nguoi(q)));
                    return;
                }
                if (d.size() == 2 && "xem-truoc-huy".equals(d.get(1))) {
                    var huyDon = new vn.edu.quanlyhomestay.service.HuyDonService();
                    String maChiTiet = q.getParameter("maChiTiet");
                    Object duKien = maChiTiet == null || maChiTiet.isBlank()
                            ? huyDon.xemTruocHuyQuanLy(maLong(d.get(0)), nguoi(q))
                            : huyDon.xemTruocHuyChiTietQuanLy(maLong(d.get(0)), maLong(maChiTiet), nguoi(q));
                    thanhCong(p, 200, "Xem trước tiền hoàn cọc.", duKien);
                    return;
                }
                if (d.size() == 1) {
                    thanhCong(p, 200, "Lấy chi tiết đơn đặt phòng thành công.",
                            dichVu.layDon(maLong(d.get(0)), nguoi(q)));
                    return;
                }
                phuongThuc(p);
                return;
            }
            if ("POST".equals(q.getMethod()) && d.size() == 2 && "xac-nhan-coc".equals(d.get(1))) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p)) return;
                YeuCauXacNhanCoc y = doc(q, YeuCauXacNhanCoc.class);
                thanhCong(p, 200, "Đã ghi nhận đối soát cọc.",
                        new vn.edu.quanlyhomestay.service.CocDatPhongService().xacNhan(maLong(d.get(0)),
                                y.soTienThucNhan, y.phuongThuc, y.maGiaoDich, nguoi(q)));
                return;
            }
            if ("POST".equals(q.getMethod()) && d.size() == 2 && "tu-choi-coc".equals(d.get(1))) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p)) return;
                YeuCauTuChoiCoc y = doc(q, YeuCauTuChoiCoc.class);
                thanhCong(p, 200, "Đã từ chối cọc và hủy đơn.",
                        new vn.edu.quanlyhomestay.service.CocDatPhongService().tuChoi(maLong(d.get(0)), y.lyDo, nguoi(q)));
                return;
            }
            if ("POST".equals(q.getMethod()) && d.isEmpty()) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                Map<String, Object> kq = dichVu.taoDon(doc(q, DonDatPhongYeuCau.class), nguoi(q));
                thanhCong(p, 201, "Tạo đơn đặt phòng thành công.", kq);
                return;
            }
            if ("POST".equals(q.getMethod()) && d.size() == 2 && "huy".equals(d.get(1))) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                YeuCauHuyDon y = doc(q, YeuCauHuyDon.class);
                Map<String, Object> kq = new vn.edu.quanlyhomestay.service.HuyDonService()
                        .huyDonQuanLy(maLong(d.get(0)), y.lyDo, y.soTienHoan, y.phuongThucHoan, nguoi(q));
                thanhCong(p, 200, "Đã hủy đơn và ghi nhận hoàn cọc.", kq);
                return;
            }
            if ("POST".equals(q.getMethod()) && d.size() == 4 && "chi-tiet".equals(d.get(1))
                    && "huy".equals(d.get(3))) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                YeuCauHuyDon y = doc(q, YeuCauHuyDon.class);
                Map<String, Object> kq = new vn.edu.quanlyhomestay.service.HuyDonService()
                        .huyChiTietQuanLy(maLong(d.get(0)), maLong(d.get(2)), y.lyDo, nguoi(q));
                thanhCong(p, 200, "Đã hủy phòng và ghi nhận hoàn cọc.", kq);
                return;
            }
            if ("PUT".equals(q.getMethod()) && d.size() == 1) {
                if (!TienIchJson.yeuCauNoiDungJson(q, p))
                    return;
                Map<String, Object> kq = dichVu.suaDon(maLong(d.get(0)), doc(q, DonDatPhongYeuCau.class), nguoi(q));
                thanhCong(p, 200, "Cập nhật đơn đặt phòng thành công.", kq);
                return;
            }
            phuongThuc(p);
        } catch (Exception e) {
            thatBai(p, e);
        }
    }

    private static class YeuCauHuyDon {
        private String lyDo;
        private BigDecimal soTienHoan;
        private String phuongThucHoan;
    }

    private static class YeuCauXacNhanCoc {
        private BigDecimal soTienThucNhan;
        private String phuongThuc;
        private String maGiaoDich;
    }

    private static class YeuCauTuChoiCoc { private String lyDo; }
}
