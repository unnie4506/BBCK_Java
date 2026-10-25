package vn.edu.quanlyhomestay.service;

import vn.edu.quanlyhomestay.dao.AnhPhongDAO;
import vn.edu.quanlyhomestay.util.KetNoiCoSoDuLieu;
import vn.edu.quanlyhomestay.util.TepAnhUtil;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class AnhPhongService {
    private final AnhPhongDAO dao = new AnhPhongDAO();

    public List<Long> themNhieu(long maPhong, List<String> tenTep, List<byte[]> noiDung)
            throws SQLException, IOException {
        if (tenTep.isEmpty() || tenTep.size() != noiDung.size())
            throw new NghiepVuException(400, "Chọn ít nhất một ảnh hợp lệ.");
        List<String> tepDaLuu = new ArrayList<>();
        try {
            // Kiểm tra và lưu ảnh vào thư mục tạm tên UUID; nếu bất kỳ ảnh sai, dọn hết tệp đã ghi.
            for (int i = 0; i < tenTep.size(); i++)
                tepDaLuu.add(TepAnhUtil.kiemTraVaLuu(tenTep.get(i), noiDung.get(i)));
            try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
                c.setAutoCommit(false);
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT ma_phong FROM phong WHERE ma_phong=? FOR UPDATE")) {
                    p.setLong(1, maPhong);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next())
                            throw new NghiepVuException(404, "Không tìm thấy phòng.");
                    }
                }
                List<Long> ids = new ArrayList<>();
                for (String ten : tepDaLuu)
                    ids.add(dao.them(c, maPhong, "/api/cong-khai/anh-phong/" + ten));
                c.commit();
                return ids;
            }
        } catch (Exception e) {
            for (String ten : tepDaLuu)
                try {
                    Files.deleteIfExists(TepAnhUtil.thuMuc().resolve(ten));
                } catch (Exception ignored) {
                }
            throw e;
        }
    }

    public void xoa(long maPhong, long maAnh) throws SQLException, IOException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                String ten = dao.voHieuHoa(c, maPhong, maAnh);
                if (ten == null)
                    throw new NghiepVuException(404, "Không tìm thấy ảnh của phòng.");
                c.commit();
                String tenFile = ten.substring(ten.lastIndexOf('/') + 1);
                Files.deleteIfExists(TepAnhUtil.duongDanAnToan(tenFile));
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void datDaiDien(long maPhong, long maAnh) throws SQLException {
        try (Connection c = KetNoiCoSoDuLieu.moKetNoi()) {
            c.setAutoCommit(false);
            try {
                if (!dao.thuocPhong(c, maAnh, maPhong))
                    throw new NghiepVuException(404, "Không tìm thấy ảnh của phòng.");
                dao.datAnhDaiDien(c, maPhong, maAnh);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }
}
