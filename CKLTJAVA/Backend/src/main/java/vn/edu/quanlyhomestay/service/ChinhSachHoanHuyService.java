package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.ChinhSachHoanHuyDAO;
import vn.edu.quanlyhomestay.model.BacHoanHuy;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import java.sql.Connection;
import java.util.*;

public class ChinhSachHoanHuyService {
    private final ChinhSachHoanHuyDAO dao = new ChinhSachHoanHuyDAO();

    public List<Map<String, Object>> xem(NguoiThaoTac nguoi) throws Exception {
        kiemTraQuanLy(nguoi);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            return dao.danhSachDangDung(c);
        }
    }

    public void capNhat(List<BacHoanHuy> ds, NguoiThaoTac nguoi) throws Exception {
        kiemTraQuanLy(nguoi);
        if (ds == null || ds.isEmpty())
            throw loi("CHINH_SACH_RONG", "Phải có ít nhất một bậc hoàn hủy.");
        List<BacHoanHuy> sapXep = new ArrayList<>(ds);
        sapXep.sort(Comparator.comparing(BacHoanHuy::getSoNgayToiThieu, Comparator.nullsFirst(Integer::compareTo)));
        Integer ketThucTruoc = null;
        for (int i = 0; i < sapXep.size(); i++) {
            BacHoanHuy b = sapXep.get(i);
            if (b.getSoNgayToiThieu() == null || b.getSoNgayToiThieu() < 0 || b.getTyLeHoan() == null
                    || b.getTyLeHoan().signum() < 0 || b.getTyLeHoan().compareTo(new java.math.BigDecimal("100")) > 0)
                throw loi("BAC_CHINH_SACH_KHONG_HOP_LE", "Ngưỡng ngày phải từ 0, tỷ lệ hoàn từ 0 đến 100.");
            if (b.getSoNgayToiDa() != null && b.getSoNgayToiDa() < b.getSoNgayToiThieu())
                throw loi("BAC_CHINH_SACH_KHONG_HOP_LE", "Số ngày tối đa phải lớn hơn hoặc bằng số ngày tối thiểu.");
            if (ketThucTruoc != null && b.getSoNgayToiThieu() <= ketThucTruoc)
                throw loi("BAC_CHINH_SACH_CHONG_LAN", "Các bậc hoàn hủy bị chồng lấn.");
            ketThucTruoc = b.getSoNgayToiDa();
            if (ketThucTruoc == null && i < sapXep.size() - 1)
                throw loi("BAC_CHINH_SACH_CHONG_LAN", "Bậc không giới hạn phải là bậc cuối cùng.");
        }
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                dao.voHieuHoaTatCa(c);
                for (BacHoanHuy b : sapXep) {
                    String ten = "Hoàn " + b.getTyLeHoan().stripTrailingZeros().toPlainString() + "% khi hủy trước "
                            + b.getSoNgayToiThieu() + " ngày";
                    dao.them(c, ten, b, nguoi.getMa());
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void kiemTraQuanLy(NguoiThaoTac n) {
        if (n == null || n.getMa() <= 0 || !"QUAN_LY".equals(n.getLoai()))
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Chỉ quản lý mới được cấu hình chính sách.");
    }

    private NghiepVuException loi(String ma, String tb) {
        return new NghiepVuException(422, ma, tb);
    }
}
