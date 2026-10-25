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

export function KhungKhach() {
  const { nguoiDung, dangXuat } = useXacThuc();
  const [moMenu, setMoMenu] = useState(false);
  const chuyenTrang = useNavigate();
  async function xuLyDangXuat() {
    await dangXuat();
    chuyenTrang("/", { replace: true });
  }
  return (
    <div className="UngDung">
      <header className="ThanhTren">
        <Link className="ThuongHieu" to="/">
          Nhà Mây <span>Homestay</span>
        </Link>
        <button
          className="NutMenu"
          onClick={() => setMoMenu(!moMenu)}
          aria-expanded={moMenu}
        >
          Menu
        </button>
        <nav className={`ThanhDieuHuong ${moMenu ? "DangMo" : ""}`}>
          <Link to="/phong">Phòng</Link>
          <Link to="/tim-phong">Tìm phòng</Link>
          {nguoiDung ? (
            <>
              <Link to="/tai-khoan/ho-so">Hồ sơ</Link>
              <Link to="/tai-khoan/don-cua-toi">Đơn của tôi</Link>
              <Link to="/tai-khoan/lich-su-luu-tru">Lịch sử lưu trú</Link>
              <button className="NutLienKet" onClick={xuLyDangXuat}>
                Đăng xuất
              </button>
            </>
          ) : (
            <>
              <Link to="/dang-nhap">Đăng nhập</Link>
              <Link className="NutNho" to="/dang-ky">
                Đăng ký
              </Link>
            </>
          )}
        </nav>
      </header>
      <main className="NoiDungChinh">
        <Outlet />
      </main>
      <footer className="ChanTrang">
        <div className="NoiDungTrenChanTrang">
          <section className="GioiThieuChanTrang">
            <Link className="ThuongHieuChanTrang" to="/">
              Nhà Mây Homestay
            </Link>
            <p>
              Một chốn nghỉ bình yên để bạn thư giãn, khám phá và tận hưởng
              những khoảnh khắc đáng nhớ.
            </p>
            <Link className="LienKetDatPhongChanTrang" to="/tim-phong">
              Tìm phòng phù hợp <span aria-hidden="true">→</span>
            </Link>
          </section>

          <nav className="NhomLienKetChanTrang" aria-label="Khám phá homestay">
            <h2>Khám phá</h2>
            <Link to="/">Trang chủ</Link>
            <Link to="/phong">Các loại phòng</Link>
            <Link to="/tim-phong">Tìm phòng trống</Link>
          </nav>

          <nav className="NhomLienKetChanTrang" aria-label="Thông tin khách hàng">
            <h2>Dành cho bạn</h2>
            <Link to="/tai-khoan/don-cua-toi">Đơn đặt phòng</Link>
            <Link to="/tai-khoan/lich-su-luu-tru">Lịch sử lưu trú</Link>
            <Link to="/tai-khoan/ho-so">Thông tin cá nhân</Link>
          </nav>
        </div>

        <div className="ChanTrangCuoi">
          <p>© {new Date().getFullYear()} Nhà Mây Homestay. Chúc bạn có một kỳ nghỉ thật vui.</p>
          <p>Đặt phòng trực tuyến · Nghỉ ngơi trọn vẹn</p>
        </div>
      </footer>
    </div>
  );
}
