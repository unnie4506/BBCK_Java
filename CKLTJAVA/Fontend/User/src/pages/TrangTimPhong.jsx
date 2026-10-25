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
import { ThanhGio } from "./ThanhGio.jsx";

export function TrangTimPhong() {
  const { gio, phong, capNhatTimKiem, chonPhong, phongDaChon } = useGioPhong();
  const [search, setSearch] = useSearchParams();
  const [loi, setLoi] = useState("");
  const [dangTai, setDangTai] = useState(false);
  const [danhSach, setDanhSach] = useState([]);
  const chuyenTrang = useNavigate();
  useEffect(() => {
    const duLieu = {
      ngayNhan: search.get("ngayNhan") || gio.ngayNhan || "",
      ngayTra: search.get("ngayTra") || gio.ngayTra || "",
      soKhach: Number(search.get("soKhach") || gio.soKhach || 1),
      maLoaiPhong: search.get("maLoaiPhong") || "",
    };
    capNhatTimKiem(duLieu);
    if (duLieu.ngayNhan && duLieu.ngayTra) tim(duLieu);
  }, [search.toString()]);
  async function tim(duLieu = gio) {
    setLoi("");
    const batDau = new Date(`${duLieu.ngayNhan}T00:00:00`),
      ketThuc = new Date(`${duLieu.ngayTra}T00:00:00`),
      dem = (ketThuc - batDau) / 86400000;
    if (!duLieu.ngayNhan || !duLieu.ngayTra || dem < 1) {
      setLoi("Ngày trả phải sau ngày nhận.");
      return;
    }
    if (duLieu.ngayNhan < homNay()) {
      setLoi("Ngày nhận không được ở quá khứ.");
      return;
    }
    if (dem > 30) {
      setLoi("Mỗi phòng được đặt tối đa 30 đêm.");
      return;
    }
    setDangTai(true);
    try {
      const r = await congKhaiApi.phongTrong({
        ngayNhan: duLieu.ngayNhan,
        ngayTra: duLieu.ngayTra,
        soKhach: duLieu.soKhach,
        maLoaiPhong: duLieu.maLoaiPhong || undefined,
      });
      setDanhSach(layDanhSach(layDuLieu(r)));
    } catch (e) {
      setLoi(goiApi(e));
    } finally {
      setDangTai(false);
    }
  }
  function gui(e) {
    e.preventDefault();
    const duLieu = Object.fromEntries(new FormData(e.currentTarget));
    duLieu.soKhach = Number(duLieu.soKhach);
    capNhatTimKiem(duLieu);
    setSearch(Object.fromEntries(Object.entries(duLieu).filter(([, v]) => v)));
  }
  return (
    <section className="KhoangTrang">
      <TieuDe
        tieuDe="Tìm phòng trống"
        moTa="Chọn ngày lưu trú để xem phòng còn phù hợp."
      />
      <form className="FormTimKiem" onSubmit={gui}>
        <label>
          Ngày nhận
          <input
            name="ngayNhan"
            type="date"
            min={homNay()}
            required
            defaultValue={search.get("ngayNhan") || gio.ngayNhan}
          />
        </label>
        <label>
          Ngày trả
          <input
            name="ngayTra"
            type="date"
            min={homNay()}
            required
            defaultValue={search.get("ngayTra") || gio.ngayTra}
          />
        </label>
        <label>
          Số khách
          <input
            name="soKhach"
            type="number"
            min="1"
            max="30"
            required
            defaultValue={search.get("soKhach") || gio.soKhach}
          />
        </label>
        <label>
          Loại phòng
          <input
            name="maLoaiPhong"
            defaultValue={search.get("maLoaiPhong") || ""}
            placeholder="Tất cả loại phòng"
          />
        </label>
        <button className="NutChinh">Tìm phòng</button>
      </form>
      {loi && <LoiBao loi={loi} />}
      {dangTai ? (
        <TheTai />
      ) : danhSach.length ? (
        <div className="LuoiThe LuoiPhongTrong">
          {danhSach.map((p) => (
            <article className="ThePhong" key={p.maPhong}>
              <AnhPhong src={p.anhDaiDien} alt={p.soPhong} />
              <div className="NoiDungThe">
                <h2>Phòng {p.soPhong}</h2>
                <p>
                  {p.tenLoaiPhong} · Tầng {p.soTang} · tối đa {p.sucChua} khách
                </p>
                <p>
                  {p.soDemNgayThuong || 0} đêm thường, {p.soDemCuoiTuan || 0}{" "}
                  đêm cuối tuần
                </p>
                <b>{tienVnd(p.tienPhongDuKien)}</b>
                <button className="NutPhu" onClick={() => chonPhong(p.maPhong)}>
                  {gio.danhSachMaPhong.includes(p.maPhong)
                    ? "Bỏ chọn"
                    : "Chọn phòng"}
                </button>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <p className="TrangTrong">
          Chưa tìm thấy phòng trống. Hãy thử đổi ngày hoặc loại phòng.
        </p>
      )}
      <ThanhGio />
    </section>
  );
}
