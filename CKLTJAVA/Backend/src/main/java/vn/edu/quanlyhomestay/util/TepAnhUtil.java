package vn.edu.quanlyhomestay.util;

import vn.edu.quanlyhomestay.service.NghiepVuException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Properties;

public final class TepAnhUtil {
    public static final int TOI_DA_BYTE = 5 * 1024 * 1024;

    private TepAnhUtil() {
    }

    public static Path thuMuc() throws IOException {
        Properties p = new Properties();
        try (InputStream in = TepAnhUtil.class.getClassLoader()
                .getResourceAsStream("cau-hinh-co-so-du-lieu.properties")) {
            if (in == null)
                throw new IOException("Thiếu cấu hình.");
            p.load(in);
        }
        String s = p.getProperty("thu_muc_anh");
        if (s == null || s.isBlank())
            throw new IOException("Thiếu thu_muc_anh trong cấu hình.");
        Path thuMuc = Paths.get(s).toAbsolutePath().normalize();
        Files.createDirectories(thuMuc);
        return thuMuc;
    }

    public static String kiemTraVaLuu(String tenGoc, byte[] duLieu) throws IOException {
        if (duLieu.length > TOI_DA_BYTE)
            throw new NghiepVuException(413, "Mỗi ảnh tối đa 5 MB.");
        String moRong = "";
        int dau = tenGoc == null ? -1 : tenGoc.lastIndexOf('.');
        if (dau >= 0)
            moRong = tenGoc.substring(dau + 1).toLowerCase(Locale.ROOT);
        if (!Set.of("jpg", "jpeg", "png").contains(moRong))
            throw new NghiepVuException(415, "Chỉ nhận ảnh JPG, JPEG hoặc PNG.");
        // BR-11 (Admin): kiểm tra cả phần mở rộng, chữ ký byte và khả năng giải mã ảnh; không tin Content-Type do trình duyệt gửi.
        boolean laPng = duLieu.length >= 8 && (duLieu[0] & 255) == 0x89 && duLieu[1] == 'P' && duLieu[2] == 'N'
                && duLieu[3] == 'G' && duLieu[4] == 13 && duLieu[5] == 10 && duLieu[6] == 26 && duLieu[7] == 10;
        boolean laJpeg = duLieu.length >= 3 && (duLieu[0] & 255) == 0xff && (duLieu[1] & 255) == 0xd8
                && (duLieu[2] & 255) == 0xff;
        if ((moRong.equals("png") && !laPng) || (!moRong.equals("png") && !laJpeg))
            throw new NghiepVuException(415, "Phần mở rộng không khớp với định dạng ảnh thật.");
        BufferedImage anh;
        try (ByteArrayInputStream in = new ByteArrayInputStream(duLieu)) {
            anh = ImageIO.read(in);
        }
        if (anh == null || anh.getWidth() < 1 || anh.getHeight() < 1)
            throw new NghiepVuException(415, "Nội dung tệp không phải ảnh hợp lệ.");
        String loai = moRong.equals("png") ? "png" : "jpg";
        String ten = UUID.randomUUID() + "." + loai;
        Path dich = thuMuc().resolve(ten).normalize();
        if (!dich.getParent().equals(thuMuc()))
            throw new NghiepVuException(415, "Tên tệp không hợp lệ.");
        Files.write(dich, duLieu, StandardOpenOption.CREATE_NEW);
        return ten;
    }

    public static Path duongDanAnToan(String tenFile) throws IOException {
        if (tenFile == null || !tenFile.matches("[0-9a-fA-F-]{36}\\.(jpg|png)"))
            throw new FileNotFoundException();
        Path goc = thuMuc();
        Path f = goc.resolve(tenFile).normalize();
        if (!f.getParent().equals(goc) || !Files.isRegularFile(f))
            throw new FileNotFoundException();
        return f;
    }
}
