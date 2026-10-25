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

export function TrangDanhSachDon() {
  const [ds, setDs] = useState([]),
    [trangThai, setTrangThai] = useState(""),
    [loi, setLoi] = useState("");
  async function tai() {
    try {
      setDs(
        layDanhSach(
          layDuLieu(
            await donDatPhongApi.danhSach({
              trangThai: trangThai || undefined,
              trang: 1,
              kichThuoc: 20,
            }),
          ),
        ),
      );
    } catch (e) {
      setLoi(goiApi(e));
    }
  }
  useEffect(() => {
    tai();
  }, [trangThai]);
  return (
    <section className="KhoangTrang">
      <TieuDe tieuDe="Đơn của tôi" />
      <label className="BoLoc">
        Lọc trạng thái
        <select
          value={trangThai}
          onChange={(e) => setTrangThai(e.target.value)}
        >
          <option value="">Tất cả</option>
          {["CHO_COC", "HET_HAN", "DA_DAT", "DANG_O", "HOAN_TAT", "DA_HUY"].map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
      </label>
      {loi && <LoiBao loi={loi} />}
      {ds.length ? (
        <div className="DanhSachDong">
          {ds.map((d) => (
            <Link
              className="DongDon"
              to={`/tai-khoan/don-cua-toi/${d.maDonDatPhong}`}
              key={d.maDonDatPhong}
            >
              <b>Đơn {d.maDonDatPhong}</b>
              <span>
                {ngayHienThi(d.ngayNhan)} – {ngayHienThi(d.ngayTra)}
              </span>
              <span>{d.trangThai}</span>
              <b>{tienVnd(d.tongTienPhong)}</b>
            </Link>
          ))}
        </div>
      ) : (
        <p className="TrangTrong">
          Bạn chưa có đơn đặt phòng. <Link to="/tim-phong">Tìm phòng</Link>
        </p>
      )}
    </section>
  );
}
