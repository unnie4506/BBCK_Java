package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.util.DocBanGhiUtil;
import vn.edu.quanlyhomestay.model.NguoiThaoTac;
import java.sql.*;
import java.util.*;

public class DonDatPhongDAO {
    public long them(Connection c, String maTraCuu, long maKhachHang, NguoiThaoTac actor, String ghiChu,
            java.math.BigDecimal tienCoc) throws SQLException {
        return them(c, maTraCuu, maKhachHang, actor, ghiChu, tienCoc, 1);
    }

    public long them(Connection c, String maTraCuu, long maKhachHang, NguoiThaoTac actor, String ghiChu,
            java.math.BigDecimal tienCoc, int soKhach) throws SQLException {
        String sql = "INSERT INTO don_dat_phong(ma_tra_cuu_don,ma_khach_hang,ma_tai_khoan_khach_hang,ma_quan_ly_tao,nguon_tao,so_khach,trang_thai,tien_coc_da_nhan,ghi_chu) VALUES(?,?,?,?,?,?,'DA_DAT',?,?)";
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, maTraCuu);
            p.setLong(2, maKhachHang);
            if ("KHACH_HANG".equals(actor.getLoai())) {
                p.setLong(3, actor.getMa());
                p.setNull(4, Types.BIGINT);
                p.setString(5, "TRUC_TUYEN");
            } else {
                p.setNull(3, Types.BIGINT);
                p.setLong(4, actor.getMa());
                p.setString(5, "QUAN_LY");
            }
            p.setInt(6, soKhach);
            p.setBigDecimal(7, tienCoc);
            p.setString(8, ghiChu);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next())
                    throw new SQLException("Không nhận được mã đơn mới.");
                return r.getLong(1);
            }
        }
    }

    public long themDonChoCoc(Connection c, String maTraCuu, long maKhachHang, NguoiThaoTac actor, String ghiChu,
            java.math.BigDecimal tienCocYeuCau, int soKhach, int thoiGianGiuPhut) throws SQLException {
        String sql = "INSERT INTO don_dat_phong(ma_tra_cuu_don,ma_khach_hang,ma_tai_khoan_khach_hang,ma_quan_ly_tao,nguon_tao,so_khach,trang_thai,tien_coc_da_nhan,tien_coc_yeu_cau,han_giu_cho,ghi_chu) VALUES(?,?,?,?,?,?,'CHO_COC',0,?,TIMESTAMPADD(MINUTE,?,CURRENT_TIMESTAMP(3)),?)";
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, maTraCuu);
            p.setLong(2, maKhachHang);
            p.setLong(3, actor.getMa());
            p.setNull(4, Types.BIGINT);
            p.setString(5, "TRUC_TUYEN");
            p.setInt(6, soKhach);
            p.setBigDecimal(7, tienCocYeuCau);
            p.setInt(8, thoiGianGiuPhut);
            p.setString(9, ghiChu);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("Không nhận được mã đơn mới.");
                return r.getLong(1);
            }
        }
    }

    public boolean khoaHoSoKhachHang(Connection c, long maKhachHang) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT ma_khach_hang FROM ho_so_khach_hang WHERE ma_khach_hang=? AND dang_hoat_dong=TRUE FOR UPDATE")) {
            p.setLong(1, maKhachHang);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public int demDonDaDatTheoTaiKhoan(Connection c, long maTaiKhoan) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT COUNT(*) FROM don_dat_phong WHERE ma_tai_khoan_khach_hang=? AND trang_thai='DA_DAT'")) {
            p.setLong(1, maTaiKhoan);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    public int demDonDangChoTheoTaiKhoan(Connection c, long maTaiKhoan) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM don_dat_phong WHERE ma_tai_khoan_khach_hang=? AND (trang_thai='DA_DAT' OR (trang_thai='CHO_COC' AND han_giu_cho>CURRENT_TIMESTAMP(3)))")) {
            p.setLong(1, maTaiKhoan);
            try (ResultSet r = p.executeQuery()) { r.next(); return r.getInt(1); }
        }
    }

    public List<Map<String, Object>> donKhoa(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT d.*,CURRENT_TIMESTAMP(3) AS thoi_gian_may_chu,UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS thoi_gian_may_chu_ms,UNIX_TIMESTAMP(d.han_giu_cho)*1000 AS han_giu_cho_ms,(d.han_giu_cho>CURRENT_TIMESTAMP(3)) AS con_han FROM don_dat_phong d WHERE d.ma_don_dat_phong=? FOR UPDATE")) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) { return DocBanGhiUtil.docDanhSach(r); }
        }
    }

    public int doiTrangThaiVaCongCoc(Connection c, long maDon, String trangThai, java.math.BigDecimal soTienCoc)
            throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE don_dat_phong SET trang_thai=?,tien_coc_da_nhan=tien_coc_da_nhan+? WHERE ma_don_dat_phong=?")) {
            p.setString(1, trangThai); p.setBigDecimal(2, soTienCoc); p.setLong(3, maDon);
            return p.executeUpdate();
        }
    }

    public int capNhatTrangThaiVaCoc(Connection c, long maDon, String trangThai, java.math.BigDecimal tongCoc)
            throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE don_dat_phong SET trang_thai=?,tien_coc_da_nhan=? WHERE ma_don_dat_phong=?")) {
            p.setString(1, trangThai); p.setBigDecimal(2, tongCoc); p.setLong(3, maDon);
            return p.executeUpdate();
        }
    }

    public int huyChiTietChoCoc(Connection c, long maDon, String lyDo) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE chi_tiet_dat_phong SET trang_thai='DA_HUY',huy_luc=CURRENT_TIMESTAMP,ly_do_huy=? WHERE ma_don_dat_phong=? AND trang_thai='DA_DAT'")) {
            p.setString(1, lyDo); p.setLong(2, maDon); return p.executeUpdate();
        }
    }

    public List<Map<String, Object>> danhSachDonChoCoc(Connection c) throws SQLException {
        String sql = "SELECT d.ma_don_dat_phong,d.ma_tra_cuu_don,d.trang_thai,d.tien_coc_yeu_cau,d.tien_coc_da_nhan,d.han_giu_cho,UNIX_TIMESTAMP(d.han_giu_cho)*1000 AS han_giu_cho_ms,(d.han_giu_cho>CURRENT_TIMESTAMP(3)) AS con_han,d.khach_bao_da_chuyen_luc,d.tao_luc,k.ho_ten,k.so_dien_thoai,COALESCE(SUM(CASE WHEN x.trang_thai<>'DA_HUY' THEN x.tien_phong ELSE 0 END),0) AS tong_tien_phong,CURRENT_TIMESTAMP(3) AS thoi_gian_may_chu,UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS thoi_gian_may_chu_ms FROM don_dat_phong d JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang LEFT JOIN chi_tiet_dat_phong x ON x.ma_don_dat_phong=d.ma_don_dat_phong WHERE d.nguon_tao='TRUC_TUYEN' AND (d.trang_thai='CHO_COC' OR (d.trang_thai='HET_HAN' AND d.han_giu_cho>=CURRENT_TIMESTAMP(3)-INTERVAL 48 HOUR)) GROUP BY d.ma_don_dat_phong ORDER BY (d.khach_bao_da_chuyen_luc IS NOT NULL) DESC,d.khach_bao_da_chuyen_luc ASC,d.han_giu_cho ASC,d.ma_don_dat_phong";
        try (PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) { return DocBanGhiUtil.docDanhSach(r); }
    }

    public List<Map<String, Object>> donChoCocHetHan(Connection c) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT ma_don_dat_phong FROM don_dat_phong WHERE trang_thai='CHO_COC' AND han_giu_cho<=CURRENT_TIMESTAMP(3) ORDER BY han_giu_cho,ma_don_dat_phong")) {
            try (ResultSet r = p.executeQuery()) { return DocBanGhiUtil.docDanhSach(r); }
        }
    }

    public List<Map<String, Object>> donChoCocDemo(Connection c, int soGiay) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT ma_don_dat_phong,tien_coc_yeu_cau,tien_coc_da_nhan FROM don_dat_phong WHERE trang_thai='CHO_COC' AND han_giu_cho>CURRENT_TIMESTAMP(3) AND tao_luc<=DATE_SUB(CURRENT_TIMESTAMP(3),INTERVAL ? SECOND) ORDER BY tao_luc,ma_don_dat_phong")) {
            p.setInt(1, soGiay); try (ResultSet r = p.executeQuery()) { return DocBanGhiUtil.docDanhSach(r); }
        }
    }

    public int hetHanDon(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE don_dat_phong SET trang_thai='HET_HAN' WHERE ma_don_dat_phong=? AND trang_thai='CHO_COC' AND han_giu_cho<=CURRENT_TIMESTAMP(3)")) {
            p.setLong(1, maDon); return p.executeUpdate();
        }
    }

    public int baoDaChuyenKhoan(Connection c, long maDon, long maTaiKhoan) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("UPDATE don_dat_phong SET khach_bao_da_chuyen_luc=COALESCE(khach_bao_da_chuyen_luc,CURRENT_TIMESTAMP(3)) WHERE ma_don_dat_phong=? AND ma_tai_khoan_khach_hang=? AND trang_thai='CHO_COC' AND han_giu_cho>CURRENT_TIMESTAMP(3)")) {
            p.setLong(1, maDon); p.setLong(2, maTaiKhoan); return p.executeUpdate();
        }
    }

    public List<Map<String, Object>> khoaDeSua(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c
                .prepareStatement("SELECT * FROM don_dat_phong WHERE ma_don_dat_phong=? FOR UPDATE")) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public boolean thuocKhachHang(Connection c, long maDon, long maTaiKhoan) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT 1 FROM don_dat_phong WHERE ma_don_dat_phong=? AND ma_tai_khoan_khach_hang=?")) {
            p.setLong(1, maDon);
            p.setLong(2, maTaiKhoan);
            try (ResultSet r = p.executeQuery()) {
                return r.next();
            }
        }
    }

    public int capNhat(Connection c, long maDon, String ghiChu, java.math.BigDecimal tienCoc) throws SQLException {
        try (PreparedStatement p = c
                .prepareStatement("UPDATE don_dat_phong SET ghi_chu=?,tien_coc_da_nhan=? WHERE ma_don_dat_phong=?")) {
            p.setString(1, ghiChu);
            p.setBigDecimal(2, tienCoc);
            p.setLong(3, maDon);
            return p.executeUpdate();
        }
    }

    public int capNhatTrangThai(Connection c, long maDon, String trangThai) throws SQLException {
        try (PreparedStatement p = c
                .prepareStatement("UPDATE don_dat_phong SET trang_thai=? WHERE ma_don_dat_phong=?")) {
            p.setString(1, trangThai);
            p.setLong(2, maDon);
            return p.executeUpdate();
        }
    }

    public List<Map<String, Object>> chiTietDon(Connection c, long maDon) throws SQLException {
        String sql = "SELECT d.*,p.so_phong,l.ten AS ten_loai_phong FROM chi_tiet_dat_phong d JOIN phong p ON p.ma_phong=d.ma_phong JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE d.ma_don_dat_phong=? ORDER BY d.ma_chi_tiet_dat_phong";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> layDonDayDu(Connection c, long maDon) throws SQLException {
        String sql = "SELECT d.*,k.ho_ten,k.so_dien_thoai,k.thu_dien_tu,k.so_giay_to,CURRENT_TIMESTAMP(3) AS thoi_gian_may_chu,UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS thoi_gian_may_chu_ms,UNIX_TIMESTAMP(d.han_giu_cho)*1000 AS han_giu_cho_ms,(d.han_giu_cho>CURRENT_TIMESTAMP(3)) AS con_han FROM don_dat_phong d JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang WHERE d.ma_don_dat_phong=?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public Map<String, Object> timKiem(Connection c, String tuKhoa, String trangThai, java.sql.Date tuNgay,
            java.sql.Date denNgay, Long maTaiKhoanKhachHang, int kichThuoc, int boQua) throws SQLException {
        return timKiem(c, tuKhoa, trangThai, tuNgay, denNgay, maTaiKhoanKhachHang, null, kichThuoc, boQua);
    }

    public Map<String, Object> timKiem(Connection c, String tuKhoa, String trangThai, java.sql.Date tuNgay,
            java.sql.Date denNgay, Long maTaiKhoanKhachHang, String nguonDat, int kichThuoc, int boQua)
            throws SQLException {
        String k = tuKhoa == null || tuKhoa.isBlank() ? null
                : "%" + tuKhoa.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        String dieu = " FROM don_dat_phong d JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang WHERE d.trang_thai NOT IN ('CHO_COC','HET_HAN') AND (? IS NULL OR d.ma_tra_cuu_don LIKE ? ESCAPE '!' OR k.ho_ten LIKE ? ESCAPE '!' OR k.so_dien_thoai LIKE ? ESCAPE '!') AND (? IS NULL OR d.trang_thai=?) AND (? IS NULL OR EXISTS(SELECT 1 FROM chi_tiet_dat_phong c WHERE c.ma_don_dat_phong=d.ma_don_dat_phong AND c.ngay_nhan>=?)) AND (? IS NULL OR EXISTS(SELECT 1 FROM chi_tiet_dat_phong c WHERE c.ma_don_dat_phong=d.ma_don_dat_phong AND c.ngay_nhan<=?)) AND (? IS NULL OR d.ma_tai_khoan_khach_hang=?) AND (? IS NULL OR CASE WHEN d.nguon_tao='QUAN_LY' THEN 'QUAN_LY' ELSE 'TRUC_TUYEN' END=?)";
        String base = "SELECT d.ma_don_dat_phong,d.ma_tra_cuu_don,d.ma_khach_hang,d.trang_thai,d.tien_coc_da_nhan,d.ghi_chu,d.tao_luc,CASE WHEN d.nguon_tao='QUAN_LY' THEN 'QUAN_LY' ELSE 'TRUC_TUYEN' END AS nguon_dat,k.ho_ten,k.so_dien_thoai,(SELECT COALESCE(SUM(c.tien_phong),0) FROM chi_tiet_dat_phong c WHERE c.ma_don_dat_phong=d.ma_don_dat_phong AND c.trang_thai<>'DA_HUY') AS tong_tien_phong"
                + dieu;
        try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*)" + dieu)) {
            ganLoc(p, k, trangThai, tuNgay, denNgay, maTaiKhoanKhachHang, nguonDat);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                long tong = r.getLong(1);
                List<Map<String, Object>> ds;
                try (PreparedStatement q = c
                        .prepareStatement(base + " ORDER BY d.ma_don_dat_phong DESC LIMIT ? OFFSET ?")) {
                    ganLoc(q, k, trangThai, tuNgay, denNgay, maTaiKhoanKhachHang, nguonDat);
                    q.setInt(15, kichThuoc);
                    q.setInt(16, boQua);
                    try (ResultSet rs = q.executeQuery()) {
                        ds = DocBanGhiUtil.docDanhSach(rs);
                    }
                }
                Map<String, Object> ketQua = new LinkedHashMap<>();
                ketQua.put("danhSach", ds);
                ketQua.put("tongSoBanGhi", tong);
                return ketQua;
            }
        }
    }

    private void ganLoc(PreparedStatement p, String k, String tt, java.sql.Date tu, java.sql.Date den,
            Long maTaiKhoanKhachHang, String nguonDat) throws SQLException {
        int i = 1;
        p.setString(i++, k);
        p.setString(i++, k);
        p.setString(i++, k);
        p.setString(i++, k);
        p.setString(i++, tt);
        p.setString(i++, tt);
        p.setDate(i++, tu);
        p.setDate(i++, tu);
        p.setDate(i++, den);
        p.setDate(i++, den);
        p.setObject(i++, maTaiKhoanKhachHang);
        p.setObject(i++, maTaiKhoanKhachHang);
        p.setString(i++, nguonDat);
        p.setString(i, nguonDat);
    }

    public List<Map<String, Object>> lich(Connection c, java.sql.Date tu, java.sql.Date den) throws SQLException {
        String sql = "SELECT d.ma_don_dat_phong,d.ma_tra_cuu_don,d.trang_thai AS trang_thai_don,k.ho_ten,p.so_phong,c.ngay_nhan,c.ngay_tra,c.trang_thai AS trang_thai_chi_tiet FROM chi_tiet_dat_phong c JOIN don_dat_phong d ON d.ma_don_dat_phong=c.ma_don_dat_phong JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang JOIN phong p ON p.ma_phong=c.ma_phong WHERE c.trang_thai<>'DA_HUY' AND (d.trang_thai IN ('DA_DAT','DANG_O') OR (d.trang_thai='CHO_COC' AND d.han_giu_cho>CURRENT_TIMESTAMP(3))) AND c.ngay_nhan<=? AND c.ngay_tra>? ORDER BY c.ngay_nhan,p.so_phong";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, den);
            p.setDate(2, tu);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }
}
