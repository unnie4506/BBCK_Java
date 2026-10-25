import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { congKhaiApi } from "../services/congKhaiApi.js";
const KHOA = "gioPhongTam";
const NguCanh = createContext(null);
function docGio() {
  try {
    return (
      JSON.parse(sessionStorage.getItem(KHOA)) || {
        ngayNhan: "",
        ngayTra: "",
        soKhach: 1,
        maLoaiPhong: "",
        danhSachMaPhong: [],
      }
    );
  } catch {
    return {
      ngayNhan: "",
      ngayTra: "",
      soKhach: 1,
      maLoaiPhong: "",
      danhSachMaPhong: [],
    };
  }
}
export function GioPhongProvider({ children }) {
  const [gio, setGio] = useState(docGio);
  const [phong, setPhong] = useState([]);
  useEffect(() => {
    sessionStorage.setItem(
      KHOA,
      JSON.stringify({
        ngayNhan: gio.ngayNhan,
        ngayTra: gio.ngayTra,
        soKhach: gio.soKhach,
        maLoaiPhong: gio.maLoaiPhong,
        danhSachMaPhong: gio.danhSachMaPhong,
      }),
    );
  }, [gio]);
  useEffect(() => {
    if (!gio.ngayNhan || !gio.ngayTra) {
      setPhong([]);
      return;
    }
    congKhaiApi
      .phongTrong({
        ngayNhan: gio.ngayNhan,
        ngayTra: gio.ngayTra,
        soKhach: gio.soKhach,
      })
      .then((r) => setPhong(r.data.duLieu || []))
      .catch(() => setPhong([]));
  }, [gio.ngayNhan, gio.ngayTra, gio.soKhach]);
  const capNhatTimKiem = (duLieu) =>
    setGio((g) => ({
      ...g,
      ...duLieu,
      danhSachMaPhong:
        duLieu.ngayNhan !== g.ngayNhan || duLieu.ngayTra !== g.ngayTra
          ? []
          : g.danhSachMaPhong,
    }));
  const chonPhong = (maPhong) =>
    setGio((g) => ({
      ...g,
      danhSachMaPhong: g.danhSachMaPhong.includes(maPhong)
        ? g.danhSachMaPhong.filter((x) => x !== maPhong)
        : [...g.danhSachMaPhong, maPhong],
    }));
  const xoaPhong = (maPhong) =>
    setGio((g) => ({
      ...g,
      danhSachMaPhong: g.danhSachMaPhong.filter((x) => x !== maPhong),
    }));
  const phongDaChon = phong.filter((p) =>
    gio.danhSachMaPhong.includes(p.maPhong),
  );
  const giaTri = useMemo(
    () => ({
      gio,
      phong,
      phongDaChon,
      capNhatTimKiem,
      chonPhong,
      xoaPhong,
      xoaGio: () =>
        setGio({
          ngayNhan: "",
          ngayTra: "",
          soKhach: 1,
          maLoaiPhong: "",
          danhSachMaPhong: [],
        }),
    }),
    [gio, phong, phongDaChon],
  );
  return <NguCanh.Provider value={giaTri}>{children}</NguCanh.Provider>;
}
export function useGioPhong() {
  return useContext(NguCanh);
}
