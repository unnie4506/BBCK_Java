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
import { TieuDe } from "./TieuDe.jsx";
import { LoiBao } from "./LoiBao.jsx";
import { TheTai } from "./TheTai.jsx";

export function TrangLichSu() {
  const [ds, setDs] = useState([]),
    [loi, setLoi] = useState("");
  useEffect(() => {
    lichSuApi
      .danhSach(1)
      .then((r) => setDs(layDanhSach(layDuLieu(r))))
      .catch((e) => setLoi(goiApi(e)));
  }, []);
  return (
    <section className="KhoangTrang">
      <TieuDe tieuDe="Lịch sử lưu trú" />
      {loi && <LoiBao loi={loi} />}
      {ds.length ? (
        <div className="DanhSachDong">
          {ds.map((d) => (
            <Link
              className="DongDon"
              key={d.maDonDatPhong}
              to={`/tai-khoan/lich-su-luu-tru/${d.maDonDatPhong}`}
            >
              <b>Đơn {d.maDonDatPhong}</b>
              <span>
                {ngayHienThi(d.ngayNhan)} – {ngayHienThi(d.ngayTra)}
              </span>
              <b>{tienVnd(d.tongThanhToan)}</b>
            </Link>
          ))}
        </div>
      ) : (
        <p className="TrangTrong">Chưa có lịch sử lưu trú.</p>
      )}
    </section>
  );
}
