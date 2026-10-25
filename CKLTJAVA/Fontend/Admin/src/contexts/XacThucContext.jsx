import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import {
  dangNhap as goiDangNhap,
  dangXuat as goiDangXuat,
  layHoSoPhien,
} from "../services/xacThucApi.js";
import { datXuLyHetPhien } from "../services/axios.js";

const NguCanhXacThuc = createContext(null);
const THOI_GIAN_KHONG_HOAT_DONG_MS = 60 * 60 * 1000;

export function XacThucProvider({ children, chuyenTrang }) {
  const [nguoiDung, datNguoiDung] = useState(null);
  const [dangKiemTra, datDangKiemTra] = useState(true);
  const xoaPhien = useCallback(
    (coThongBao = false) => {
      datNguoiDung(null);
      if (coThongBao) chuyenTrang("het-phien");
    },
    [chuyenTrang],
  );

  useEffect(() => {
    datXuLyHetPhien(() => xoaPhien(true));
    layHoSoPhien()
      .then(datNguoiDung)
      .catch(() => datNguoiDung(null))
      .finally(() => datDangKiemTra(false));
  }, [xoaPhien]);

  useEffect(() => {
    if (!nguoiDung) return undefined;
    let henGio;
    const batDauDem = () => {
      window.clearTimeout(henGio);
      henGio = window.setTimeout(
        () => xoaPhien(true),
        THOI_GIAN_KHONG_HOAT_DONG_MS,
      );
    };
    const hoatDong = ["click", "keydown", "mousemove", "scroll", "touchstart"];
    hoatDong.forEach((suKien) =>
      window.addEventListener(suKien, batDauDem, { passive: true }),
    );
    batDauDem();
    return () => {
      window.clearTimeout(henGio);
      hoatDong.forEach((suKien) =>
        window.removeEventListener(suKien, batDauDem),
      );
    };
  }, [nguoiDung, xoaPhien]);

  const dangNhap = async (ten, matKhau) => {
    const duLieu = await goiDangNhap(ten, matKhau);
    datNguoiDung(duLieu);
    return duLieu;
  };
  const dangXuat = async () => {
    try {
      await goiDangXuat();
    } finally {
      xoaPhien(false);
    }
  };
  const giaTri = useMemo(
    () => ({ nguoiDung, dangKiemTra, dangNhap, dangXuat }),
    [nguoiDung, dangKiemTra],
  );
  return (
    <NguCanhXacThuc.Provider value={giaTri}>{children}</NguCanhXacThuc.Provider>
  );
}

export function useXacThuc() {
  return useContext(NguCanhXacThuc);
}
