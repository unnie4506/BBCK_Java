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
import { TheTai } from "./TheTai.jsx";

export function BaoVe({ children }) {
  const { nguoiDung, dangTai } = useXacThuc();
  const viTri = useLocation();
  if (dangTai) return <TheTai soLuong={1} />;
  if (!nguoiDung)
    return (
      <Navigate
        to="/dang-nhap"
        replace
        state={{ from: `${viTri.pathname}${viTri.search}` }}
      />
    );
  return children;
}
