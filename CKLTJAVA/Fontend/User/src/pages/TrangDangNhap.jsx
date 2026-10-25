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

export function TrangDangNhap() {
  const { dangNhap } = useXacThuc();
  const chuyenTrang = useNavigate();
  const viTri = useLocation();
  const [loi, setLoi] = useState(viTri.state?.thongBao || "");
  const [dangGui, setDangGui] = useState(false);
  async function gui(e) {
    e.preventDefault();
    setDangGui(true);
    setLoi("");
    const form = Object.fromEntries(new FormData(e.currentTarget));
    try {
      await dangNhap(form);
      chuyenTrang(viTri.state?.from || "/", { replace: true });
    } catch (error) {
      setLoi(
        error.response?.status === 423
          ? `Tài khoản tạm khóa. Thời gian chờ: ${error.response?.data?.duLieu?.thoiGianChoGiay || error.response?.headers?.["retry-after"] || "hãy thử lại sau"} giây.`
          : "Sai tài khoản hoặc mật khẩu.",
      );
    } finally {
      setDangGui(false);
    }
  }
  return (
    <section className="KhungForm">
      <TieuDe
        tieuDe="Đăng Nhập"
      />
      <form className="FormDoc" onSubmit={gui}>
        <label>
          Email
          <input name="thuDienTu" type="email" required autoComplete="email" />
        </label>
        <label>
          Mật khẩu
          <input
            name="matKhau"
            type="password"
            required
            autoComplete="current-password"
          />
        </label>
        {loi && <LoiBao loi={loi} />}
        <button className="NutChinh" disabled={dangGui}>
          {dangGui ? "Đang đăng nhập…" : "Đăng nhập"}
        </button>
        <p>
          Chưa có tài khoản? <Link to="/dang-ky">Đăng ký</Link>
        </p>
      </form>
    </section>
  );
}
