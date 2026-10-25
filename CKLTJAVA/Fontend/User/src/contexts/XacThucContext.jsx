import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { datXuLyHetPhien } from "../services/axios.js";
import { xacThucApi } from "../services/xacThucApi.js";

const NguCanh = createContext(null);
const THOI_GIAN_KHONG_HOAT_DONG_MS = 60 * 60 * 1000;
export function XacThucProvider({ children }) {
  const [nguoiDung, setNguoiDung] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();
  const hetPhien = useCallback(() => {
    if (!nguoiDung) return;
    setNguoiDung(null);
    const duongDan = `${location.pathname}${location.search}`;
    navigate("/dang-nhap", {
      replace: true,
      state: {
        from: duongDan,
        thongBao: "Phiên đã hết hạn, vui lòng đăng nhập lại",
      },
    });
  }, [location.pathname, location.search, navigate, nguoiDung]);
  useEffect(() => {
    datXuLyHetPhien(hetPhien);
    return () => datXuLyHetPhien(null);
  }, [hetPhien]);
  useEffect(() => {
    if (!nguoiDung) return undefined;
    let henGio;
    const datLaiHenGio = () => {
      window.clearTimeout(henGio);
      henGio = window.setTimeout(hetPhien, THOI_GIAN_KHONG_HOAT_DONG_MS);
    };
    const suKienHoatDong = [
      "click",
      "keydown",
      "mousemove",
      "scroll",
      "touchstart",
    ];
    suKienHoatDong.forEach((suKien) =>
      window.addEventListener(suKien, datLaiHenGio, { passive: true }),
    );
    datLaiHenGio();
    return () => {
      window.clearTimeout(henGio);
      suKienHoatDong.forEach((suKien) =>
        window.removeEventListener(suKien, datLaiHenGio),
      );
    };
  }, [nguoiDung, hetPhien]);
  const taiNguoiDung = useCallback(async () => {
    try {
      const r = await xacThucApi.hoSo();
      setNguoiDung(r.data.duLieu);
    } catch {
      setNguoiDung(null);
    } finally {
      setDangTai(false);
    }
  }, []);
  useEffect(() => {
    taiNguoiDung();
  }, [taiNguoiDung]);
  const dangNhap = async (duLieu) => {
    const r = await xacThucApi.dangNhap(duLieu);
    setNguoiDung(r.data.duLieu);
    return r.data;
  };
  const dangXuat = async () => {
    try {
      await xacThucApi.dangXuat();
    } finally {
      setNguoiDung(null);
    }
  };
  const giaTri = useMemo(
    () => ({
      nguoiDung,
      dangTai,
      dangNhap,
      dangXuat,
      taiNguoiDung,
      setNguoiDung,
    }),
    [nguoiDung, dangTai, taiNguoiDung],
  );
  return <NguCanh.Provider value={giaTri}>{children}</NguCanh.Provider>;
}
export function useXacThuc() {
  return useContext(NguCanh);
}
