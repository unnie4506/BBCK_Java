package vn.edu.quanlyhomestay.model;

import java.math.BigDecimal;

public class BacHoanHuy {
    private Integer soNgayToiThieu;
    private Integer soNgayToiDa;
    private BigDecimal tyLeHoan;

    public Integer getSoNgayToiThieu() {
        return soNgayToiThieu;
    }

    public void setSoNgayToiThieu(Integer v) {
        soNgayToiThieu = v;
    }

    public Integer getSoNgayToiDa() {
        return soNgayToiDa;
    }

    public void setSoNgayToiDa(Integer v) {
        soNgayToiDa = v;
    }

    public BigDecimal getTyLeHoan() {
        return tyLeHoan;
    }

    public void setTyLeHoan(BigDecimal v) {
        tyLeHoan = v;
    }
}
