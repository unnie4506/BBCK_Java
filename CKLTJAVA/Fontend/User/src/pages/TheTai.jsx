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

export function TheTai({ soLuong = 3 }) {
  return (
    <div className="LuoiThe">
      {Array.from({ length: soLuong }, (_, i) => (
        <div className="TheThe" key={i}>
          <div className="XuongTai" />
          <div className="XuongDong" />
          <div className="XuongDong Ngan" />
        </div>
      ))}
    </div>
  );
}
