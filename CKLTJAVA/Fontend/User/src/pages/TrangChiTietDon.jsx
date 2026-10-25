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
import { ModalHuy } from "./ModalHuy.jsx";

export function TrangChiTietDon() {
  const { ma } = useParams();
  const [don, setDon] = useState(null),
    [loi, setLoi] = useState(""),
    [thongBao, setThongBao] = useState(""),
    [moHuy, setMoHuy] = useState(false),
    [loiHuy, setLoiHuy] = useState(""),
    [xemTruoc, setXemTruoc] = useState(null),
    [dangGui, setDangGui] = useState(false);
  async function tai() {
    try {
      setDon(layDuLieu(await donDatPhongApi.chiTiet(ma)));
      setLoi("");
    } catch (e) {
      setLoi(e.response?.status === 404 ? "Không tìm thấy đơn." : goiApi(e));
    }
  }
  useEffect(() => {
    tai();
  }, [ma]);
  async function moHopHuy() {
    setLoiHuy("");
    try {
      setXemTruoc(layDuLieu(await donDatPhongApi.xemTruocHuy(ma)));
      setMoHuy(true);
    } catch (e) {
      setLoi(goiApi(e));
    }
  }
  async function huy() {
    setDangGui(true);
    setLoiHuy("");
    try {
      await donDatPhongApi.huy(ma, {
        lyDo: document.getElementById("LyDoHuy")?.value || "",
      });
      setMoHuy(false);
      setLoiHuy("");
      setThongBao("Đã hủy đơn thành công. Trạng thái đơn đã được cập nhật.");
      await tai();
    } catch (e) {
      setLoiHuy(goiApi(e));
    } finally {
      setDangGui(false);
    }
  }
  if (loi && !don)
    return (
      <section className="KhoangTrang">
        <TieuDe tieuDe="Không tìm thấy đơn" />
        <p>{loi}</p>
      </section>
    );
  if (!don) return <TheTai soLuong={1} />;
  const ds = don.danhSachChiTiet || don.danhSachPhong || [];
  return (
    <section className="KhoangTrang">
      <TieuDe
        tieuDe={`Đơn ${don.maDonDatPhong || ma}`}
        moTa={`Trạng thái: ${don.trangThai}`}
      />
      {loi && <LoiBao loi={loi} />}
      {thongBao && <p role="status" className="ThongBaoThanhCong">{thongBao}</p>}
      <p>Tiền cọc: {tienVnd(don.tienCoc)}</p>
      {don.trangThai === "CHO_COC" && <Link className="NutChinh NutLien" to={`/thanh-toan-dat-coc/${ma}`}>Mở thanh toán đặt cọc</Link>}
      {don.trangThai === "HET_HAN" && <Link className="NutChinh NutLien" to="/tim-phong">Tìm phòng lại</Link>}
      <div className="DanhSachDong">
        {ds.map((d, i) => (
          <div className="DongDon" key={d.maChiTiet || i}>
            <b>Phòng {d.soPhong}</b>
            <span>
              {ngayHienThi(d.ngayNhan)} – {ngayHienThi(d.ngayTra)}
            </span>
            <span>{d.trangThai}</span>
            <b>{tienVnd(d.tienPhong)}</b>
          </div>
        ))}
      </div>
      {don.choPhepHuy && (
        <button className="NutNguyHiem" onClick={moHopHuy}>
          Hủy đơn
        </button>
      )}
      <ModalHuy
        mo={moHuy}
        dong={() => setMoHuy(false)}
        xemTruoc={xemTruoc}
        choCoc={don.trangThai === "CHO_COC"}
        loi={loiHuy}
        xacNhan={huy}
        dangGui={dangGui}
      />
    </section>
  );
}
