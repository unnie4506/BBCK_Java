package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.HoanCocDAO;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class HoanCocService {
    private final HoanCocDAO dao = new HoanCocDAO();
    private final NhatKyThaoTacService nhatKy = new NhatKyThaoTacService();

    public List<Map<String, Object>> danhSach(String trangThai, int kichThuoc, int trang, NguoiThaoTac actor)
            throws SQLException {
        kiemTraQuanLy(actor);
        if (trangThai != null && !Set.of("CHO_XU_LY", "DA_XAC_NHAN").contains(trangThai))
            throw new NghiepVuException(400, "TRANG_THAI_KHONG_HOP_LE", "Trạng thái hoàn cọc không hợp lệ.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            return dao.danhSachYeuCauHoanCoc(c, trangThai, kichThuoc, (trang - 1) * kichThuoc);
        }
    }

    public Map<String, Object> xacNhan(long maThanhToan, BigDecimal soTienThucHoan, String phuongThuc,
            NguoiThaoTac actor) throws SQLException {
        kiemTraQuanLy(actor);
        if (soTienThucHoan == null || soTienThucHoan.signum() < 0)
            throw new NghiepVuException(422, "SO_TIEN_HOAN_KHONG_HOP_LE",
                    "Số tiền thực hoàn phải bằng hoặc lớn hơn 0.");
        String pt = phuongThuc == null || phuongThuc.isBlank() ? "CHUYEN_KHOAN" : phuongThuc;
        if (!Set.of("TIEN_MAT", "CHUYEN_KHOAN", "VI_DIEN_TU").contains(pt))
            throw new NghiepVuException(422, "PHUONG_THUC_KHONG_HOP_LE", "Phương thức hoàn cọc không hợp lệ.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> ds = dao.yeuCauHoanCoc(c, maThanhToan);
                if (ds.isEmpty())
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_YEU_CAU_HOAN",
                            "Không tìm thấy yêu cầu hoàn cọc đang chờ xử lý.");
                Map<String, Object> r = ds.get(0);
                BigDecimal deXuat = (BigDecimal) r.get("soTien");
                if (soTienThucHoan.compareTo(deXuat) > 0)
                    throw new NghiepVuException(422, "VUOT_SO_TIEN_DE_XUAT",
                            "Số tiền thực hoàn không được vượt số tiền đã đề xuất.");
                long maDon = ((Number) r.get("maDonDatPhong")).longValue();
                if (dao.xacNhanHoanCoc(c, maThanhToan, soTienThucHoan, pt, actor.getMa()) != 1)
                    throw new NghiepVuException(409, "HOAN_COC_VUA_THAY_DOI", "Yêu cầu hoàn cọc vừa được xử lý.");
                nhatKy.ghi(c, actor, maDon, "QUAN_LY_XAC_NHAN_HOAN_COC",
                        "Quản lý xác nhận hoàn " + soTienThucHoan.toPlainString() + " đồng bằng " + pt + ".");
                c.commit();
                Map<String, Object> kq = new LinkedHashMap<>();
                kq.put("maThanhToan", maThanhToan);
                kq.put("soTienThucHoan", soTienThucHoan);
                kq.put("phuongThucHoan", pt);
                kq.put("trangThaiHoanCoc", "DA_XAC_NHAN");
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void kiemTraQuanLy(NguoiThaoTac a) {
        if (a == null || a.getMa() <= 0 || !"QUAN_LY".equals(a.getLoai()))
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Chỉ Quản lý mới được xử lý hoàn cọc.");
    }
}
