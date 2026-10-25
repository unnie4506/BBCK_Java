package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.*;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import vn.edu.quanlyhomestay.util.CauHinhDatCoc;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/** Xử lý đối soát chuyển khoản cọc; chỉ Quản lý ghi nhận tiền thực nhận. */
public class CocDatPhongService {
    private final DonDatPhongDAO donDAO = new DonDatPhongDAO();
    private final ChiTietDatPhongDAO chiTietDAO = new ChiTietDatPhongDAO();
    private final PhongDatDAO phongDAO = new PhongDatDAO();
    private final HoaDonDAO thanhToanDAO = new HoaDonDAO();
    private final CongKhaiDAO hoanCocDAO = new CongKhaiDAO();
    private final NhatKyThaoTacService nhatKy = new NhatKyThaoTacService();

    public Map<String, Object> danhSach(NguoiThaoTac actor) throws SQLException {
        kiemTraQuanLy(actor);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = donDAO.danhSachDonChoCoc(c);
            Map<String, Object> kq = new LinkedHashMap<>();
            kq.put("danhSach", ds);
            kq.put("soDangCho", ds.size());
            return kq;
        }
    }

    public Map<String, Object> baoDaChuyen(long maDon, long maTaiKhoan) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> ds = donDAO.donKhoa(c, maDon);
                if (ds.isEmpty() || !Objects.equals(((Number) ds.get(0).get("maTaiKhoanKhachHang")).longValue(), maTaiKhoan))
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
                if (donDAO.baoDaChuyenKhoan(c, maDon, maTaiKhoan) != 1) {
                    if (ds.get(0).get("khachBaoDaChuyenLuc") != null) {
                        c.commit();
                        return Map.of("daBaoDaChuyenKhoan", true, "trangThai", ds.get(0).get("trangThai"));
                    }
                    throw new NghiepVuException(409, "DON_KHONG_CHO_COC",
                            "Chỉ đơn đang chờ cọc và còn hạn mới nhận thông báo chuyển khoản.");
                }
                nhatKy.ghi(c, new NguoiThaoTac("KHACH_HANG", maTaiKhoan), maDon,
                        "KHACH_BAO_DA_CHUYEN_COC", "Khách thông báo đã chuyển khoản; chờ Quản lý đối soát.");
                c.commit();
                return Map.of("daBaoDaChuyenKhoan", true, "trangThai", "CHO_COC");
            } catch (Exception e) { c.rollback(); throw e; }
        }
    }

    public Map<String, Object> xacNhan(long maDon, BigDecimal soTien, String phuongThuc, String maGiaoDich,
            NguoiThaoTac actor) throws SQLException {
        kiemTraQuanLy(actor);
        String pt = phuongThuc == null || phuongThuc.isBlank() ? "CHUYEN_KHOAN" : phuongThuc;
        return ghiNhan(maDon, soTien, pt, maGiaoDich, actor, false);
    }

    public Map<String, Object> tuChoi(long maDon, String lyDo, NguoiThaoTac actor) throws SQLException {
        kiemTraQuanLy(actor);
        if (lyDo == null || lyDo.isBlank())
            throw new NghiepVuException(422, "THIEU_LY_DO_TU_CHOI", "Vui lòng nhập lý do từ chối.");
        if (lyDo.length() > 1000)
            throw new NghiepVuException(422, "LY_DO_QUA_DAI", "Lý do tối đa 1000 ký tự.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> don = donDAO.donKhoa(c, maDon);
                if (don.isEmpty() || !"TRUC_TUYEN".equals(don.get(0).get("nguonTao")))
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_DON_CHO_COC", "Không tìm thấy đơn trực tuyến.");
                String status = (String) don.get(0).get("trangThai");
                if (!Set.of("CHO_COC", "HET_HAN").contains(status))
                    throw new NghiepVuException(409, "DON_KHONG_CHO_COC", "Đơn này không còn chờ xác nhận cọc.");
                List<Map<String, Object>> details = chiTietDAO.khoaTheoDon(c, maDon);
                khoaPhongCuaDon(c, details);
                BigDecimal daNhan = tien(don.get(0).get("tienCocDaNhan"));
                donDAO.huyChiTietChoCoc(c, maDon, "Từ chối xác nhận cọc: " + lyDo);
                donDAO.capNhatTrangThaiVaCoc(c, maDon, "DA_HUY", daNhan);
                if (daNhan.signum() > 0)
                    hoanCocDAO.themYeuCauHoanCoc(c, maDon, daNhan, "Hoàn toàn bộ tiền cọc đã ghi nhận sau khi từ chối: " + lyDo);
                nhatKy.ghi(c, actor, maDon, "QUAN_LY_TU_CHOI_COC", "Từ chối xác nhận cọc. Lý do: " + lyDo);
                c.commit();
                Map<String, Object> kq = new LinkedHashMap<>();
                kq.put("maDonDatPhong", maDon); kq.put("trangThai", "DA_HUY");
                kq.put("soTienHoanChoXuLy", daNhan); kq.put("lyDo", lyDo); return kq;
            } catch (Exception e) { c.rollback(); throw e; }
        }
    }

    /** Được gọi mỗi phút; luôn xử lý mô phỏng trước hạn, rồi mới hết hạn thật. */
    public void chayTienTrinhNen() throws SQLException {
        if (CauHinhDatCoc.demoBat() && CauHinhDatCoc.demoSauGiay() > 0) {
            try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
                for (Map<String, Object> d : donDAO.donChoCocDemo(c, CauHinhDatCoc.demoSauGiay())) {
                    BigDecimal conThieu = tien(d.get("tienCocYeuCau")).subtract(tien(d.get("tienCocDaNhan")));
                    if (conThieu.signum() > 0)
                        ghiNhan(((Number) d.get("maDonDatPhong")).longValue(), conThieu,
                                "CHUYEN_KHOAN", "DEMO-" + d.get("maDonDatPhong"), null, true);
                }
            }
        }
        hetHanDonChoCoc();
    }

    private Map<String, Object> ghiNhan(long maDon, BigDecimal soTien, String pt, String maGiaoDich,
            NguoiThaoTac actor, boolean demo) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> rows = donDAO.donKhoa(c, maDon);
                if (rows.isEmpty() || !"TRUC_TUYEN".equals(rows.get(0).get("nguonTao")))
                    throw new NghiepVuException(404, "KHONG_TIM_THAY_DON_CHO_COC", "Không tìm thấy đơn trực tuyến.");
                Map<String, Object> don = rows.get(0);
                String trangThai = (String) don.get("trangThai");
                if ("DA_DAT".equals(trangThai)) {
                    c.commit();
                    return ketQua(maDon, trangThai, tien(don.get("tienCocDaNhan")), tien(don.get("tienCocYeuCau")), true, BigDecimal.ZERO);
                }
                if (!Set.of("CHO_COC", "HET_HAN").contains(trangThai))
                    throw new NghiepVuException(409, "DON_KHONG_CHO_COC", "Đơn này không còn chờ xác nhận cọc.");
                kiemTraSoTien(soTien);
                if (!Set.of("TIEN_MAT", "CHUYEN_KHOAN", "VI_DIEN_TU").contains(pt))
                    throw new NghiepVuException(422, "PHUONG_THUC_KHONG_HOP_LE", "Phương thức nhận cọc không hợp lệ.");
                if (maGiaoDich != null && maGiaoDich.length() > 100)
                    throw new NghiepVuException(422, "MA_GIAO_DICH_QUA_DAI", "Mã giao dịch tối đa 100 ký tự.");
                boolean conHan = Boolean.TRUE.equals(don.get("conHan"));
                boolean daHetHan = "HET_HAN".equals(trangThai) || !conHan;
                if (daHetHan && "CHO_COC".equals(trangThai)) donDAO.hetHanDon(c, maDon);
                List<Map<String, Object>> details = chiTietDAO.khoaTheoDon(c, maDon);
                if (details.isEmpty()) throw new NghiepVuException(409, "DON_THIEU_PHONG", "Đơn không có phòng để xác nhận.");
                khoaPhongCuaDon(c, details);
                List<Map<String, Object>> biTrung = new ArrayList<>();
                if (daHetHan) {
                    for (Map<String, Object> d : details) {
                        if (!"DA_HUY".equals(d.get("trangThai"))) {
                            long maPhong = ((Number) d.get("maPhong")).longValue();
                            if (!chiTietDAO.trungLichNgoaiDon(c, maPhong,
                                    java.time.LocalDate.parse(String.valueOf(d.get("ngayNhan"))),
                                    java.time.LocalDate.parse(String.valueOf(d.get("ngayTra"))), maDon).isEmpty()) {
                                Map<String, Object> conflict = new LinkedHashMap<>();
                                conflict.put("maPhong", maPhong); conflict.put("soPhong", d.get("soPhong"));
                                biTrung.add(conflict);
                            }
                        }
                    }
                }
                thanhToanDAO.themTienCoc(c, maDon, pt, soTien, actor == null ? null : actor.getMa(), maGiaoDich,
                        demo ? "Tự xác nhận cọc mô phỏng." : "Quản lý đối soát sao kê và xác nhận tiền thực nhận.");
                BigDecimal daNhanMoi = tien(don.get("tienCocDaNhan")).add(soTien);
                BigDecimal yeuCau = tien(don.get("tienCocYeuCau"));
                Map<String, Object> ketQua;
                if (!biTrung.isEmpty()) {
                    String lyDo = "Hết hạn giữ chỗ; phòng đã được đặt trong thời gian chờ xác nhận.";
                    donDAO.huyChiTietChoCoc(c, maDon, lyDo);
                    donDAO.capNhatTrangThaiVaCoc(c, maDon, "DA_HUY", daNhanMoi);
                    if (daNhanMoi.signum() > 0)
                        hoanCocDAO.themYeuCauHoanCoc(c, maDon, daNhanMoi,
                                "Hoàn toàn bộ tiền cọc nhận sau khi đơn hết hạn và phòng đã có người đặt.");
                    ghiNhatKy(c, actor, maDon, demo, "DON_HET_HAN_PHONG_DA_DAT",
                            "Đã ghi nhận " + soTien.toPlainString() + " đồng; hủy đơn và tạo hoàn toàn bộ tiền nhận.");
                    ketQua = ketQua(maDon, "DA_HUY", daNhanMoi, yeuCau, false, daNhanMoi);
                    ketQua.put("danhSachPhongBiTrung", biTrung);
                } else {
                    String ttMoi = daNhanMoi.compareTo(yeuCau) >= 0 ? "DA_DAT" : (daHetHan ? "HET_HAN" : "CHO_COC");
                    donDAO.capNhatTrangThaiVaCoc(c, maDon, ttMoi, daNhanMoi);
                    ghiNhatKy(c, actor, maDon, demo, ttMoi.equals("DA_DAT") ? "QUAN_LY_XAC_NHAN_COC" : "QUAN_LY_NHAN_COC_MOT_PHAN",
                            "Ghi nhận thêm " + soTien.toPlainString() + " đồng; tổng đã nhận " + daNhanMoi.toPlainString() + "/" + yeuCau.toPlainString() + " đồng.");
                    ketQua = ketQua(maDon, ttMoi, daNhanMoi, yeuCau, false, BigDecimal.ZERO);
                }
                c.commit();
                return ketQua;
            } catch (Exception e) { c.rollback(); throw e; }
        }
    }

    private void hetHanDonChoCoc() throws SQLException {
        List<Map<String, Object>> ds;
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) { ds = donDAO.donChoCocHetHan(c); }
        for (Map<String, Object> r : ds) {
            long id = ((Number) r.get("maDonDatPhong")).longValue();
            try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
                c.setAutoCommit(false);
                try {
                    if (!donDAO.donKhoa(c, id).isEmpty() && donDAO.hetHanDon(c, id) == 1) {
                        nhatKy.ghiHeThong(c, id, "DON_CHO_COC_HET_HAN", "Hết hạn giữ chỗ; lịch phòng được nhả tự động.");
                    }
                    c.commit();
                } catch (Exception e) { c.rollback(); throw e; }
            }
        }
    }

    private void khoaPhongCuaDon(Connection c, List<Map<String, Object>> details) throws SQLException {
        TreeSet<Long> ids = new TreeSet<>();
        for (Map<String, Object> d : details) if (!"DA_HUY".equals(d.get("trangThai"))) ids.add(((Number) d.get("maPhong")).longValue());
        for (long id : ids) if (phongDAO.layDeKhoa(c, id).isEmpty())
            throw new NghiepVuException(409, "PHONG_KHONG_TON_TAI", "Một phòng trong đơn không còn tồn tại.");
    }

    private void ghiNhatKy(Connection c, NguoiThaoTac actor, long maDon, boolean demo, String hanhDong, String moTa)
            throws SQLException {
        if (demo) nhatKy.ghiHeThong(c, maDon, "DEMO_XAC_NHAN_COC", moTa);
        else nhatKy.ghi(c, actor, maDon, hanhDong, moTa);
    }

    private Map<String, Object> ketQua(long maDon, String status, BigDecimal received, BigDecimal required,
            boolean daDatTruoc, BigDecimal hoan) {
        Map<String, Object> k = new LinkedHashMap<>();
        k.put("maDonDatPhong", maDon); k.put("trangThai", status); k.put("tienCocDaNhan", received);
        k.put("tienCocYeuCau", required); k.put("daDatTruocDo", daDatTruoc); k.put("soTienHoanChoXuLy", hoan);
        return k;
    }
    private BigDecimal tien(Object x) { return x instanceof BigDecimal b ? b : new BigDecimal(String.valueOf(x == null ? "0" : x)); }
    private void kiemTraSoTien(BigDecimal n) {
        if (n == null || n.signum() <= 0 || n.stripTrailingZeros().scale() > 0)
            throw new NghiepVuException(422, "SO_TIEN_COC_KHONG_HOP_LE", "Số tiền thực nhận phải là số nguyên dương.");
    }
    private void kiemTraQuanLy(NguoiThaoTac actor) {
        if (actor == null || actor.getMa() <= 0 || !"QUAN_LY".equals(actor.getLoai()))
            throw new NghiepVuException(403, "KHONG_CO_QUYEN", "Chỉ Quản lý mới được đối soát tiền cọc.");
    }
}
