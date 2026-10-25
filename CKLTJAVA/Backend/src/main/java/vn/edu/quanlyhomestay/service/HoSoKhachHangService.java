package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.CongKhaiDAO;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.util.*;
import java.sql.*;
import java.util.*;

public class HoSoKhachHangService {
    private final CongKhaiDAO dao = new CongKhaiDAO();
    private final NhatKyThaoTacService nhatKy = new NhatKyThaoTacService();

    public Map<String, Object> xem(long maTaiKhoan) throws SQLException {
        return lay(maTaiKhoan);
    }

    public Map<String, Object> capNhat(long maTaiKhoan, String hoTen, String soDienThoai, String soGiayTo)
            throws SQLException {
        String ten = TienIchKiemTraDuLieu.lamSach(hoTen),
                dienThoai = TienIchKiemTraDuLieu.chuanHoaSoDienThoaiVietNam(soDienThoai),
                giayTo = TienIchKiemTraDuLieu.lamSach(soGiayTo);
        // BR-01 (Khách): họ tên và số điện thoại bắt buộc; số điện thoại dùng cùng bộ kiểm tra với Giai đoạn 3.
        if (ten == null || ten.isBlank() || ten.length() > 120)
            throw new NghiepVuException(422, "HO_TEN_KHONG_HOP_LE", "Họ tên là bắt buộc và tối đa 120 ký tự.");
        if (dienThoai == null)
            throw new NghiepVuException(422, "SO_DIEN_THOAI_KHONG_HOP_LE",
                    "Số điện thoại Việt Nam không đúng định dạng.");
        if (giayTo != null && giayTo.length() > 30)
            throw new NghiepVuException(422, "SO_GIAY_TO_KHONG_HOP_LE", "Số giấy tờ tối đa 30 ký tự.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                Map<String, Object> truoc = lay(c, maTaiKhoan);
                long maKhach = ((Number) truoc.get("maKhachHang")).longValue();
                if (dao.capNhatHoSo(c, maKhach, ten, dienThoai, giayTo) != 1)
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_HO_SO", "Không tìm thấy hồ sơ khách hàng.");
                // BR-10 (Khách): cập nhật hồ sơ và nhật ký cùng transaction. Không kiểm tra điện thoại trùng để tránh lộ hồ sơ khác.
                nhatKy.ghi(c, new NguoiThaoTac("KHACH_HANG", maTaiKhoan), "KHACH_HANG_CAP_NHAT_HO_SO",
                        "Khách cập nhật họ tên, số điện thoại hoặc số giấy tờ.");
                c.commit();
                return lay(c, maTaiKhoan);
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private Map<String, Object> lay(long maTaiKhoan) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            return lay(c, maTaiKhoan);
        }
    }

    private Map<String, Object> lay(Connection c, long maTaiKhoan) throws SQLException {
        List<Map<String, Object>> ds = dao.hoSoTheoTaiKhoan(c, maTaiKhoan);
        if (ds.isEmpty())
            throw new NghiepVuException(404, "KHONG_TIM_THAY_HO_SO", "Không tìm thấy hồ sơ khách hàng.");
        return ds.get(0);
    }
}
