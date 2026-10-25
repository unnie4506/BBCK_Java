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
import { AnhPhong } from "./AnhPhong.jsx";
import { TheLoaiPhong } from "./TheLoaiPhong.jsx";

export function TrangChiTietLoaiPhong() {
  const { ma } = useParams();
  const [loai, setLoai] = useState(null),
    [loi, setLoi] = useState("");
  useEffect(() => {
    congKhaiApi
      .chiTietLoaiPhong(ma)
      .then((r) => setLoai(layDuLieu(r)))
      .catch((e) => setLoi(goiApi(e)));
  }, [ma]);
  if (loi) return <LoiBao loi={loi} />;
  if (!loai) return <TheTai soLuong={1} />;
  return (
    <section className="KhoangTrang">
      <TieuDe tieuDe={loai.ten} moTa={loai.moTa} />
      <div className="ChiTietPhong">
        <AnhPhong
          src={loai.anhDaiDien || loai.danhSachAnh?.[0]?.duongDan}
          alt={loai.ten}
        />
        <div>
          <p>Sức chứa tối đa: {loai.sucChua} khách</p>
          <p>
            Ngày thường: <b>{tienVnd(loai.giaNgayThuong)}</b>
          </p>
          <p>
            Cuối tuần: <b>{tienVnd(loai.giaCuoiTuan)}</b>
          </p>
          <h2>Tiện nghi</h2>
          <ul>
            {(loai.tienNghi || []).map((x, i) => (
              <li key={x.maTienNghi || i}>{typeof x === "string" ? x : x.ten}</li>
            ))}
          </ul>
          <Link
            className="NutChinh NutLien"
            to={`/tim-phong?maLoaiPhong=${ma}`}
          >
            Xem phòng trống
          </Link>
        </div>
      </div>
      <div className="LuoiThe AnhNho">
        {(loai.danhSachAnh || []).map((anh, i) => (
          <AnhPhong
            key={anh.maAnh || anh.maAnhLoaiPhong || i}
            src={typeof anh === "string" ? anh : anh.duongDan || anh.duongDanAnh || anh.url}
            alt={`${loai.ten} ${i + 1}`}
          />
        ))}
      </div>
    </section>
  );
}
