package vn.edu.quanlyhomestay.util;

import org.mindrot.jbcrypt.BCrypt;

public final class TienIchMatKhau {
    private TienIchMatKhau() {
    }

    public static String bam(String matKhau) {
        return BCrypt.hashpw(matKhau, BCrypt.gensalt(10));
    }

    public static boolean doiChieuMatKhau(String matKhau, String matKhauDaBam) {
        if (matKhau == null || matKhauDaBam == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(matKhau, matKhauDaBam);
        } catch (IllegalArgumentException ngoaiLe) {
            return false;
        }
    }

    public static boolean laMatKhauHopLe(String matKhau) {
        return matKhau != null && matKhau.matches("(?s)(?=.*\\p{L})(?=.*\\d).{8,}");
    }
}
