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

export function TrangLoaiPhong() {
  const [ds, setDs] = useState([]),
    [loi, setLoi] = useState(""),
    [dangTai, setDangTai] = useState(true);
  async function tai() {
    setDangTai(true);
    try {
      setDs(layDanhSach(layDuLieu(await congKhaiApi.loaiPhong())));
      setLoi("");
    } catch (e) {
      setLoi(goiApi(e));
    } finally {
      setDangTai(false);
    }
  }
  useEffect(() => {
    tai();
  }, []);
  return (
    <section className="KhoangTrang">
      <TieuDe
        tieuDe="Các loại phòng"
        moTa="Tìm căn phòng phù hợp với số khách và ngân sách của bạn."
      />
      {loi && <LoiBao loi={loi} thuLai={tai} />}
      {dangTai ? (
        <TheTai />
      ) : ds.length ? (
        <div className="LuoiThe LuoiLoaiPhong">
          {ds.map((loai) => (
            <TheLoaiPhong key={loai.maLoaiPhong} loai={loai} />
          ))}
        </div>
      ) : (
        <p className="TrangTrong">Chưa có phòng để hiển thị.</p>
      )}
    </section>
  );
}
