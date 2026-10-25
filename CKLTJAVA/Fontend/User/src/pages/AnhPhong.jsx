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
import { layGocApi } from "../services/axios.js";

export function AnhPhong({ src, alt, className = "" }) {
  const [loi, setLoi] = useState(false);
  const duongDanApi = layGocApi();
  const duongDanAnh = !src
    ? ""
    : src.startsWith("http")
      ? src
      : src.startsWith("/api/")
        ? `${duongDanApi}${src.slice(4)}`
        : src.startsWith("/")
          ? src
          : `${duongDanApi}/cong-khai/anh-phong/${encodeURIComponent(src)}`;
  return (
    <img
      className={`AnhPhong ${className}`}
      src={
        !duongDanAnh || loi
          ? "data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22600%22 height=%22360%22 viewBox=%220 0 600 360%22%3E%3Crect width=%22600%22 height=%22360%22 fill=%22%23e8eee9%22/%3E%3Cpath d=%22M0 290 180 145l110 90 90-120 220 175v70H0z%22 fill=%22%23bfd1c5%22/%3E%3C/svg%3E"
          : duongDanAnh
      }
      alt={alt || "Ảnh phòng homestay"}
      loading="lazy"
      onError={() => setLoi(true)}
    />
  );
}
