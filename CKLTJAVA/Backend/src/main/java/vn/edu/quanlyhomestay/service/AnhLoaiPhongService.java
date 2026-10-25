package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.AnhLoaiPhongDAO;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import vn.edu.quanlyhomestay.util.TepAnhUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public class AnhLoaiPhongService {
    private final AnhLoaiPhongDAO dao = new AnhLoaiPhongDAO();

    public List<Map<String, Object>> danhSach(long maLoai) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            return dao.timTheoLoai(c, maLoai);
        }
    }

    public List<Long> themNhieu(long maLoai, List<String> tenTep, List<byte[]> noiDung)
            throws SQLException, IOException {
        if (tenTep.isEmpty() || tenTep.size() != noiDung.size())
            throw new NghiepVuException(400, "Chọn ít nhất một ảnh hợp lệ.");
        List<String> tepDaLuu = new ArrayList<>();
        try {
            for (int i = 0; i < tenTep.size(); i++)
                tepDaLuu.add(TepAnhUtil.kiemTraVaLuu(tenTep.get(i), noiDung.get(i)));
            try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
                c.setAutoCommit(false);
                try {
                    try (PreparedStatement p = c.prepareStatement(
                            "SELECT ma_loai_phong FROM loai_phong WHERE ma_loai_phong=? FOR UPDATE")) {
                        p.setLong(1, maLoai);
                        try (ResultSet r = p.executeQuery()) {
                            if (!r.next()) throw new NghiepVuException(404, "Không tìm thấy loại phòng.");
                        }
                    }
                    List<Long> ids = new ArrayList<>();
                    for (String ten : tepDaLuu)
                        ids.add(dao.them(c, maLoai, "/api/cong-khai/anh-phong/" + ten));
                    c.commit();
                    return ids;
                } catch (Exception e) {
                    c.rollback();
                    throw e;
                }
            }
        } catch (Exception e) {
            for (String ten : tepDaLuu)
                try { Files.deleteIfExists(TepAnhUtil.thuMuc().resolve(ten)); } catch (Exception ignored) { }
            throw e;
        }
    }

    public void xoa(long maLoai, long maAnh) throws SQLException, IOException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                String duongDan = dao.voHieuHoa(c, maLoai, maAnh);
                if (duongDan == null) throw new NghiepVuException(404, "Không tìm thấy ảnh của loại phòng.");
                c.commit();
                Files.deleteIfExists(TepAnhUtil.duongDanAnToan(duongDan.substring(duongDan.lastIndexOf('/') + 1)));
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void datDaiDien(long maLoai, long maAnh) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                if (!dao.thuocLoai(c, maAnh, maLoai))
                    throw new NghiepVuException(404, "Không tìm thấy ảnh của loại phòng.");
                dao.datAnhDaiDien(c, maLoai, maAnh);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }
}
