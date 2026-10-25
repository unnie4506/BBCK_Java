package vn.edu.quanlyhomestay.controller.quanly;

import com.google.gson.JsonParseException;
import vn.edu.quanlyhomestay.model.NguoiDungDaXacThuc;
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

public class QuanLyXacThucServlet extends HttpServlet {
    private final XacThucService xacthucService = new XacThucService();

    @Override
    protected void doPost(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        String duongDan = yeuCau.getServletPath();
        if (!TienIchJson.yeuCauNoiDungJson(yeuCau, phanHoi)) {
            return;
        }
        if ("/api/quan-ly/xac-thuc/dang-nhap".equals(duongDan)) {
            dangNhap(yeuCau, phanHoi);
        } else if ("/api/quan-ly/xac-thuc/dang-xuat".equals(duongDan)) {
            dangXuat(yeuCau, phanHoi);
        } else {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_NOT_FOUND,
                    PhanHoiApi.thatBai("Không tìm thấy tài nguyên."));
        }
    }

    @Override
    protected void doGet(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        if (!"/api/quan-ly/xac-thuc/ho-so".equals(yeuCau.getServletPath())) {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_NOT_FOUND,
                    PhanHoiApi.thatBai("Không tìm thấy tài nguyên."));
            return;
        }
        Map<String, Object> duLieu = new LinkedHashMap<>();
        duLieu.put("ma", TienIchPhien.getMaNguoiDung(yeuCau));
        duLieu.put("vaiTro", TienIchPhien.getVaiTro(yeuCau));
        duLieu.put("tenHienThi", TienIchPhien.getTenHienThi(yeuCau));
        TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_OK,
                PhanHoiApi.thanhCong("Phiên quản lý hợp lệ.", duLieu));
    }

    private void dangNhap(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        try {
            YeuCauDangNhap thanJson = TienIchJson.docThanJson(yeuCau, YeuCauDangNhap.class);
            if (thanJson == null) {
                TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_BAD_REQUEST,
                        PhanHoiApi.thatBai("Nội dung JSON không hợp lệ."));
                return;
            }
            NguoiDungDaXacThuc nguoiDung = xacthucService.dangNhapQuanLy(thanJson.tenDangNhap, thanJson.matKhau,
                    yeuCau.getRemoteAddr());
            TienIchPhien.taoPhienDaXacThuc(yeuCau, nguoiDung.getMaNguoiDung(), nguoiDung.getVaiTro(),
                    nguoiDung.getTenHienThi());
            Map<String, Object> duLieu = new LinkedHashMap<>();
            duLieu.put("ma", nguoiDung.getMaNguoiDung());
            duLieu.put("vaiTro", nguoiDung.getVaiTro());
            duLieu.put("tenHienThi", nguoiDung.getTenHienThi());
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_OK,
                    PhanHoiApi.thanhCong("Đăng nhập thành công.", duLieu));
        } catch (JsonParseException ngoaiLe) {
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_BAD_REQUEST,
                    PhanHoiApi.thatBai("Nội dung JSON không hợp lệ."));
        } catch (LoiXacThuc ngoaiLe) {
            if (ngoaiLe.getMaTrangThai() == 423) {
                Map<String, Object> duLieuCho = new LinkedHashMap<>();
                duLieuCho.put("thoiGianChoGiay", ngoaiLe.getThoiGianChoGiay());
                phanHoi.setHeader("Retry-After", String.valueOf(ngoaiLe.getThoiGianChoGiay()));
                TienIchJson.ghiPhanHoiJson(phanHoi, 423, new PhanHoiApi(false, ngoaiLe.getMessage(), duLieuCho));
            } else {
                TienIchJson.ghiPhanHoiJson(phanHoi, ngoaiLe.getMaTrangThai(), PhanHoiApi.thatBai(ngoaiLe.getMessage()));
            }
        }
    }

    private void dangXuat(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        Long maQuanLy = TienIchPhien.getMaNguoiDung(yeuCau);
        try {
            xacthucService.ghiNhanDangXuat(TienIchPhien.VAI_TRO_QUAN_LY, maQuanLy == null ? 0 : maQuanLy);
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_OK,
                    PhanHoiApi.thanhCong("Đăng xuất thành công.", null));
        } catch (SQLException ngoaiLe) {
            getServletContext().log("Không ghi được audit log khi quản lý đăng xuất.", ngoaiLe);
            TienIchJson.ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    PhanHoiApi.thatBai("Không thể ghi nhận đăng xuất lúc này."));
        } finally {
            TienIchPhien.huyPhien(yeuCau);
        }
    }

    private static class YeuCauDangNhap {
        private String tenDangNhap;
        private String matKhau;
    }
}
