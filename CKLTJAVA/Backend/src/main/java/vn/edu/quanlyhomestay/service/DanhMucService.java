package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.DanhMucDAO;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import vn.edu.quanlyhomestay.util.TienIchKiemTraDuLieu;
import java.sql.*;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;

/** Nghiệp vụ danh mục. Các thao tác cập nhật nhiều bảng đều được đóng trong transaction. */
public class DanhMucService {
    private final DanhMucDAO dao = new DanhMucDAO();

    private NghiepVuException loi(int ma, String tb) {
        return new NghiepVuException(ma, tb);
    }

    private String chuan(String s) {
        return s == null ? "" : s.trim();
    }

    public List<Map<String, Object>> loaiPhong(String tuKhoa, Boolean hoatDong) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            String sql = "SELECT * FROM loai_phong WHERE (? IS NULL OR LOWER(ten) LIKE LOWER(?) ESCAPE '!') AND (? IS NULL OR dang_hoat_dong=?) ORDER BY ten";
            String k = tuKhoa == null ? null : "%" + thoatLike(tuKhoa.trim()) + "%";
            return dao.truyVan(c, sql, k, k, hoatDong, hoatDong);
        }
    }

    public List<Map<String, Object>> chiTietLoai(long ma) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> d = dao.truyVan(c, "SELECT * FROM loai_phong WHERE ma_loai_phong=?", ma);
            if (d.isEmpty())
                throw loi(404, "Không tìm thấy loại phòng.");
            return d;
        }
    }

    // Giá hiện hành chỉ áp dụng cho đơn tạo sau; chi tiết đơn lưu giá chốt riêng.
    public long luuLoai(Long ma, Map<String, Object> x) throws SQLException {
        String ten = chuan((String) x.get("ten"));
        int suc = soNguyen(x.get("sucChua"), "Sức chứa");
        long thuong = tien(x.get("giaNgayThuong")), cuoi = tien(x.get("giaCuoiTuan"));
        String moTa = (String) x.get("moTa");
        // Chuẩn hóa khoảng trắng; tên duy nhất, sức chứa nguyên dương, hai giá dương.
        if (ten.isEmpty() || ten.length() > 80 || suc < 1 || thuong <= 0 || cuoi <= 0)
            throw loi(422, "Tên, sức chứa và giá phòng chưa hợp lệ.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> trung = dao.truyVan(c,
                        "SELECT ma_loai_phong FROM loai_phong WHERE LOWER(TRIM(ten))=LOWER(?) AND (? IS NULL OR ma_loai_phong<>?)",
                        ten, ma, ma);
                if (!trung.isEmpty())
                    throw loi(409, "Tên loại phòng đã tồn tại.");
                long kq;
                if (ma == null)
                    kq = dao.them(c,
                            "INSERT INTO loai_phong(ten,suc_chua,mo_ta,gia_ngay_thuong,gia_cuoi_tuan) VALUES(?,?,?,?,?)",
                            ten, suc, moTa, thuong, cuoi);
                else {
                    if (dao.capNhat(c,
                            "UPDATE loai_phong SET ten=?,suc_chua=?,mo_ta=?,gia_ngay_thuong=?,gia_cuoi_tuan=? WHERE ma_loai_phong=?",
                            ten, suc, moTa, thuong, cuoi, ma) != 1)
                        throw loi(404, "Không tìm thấy loại phòng.");
                    kq = ma;
                }
                c.commit();
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void doiHoatDong(String bang, String cot, long ma, boolean hoatDong) throws SQLException {
        if (!Set.of("loai_phong", "phong", "ho_so_khach_hang", "tien_nghi").contains(bang))
            throw loi(400, "Loại dữ liệu không hợp lệ.");
        String id = bang.equals("loai_phong") ? "ma_loai_phong"
                : bang.equals("phong") ? "ma_phong"
                : bang.equals("tien_nghi") ? "ma_tien_nghi" : "ma_khach_hang";
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                // Kiểm tra bản ghi tồn tại riêng; MySQL có thể báo 0 dòng thay đổi
                // khi trạng thái được gửi lên trùng với trạng thái hiện tại.
                if (dao.truyVan(c, "SELECT " + id + " FROM " + bang + " WHERE " + id + "=? FOR UPDATE", ma)
                        .isEmpty())
                    throw loi(404, "Không tìm thấy dữ liệu.");

                // Dữ liệu phát sinh được giữ lại; ngừng sử dụng chỉ khi không còn đối tượng đang dùng.
                if (!hoatDong && !bang.equals("tien_nghi")) {
                    String sql = bang.equals("loai_phong")
                            ? "SELECT COUNT(*) n FROM chi_tiet_dat_phong d JOIN phong p ON p.ma_phong=d.ma_phong WHERE p.ma_loai_phong=? AND d.trang_thai IN ('DA_DAT','DANG_O')"
                            : bang.equals("phong")
                                    ? "SELECT COUNT(*) n FROM chi_tiet_dat_phong WHERE ma_phong=? AND trang_thai IN ('DA_DAT','DANG_O')"
                                    : "SELECT COUNT(*) n FROM don_dat_phong WHERE ma_khach_hang=? AND trang_thai IN ('DA_DAT','DANG_O')";
                    long n = ((Number) dao.truyVan(c, sql, ma).get(0).get("n")).longValue();
                    if (n > 0)
                        throw loi(409,
                                bang.equals("loai_phong") ? "Còn phòng hoạt động hoặc đơn chưa hoàn tất thuộc loại này."
                                        : "Còn dữ liệu đang sử dụng, không thể ngừng sử dụng.");
                }
                dao.capNhat(c, "UPDATE " + bang + " SET dang_hoat_dong=? WHERE " + id + "=?", hoatDong, ma);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public List<Map<String, Object>> phong(Map<String, String> f) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            String k = f.get("tuKhoa");
            if (k != null)
                k = "%" + thoatLike(k) + "%";
            return dao.truyVan(c,
                    "SELECT p.*,l.ten AS ten_loai_phong FROM phong p JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE (? IS NULL OR p.tang=?) AND (? IS NULL OR p.ma_loai_phong=?) AND (? IS NULL OR p.trang_thai=?) AND (? IS NULL OR p.dang_hoat_dong=?) AND (? IS NULL OR p.so_phong LIKE ? ESCAPE '!') ORDER BY p.tang,p.so_phong",
                    soInt(f.get("tang")), soInt(f.get("tang")), soLong(f.get("maLoaiPhong")),
                    soLong(f.get("maLoaiPhong")), f.get("trangThai"), f.get("trangThai"), soBool(f.get("dangHoatDong")),
                    soBool(f.get("dangHoatDong")), k, k);
        }
    }

    public List<Map<String, Object>> chiTietPhong(long ma) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> d = dao.truyVan(c,
                    "SELECT p.*,l.ten AS ten_loai_phong FROM phong p JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE p.ma_phong=?",
                    ma);
            if (d.isEmpty())
                throw loi(404, "Không tìm thấy phòng.");
            d.get(0).put("danhSachTienNghi", dao.truyVan(c,
                    "SELECT t.* FROM tien_nghi t JOIN phong_tien_nghi pt ON pt.ma_tien_nghi=t.ma_tien_nghi WHERE pt.ma_phong=? ORDER BY t.ten",
                    ma));
            d.get(0).put("danhSachAnh", dao.truyVan(c,
                    "SELECT * FROM anh_phong WHERE ma_phong=? AND dang_hoat_dong=TRUE AND duong_dan_anh LIKE '/api/cong-khai/anh-phong/%' ORDER BY thu_tu_hien_thi,ma_anh_phong",
                    ma));
            return d;
        }
    }

    // Phòng vừa tạo luôn ở trạng thái TRONG.
    public long luuPhong(Long ma, Map<String, Object> x) throws SQLException {
        String so = chuan((String) x.get("soPhong")), moTa = (String) x.get("moTa");
        int tang = soNguyen(x.get("tang"), "Tầng");
        long loai = soMa(x.get("maLoaiPhong"));
        List<Long> tienNghi = docDanhSachMa(x.get("danhSachMaTienNghi"));
        if (so.isEmpty() || so.length() > 20 || tang < 1 || (moTa != null && moTa.length() > 1000))
            throw loi(422, "Thông tin phòng chưa hợp lệ.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> lp = dao.truyVan(c,
                        "SELECT ma_loai_phong FROM loai_phong WHERE ma_loai_phong=? AND dang_hoat_dong=TRUE FOR UPDATE",
                        loai);
                if (lp.isEmpty())
                    throw loi(422, "Loại phòng không tồn tại hoặc đã ngừng sử dụng.");
                List<Map<String, Object>> tr = dao.truyVan(c,
                        "SELECT ma_phong FROM phong WHERE so_phong=? AND (? IS NULL OR ma_phong<>?)", so, ma, ma);
                if (!tr.isEmpty())
                    throw loi(409, "Số phòng đã tồn tại.");
                long kq;
                if (ma == null) {
                    kq = dao.them(c,
                            "INSERT INTO phong(so_phong,tang,ma_loai_phong,mo_ta,trang_thai) VALUES(?,?,?,?, 'TRONG')",
                            so, tang, loai, moTa);
                } else {
                    if (dao.capNhat(c, "UPDATE phong SET so_phong=?,tang=?,ma_loai_phong=?,mo_ta=? WHERE ma_phong=?",
                            so, tang, loai, moTa, ma) != 1)
                        throw loi(404, "Không tìm thấy phòng.");
                    kq = ma;
                    dao.capNhat(c, "DELETE FROM phong_tien_nghi WHERE ma_phong=?", kq);
                }
                for (long mt : tienNghi) {
                    if (dao.truyVan(c,
                            "SELECT ma_tien_nghi FROM tien_nghi WHERE ma_tien_nghi=? AND dang_hoat_dong=TRUE", mt)
                            .isEmpty())
                        throw loi(422, "Có tiện nghi không tồn tại hoặc đã ngừng sử dụng.");
                    dao.capNhat(c, "INSERT INTO phong_tien_nghi(ma_phong,ma_tien_nghi) VALUES(?,?)", kq, mt);
                }
                c.commit();
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    // DA_DAT là trạng thái hiển thị theo ngày nhận; không lưu thay trạng thái vận hành.
    public void doiTrangThaiPhong(long ma, String tt) throws SQLException {
        if (!Set.of("TRONG", "DANG_DON", "BAO_TRI").contains(tt))
            throw loi(422, "Chỉ được chọn TRONG, DANG_DON hoặc BAO_TRI.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                List<Map<String, Object>> d = dao.truyVan(c, "SELECT trang_thai FROM phong WHERE ma_phong=? FOR UPDATE",
                        ma);
                if (d.isEmpty())
                    throw loi(404, "Không tìm thấy phòng.");
                if ("DANG_O".equals(d.get(0).get("trangThai")))
                    throw loi(409, "Phòng đang có khách, không thể chuyển trạng thái thủ công.");
                if ("BAO_TRI".equals(tt)) {
                    List<Map<String, Object>> dat = dao.truyVan(c,
                            "SELECT ngay_nhan FROM chi_tiet_dat_phong WHERE ma_phong=? AND trang_thai='DA_DAT' AND ngay_tra>? ORDER BY ngay_nhan LIMIT 1",
                            ma, java.sql.Date.valueOf(LocalDate.now()));
                    if (!dat.isEmpty())
                        throw loi(409, "Phòng có đơn đã đặt sắp tới; hãy sửa, đổi phòng hoặc hủy đơn trước.");
                }
                dao.capNhat(c, "UPDATE phong SET trang_thai=? WHERE ma_phong=?", tt, ma);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public List<Map<String, Object>> soDo(LocalDate ngay) throws SQLException {
        // Giả định DA_DAT hiển thị khi phòng TRONG có đơn đã đặt với ngày nhận đúng ngày xem sơ đồ.
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ph = dao.truyVan(c,
                    "SELECT p.ma_phong,p.so_phong,p.tang,p.trang_thai,l.ten AS ten_loai_phong,CASE WHEN p.trang_thai='TRONG' AND EXISTS(SELECT 1 FROM chi_tiet_dat_phong d WHERE d.ma_phong=p.ma_phong AND d.trang_thai='DA_DAT' AND d.ngay_nhan=?) THEN 'DA_DAT' ELSE p.trang_thai END AS trang_thai_hien_thi FROM phong p JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE p.dang_hoat_dong=TRUE ORDER BY p.tang,p.so_phong",
                    java.sql.Date.valueOf(ngay));
            TreeMap<Integer, List<Map<String, Object>>> tang = new TreeMap<>();
            for (Map<String, Object> p : ph) {
                int t = ((Number) p.get("tang")).intValue();
                tang.computeIfAbsent(t, z -> new ArrayList<>()).add(p);
            }
            List<Map<String, Object>> kq = new ArrayList<>();
            for (var e : tang.entrySet()) {
                Map<String, Object> f = new LinkedHashMap<>();
                f.put("soTang", e.getKey());
                f.put("tongSoPhong", e.getValue().size());
                f.put("danhSachPhong", e.getValue());
                kq.add(f);
            }
            Map<String, Object> bao = new LinkedHashMap<>();
            bao.put("ngay", ngay.toString());
            bao.put("tongSoPhong", ph.size());
            Map<String, Long> dem = new TreeMap<>();
            for (String tt : List.of("TRONG", "DA_DAT", "DANG_O", "DANG_DON", "BAO_TRI"))
                dem.put(tt, 0L);
            for (Map<String, Object> p : ph)
                dem.merge((String) p.get("trangThaiHienThi"), 1L, Long::sum);
            bao.put("tongTheoTrangThai", dem);
            bao.put("danhSachTang", kq);
            return List.of(bao);
        }
    }

    public List<Map<String, Object>> tienNghi() throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            return dao.truyVan(c, "SELECT * FROM tien_nghi ORDER BY ten");
        }
    }

    public long luuTienNghi(Long ma, String ten) throws SQLException {
        ten = chuan(ten);
        if (ten.isEmpty() || ten.length() > 80)
            throw loi(422, "Tên tiện nghi phải có từ 1 đến 80 ký tự.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> d = dao.truyVan(c,
                    "SELECT ma_tien_nghi FROM tien_nghi WHERE LOWER(TRIM(ten))=LOWER(?) AND (? IS NULL OR ma_tien_nghi<>?)",
                    ten, ma, ma);
            if (!d.isEmpty())
                throw loi(409, "Tên tiện nghi đã tồn tại.");
            if (ma == null)
                return dao.them(c, "INSERT INTO tien_nghi(ten) VALUES(?)", ten);
            if (dao.capNhat(c, "UPDATE tien_nghi SET ten=? WHERE ma_tien_nghi=?", ten, ma) != 1)
                throw loi(404, "Không tìm thấy tiện nghi.");
            return ma;
        }
    }

    public void xoaTienNghi(long ma) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            if (dao.capNhat(c, "UPDATE tien_nghi SET dang_hoat_dong=FALSE WHERE ma_tien_nghi=?", ma) != 1)
                throw loi(404, "Không tìm thấy tiện nghi.");
        }
    }

    public Map<String, Object> khachHang(String tuKhoa, int trang, int kichThuoc) throws SQLException {
        String k = tuKhoa == null ? null : "%" + thoatLike(tuKhoa.trim()) + "%";
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = dao.truyVan(c,
                    "SELECT * FROM ho_so_khach_hang WHERE (? IS NULL OR ho_ten LIKE ? ESCAPE '!' OR so_dien_thoai LIKE ? ESCAPE '!' OR so_giay_to LIKE ? ESCAPE '!') ORDER BY ma_khach_hang DESC LIMIT ? OFFSET ?",
                    k, k, k, k, kichThuoc, (trang - 1) * kichThuoc);
            List<Map<String, Object>> tong = dao.truyVan(c,
                    "SELECT COUNT(*) AS tong FROM ho_so_khach_hang WHERE (? IS NULL OR ho_ten LIKE ? ESCAPE '!' OR so_dien_thoai LIKE ? ESCAPE '!' OR so_giay_to LIKE ? ESCAPE '!')",
                    k, k, k, k);
            long so = ((Number) tong.get(0).get("tong")).longValue();
            Map<String, Object> ketQua = new LinkedHashMap<>();
            ketQua.put("danhSach", ds);
            ketQua.put("trang", trang);
            ketQua.put("kichThuoc", kichThuoc);
            ketQua.put("tongSoBanGhi", so);
            ketQua.put("tongSoTrang", (so + kichThuoc - 1) / kichThuoc);
            return ketQua;
        }
    }

    public List<Map<String, Object>> khachTheoMa(long ma) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> d = dao.truyVan(c, "SELECT * FROM ho_so_khach_hang WHERE ma_khach_hang=?", ma);
            if (d.isEmpty())
                throw loi(404, "Không tìm thấy khách hàng.");
            return d;
        }
    }

    public long luuKhach(Long ma, Map<String, Object> x) throws SQLException {
        String ten = chuan((String) x.get("hoTen")),
                sdt = TienIchKiemTraDuLieu.chuanHoaSoDienThoaiVietNam((String) x.get("soDienThoai"));
        String email = chuan((String) x.get("thuDienTu"));
        String giay = chuan((String) x.get("soGiayTo"));
        String ghi = (String) x.get("ghiChu");
        if (!ten.matches(".{1,120}") || sdt == null)
            throw loi(422, "Họ tên và số điện thoại Việt Nam là bắt buộc.");
        if (!email.isEmpty() && !email.matches("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$"))
            throw loi(422, "Email không đúng định dạng.");
        if (ghi != null && ghi.length() > 1000)
            throw loi(422, "Ghi chú tối đa 1000 ký tự.");
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                String sql = "SELECT ma_khach_hang,ho_ten,so_dien_thoai,so_giay_to FROM ho_so_khach_hang WHERE (so_dien_thoai=? OR (?<>'' AND so_giay_to=?)) AND (? IS NULL OR ma_khach_hang<>?)";
                List<Map<String, Object>> trung = dao.truyVan(c, sql, sdt, giay, giay, ma, ma);
                if (!trung.isEmpty() && !Boolean.TRUE.equals(x.get("xacNhanTaoMoi")) && ma == null)
                    throw new NghiepVuException(409,
                            "Tìm thấy hồ sơ trùng. Xác nhận tạo hồ sơ mới nếu vẫn muốn tiếp tục.", trung);
                if (!email.isEmpty()) {
                    List<Map<String, Object>> de = dao.truyVan(c,
                            "SELECT ma_khach_hang FROM ho_so_khach_hang WHERE thu_dien_tu=? AND (? IS NULL OR ma_khach_hang<>?) UNION SELECT ma_khach_hang FROM tai_khoan_khach_hang WHERE thu_dien_tu=? AND (? IS NULL OR ma_khach_hang<>?)",
                            email, ma, ma, email, ma, ma);
                    if (!de.isEmpty())
                        throw loi(409, "Email đã được dùng bởi một hồ sơ hoặc tài khoản khách hàng khác.");
                }
                long kq;
                if (ma == null)
                    kq = dao.them(c,
                            "INSERT INTO ho_so_khach_hang(ho_ten,so_dien_thoai,thu_dien_tu,so_giay_to,ghi_chu) VALUES(?,?,?,?,?)",
                            ten, sdt, email.isEmpty() ? null : email, giay.isEmpty() ? null : giay, ghi);
                else {
                    if (dao.capNhat(c,
                            "UPDATE ho_so_khach_hang SET ho_ten=?,so_dien_thoai=?,thu_dien_tu=?,so_giay_to=?,ghi_chu=? WHERE ma_khach_hang=?",
                            ten, sdt, email.isEmpty() ? null : email, giay.isEmpty() ? null : giay, ghi, ma) != 1)
                        throw loi(404, "Không tìm thấy khách hàng.");
                    kq = ma;
                }
                c.commit();
                return kq;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private String thoatLike(String s) {
        return s.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    private int soNguyen(Object x, String ten) {
        try {
            return new java.math.BigDecimal(String.valueOf(x)).intValueExact();
        } catch (Exception e) {
            throw loi(422, ten + " phải là số nguyên.");
        }
    }

    private long soMa(Object x) {
        try {
            long ma = new java.math.BigDecimal(String.valueOf(x)).longValueExact();
            if (ma > 0)
                return ma;
        } catch (Exception ignored) {
        }
        throw loi(422, "Mã dữ liệu phải là số nguyên dương.");
    }

    private long tien(Object x) {
        try {
            long n = new java.math.BigDecimal(String.valueOf(x)).longValueExact();
            if (n > 0)
                return n;
        } catch (Exception ignored) {
        }
        throw loi(422, "Giá phải là số VND nguyên dương.");
    }

    private Integer soInt(String s) {
        try {
            return s == null || s.isBlank() ? null : Integer.valueOf(s);
        } catch (Exception e) {
            throw loi(400, "Tham số số nguyên không hợp lệ.");
        }
    }

    private Long soLong(String s) {
        try {
            return s == null || s.isBlank() ? null : Long.valueOf(s);
        } catch (Exception e) {
            throw loi(400, "Tham số mã không hợp lệ.");
        }
    }

    private Boolean soBool(String s) {
        if (s == null || s.isBlank())
            return null;
        if (s.equalsIgnoreCase("true"))
            return true;
        if (s.equalsIgnoreCase("false"))
            return false;
        throw loi(400, "Tham số hoạt động phải là true hoặc false.");
    }

    private List<Long> docDanhSachMa(Object x) {
        List<Long> k = new ArrayList<>();
        if (x instanceof List<?> l)
            for (Object v : l)
                k.add(soMa(v));
        return k;
    }
}
