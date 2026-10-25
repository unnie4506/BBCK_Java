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

export function TrangHoSo() {
  const [hoSo, setHoSo] = useState(null),
    [loi, setLoi] = useState(""),
    [dangGui, setDangGui] = useState(false);
  useEffect(() => {
    hoSoApi
      .xem()
      .then((r) => setHoSo(layDuLieu(r)))
      .catch((e) => setLoi(goiApi(e)));
  }, []);
  async function gui(e) {
    e.preventDefault();
    setDangGui(true);
    setLoi("");
    const d = Object.fromEntries(new FormData(e.currentTarget));
    delete d.thuDienTu;
    try {
      await hoSoApi.capNhat(d);
      setLoi("Đã cập nhật hồ sơ.");
    } catch (error) {
      setLoi(goiApi(error));
    } finally {
      setDangGui(false);
    }
  }
  return (
    <section className="KhungForm">
      <TieuDe tieuDe="Hồ sơ cá nhân" />
      {loi && <LoiBao loi={loi} />}
      {hoSo && (
        <form className="FormDoc" onSubmit={gui}>
          <label>
            Họ tên
            <input name="hoTen" defaultValue={hoSo.hoTen} required />
          </label>
          <label>
            Số điện thoại
            <input
              name="soDienThoai"
              defaultValue={hoSo.soDienThoai}
              required
              pattern="(\+84|0)[0-9]{9,10}"
            />
          </label>
          <label>
            Số giấy tờ
            <input name="soGiayTo" defaultValue={hoSo.soGiayTo || ""} />
          </label>
          <label>
            Email
            <input name="thuDienTu" value={hoSo.thuDienTu || ""} readOnly />
          </label>
          <button className="NutChinh" disabled={dangGui}>
            Lưu thay đổi
          </button>
        </form>
      )}
    </section>
  );
}
