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

export function TrangDangKy() {
  const [loi, setLoi] = useState(""),
    [thanhCong, setThanhCong] = useState(""),
    [dangGui, setDangGui] = useState(false);
  async function gui(e) {
    e.preventDefault();
    setLoi("");
    setThanhCong("");
    setDangGui(true);
    const d = Object.fromEntries(new FormData(e.currentTarget));
    if (d.matKhau !== d.nhapLai) {
      setLoi("Mật khẩu nhập lại chưa khớp.");
      setDangGui(false);
      return;
    }
    if (
      d.matKhau.length < 8 ||
      !/[A-Za-z]/.test(d.matKhau) ||
      !/[0-9]/.test(d.matKhau)
    ) {
      setLoi("Mật khẩu cần ít nhất 8 ký tự, gồm chữ và số.");
      setDangGui(false);
      return;
    }
    delete d.nhapLai;
    try {
      const r = await xacThucApi.dangKy(d);
      setThanhCong(r.data?.thongBao || "Đăng ký thành công. Hãy đăng nhập.");
      e.currentTarget.reset();
    } catch (error) {
      setLoi(goiApi(error));
    } finally {
      setDangGui(false);
    }
  }
  return (
    <section className="KhungForm">
      <TieuDe
        tieuDe="Tạo tài khoản"
      />
      <form className="FormDoc" onSubmit={gui}>
        <label>
          Họ tên
          <input name="hoTen" required />
        </label>
        <label>
          Số điện thoại
          <input name="soDienThoai" required pattern="(\+84|0)[0-9]{9,10}" />
        </label>
        <label>
          Email
          <input name="thuDienTu" type="email" required />
        </label>
        <label>
          Mật khẩu
          <input name="matKhau" type="password" minLength="8" required />
          <small>Ít nhất 8 ký tự, gồm chữ và số.</small>
        </label>
        <label>
          Nhập lại mật khẩu
          <input name="nhapLai" type="password" required />
        </label>
        {loi && <LoiBao loi={loi} />}
        {thanhCong && <p className="ThongBaoThanhCong">{thanhCong}</p>}
        <button className="NutChinh" disabled={dangGui}>
          {dangGui ? "Đang gửi…" : "Đăng ký"}
        </button>
        <p>
          Đã có tài khoản? <Link to="/dang-nhap">Đăng nhập</Link>
        </p>
      </form>
    </section>
  );
}
