package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.util.DocBanGhiUtil;
import java.sql.*;
import java.util.*;

/** Các truy vấn chỉ đọc dữ liệu được phép công khai hoặc thuộc tài khoản khách hiện tại. */
public class CongKhaiDAO {
    public List<Map<String, Object>> lichSuCuaHoSo(Connection c, long maKhach, int kichThuoc, int boQua)
            throws SQLException {
        String sql = "SELECT d.ma_don_dat_phong,d.ma_tra_cuu_don,d.trang_thai,h.tien_phong AS tong_thanh_toan,MIN(x.ngay_nhan) AS ngay_nhan,MAX(x.ngay_tra) AS ngay_tra,GROUP_CONCAT(DISTINCT p.so_phong ORDER BY p.so_phong SEPARATOR ', ') AS danh_sach_phong FROM don_dat_phong d JOIN hoa_don h ON h.ma_don_dat_phong=d.ma_don_dat_phong AND h.trang_thai='DA_THANH_TOAN' JOIN chi_tiet_dat_phong x ON x.ma_don_dat_phong=d.ma_don_dat_phong AND x.trang_thai='DA_TRA' JOIN phong p ON p.ma_phong=x.ma_phong WHERE d.ma_khach_hang=? AND d.trang_thai='HOAN_TAT' GROUP BY d.ma_don_dat_phong,h.ma_hoa_don ORDER BY d.tao_luc DESC,d.ma_don_dat_phong DESC LIMIT ? OFFSET ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maKhach);
            p.setInt(2, kichThuoc);
            p.setInt(3, boQua);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> thongKeLichSuTheoHoSo(Connection c, long maKhach) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT d.ma_don_dat_phong) AS tong_so_lan_luu_tru,COALESCE(SUM(h.tien_phong),0) AS tong_chi_tieu FROM don_dat_phong d JOIN hoa_don h ON h.ma_don_dat_phong=d.ma_don_dat_phong AND h.trang_thai='DA_THANH_TOAN' WHERE d.ma_khach_hang=? AND d.trang_thai='HOAN_TAT'";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maKhach);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> danhSachLoaiPhong(Connection c) throws SQLException {
        String sql = "SELECT l.ma_loai_phong,l.ten,l.suc_chua,l.mo_ta,l.gia_ngay_thuong,l.gia_cuoi_tuan,"
                + "COALESCE((SELECT al.duong_dan_anh FROM anh_loai_phong al WHERE al.ma_loai_phong=l.ma_loai_phong "
                + "AND al.dang_hoat_dong=TRUE AND al.duong_dan_anh LIKE '/api/cong-khai/anh-phong/%' "
                + "ORDER BY al.thu_tu_hien_thi,al.ma_anh_loai_phong LIMIT 1),"
                + "(SELECT a.duong_dan_anh FROM anh_phong a JOIN phong p ON p.ma_phong=a.ma_phong "
                + "WHERE p.ma_loai_phong=l.ma_loai_phong AND p.dang_hoat_dong=TRUE AND a.dang_hoat_dong=TRUE "
                + "AND a.duong_dan_anh LIKE '/api/cong-khai/anh-phong/%' "
                + "ORDER BY a.thu_tu_hien_thi,a.ma_anh_phong LIMIT 1)) AS anh_dai_dien "
                + "FROM loai_phong l WHERE l.dang_hoat_dong=TRUE ORDER BY l.ten";
        try (PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            return DocBanGhiUtil.docDanhSach(r);
        }
    }

    public List<Map<String, Object>> chiTietLoaiPhong(Connection c, long ma) throws SQLException {
        String sql = "SELECT l.ma_loai_phong,l.ten,l.suc_chua,l.mo_ta,l.gia_ngay_thuong,l.gia_cuoi_tuan,"
                + "COALESCE((SELECT al.duong_dan_anh FROM anh_loai_phong al WHERE al.ma_loai_phong=l.ma_loai_phong "
                + "AND al.dang_hoat_dong=TRUE AND al.duong_dan_anh LIKE '/api/cong-khai/anh-phong/%' "
                + "ORDER BY al.thu_tu_hien_thi,al.ma_anh_loai_phong LIMIT 1),"
                + "(SELECT a.duong_dan_anh FROM anh_phong a JOIN phong p ON p.ma_phong=a.ma_phong "
                + "WHERE p.ma_loai_phong=l.ma_loai_phong AND p.dang_hoat_dong=TRUE AND a.dang_hoat_dong=TRUE "
                + "AND a.duong_dan_anh LIKE '/api/cong-khai/anh-phong/%' "
                + "ORDER BY a.thu_tu_hien_thi,a.ma_anh_phong LIMIT 1)) AS anh_dai_dien "
                + "FROM loai_phong l WHERE l.ma_loai_phong=? AND l.dang_hoat_dong=TRUE";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, ma);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> tienNghiTheoLoai(Connection c, long ma) throws SQLException {
        String sql = "SELECT DISTINCT t.ten FROM tien_nghi t JOIN phong_tien_nghi pt ON pt.ma_tien_nghi=t.ma_tien_nghi JOIN phong p ON p.ma_phong=pt.ma_phong WHERE p.ma_loai_phong=? AND p.dang_hoat_dong=TRUE AND t.dang_hoat_dong=TRUE ORDER BY t.ten";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, ma);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> anhTheoLoai(Connection c, long ma) throws SQLException {
        String sql = "SELECT duong_dan_anh,chu_thich FROM ("
                + "SELECT al.duong_dan_anh,al.chu_thich,0 AS nguon,al.thu_tu_hien_thi,al.ma_anh_loai_phong AS ma_anh "
                + "FROM anh_loai_phong al WHERE al.ma_loai_phong=? AND al.dang_hoat_dong=TRUE "
                + "AND al.duong_dan_anh LIKE '/api/cong-khai/anh-phong/%' "
                + "UNION ALL "
                + "SELECT a.duong_dan_anh,a.chu_thich,1 AS nguon,a.thu_tu_hien_thi,a.ma_anh_phong AS ma_anh "
                + "FROM anh_phong a JOIN phong p ON p.ma_phong=a.ma_phong "
                + "WHERE p.ma_loai_phong=? AND p.dang_hoat_dong=TRUE AND a.dang_hoat_dong=TRUE "
                + "AND a.duong_dan_anh LIKE '/api/cong-khai/anh-phong/%'"
                + ") anh_gop ORDER BY nguon,thu_tu_hien_thi,ma_anh";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, ma);
            p.setLong(2, ma);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> phongTheoMa(Connection c, List<Long> maPhong) throws SQLException {
        if (maPhong == null || maPhong.isEmpty())
            return List.of();
        String dauHoi = String.join(",", Collections.nCopies(maPhong.size(), "?"));
        String sql = "SELECT p.ma_phong,p.so_phong,p.ma_loai_phong,l.ten AS ten_loai_phong,l.suc_chua,"
                + "COALESCE((SELECT a.duong_dan_anh FROM anh_phong a WHERE a.ma_phong=p.ma_phong AND a.dang_hoat_dong=TRUE "
                + "AND a.duong_dan_anh LIKE '/api/cong-khai/anh-phong/%' "
                + "ORDER BY a.thu_tu_hien_thi,a.ma_anh_phong LIMIT 1),"
                + "(SELECT al.duong_dan_anh FROM anh_loai_phong al WHERE al.ma_loai_phong=p.ma_loai_phong "
                + "AND al.dang_hoat_dong=TRUE AND al.duong_dan_anh LIKE '/api/cong-khai/anh-phong/%' "
                + "ORDER BY al.thu_tu_hien_thi,al.ma_anh_loai_phong LIMIT 1)) AS anh_dai_dien "
                + "FROM phong p JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE p.ma_phong IN ("
                + dauHoi + ") AND p.dang_hoat_dong=TRUE AND l.dang_hoat_dong=TRUE ORDER BY p.ma_phong";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            for (int i = 0; i < maPhong.size(); i++)
                p.setLong(i + 1, maPhong.get(i));
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> hoSoTheoTaiKhoan(Connection c, long maTaiKhoan) throws SQLException {
        String sql = "SELECT p.ma_khach_hang,p.ho_ten,p.so_dien_thoai,p.so_giay_to,a.thu_dien_tu FROM tai_khoan_khach_hang a JOIN ho_so_khach_hang p ON p.ma_khach_hang=a.ma_khach_hang WHERE a.ma_tai_khoan_khach_hang=? AND a.dang_hoat_dong=TRUE AND p.dang_hoat_dong=TRUE";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maTaiKhoan);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public int capNhatHoSo(Connection c, long maKhach, String hoTen, String dienThoai, String giayTo)
            throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE ho_so_khach_hang SET ho_ten=?,so_dien_thoai=?,so_giay_to=? WHERE ma_khach_hang=? AND dang_hoat_dong=TRUE")) {
            p.setString(1, hoTen);
            p.setString(2, dienThoai);
            p.setString(3, giayTo);
            p.setLong(4, maKhach);
            return p.executeUpdate();
        }
    }

    public List<Map<String, Object>> donCuaKhach(Connection c, long maTaiKhoan, String trangThai, int kichThuoc,
            int boQua) throws SQLException {
        String sql = "SELECT d.ma_don_dat_phong,d.ma_tra_cuu_don,CASE WHEN d.trang_thai='CHO_COC' AND d.han_giu_cho<=CURRENT_TIMESTAMP(3) THEN 'HET_HAN' ELSE d.trang_thai END AS trang_thai,d.so_khach,d.tien_coc_da_nhan,d.tien_coc_yeu_cau,d.han_giu_cho,d.khach_bao_da_chuyen_luc,d.tao_luc,COALESCE(SUM(CASE WHEN x.trang_thai<>'DA_HUY' THEN x.tien_phong ELSE 0 END),0) AS tong_tien_phong FROM don_dat_phong d LEFT JOIN chi_tiet_dat_phong x ON x.ma_don_dat_phong=d.ma_don_dat_phong WHERE d.ma_tai_khoan_khach_hang=? AND (? IS NULL OR (CASE WHEN d.trang_thai='CHO_COC' AND d.han_giu_cho<=CURRENT_TIMESTAMP(3) THEN 'HET_HAN' ELSE d.trang_thai END)=?) GROUP BY d.ma_don_dat_phong ORDER BY d.tao_luc DESC,d.ma_don_dat_phong DESC LIMIT ? OFFSET ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maTaiKhoan);
            p.setString(2, trangThai);
            p.setString(3, trangThai);
            p.setInt(4, kichThuoc);
            p.setInt(5, boQua);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> demDonCuaKhach(Connection c, long maTaiKhoan, String trangThai)
            throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT COUNT(*) AS tong FROM don_dat_phong WHERE ma_tai_khoan_khach_hang=? AND (? IS NULL OR (CASE WHEN trang_thai='CHO_COC' AND han_giu_cho<=CURRENT_TIMESTAMP(3) THEN 'HET_HAN' ELSE trang_thai END)=?)")) {
            p.setLong(1, maTaiKhoan);
            p.setString(2, trangThai);
            p.setString(3, trangThai);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> thongKeLichSu(Connection c, long maTaiKhoan) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT d.ma_don_dat_phong) AS tong_so_lan_luu_tru,COALESCE(SUM(h.tien_phong),0) AS tong_chi_tieu FROM don_dat_phong d JOIN hoa_don h ON h.ma_don_dat_phong=d.ma_don_dat_phong AND h.trang_thai='DA_THANH_TOAN' WHERE d.ma_tai_khoan_khach_hang=? AND d.trang_thai='HOAN_TAT'";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maTaiKhoan);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> chiTietDonCuaKhach(Connection c, long maTaiKhoan, long maDon) throws SQLException {
        String sql = "SELECT d.ma_don_dat_phong,d.ma_tra_cuu_don,CASE WHEN d.trang_thai='CHO_COC' AND d.han_giu_cho<=CURRENT_TIMESTAMP(3) THEN 'HET_HAN' ELSE d.trang_thai END AS trang_thai_don,d.tien_coc_da_nhan,d.tien_coc_yeu_cau,d.han_giu_cho,UNIX_TIMESTAMP(d.han_giu_cho)*1000 AS han_giu_cho_ms,(d.han_giu_cho>CURRENT_TIMESTAMP(3)) AS con_han,d.khach_bao_da_chuyen_luc,d.so_khach,CURRENT_TIMESTAMP(3) AS thoi_gian_may_chu,UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000 AS thoi_gian_may_chu_ms,x.ma_chi_tiet_dat_phong,x.ma_phong,p.so_phong,l.ten AS ten_loai_phong,x.ngay_nhan,x.ngay_tra,x.gia_ngay_thuong_luc_dat,x.gia_cuoi_tuan_luc_dat,x.tien_phong,x.trang_thai AS trang_thai_chi_tiet FROM don_dat_phong d JOIN chi_tiet_dat_phong x ON x.ma_don_dat_phong=d.ma_don_dat_phong JOIN phong p ON p.ma_phong=x.ma_phong JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE d.ma_tai_khoan_khach_hang=? AND d.ma_don_dat_phong=? ORDER BY x.ma_chi_tiet_dat_phong";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maTaiKhoan);
            p.setLong(2, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> cacDonHoanTat(Connection c, long maTaiKhoan, int kichThuoc, int boQua)
            throws SQLException {
        String sql = "SELECT d.ma_don_dat_phong,d.ma_tra_cuu_don,d.trang_thai,d.tao_luc,h.ma_hoa_don,h.so_hoa_don,h.tien_phong,h.tien_coc_duoc_tru,h.so_tien_con_phai_thu,h.trang_thai AS trang_thai_hoa_don,MIN(x.ngay_nhan) AS ngay_nhan,MAX(x.ngay_tra) AS ngay_tra, GROUP_CONCAT(DISTINCT p.so_phong ORDER BY p.so_phong SEPARATOR ', ') AS danh_sach_phong FROM don_dat_phong d JOIN hoa_don h ON h.ma_don_dat_phong=d.ma_don_dat_phong AND h.trang_thai='DA_THANH_TOAN' LEFT JOIN chi_tiet_dat_phong x ON x.ma_don_dat_phong=d.ma_don_dat_phong AND x.trang_thai='DA_TRA' LEFT JOIN phong p ON p.ma_phong=x.ma_phong WHERE d.ma_tai_khoan_khach_hang=? AND d.trang_thai='HOAN_TAT' GROUP BY d.ma_don_dat_phong ORDER BY d.tao_luc DESC LIMIT ? OFFSET ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maTaiKhoan);
            p.setInt(2, kichThuoc);
            p.setInt(3, boQua);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> hoaDonCuaKhach(Connection c, long maTaiKhoan, long maDon) throws SQLException {
        String sql = "SELECT h.*,d.ma_don_dat_phong,d.ma_tra_cuu_don,k.ho_ten,k.so_dien_thoai,k.thu_dien_tu FROM hoa_don h JOIN don_dat_phong d ON d.ma_don_dat_phong=h.ma_don_dat_phong JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang WHERE d.ma_tai_khoan_khach_hang=? AND d.ma_don_dat_phong=? AND d.trang_thai='HOAN_TAT' AND h.trang_thai='DA_THANH_TOAN'";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maTaiKhoan);
            p.setLong(2, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> phongTrongKiemTraDuLieu(Connection c, long maPhong) throws SQLException {
        String sql = "SELECT p.ma_phong,p.so_phong,p.tang,p.trang_thai,p.dang_hoat_dong,l.ten,l.suc_chua,l.gia_ngay_thuong,l.gia_cuoi_tuan FROM phong p JOIN loai_phong l ON l.ma_loai_phong=p.ma_loai_phong WHERE p.ma_phong=? AND p.dang_hoat_dong=TRUE AND l.dang_hoat_dong=TRUE";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maPhong);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> cacPhongCuaDon(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT ma_phong FROM chi_tiet_dat_phong WHERE ma_don_dat_phong=? ORDER BY ma_phong FOR UPDATE")) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> cacDongDon(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT d.*,(SELECT p.so_phong FROM phong p WHERE p.ma_phong=d.ma_phong) AS so_phong "
                        + "FROM chi_tiet_dat_phong d WHERE d.ma_don_dat_phong=? "
                        + "ORDER BY d.ma_phong,d.ma_chi_tiet_dat_phong FOR UPDATE")) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> donTheoChiTiet(Connection c, long maChiTiet) throws SQLException {
        try (PreparedStatement p = c
                .prepareStatement("SELECT ma_don_dat_phong FROM chi_tiet_dat_phong WHERE ma_chi_tiet_dat_phong=?")) {
            p.setLong(1, maChiTiet);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> chiTietKhoa(Connection c, long maChiTiet) throws SQLException {
        try (PreparedStatement p = c
                .prepareStatement("SELECT * FROM chi_tiet_dat_phong WHERE ma_chi_tiet_dat_phong=? FOR UPDATE")) {
            p.setLong(1, maChiTiet);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> thongTinKhachTheoDon(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT k.ma_khach_hang,k.so_giay_to FROM don_dat_phong d JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang WHERE d.ma_don_dat_phong=?")) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public int nhanPhong(Connection c, long maChiTiet) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE chi_tiet_dat_phong SET trang_thai='DANG_O',thoi_diem_nhan_thuc_te=NOW() WHERE ma_chi_tiet_dat_phong=? AND trang_thai='DA_DAT'")) {
            p.setLong(1, maChiTiet);
            return p.executeUpdate();
        }
    }

    public int doiTrangThaiPhong(Connection c, long maPhong, String trangThai) throws SQLException {
        try (PreparedStatement p = c
                .prepareStatement("UPDATE phong SET trang_thai=? WHERE ma_phong=? AND dang_hoat_dong=TRUE")) {
            p.setString(1, trangThai);
            p.setLong(2, maPhong);
            return p.executeUpdate();
        }
    }

    /** Chỉ giải phóng phòng nếu trạng thái vận hành hiện tại vẫn là đã đặt. */
    public int giaiPhongPhongDat(Connection c, long maPhong) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE phong SET trang_thai='TRONG' WHERE ma_phong=? AND dang_hoat_dong=TRUE AND trang_thai='DA_DAT'")) {
            p.setLong(1, maPhong);
            return p.executeUpdate();
        }
    }

    public int huyDong(Connection c, long maChiTiet, String lyDo) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE chi_tiet_dat_phong SET trang_thai='DA_HUY',huy_luc=NOW(),ly_do_huy=? WHERE ma_chi_tiet_dat_phong=? AND trang_thai='DA_DAT'")) {
            p.setString(1, lyDo);
            p.setLong(2, maChiTiet);
            return p.executeUpdate();
        }
    }

    public int huyDon(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "UPDATE don_dat_phong SET trang_thai='DA_HUY' WHERE ma_don_dat_phong=? AND trang_thai='DA_DAT'")) {
            p.setLong(1, maDon);
            return p.executeUpdate();
        }
    }

    public List<Map<String, Object>> chinhSach(Connection c, int soNgay) throws SQLException {
        String sql = "SELECT phan_tram_hoan FROM chinh_sach_hoan_huy WHERE dang_hoat_dong=TRUE AND so_ngay_truoc_toi_thieu<=? AND (so_ngay_truoc_toi_da IS NULL OR so_ngay_truoc_toi_da>=?) ORDER BY so_ngay_truoc_toi_thieu DESC LIMIT 1";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, soNgay);
            p.setInt(2, soNgay);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> hoaDonLichSu(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT h.*,d.ma_tra_cuu_don,k.ho_ten,k.so_dien_thoai,k.thu_dien_tu FROM hoa_don h JOIN don_dat_phong d ON d.ma_don_dat_phong=h.ma_don_dat_phong JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang WHERE d.ma_don_dat_phong=? AND d.trang_thai='HOAN_TAT' AND h.trang_thai='DA_THANH_TOAN'")) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> thanhToanHoaDon(Connection c, long maHoaDon, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT loai_thanh_toan,phuong_thuc,so_tien,tao_luc,ghi_chu FROM thanh_toan WHERE (ma_hoa_don=? AND loai_thanh_toan='THANH_TOAN') OR (ma_don_dat_phong=? AND loai_thanh_toan='TIEN_COC') ORDER BY tao_luc,ma_thanh_toan")) {
            p.setLong(1, maHoaDon);
            p.setLong(2, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> tongCocDaNhan(Connection c, long maDon) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT GREATEST(d.tien_coc_da_nhan-COALESCE(SUM(CASE WHEN t.trang_thai_hoan_coc='DA_XAC_NHAN' THEN COALESCE(t.so_tien_thuc_hoan,t.so_tien) ELSE 0 END),0),0) AS tien_coc_con_lai FROM don_dat_phong d LEFT JOIN thanh_toan t ON t.ma_don_dat_phong=d.ma_don_dat_phong AND t.loai_thanh_toan='HOAN_COC' WHERE d.ma_don_dat_phong=? GROUP BY d.ma_don_dat_phong")) {
            p.setLong(1, maDon);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public int themYeuCauHoanCoc(Connection c, long maDon, java.math.BigDecimal soTien, String ghiChu)
            throws SQLException {
        String sql = "INSERT INTO thanh_toan(ma_don_dat_phong,loai_thanh_toan,phuong_thuc,so_tien,ghi_chu,trang_thai_hoan_coc) VALUES(?,'HOAN_COC','CHUYEN_KHOAN',?,?, 'CHO_XU_LY')";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maDon);
            p.setBigDecimal(2, soTien);
            p.setString(3, ghiChu);
            return p.executeUpdate();
        }
    }

    public List<Map<String, Object>> yeuCauHoanCoc(Connection c, long maThanhToan) throws SQLException {
        String sql = "SELECT t.*,d.ma_tra_cuu_don,k.ho_ten FROM thanh_toan t JOIN don_dat_phong d ON d.ma_don_dat_phong=t.ma_don_dat_phong JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang WHERE t.ma_thanh_toan=? AND t.loai_thanh_toan='HOAN_COC' AND t.trang_thai_hoan_coc='CHO_XU_LY' FOR UPDATE";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, maThanhToan);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public List<Map<String, Object>> danhSachYeuCauHoanCoc(Connection c, String trangThai, int kichThuoc, int boQua)
            throws SQLException {
        String sql = "SELECT t.ma_thanh_toan,t.ma_don_dat_phong,t.so_tien,t.trang_thai_hoan_coc,t.ghi_chu,t.tao_luc,d.ma_tra_cuu_don,k.ho_ten FROM thanh_toan t JOIN don_dat_phong d ON d.ma_don_dat_phong=t.ma_don_dat_phong JOIN ho_so_khach_hang k ON k.ma_khach_hang=d.ma_khach_hang WHERE t.loai_thanh_toan='HOAN_COC' AND (? IS NULL OR t.trang_thai_hoan_coc=?) ORDER BY t.tao_luc DESC,t.ma_thanh_toan DESC LIMIT ? OFFSET ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, trangThai);
            p.setString(2, trangThai);
            p.setInt(3, kichThuoc);
            p.setInt(4, boQua);
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public int xacNhanHoanCoc(Connection c, long maThanhToan, java.math.BigDecimal soTien, String phuongThuc,
            long maQuanLy) throws SQLException {
        String sql = "UPDATE thanh_toan SET so_tien_thuc_hoan=?,phuong_thuc_hoan=?,ma_quan_ly_ghi_nhan=?,trang_thai_hoan_coc='DA_XAC_NHAN' WHERE ma_thanh_toan=? AND loai_thanh_toan='HOAN_COC' AND trang_thai_hoan_coc='CHO_XU_LY'";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setBigDecimal(1, soTien);
            p.setString(2, phuongThuc);
            p.setLong(3, maQuanLy);
            p.setLong(4, maThanhToan);
            return p.executeUpdate();
        }
    }
}
