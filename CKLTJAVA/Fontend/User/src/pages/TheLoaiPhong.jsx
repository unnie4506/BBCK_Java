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

export function TheLoaiPhong({ loai }) {
  return (
    <article className="ThePhong">
      <Link to={`/phong/${loai.maLoaiPhong}`}>
        <AnhPhong src={loai.anhDaiDien} alt={loai.ten} />
        <div className="NoiDungThe">
          <h2>{loai.ten}</h2>
          <p>Tối đa {loai.sucChua} khách</p>
          <p>
            Ngày thường <b>{tienVnd(loai.giaNgayThuong)}</b>
          </p>
          <p>
            Cuối tuần <b>{tienVnd(loai.giaCuoiTuan)}</b>
          </p>
          <p className="DoanVan">{loai.moTa}</p>
          <span className="NutChu">Xem chi tiết</span>
        </div>
      </Link>
    </article>
  );
}
