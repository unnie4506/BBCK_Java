package vn.edu.quanlyhomestay.controller.khachhang;

import com.google.gson.JsonParseException;
import vn.edu.quanlyhomestay.model.NguoiDungDaXacThuc;
import vn.edu.quanlyhomestay.model.KhachHang;
import vn.edu.quanlyhomestay.service.LoiXacThuc;
import vn.edu.quanlyhomestay.service.XacThucService;
import vn.edu.quanlyhomestay.util.PhanHoiApi;
import vn.edu.quanlyhomestay.util.TienIchJson;
import vn.edu.quanlyhomestay.util.TienIchPhien;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class KhachHangXacThucServlet extends HttpServlet {
    private final XacThucService xacthucService = new XacThucService();

    @Override
    protected void doPost(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        if (!TienIchJson.yeuCauNoiDungJson(yeuCau, phanHoi)) {
            return;
        }

        String duongDan = yeuCau.getServletPath();
        if ("/api/cong-khai/xac-thuc/dang-ky".equals(duongDan)) {
            dangKy(yeuCau, phanHoi);
        } else if ("/api/cong-khai/xac-thuc/dang-nhap".equals(duongDan)) {
            dangNhap(yeuCau, phanHoi);
        } else if ("/api/khach-hang/xac-thuc/dang-xuat".equals(duongDan)) {
            dangXuat(yeuCau, phanHoi);
        } else {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_NOT_FOUND,
                    PhanHoiApi.thatBai("Không tìm thấy tài nguyên."));
        }
    }

    @Override
    protected void doGet(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        if (!"/api/khach-hang/xac-thuc/ho-so".equals(yeuCau.getServletPath())) {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_NOT_FOUND,
                    PhanHoiApi.thatBai("Không tìm thấy tài nguyên."));
            return;
        }

        Long maTaiKhoan = TienIchPhien.getMaNguoiDung(yeuCau);
        if (maTaiKhoan == null) {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_UNAUTHORIZED,
                    PhanHoiApi.thatBai("Vui lòng đăng nhập để tiếp tục."));
            return;
        }
        try {
            KhachHang khachHang = xacthucService.layKhachHang(maTaiKhoan);
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_OK,
                    PhanHoiApi.thanhCong("Lấy hồ sơ thành công.", duLieuKhachHang(khachHang)));
        } catch (LoiXacThuc ngoaiLe) {
            ghiLoi(phanHoi, ngoaiLe);
        }
    }

    @Override
    protected void doPut(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        if (!"/api/khach-hang/xac-thuc/ho-so".equals(yeuCau.getServletPath())) {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_NOT_FOUND,
                    PhanHoiApi.thatBai("Không tìm thấy tài nguyên."));
            return;
        }
        if (!TienIchJson.yeuCauNoiDungJson(yeuCau, phanHoi)) {
            return;
        }
        Long maTaiKhoan = TienIchPhien.getMaNguoiDung(yeuCau);
        if (maTaiKhoan == null) {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_UNAUTHORIZED,
                    PhanHoiApi.thatBai("Vui lòng đăng nhập để tiếp tục."));
            return;
        }
        try {
            YeuCauHoSo thanJson = TienIchJson.docThanJson(yeuCau, YeuCauHoSo.class);
            if (thanJson == null) {
                TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_BAD_REQUEST,
                        PhanHoiApi.thatBai("Nội dung JSON không hợp lệ."));
                return;
            }
            KhachHang khachHang = xacthucService.capNhatKhachHang(maTaiKhoan, thanJson.hoTen, thanJson.soDienThoai,
                    thanJson.thuDienTu);
            yeuCau.getSession(false).setAttribute(TienIchPhien.TEN_HIEN_THI, khachHang.getHoTen());
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_OK,
                    PhanHoiApi.thanhCong("Cập nhật hồ sơ thành công.", duLieuKhachHang(khachHang)));
        } catch (JsonParseException ngoaiLe) {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_BAD_REQUEST,
                    PhanHoiApi.thatBai("Nội dung JSON không hợp lệ."));
        } catch (LoiXacThuc ngoaiLe) {
            ghiLoi(phanHoi, ngoaiLe);
        }
    }

    private void dangKy(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        try {
            YeuCauDangKy thanJson = TienIchJson.docThanJson(yeuCau, YeuCauDangKy.class);
            if (thanJson == null) {
                TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_BAD_REQUEST,
                        PhanHoiApi.thatBai("Nội dung JSON không hợp lệ."));
                return;
            }
            long maTaiKhoan = xacthucService.dangKyKhachHang(thanJson.hoTen, thanJson.soDienThoai, thanJson.thuDienTu,
                    thanJson.matKhau);
            Map<String, Object> duLieu = new LinkedHashMap<>();
            duLieu.put("maTaiKhoanKhachHang", maTaiKhoan);
            duLieu.put("thuDienTu", thanJson.thuDienTu == null ? null : thanJson.thuDienTu.trim().toLowerCase());
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_OK,
                    PhanHoiApi.thanhCong("Đăng ký tài khoản thành công. Vui lòng đăng nhập.", duLieu));
        } catch (JsonParseException ngoaiLe) {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_BAD_REQUEST,
                    PhanHoiApi.thatBai("Nội dung JSON không hợp lệ."));
        } catch (LoiXacThuc ngoaiLe) {
            ghiLoi(phanHoi, ngoaiLe);
        }
    }

    private void dangNhap(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        try {
            YeuCauDangNhap thanJson = TienIchJson.docThanJson(yeuCau, YeuCauDangNhap.class);
            if (thanJson == null) {
                TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_BAD_REQUEST,
                        PhanHoiApi.thatBai("Nội dung JSON không hợp lệ."));
                return;
            }
            NguoiDungDaXacThuc nguoiDung = xacthucService.dangNhapKhachHang(thanJson.thuDienTu, thanJson.matKhau,
                    yeuCau.getRemoteAddr());
            TienIchPhien.taoPhienDaXacThuc(yeuCau, nguoiDung.getMaNguoiDung(), nguoiDung.getVaiTro(),
                    nguoiDung.getTenHienThi());
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_OK,
                    PhanHoiApi.thanhCong("Đăng nhập thành công.", duLieuXacThuc(nguoiDung)));
        } catch (JsonParseException ngoaiLe) {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_BAD_REQUEST,
                    PhanHoiApi.thatBai("Nội dung JSON không hợp lệ."));
        } catch (LoiXacThuc ngoaiLe) {
            ghiLoi(phanHoi, ngoaiLe);
        }
    }

    private void dangXuat(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        Long maTaiKhoan = TienIchPhien.getMaNguoiDung(yeuCau);
        String vaiTro = TienIchPhien.getVaiTro(yeuCau);
        try {
            xacthucService.ghiNhanDangXuat(vaiTro, maTaiKhoan == null ? 0 : maTaiKhoan);
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_OK,
                    PhanHoiApi.thanhCong("Đăng xuất thành công.", null));
        } catch (SQLException ngoaiLe) {
            getServletContext().log("Không ghi được audit log khi khách hàng đăng xuất.", ngoaiLe);
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    PhanHoiApi.thatBai("Không thể ghi nhận đăng xuất lúc này."));
        } finally {
            TienIchPhien.huyPhien(yeuCau);
        }
    }

    private Map<String, Object> duLieuXacThuc(NguoiDungDaXacThuc nguoiDung) {
        Map<String, Object> duLieu = new LinkedHashMap<>();
        duLieu.put("ma", nguoiDung.getMaNguoiDung());
        duLieu.put("vaiTro", nguoiDung.getVaiTro());
        duLieu.put("tenHienThi", nguoiDung.getTenHienThi());
        duLieu.put("thuDienTu", nguoiDung.getThuDienTu());
        return duLieu;
    }

    private Map<String, Object> duLieuKhachHang(KhachHang khachHang) {
        Map<String, Object> duLieu = new LinkedHashMap<>();
        duLieu.put("maTaiKhoanKhachHang", khachHang.getMaTaiKhoanKhachHang());
        duLieu.put("maKhachHang", khachHang.getMaKhachHang());
        duLieu.put("hoTen", khachHang.getHoTen());
        duLieu.put("soDienThoai", khachHang.getSoDienThoai());
        duLieu.put("thuDienTu", khachHang.getThuDienTu());
        return duLieu;
    }

    private void ghiLoi(HttpServletResponse phanHoi, LoiXacThuc ngoaiLe) throws IOException {
        if (ngoaiLe.getMaTrangThai() == 423) {
            long thoiGianCho = Math.max(1, ngoaiLe.getThoiGianChoGiay());
            phanHoi.setHeader("Retry-After", Long.toString(thoiGianCho));
            Map<String, Object> duLieu = new LinkedHashMap<>();
            duLieu.put("thoiGianChoGiay", thoiGianCho);
            TienIchJson.ghiPhanHoiJson(phanHoi, ngoaiLe.getMaTrangThai(),
                    new PhanHoiApi(false, ngoaiLe.getMessage(), duLieu));
            return;
        }
        TienIchJson.ghiPhanHoiJson(phanHoi, ngoaiLe.getMaTrangThai(), PhanHoiApi.thatBai(ngoaiLe.getMessage()));
    }

    private static class YeuCauDangKy {
        private String hoTen;
        private String soDienThoai;
        private String thuDienTu;
        private String matKhau;
    }

    private static class YeuCauDangNhap {
        private String thuDienTu;
        private String matKhau;
    }

    private static class YeuCauHoSo {
        private String hoTen;
        private String soDienThoai;
        private String thuDienTu;
    }
}
