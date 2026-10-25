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

export function TrangDatPhong() {
  const { gio, phongDaChon, xoaGio } = useGioPhong();
  const [xemTruoc, setXemTruoc] = useState(null),
    [loi, setLoi] = useState(""),
    [dangGui, setDangGui] = useState(false);
  const chuyenTrang = useNavigate();
  const duLieu = useMemo(
    () => ({
      ngayNhan: gio.ngayNhan,
      ngayTra: gio.ngayTra,
      soKhach: Number(gio.soKhach),
      danhSachMaPhong: gio.danhSachMaPhong,
    }),
    [gio],
  );
  useEffect(() => {
    if (gio.danhSachMaPhong.length)
      donDatPhongApi
        .xemTruoc(duLieu)
        .then((r) => setXemTruoc(layDuLieu(r)))
        .catch((e) => setLoi(goiApi(e)));
  }, []);
  async function gui(e) {
    e.preventDefault();
    setDangGui(true);
    setLoi("");
    try {
      const r = await donDatPhongApi.datPhong({
        ...duLieu,
        ghiChu: new FormData(e.currentTarget).get("ghiChu"),
      });
      const ma = layDuLieu(r)?.maDonDatPhong || layDuLieu(r)?.maDon;
      xoaGio();
      if (ma) chuyenTrang(`/thanh-toan-dat-coc/${ma}`);
    } catch (error) {
      setLoi(
        `${error.response?.data?.thongBao || goiApi(error)}${error.response?.data?.maLoi === "DA_DAT_TOI_DA" ? " Bạn đã có 3 đơn Đã đặt." : ""}`,
      );
    } finally {
      setDangGui(false);
    }
  }
  return (
    <section className="KhungForm">
      <TieuDe
        tieuDe="Xác nhận đặt phòng"
        moTa="Xác nhận phòng để tạo đơn chờ cọc. Đơn chỉ được xác nhận sau khi Quản lý đối soát đủ tiền chuyển khoản."
      />
      {loi && <LoiBao loi={loi} />}
      <p>
        {ngayHienThi(gio.ngayNhan)} – {ngayHienThi(gio.ngayTra)} · {gio.soKhach}{" "}
        khách
      </p>
      {phongDaChon.map((p) => (
        <div className="DongTomTat" key={p.maPhong}>
          <span>
            Phòng {p.soPhong} · {p.tenLoaiPhong}
          </span>
          <b>{tienVnd(p.tienPhongDuKien)}</b>
        </div>
      ))}
      <p className="TongTien">
        Tổng tạm tính:{" "}
        {tienVnd(
          xemTruoc?.tongTienPhong ||
            phongDaChon.reduce((s, p) => s + Number(p.tienPhongDuKien || 0), 0),
        )}
      </p>
      {xemTruoc && <p>Số tiền cọc cần chuyển ({xemTruoc.tyLeCoc}%): <b>{tienVnd(xemTruoc.tienCocYeuCau || xemTruoc.tienCoc)}</b></p>}
      <form className="FormDoc" onSubmit={gui}>
        <label>
          Ghi chú
          <textarea name="ghiChu" rows="3" />
        </label>
        <button className="NutChinh" disabled={dangGui || !phongDaChon.length}>
          {dangGui ? "Đang đặt…" : "Xác nhận đặt phòng"}
        </button>
      </form>
    </section>
  );
}
