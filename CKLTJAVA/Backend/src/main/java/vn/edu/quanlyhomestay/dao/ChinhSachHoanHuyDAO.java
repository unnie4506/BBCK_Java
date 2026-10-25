package vn.edu.quanlyhomestay.dao;

import vn.edu.quanlyhomestay.model.BacHoanHuy;
import vn.edu.quanlyhomestay.util.DocBanGhiUtil;
import java.sql.*;
import java.util.*;

public class ChinhSachHoanHuyDAO {
    public List<Map<String, Object>> danhSachDangDung(Connection c) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT ma_chinh_sach_hoan_huy,ten,so_ngay_truoc_toi_thieu AS so_ngay_toi_thieu,so_ngay_truoc_toi_da AS so_ngay_toi_da,phan_tram_hoan AS ty_le_hoan FROM chinh_sach_hoan_huy WHERE dang_hoat_dong=TRUE ORDER BY so_ngay_truoc_toi_thieu")) {
            try (ResultSet r = p.executeQuery()) {
                return DocBanGhiUtil.docDanhSach(r);
            }
        }
    }

    public int voHieuHoaTatCa(Connection c) throws SQLException {
        try (PreparedStatement p = c
                .prepareStatement("UPDATE chinh_sach_hoan_huy SET dang_hoat_dong=FALSE WHERE dang_hoat_dong=TRUE")) {
            return p.executeUpdate();
        }
    }

    public void them(Connection c, String ten, BacHoanHuy bac, Long maQuanLy) throws SQLException {
        String sql = "INSERT INTO chinh_sach_hoan_huy(ten,so_ngay_truoc_toi_thieu,so_ngay_truoc_toi_da,phan_tram_hoan,dang_hoat_dong,ma_quan_ly_tao) VALUES(?,?,?,?,TRUE,?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, ten);
            p.setInt(2, bac.getSoNgayToiThieu());
            if (bac.getSoNgayToiDa() == null)
                p.setNull(3, Types.SMALLINT);
            else
                p.setInt(3, bac.getSoNgayToiDa());
            p.setBigDecimal(4, bac.getTyLeHoan());
            if (maQuanLy == null)
                p.setNull(5, Types.BIGINT);
            else
                p.setLong(5, maQuanLy);
            p.executeUpdate();
        }
    }
}
