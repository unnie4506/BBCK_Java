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

export function TrangChu() {
  const [ds, setDs] = useState([]);
  const [loi, setLoi] = useState("");
  const chuyenTrang = useNavigate();
  useEffect(() => {
    congKhaiApi
      .loaiPhong()
      .then((r) => setDs(layDanhSach(layDuLieu(r)).slice(0, 4)))
      .catch((e) => setLoi(goiApi(e)));
  }, []);
  function timNhanh(e) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const thamSo = new URLSearchParams(Object.fromEntries(form));
    chuyenTrang(`/tim-phong?${thamSo.toString()}`);
  }
  return (
    <>
      <section className="AnhBia">
        <div className="NoiDungAnhBia">
          <p className="NhanTieuDe">NHÀ MÂY HOMESTAY</p>
          <h1>Một nơi nghỉ dưỡng an yên, giữa hành trình của bạn.</h1>
          <p>Không gian ấm áp, tiện nghi và gần gũi như ở nhà.</p>
        </div>
        <div className="KhungAnhBia">
          <AnhPhong
            src={ds.find((loai) => loai.anhDaiDien)?.anhDaiDien}
            alt="Không gian nghỉ tại Nhà Mây Homestay"
            className="AnhMinhHoaBia"
          />
        </div>
      </section>
      <form className="FormTimNhanh" onSubmit={timNhanh}>
        <label>
          Ngày nhận
          <input name="ngayNhan" type="date" min={homNay()} required />
        </label>
        <label>
          Ngày trả
          <input name="ngayTra" type="date" min={homNay()} required />
        </label>
        <label>
          Số khách
          <input
            name="soKhach"
            type="number"
            min="1"
            max="30"
            defaultValue="2"
            required
          />
        </label>
        <button className="NutChinh">Tìm phòng</button>
      </form>
      <section className="KhoangTrang BoCucPhongNoiBat">
        <div className="DauMucPhongNoiBat">
          <TieuDe
            tieuDe="Không gian nghỉ dưỡng giữa thiên nhiên"
          />
          {loi && <LoiBao loi={loi} />}
        </div>
        <div className="NoiDungPhongNoiBat">
          {ds.length ? (
            <div className="LuoiThe LuoiPhongNoiBat" id="DanhSachPhongNoiBat">
              {ds.map((loai) => (
                <TheLoaiPhong key={loai.maLoaiPhong} loai={loai} />
              ))}
            </div>
          ) : (
            !loi && <TheTai />
          )}
        </div>
        <Link className="NutXemTatCaPhong" to="/phong">
          Xem tất cả phòng
        </Link>
      </section>
    </>
  );
}
