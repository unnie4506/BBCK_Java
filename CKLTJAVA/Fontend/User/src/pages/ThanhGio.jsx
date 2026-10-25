import * as NenTang from "../NenTangGiaoDien.jsx";
const {
  useEffect,
  useMemo,
  useState,
  HashRouter,
  Link,
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
  useNavigate,
  useParams,
  useSearchParams,
  congKhaiApi,
  xacThucApi,
  donDatPhongApi,
  hoSoApi,
  lichSuApi,
  XacThucProvider,
  useXacThuc,
  GioPhongProvider,
  useGioPhong,
  goiApi,
  layDuLieu,
  layDanhSach,
  tienVnd,
  ngayHienThi,
  homNay,
} = NenTang;
import { AnhPhong } from "./AnhPhong.jsx";

export function ThanhGio() {
  const { gio, phong, phongDaChon, chonPhong } = useGioPhong();
  const tongSucChua = phongDaChon.reduce(
    (tong, p) => tong + Number(p.sucChua || 0),
    0,
  );
  const tongTien = phongDaChon.reduce(
    (tong, p) => tong + Number(p.tienPhongDuKien || 0),
    0,
  );
  if (!phongDaChon.length) return null;
  return (
    <aside className="ThanhGio">
      <b>
        {phongDaChon.length} phòng · sức chứa {tongSucChua}/{gio.soKhach} khách
      </b>
      <span>
        {tongSucChua < gio.soKhach ? "Cần chọn thêm phòng · " : ""}
        {tienVnd(tongTien)}
      </span>
      <Link
        className={`NutChinh NutLien ${tongSucChua < gio.soKhach ? "NutVoHieu" : ""}`}
        to={tongSucChua >= gio.soKhach ? "/dat-phong" : "#"}
      >
        Tiếp tục đặt phòng
      </Link>
    </aside>
  );
}
