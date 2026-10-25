package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.TaiKhoanQuanLyDAO;
import vn.edu.quanlyhomestay.dao.NhatKyThaoTacDAO;
import vn.edu.quanlyhomestay.dao.KhachHangDAO;
import vn.edu.quanlyhomestay.dao.LanDangNhapDAO;
import vn.edu.quanlyhomestay.dao.LanDangNhapDAO.TrangThaiKhoaTam;
import vn.edu.quanlyhomestay.model.TaiKhoanQuanLy;
import vn.edu.quanlyhomestay.model.NguoiDungDaXacThuc;
import vn.edu.quanlyhomestay.model.KhachHang;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import vn.edu.quanlyhomestay.util.TienIchMatKhau;
import vn.edu.quanlyhomestay.util.TienIchPhien;
import vn.edu.quanlyhomestay.util.TienIchKiemTraDuLieu;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XacThucService {
    private static final Logger NHAT_KY = Logger.getLogger(XacThucService.class.getName());
    private static final int SO_LAN_DANG_NHAP_SAI_TOI_DA = 5;
    private static final int SO_PHUT_CUA_SO_DANG_NHAP = 10;
    private static final int SO_PHUT_KHOA_TAM = 15;
    private static final String THONG_BAO_DANG_NHAP_SAI = "Sai tài khoản hoặc mật khẩu";
    private static final String THONG_BAO_TAI_KHOAN_KHOA_TAM = "Tài khoản đang bị khóa tạm thời. Vui lòng thử lại sau.";
    private static final String MAT_KHAU_BCRYPT_GIA = "$2a$10$ObVCwOxP8OhkXqE95cB6j.H5YdiFR623UIh6I4x5R7/uIDoKsOSl.";

    private final TaiKhoanQuanLyDAO taiKhoanQuanLyDAO = new TaiKhoanQuanLyDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final LanDangNhapDAO lanDangNhapDAO = new LanDangNhapDAO();
    private final NhatKyThaoTacDAO nhatKyThaoTacDAO = new NhatKyThaoTacDAO();

    public NguoiDungDaXacThuc dangNhapQuanLy(String tenDangNhap, String matKhau, String diaChiIp) throws LoiXacThuc {
        String tenDangNhapChuanHoa = TienIchKiemTraDuLieu.lamSach(tenDangNhap);
        kiemTraThongTinDangNhap(tenDangNhapChuanHoa, matKhau);
        return xacThuc(true, tenDangNhapChuanHoa.toLowerCase(Locale.ROOT), matKhau, diaChiIp);
    }

    public NguoiDungDaXacThuc dangNhapKhachHang(String thuDienTu, String matKhau, String diaChiIp) throws LoiXacThuc {
        String thuDienTuChuanHoa = TienIchKiemTraDuLieu.lamSach(thuDienTu);
        if (thuDienTuChuanHoa == null || thuDienTuChuanHoa.isEmpty() || matKhau == null || matKhau.isEmpty()) {
            throw new LoiXacThuc(400, "Vui lòng nhập email và mật khẩu.");
        }
        if (!TienIchKiemTraDuLieu.laThuDienTu(thuDienTuChuanHoa)) {
            throw new LoiXacThuc(400, "Email không đúng định dạng.");
        }
        return xacThuc(false, thuDienTuChuanHoa.toLowerCase(Locale.ROOT), matKhau, diaChiIp);
    }

    private NguoiDungDaXacThuc xacThuc(boolean dangNhapQuanLy, String dinhDanh, String matKhau, String diaChiIp)
            throws LoiXacThuc {
        String loaiTaiKhoan = dangNhapQuanLy ? "QUAN_LY" : "KHACH_HANG";
        LocalDateTime now = LocalDateTime.now();
        NguoiDungDaXacThuc nguoiDungDaXacThuc = null;
        LoiXacThuc loiDangNhap = null;

        try (Connection ketNoi = KetNoiCoSoDuLieu.moKetNoi()) {
            // Tuần tự hóa các lần thử cùng định danh, kể cả khi tài khoản chưa tồn tại.
            boolean daKhoaDinhDanh = lanDangNhapDAO.khoaDinhDanhDangNhap(ketNoi, loaiTaiKhoan, dinhDanh);
            if (!daKhoaDinhDanh) {
                throw new LoiXacThuc(503, "Hệ thống đang bận. Vui lòng thử lại.");
            }
            try {
                ketNoi.setAutoCommit(false);
                try {
                    TaiKhoanQuanLy quanLy = dangNhapQuanLy
                            ? taiKhoanQuanLyDAO.timTheoTenDangNhapKhoaDong(ketNoi, dinhDanh)
                            : null;
                    KhachHang khachHang = dangNhapQuanLy ? null
                            : khachHangDAO.timTheoThuDienTuKhoaDong(ketNoi, dinhDanh);

                    Long maQuanLy = quanLy == null ? null : quanLy.getMaQuanLy();
                    Long maTaiKhoanKhachHang = khachHang == null ? null : khachHang.getMaTaiKhoanKhachHang();
                    LocalDateTime thoiDiemKhoaTaiKhoan = dangNhapQuanLy
                            ? (quanLy == null ? null : quanLy.getThoiDiemMoKhoa())
                            : (khachHang == null ? null : khachHang.getThoiDiemMoKhoa());

                    TrangThaiKhoaTam throttleState = lanDangNhapDAO.layTrangThaiKhoaTam(ketNoi, loaiTaiKhoan, dinhDanh,
                            now.minusMinutes(SO_PHUT_CUA_SO_DANG_NHAP));
                    LocalDateTime thoiDiemKhoaTheoDoi = layThoiDiemMoKhoaTheoTheoDoi(throttleState);

                    String matKhauDaBam = dangNhapQuanLy ? (quanLy == null ? null : quanLy.getMatKhauDaBam())
                            : (khachHang == null ? null : khachHang.getMatKhauDaBam());
                    boolean dangHoatDong = dangNhapQuanLy ? quanLy != null && quanLy.isDangHoatDong()
                            : khachHang != null && khachHang.isDangHoatDong();

                    // BR-02 (Khách), BR-08 (Admin): luôn thực hiện BCrypt kể cả khi không tồn tại tài khoản hoặc đang bị khóa.
                    String matKhauDeSoSanh = matKhauDaBam == null ? MAT_KHAU_BCRYPT_GIA : matKhauDaBam;
                    boolean matKhauKhop = TienIchMatKhau.doiChieuMatKhau(matKhau, matKhauDeSoSanh);

                    // BR-02 (Khách), BR-08 (Admin): khóa tài khoản 15 phút sau 5 lần sai trong 10 phút.
                    if (laSauThoiDiemHienTai(thoiDiemKhoaTaiKhoan, now)
                            || laSauThoiDiemHienTai(thoiDiemKhoaTheoDoi, now)) {
                        ketNoi.commit();
                        LocalDateTime moKhoaLuc = thoiDiemKhoaTaiKhoan;
                        if (thoiDiemKhoaTheoDoi != null
                                && (moKhoaLuc == null || thoiDiemKhoaTheoDoi.isAfter(moKhoaLuc)))
                            moKhoaLuc = thoiDiemKhoaTheoDoi;
                        loiDangNhap = new LoiXacThuc(423, THONG_BAO_TAI_KHOAN_KHOA_TAM, soGiayConLai(now, moKhoaLuc));
                    } else {
                        // Tài khoản khách hoặc hồ sơ đã ngừng hoạt động không được đăng nhập.
                        if (matKhauDaBam == null || !dangHoatDong || !matKhauKhop) {
                            lanDangNhapDAO.ghiNhanLanDangNhap(ketNoi, loaiTaiKhoan, maQuanLy, maTaiKhoanKhachHang,
                                    dinhDanh, false, diaChiIp, now);
                            TrangThaiKhoaTam trangThaiMoi = lanDangNhapDAO.layTrangThaiKhoaTam(ketNoi, loaiTaiKhoan,
                                    dinhDanh, now.minusMinutes(SO_PHUT_CUA_SO_DANG_NHAP));
                            boolean vuaBiKhoa = trangThaiMoi.getSoLanThatBai() >= SO_LAN_DANG_NHAP_SAI_TOI_DA;
                            if (vuaBiKhoa) {
                                LocalDateTime thoiDiemMoKhoa = layThoiDiemMoKhoaTheoTheoDoi(trangThaiMoi);
                                if (dangNhapQuanLy && quanLy != null) {
                                    taiKhoanQuanLyDAO.capNhatThoiDiemMoKhoa(ketNoi, quanLy.getMaQuanLy(),
                                            thoiDiemMoKhoa);
                                } else if (!dangNhapQuanLy && khachHang != null) {
                                    khachHangDAO.capNhatThoiDiemMoKhoa(ketNoi, khachHang.getMaTaiKhoanKhachHang(),
                                            thoiDiemMoKhoa);
                                }
                            }

                            // BR-02 (Khách), BR-08 (Admin): giữ thông tin sai đăng nhập và IP để áp dụng giới hạn thử lại.
                            nhatKyThaoTacDAO.ghiNhatKy(ketNoi, loaiTaiKhoan, maQuanLy, maTaiKhoanKhachHang,
                                    dangNhapQuanLy ? "QUAN_LY_DANG_NHAP_THAT_BAI" : "KHACH_HANG_DANG_NHAP_THAT_BAI",
                                    "Đăng nhập thất bại.", now);
                            if (vuaBiKhoa) {
                                nhatKyThaoTacDAO.ghiNhatKy(ketNoi, loaiTaiKhoan, maQuanLy, maTaiKhoanKhachHang,
                                        dangNhapQuanLy ? "QUAN_LY_BI_KHOA_TAM" : "KHACH_HANG_BI_KHOA_TAM",
                                        "Tài khoản bị khóa tạm sau 5 lần đăng nhập sai.", now);
                            }
                            loiDangNhap = new LoiXacThuc(401, THONG_BAO_DANG_NHAP_SAI);
                            if (vuaBiKhoa) {
                                loiDangNhap = new LoiXacThuc(423, THONG_BAO_TAI_KHOAN_KHOA_TAM,
                                        soGiayConLai(now, layThoiDiemMoKhoaTheoTheoDoi(trangThaiMoi)));
                            }
                        } else {
                            lanDangNhapDAO.ghiNhanLanDangNhap(ketNoi, loaiTaiKhoan, maQuanLy, maTaiKhoanKhachHang,
                                    dinhDanh, true, diaChiIp, now);
                            if (dangNhapQuanLy) {
                                taiKhoanQuanLyDAO.capNhatThoiDiemMoKhoa(ketNoi, quanLy.getMaQuanLy(), null);
                                nguoiDungDaXacThuc = new NguoiDungDaXacThuc(quanLy.getMaQuanLy(),
                                        TienIchPhien.VAI_TRO_QUAN_LY, quanLy.getHoTen(), null);
                            } else {
                                khachHangDAO.capNhatThoiDiemMoKhoa(ketNoi, khachHang.getMaTaiKhoanKhachHang(), null);
                                nguoiDungDaXacThuc = new NguoiDungDaXacThuc(khachHang.getMaTaiKhoanKhachHang(),
                                        TienIchPhien.VAI_TRO_KHACH_HANG, khachHang.getHoTen(),
                                        khachHang.getThuDienTu());
                            }
                            // BR-02 (Khách), BR-08 (Admin), BR-10 (Khách): ghi nhận đăng nhập thành công và reset bộ đếm logic.
                            nhatKyThaoTacDAO.ghiNhatKy(ketNoi, loaiTaiKhoan, maQuanLy, maTaiKhoanKhachHang,
                                    dangNhapQuanLy ? "QUAN_LY_DANG_NHAP_THANH_CONG" : "KHACH_HANG_DANG_NHAP_THANH_CONG",
                                    "Đăng nhập thành công.", now);
                        }
                        ketNoi.commit();
                    }
                } catch (SQLException | RuntimeException ngoaiLe) {
                    hoanTac(ketNoi);
                    throw ngoaiLe;
                }
            } finally {
                try {
                    lanDangNhapDAO.moKhoaDinhDanhDangNhap(ketNoi, loaiTaiKhoan, dinhDanh);
                } catch (SQLException ngoaiLe) {
                    NHAT_KY.log(Level.WARNING, "Không giải phóng được khóa đăng nhập.", ngoaiLe);
                }
            }
        } catch (SQLException | RuntimeException ngoaiLe) {
            NHAT_KY.log(Level.SEVERE, "Lỗi xử lý đăng nhập.", ngoaiLe);
            throw new LoiXacThuc(500, "Không thể xử lý đăng nhập lúc này.");
        }

        if (loiDangNhap != null) {
            throw loiDangNhap;
        }
        return nguoiDungDaXacThuc;
    }

    public long dangKyKhachHang(String hoTen, String soDienThoai, String thuDienTu, String matKhau) throws LoiXacThuc {
        String hoTenSach = TienIchKiemTraDuLieu.lamSach(hoTen);
        String soDienThoaiSach = TienIchKiemTraDuLieu.chuanHoaSoDienThoaiVietNam(soDienThoai);
        String thuDienTuSach = TienIchKiemTraDuLieu.lamSach(thuDienTu);
        // BR-01 (Khách): họ tên, số điện thoại, email bắt buộc; email đúng định dạng.
        kiemTraHoSo(hoTenSach, soDienThoaiSach, thuDienTuSach);
        // BR-01 (Khách), BR-08 (Khách): mật khẩu tối thiểu 8 ký tự, gồm chữ và số; lưu BCrypt.
        if (!TienIchMatKhau.laMatKhauHopLe(matKhau)) {
            throw new LoiXacThuc(400, "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ và số.");
        }

        String thuDienTuChuanHoa = thuDienTuSach.toLowerCase(Locale.ROOT);
        String matKhauDaBam = TienIchMatKhau.bam(matKhau);
        LocalDateTime now = LocalDateTime.now();
        try (Connection ketNoi = KetNoiCoSoDuLieu.moKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                // BR-01 (Khách): email tài khoản khách phải duy nhất; điện thoại không dùng để gắn tài khoản với hồ sơ cũ.
                if (khachHangDAO.thuDienTuDaTonTai(ketNoi, thuDienTuChuanHoa)) {
                    throw new LoiXacThuc(400, "Email đã được sử dụng.");
                }
                long maKhachHang = khachHangDAO.themHoSo(ketNoi, hoTenSach, soDienThoaiSach, thuDienTuChuanHoa);
                long maTaiKhoan = khachHangDAO.themTaiKhoan(ketNoi, maKhachHang, thuDienTuChuanHoa, matKhauDaBam);
                // BR-10 (Khách): ghi nhật ký khi đăng ký.
                nhatKyThaoTacDAO.ghiNhatKy(ketNoi, "KHACH_HANG", null, maTaiKhoan, "KHACH_HANG_DANG_KY",
                        "Khách hàng đăng ký tài khoản.", now);
                ketNoi.commit();
                return maTaiKhoan;
            } catch (LoiXacThuc ngoaiLe) {
                hoanTac(ketNoi);
                throw ngoaiLe;
            } catch (SQLException | RuntimeException ngoaiLe) {
                hoanTac(ketNoi);
                if (ngoaiLe instanceof SQLException sqlException && "23000".equals(sqlException.getSQLState())) {
                    throw new LoiXacThuc(400, "Email đã được sử dụng.");
                }
                throw ngoaiLe;
            }
        } catch (SQLException | RuntimeException ngoaiLe) {
            NHAT_KY.log(Level.SEVERE, "Lỗi tạo tài khoản khách hàng.", ngoaiLe);
            throw new LoiXacThuc(500, "Không thể tạo tài khoản lúc này.");
        }
    }

    public KhachHang layKhachHang(long maTaiKhoan) throws LoiXacThuc {
        try (Connection ketNoi = KetNoiCoSoDuLieu.moKetNoi()) {
            KhachHang khachHang = khachHangDAO.timTheoMaTaiKhoan(ketNoi, maTaiKhoan);
            if (khachHang == null) {
                throw new LoiXacThuc(401, "Phiên đăng nhập không còn hợp lệ.");
            }
            return khachHang;
        } catch (SQLException | RuntimeException ngoaiLe) {
            NHAT_KY.log(Level.SEVERE, "Lỗi tải hồ sơ khách hàng.", ngoaiLe);
            throw new LoiXacThuc(500, "Không thể tải hồ sơ khách hàng lúc này.");
        }
    }

    public KhachHang capNhatKhachHang(long maTaiKhoan, String hoTen, String soDienThoai, String thuDienTu)
            throws LoiXacThuc {
        String hoTenSach = TienIchKiemTraDuLieu.lamSach(hoTen);
        String soDienThoaiSach = TienIchKiemTraDuLieu.chuanHoaSoDienThoaiVietNam(soDienThoai);
        if (hoTenSach == null || hoTenSach.isBlank() || hoTenSach.length() > 120) {
            throw new LoiXacThuc(400, "Họ tên là bắt buộc và tối đa 120 ký tự.");
        }
        if (soDienThoaiSach == null) {
            throw new LoiXacThuc(400, "Số điện thoại không đúng định dạng.");
        }
        LocalDateTime now = LocalDateTime.now();

        try (Connection ketNoi = KetNoiCoSoDuLieu.moKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                KhachHang current = khachHangDAO.timTheoMaTaiKhoan(ketNoi, maTaiKhoan);
                if (current == null) {
                    throw new LoiXacThuc(401, "Phiên đăng nhập không còn hợp lệ.");
                }
                // BR-01 (Khách), BR-10 (Khách): email là tên đăng nhập, chỉ đọc; bỏ qua giá trị gửi lên.
                khachHangDAO.capNhatHoSo(ketNoi, current.getMaKhachHang(), hoTenSach, soDienThoaiSach,
                        current.getThuDienTu());
                // BR-10 (Khách): ghi nhật ký cập nhật hồ sơ trong cùng transaction.
                nhatKyThaoTacDAO.ghiNhatKy(ketNoi, "KHACH_HANG", null, maTaiKhoan, "KHACH_HANG_CAP_NHAT_HO_SO",
                        "Khách hàng cập nhật hồ sơ cá nhân.", now);
                ketNoi.commit();
            } catch (LoiXacThuc ngoaiLe) {
                hoanTac(ketNoi);
                throw ngoaiLe;
            } catch (SQLException | RuntimeException ngoaiLe) {
                hoanTac(ketNoi);
                if (ngoaiLe instanceof SQLException sqlException && "23000".equals(sqlException.getSQLState())) {
                    throw new LoiXacThuc(400, "Email đã được sử dụng.");
                }
                throw ngoaiLe;
            }
        } catch (SQLException | RuntimeException ngoaiLe) {
            NHAT_KY.log(Level.SEVERE, "Lỗi cập nhật hồ sơ khách hàng.", ngoaiLe);
            throw new LoiXacThuc(500, "Không thể cập nhật hồ sơ lúc này.");
        }
        return layKhachHang(maTaiKhoan);
    }

    public void ghiNhanDangXuat(String vaiTro, long maNguoiDung) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        try (Connection ketNoi = KetNoiCoSoDuLieu.moKetNoi()) {
            if (TienIchPhien.VAI_TRO_QUAN_LY.equals(vaiTro)) {
                // BR-10 (Khách): ghi nhật ký đăng xuất của khách.
                nhatKyThaoTacDAO.ghiNhatKy(ketNoi, "QUAN_LY", maNguoiDung, null, "QUAN_LY_LOGOUT", "Quản lý đăng xuất.",
                        now);
            } else {
                nhatKyThaoTacDAO.ghiNhatKy(ketNoi, "KHACH_HANG", null, maNguoiDung, "KHACH_HANG_DANG_XUAT",
                        "Khách hàng đăng xuất.", now);
            }
        }
    }

    private void kiemTraThongTinDangNhap(String tenDangNhap, String matKhau) throws LoiXacThuc {
        if (tenDangNhap == null || tenDangNhap.isEmpty() || matKhau == null || matKhau.isEmpty()) {
            throw new LoiXacThuc(400, "Vui lòng nhập tên đăng nhập và mật khẩu.");
        }
        if (tenDangNhap.length() > 50) {
            throw new LoiXacThuc(400, "Tên đăng nhập không đúng định dạng.");
        }
    }

    private void kiemTraHoSo(String hoTen, String soDienThoai, String thuDienTu) throws LoiXacThuc {
        if (hoTen == null || hoTen.isEmpty() || hoTen.length() > 120) {
            throw new LoiXacThuc(400, "Họ tên là bắt buộc và tối đa 120 ký tự.");
        }
        if (TienIchKiemTraDuLieu.chuanHoaSoDienThoaiVietNam(soDienThoai) == null) {
            throw new LoiXacThuc(400, "Số điện thoại không đúng định dạng.");
        }
        if (!TienIchKiemTraDuLieu.laThuDienTu(thuDienTu)) {
            throw new LoiXacThuc(400, "Email không đúng định dạng.");
        }
    }

    private long soGiayConLai(LocalDateTime hienTai, LocalDateTime thoiDiemMoKhoa) {
        if (thoiDiemMoKhoa == null || !thoiDiemMoKhoa.isAfter(hienTai))
            return 0;
        return Math.max(1, Duration.between(hienTai, thoiDiemMoKhoa).getSeconds());
    }

    private LocalDateTime layThoiDiemMoKhoaTheoTheoDoi(TrangThaiKhoaTam trangThaiKhoaTam) {
        if (trangThaiKhoaTam.getSoLanThatBai() < SO_LAN_DANG_NHAP_SAI_TOI_DA
                || trangThaiKhoaTam.getLanThatBaiGanNhat() == null) {
            return null;
        }
        return trangThaiKhoaTam.getLanThatBaiGanNhat().plusMinutes(SO_PHUT_KHOA_TAM);
    }

    private boolean laSauThoiDiemHienTai(LocalDateTime giaTri, LocalDateTime thoiDiemHienTai) {
        return giaTri != null && giaTri.isAfter(thoiDiemHienTai);
    }

    private void hoanTac(Connection ketNoi) {
        try {
            ketNoi.rollback();
        } catch (SQLException ignored) {
            // Giữ lỗi ban đầu; connection sẽ được đóng bởi try-with-resources.
        }
    }
}
