package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.CongKhaiDAO;
import vn.edu.quanlyhomestay.model.*;
import vn.edu.quanlyhomestay.model.dto.*;
import vn.edu.quanlyhomestay.util.*;
import vn.edu.quanlyhomestay.util.CauHinhDatCoc;
import java.math.BigDecimal;
import java.sql.Connection;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DatPhongTrucTuyenService {
    private final CongKhaiDAO dao = new CongKhaiDAO();
    private final PhongTrongService phongTrongService = new PhongTrongService();
    private final DonDatPhongService donDatPhong = new DonDatPhongService();
    private final HuyDonService huyDon = new HuyDonService();
    private final XacThucService xacThuc = new XacThucService();
    private final vn.edu.quanlyhomestay.dao.HoaDonDAO hoaDonDAO = new vn.edu.quanlyhomestay.dao.HoaDonDAO();

    public Map<String, Object> xemTruoc(DatPhongTrucTuyenYeuCau y) throws Exception {
        // Ngày hợp lệ, giá theo đêm, sức chứa và chỉ trả phòng công khai đang khả dụng.
        kiemTraYeuCau(y);
        List<Long> maPhong = maPhongDuyNhat(y);
        List<Map<String, Object>> phongTrong = phongTrongService.timPhongCongKhai(y.getNgayNhan(), y.getNgayTra(),
                null);
        Map<Long, Map<String, Object>> khaDung = new HashMap<>();
        for (Map<String, Object> p : phongTrong)
            khaDung.put(((Number) p.get("maPhong")).longValue(), p);
        List<Map<String, Object>> phong;
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            phong = dao.phongTheoMa(c, maPhong);
        }
        if (phong.size() != maPhong.size())
            throw new NghiepVuException(409, "PHONG_KHONG_KHA_DUNG", "Một hoặc nhiều phòng không còn hoạt động.");
        List<Map<String, Object>> biTrung = new ArrayList<>();
        for (Long ma : maPhong)
            if (!khaDung.containsKey(ma)) {
                Map<String, Object> x = new LinkedHashMap<>();
                x.put("maPhong", ma);
                for (Map<String, Object> meta : phong)
                    if (((Number) meta.get("maPhong")).longValue() == ma)
                        x.put("soPhong", meta.get("soPhong"));
                biTrung.add(x);
            }
        if (!biTrung.isEmpty())
            throw new NghiepVuException(409, "TRUNG_LICH",
                    "Một hoặc nhiều phòng không còn trống trong khoảng ngày đã chọn.",
                    Map.of("danhSachPhongBiTrung", biTrung));
        int tongSucChua = 0;
        BigDecimal tong = BigDecimal.ZERO;
        List<Map<String, Object>> dong = new ArrayList<>();
        for (Map<String, Object> p : phong) {
            long ma = ((Number) p.get("maPhong")).longValue();
            Map<String, Object> gia = khaDung.get(ma);
            int sucChua = ((Number) p.get("sucChua")).intValue();
            tongSucChua += sucChua;
            BigDecimal tien = (BigDecimal) gia.get("tienPhongDuKien");
            tong = tong.add(tien);
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("maPhong", ma);
            d.put("soPhong", p.get("soPhong"));
            d.put("tenLoaiPhong", p.get("tenLoaiPhong"));
            d.put("sucChua", sucChua);
            d.put("soDem", ChronoUnit.DAYS.between(y.getNgayNhan(), y.getNgayTra()));
            d.put("tienPhongDuKien", tien);
            d.put("giaNgayThuong", gia.get("giaNgayThuong"));
            d.put("giaCuoiTuan", gia.get("giaCuoiTuan"));
            dong.add(d);
        }
        // Ngày hợp lệ, sức chứa cộng từ các phòng; từng phòng nhỏ hơn số khách vẫn được chọn.
        if (tongSucChua < y.getSoKhach())
            throw new NghiepVuException(422, "SUC_CHUA_KHONG_DU", "Tổng sức chứa các phòng đã chọn chưa đủ số khách.");
        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("ngayNhan", y.getNgayNhan());
        kq.put("ngayTra", y.getNgayTra());
        kq.put("soKhach", y.getSoKhach());
        kq.put("tongSucChua", tongSucChua);
        kq.put("danhSachPhong", dong);
        kq.put("tongTienPhongDuKien", tong);
        BigDecimal cocDuKien = tong.multiply(CauHinhDatCoc.tyLeCoc()).divide(new BigDecimal("100"), 0,
                java.math.RoundingMode.HALF_UP);
        kq.put("tienCoc", cocDuKien);
        kq.put("tienCocYeuCau", cocDuKien);
        kq.put("tyLeCoc", CauHinhDatCoc.tyLeCoc());
        return kq;
    }

    public Map<String, Object> datPhong(long maTaiKhoan, DatPhongTrucTuyenYeuCau y, NguoiThaoTac actor)
            throws Exception {
        // BR-05/06/10 (Khách): đơn trực tuyến dùng ngày chung, cọc 0, ghi nhật ký và giữ chung luồng đặt lõi.
        Map<String, Object> xemTruoc = xemTruoc(y);
        KhachHang khach = xacThuc.layKhachHang(maTaiKhoan);
        DonDatPhongYeuCau yeuCau = new DonDatPhongYeuCau();
        yeuCau.setMaKhachHang(khach.getMaKhachHang());
        yeuCau.setGhiChu(y.getGhiChu());
        yeuCau.setTienCoc(BigDecimal.ZERO);
        List<ChiTietDatPhongYeuCau> ds = new ArrayList<>();
        for (Long ma : maPhongDuyNhat(y)) {
            ChiTietDatPhongYeuCau d = new ChiTietDatPhongYeuCau();
            d.setMaPhong(ma);
            d.setNgayNhan(y.getNgayNhan());
            d.setNgayTra(y.getNgayTra());
            ds.add(d);
        }
        yeuCau.setDanhSachPhong(ds);
        // BR-05 (Khách): giới hạn ba đơn được kiểm tra atomically trong DonDatPhongService cùng khóa hồ sơ.
        Map<String, Object> don = donDatPhong.taoDonChoCoc(yeuCau, actor, y.getSoKhach());
        don.put("tongSucChua", xemTruoc.get("tongSucChua"));
        return don;
    }

    public Map<String, Object> danhSachDon(long maTaiKhoan, String trangThai, int trang, int kichThuoc)
            throws Exception {
        kiemTraTrangThai(trangThai);
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            Map<String, Object> kq = new LinkedHashMap<>();
            List<Map<String, Object>> ds = dao.donCuaKhach(c, maTaiKhoan, trangThai, kichThuoc,
                    (trang - 1) * kichThuoc);
            kq.put("danhSach", ds);
            long tong = ((Number) dao.demDonCuaKhach(c, maTaiKhoan, trangThai).get(0).get("tong")).longValue();
            kq.put("trang", trang);
            kq.put("kichThuoc", kichThuoc);
            kq.put("tongSoBanGhi", tong);
            kq.put("tongSoTrang", (tong + kichThuoc - 1) / kichThuoc);
            return kq;
        }
    }

    public DonDatPhongKhachDto chiTietDon(long maTaiKhoan, long maDon) throws Exception {
        // BR-08 (Khách): mọi truy vấn chi tiết gắn với mã tài khoản lấy từ phiên; không có dữ liệu thì trả cùng 404.
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> ds = dao.chiTietDonCuaKhach(c, maTaiKhoan, maDon);
            if (ds.isEmpty())
                throw new NghiepVuException(404, "KHONG_TIM_THAY_DON", "Không tìm thấy đơn đặt phòng.");
            Map<String, Object> dau = ds.get(0);
            DonDatPhongKhachDto dto = new DonDatPhongKhachDto();
            dto.setMaDonDatPhong(maDon);
            dto.setMaTraCuuDon((String) dau.get("maTraCuuDon"));
            dto.setTrangThai((String) dau.get("trangThaiDon"));
            dto.setSoKhach(((Number) dau.get("soKhach")).intValue());
            dto.setTienCoc((BigDecimal) dau.get("tienCocDaNhan"));
            dto.setTienCocYeuCau((BigDecimal) dau.get("tienCocYeuCau"));
            dto.setHanGiuCho(dau.get("hanGiuCho"));
            dto.setThoiGianMayChuMs(((Number) dau.get("thoiGianMayChuMs")).longValue());
            if (dau.get("hanGiuChoMs") instanceof Number deadline) dto.setHanGiuChoMs(deadline.longValue());
            dto.setDaBaoDaChuyenKhoan(dau.get("khachBaoDaChuyenLuc") != null);
            dto.setNganHang(CauHinhDatCoc.nganHang());
            dto.setSoTaiKhoan(CauHinhDatCoc.soTaiKhoan());
            dto.setChuTaiKhoan(CauHinhDatCoc.chuTaiKhoan());
            dto.setNoiDungChuyenKhoan("HS" + maDon);
            BigDecimal tong = BigDecimal.ZERO;
            boolean coDat = false, duocHuy = true;
            List<Map<String, Object>> anToan = new ArrayList<>();
            for (Map<String, Object> d : ds) {
                String tt = (String) d.get("trangThaiChiTiet");
                if ("DA_DAT".equals(tt))
                    coDat = true;
                else if (!"DA_HUY".equals(tt))
                    duocHuy = false;
                if (!"DA_HUY".equals(tt))
                    tong = tong.add((BigDecimal) d.get("tienPhong"));
                Map<String, Object> x = new LinkedHashMap<>();
                for (String f : List.of("soPhong", "tenLoaiPhong", "ngayNhan", "ngayTra", "tienPhong",
                        "trangThaiChiTiet"))
                    x.put(f, d.get(f));
                x.put("soDem", ChronoUnit.DAYS.between(java.time.LocalDate.parse(String.valueOf(d.get("ngayNhan"))),
                        java.time.LocalDate.parse(String.valueOf(d.get("ngayTra")))));
                anToan.add(x);
            }
            dto.setTongTienPhong(tong);
            dto.setDanhSachPhong(anToan);
            dto.setDanhSachTienCoc(hoaDonDAO.tienCocCuaDon(c, maDon));
            // Khả năng hủy dựa trên trạng thái các phòng; trạng thái tổng hợp của đơn có thể chưa đồng bộ.
            dto.setChoPhepHuy(duocHuy && !"HET_HAN".equals(dto.getTrangThai())
                    && ("CHO_COC".equals(dto.getTrangThai()) || coDat));
            return dto;
        }
    }

    public Map<String, Object> xemTruocHuy(long maTaiKhoan, long maDon) throws Exception {
        return huyDon.xemTruocHuyDon(maTaiKhoan, maDon);
    }

    public Map<String, Object> huy(long maTaiKhoan, long maDon, String lyDo, NguoiThaoTac actor) throws Exception {
        return huyDon.huyDonKhach(maTaiKhoan, maDon, lyDo, actor);
    }

    public HoaDonKhachDto hoaDon(long maTaiKhoan, long maDon) throws Exception {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            List<Map<String, Object>> rows = dao.hoaDonCuaKhach(c, maTaiKhoan, maDon);
            if (rows.isEmpty())
                throw new NghiepVuException(404, "KHONG_TIM_THAY_HOA_DON", "Không tìm thấy hóa đơn.");
            List<Map<String, Object>> chiTiet = dao.chiTietDonCuaKhach(c, maTaiKhoan, maDon);
            List<Map<String, Object>> phongAnToan = new ArrayList<>();
            for (Map<String, Object> d : chiTiet) {
                Map<String, Object> x = new LinkedHashMap<>();
                for (String f : List.of("soPhong", "tenLoaiPhong", "ngayNhan", "ngayTra", "tienPhong"))
                    x.put(f, d.get(f));
                phongAnToan.add(x);
            }
            HoaDonKhachDto dto = new HoaDonKhachDto();
            dto.setHoaDon(rows.get(0));
            dto.setDanhSachPhong(phongAnToan);
            dto.setDanhSachThanhToan(dao.thanhToanHoaDon(c, ((Number) rows.get(0).get("maHoaDon")).longValue(), maDon));
            return dto;
        }
    }

    private void kiemTraYeuCau(DatPhongTrucTuyenYeuCau y) {
        if (y == null || y.getNgayNhan() == null || y.getNgayTra() == null || !y.getNgayTra().isAfter(y.getNgayNhan()))
            throw new NghiepVuException(422, "NGAY_KHONG_HOP_LE", "Ngày trả phải sau ngày nhận.");
        if (y.getNgayNhan().isBefore(ThoiGianUtil.homNay()))
            throw new NghiepVuException(422, "NGAY_NHAN_QUA_KHU", "Ngày nhận không được ở quá khứ.");
        if (ChronoUnit.DAYS.between(y.getNgayNhan(), y.getNgayTra()) > 30)
            throw new NghiepVuException(422, "VUOT_QUA_30_DEM", "Mỗi phòng được đặt tối đa 30 đêm.");
        if (y.getSoKhach() == null || y.getSoKhach() < 1 || y.getSoKhach() > 20)
            throw new NghiepVuException(422, "SO_KHACH_KHONG_HOP_LE", "Số khách phải là số nguyên từ 1 đến 20.");
        if (y.getDanhSachMaPhong() == null || y.getDanhSachMaPhong().isEmpty() || y.getDanhSachMaPhong().size() > 10)
            throw new NghiepVuException(422, "DANH_SACH_PHONG_KHONG_HOP_LE", "Chọn từ 1 đến 10 phòng.");
        if (y.getGhiChu() != null && y.getGhiChu().length() > 1000)
            throw new NghiepVuException(422, "GHI_CHU_QUA_DAI", "Ghi chú tối đa 1000 ký tự.");
    }

    private List<Long> maPhongDuyNhat(DatPhongTrucTuyenYeuCau y) {
        List<Long> ds = new ArrayList<>(new LinkedHashSet<>(y.getDanhSachMaPhong()));
        if (ds.size() != y.getDanhSachMaPhong().size() || ds.stream().anyMatch(m -> m == null || m <= 0))
            throw new NghiepVuException(422, "DANH_SACH_PHONG_KHONG_HOP_LE", "Mã phòng không hợp lệ hoặc bị lặp.");
        return ds;
    }

    private void kiemTraTrangThai(String t) {
        if (t != null && !t.isBlank() && !Set.of("CHO_COC", "HET_HAN", "DA_DAT", "DANG_O", "HOAN_TAT", "DA_HUY").contains(t))
            throw new NghiepVuException(400, "TRANG_THAI_KHONG_HOP_LE", "Trạng thái đơn không hợp lệ.");
    }
}
