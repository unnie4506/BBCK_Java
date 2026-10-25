package vn.edu.quanlyhomestay.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class TienIchPhien {
    public static final String MA_NGUOI_DUNG = "phien.maNguoiDung";
    public static final String VAI_TRO = "phien.vaiTro";
    public static final String TEN_HIEN_THI = "phien.tenHienThi";
    public static final String VAI_TRO_QUAN_LY = "QUAN_LY";
    public static final String VAI_TRO_KHACH_HANG = "KHACH_HANG";
    public static final int THOI_GIAN_KHONG_HOAT_DONG_GIAY = 60 * 60;

    private TienIchPhien() {
    }

    // Phiên hết hạn sau 1 giờ không hoạt động.
    public static HttpSession taoPhienDaXacThuc(HttpServletRequest yeuCau, long maNguoiDung, String vaiTro,
            String tenHienThi) {
        HttpSession phienCu = yeuCau.getSession(false);
        if (phienCu != null) {
            phienCu.invalidate();
        }

        HttpSession phien = yeuCau.getSession(true);
        phien.setMaxInactiveInterval(THOI_GIAN_KHONG_HOAT_DONG_GIAY);
        phien.setAttribute(MA_NGUOI_DUNG, maNguoiDung);
        phien.setAttribute(VAI_TRO, vaiTro);
        phien.setAttribute(TEN_HIEN_THI, tenHienThi);
        return phien;
    }

    public static Long getMaNguoiDung(HttpServletRequest yeuCau) {
        HttpSession phien = yeuCau.getSession(false);
        if (phien == null) {
            return null;
        }
        Object maNguoiDung = phien.getAttribute(MA_NGUOI_DUNG);
        return maNguoiDung instanceof Number ? ((Number) maNguoiDung).longValue() : null;
    }

    public static String getVaiTro(HttpServletRequest yeuCau) {
        HttpSession phien = yeuCau.getSession(false);
        return phien == null ? null : (String) phien.getAttribute(VAI_TRO);
    }

    public static String getTenHienThi(HttpServletRequest yeuCau) {
        HttpSession phien = yeuCau.getSession(false);
        return phien == null ? null : (String) phien.getAttribute(TEN_HIEN_THI);
    }

    public static void huyPhien(HttpServletRequest yeuCau) {
        HttpSession phien = yeuCau.getSession(false);
        if (phien != null) {
            phien.invalidate();
        }
    }
}
