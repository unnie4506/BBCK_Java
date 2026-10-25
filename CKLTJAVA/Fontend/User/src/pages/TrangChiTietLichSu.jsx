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

export function TrangChiTietLichSu() {
  const { ma } = useParams();
  const [duLieu, setDuLieu] = useState(null),
    [loi, setLoi] = useState("");
  useEffect(() => {
    lichSuApi
      .chiTiet(ma)
      .then((r) => setDuLieu(layDuLieu(r)))
      .catch((e) =>
        setLoi(
          e.response?.status === 404
            ? "Không tìm thấy lịch sử lưu trú."
            : goiApi(e),
        ),
      );
  }, [ma]);
  if (loi) return <LoiBao loi={loi} />;
  if (!duLieu) return <TheTai soLuong={1} />;
  const hoaDon = duLieu.hoaDon || duLieu;
  return (
    <section className="KhungForm NoiDungIn">
      <TieuDe tieuDe={`Hóa đơn ${hoaDon.soHoaDon || ""}`} moTa={`Đơn ${ma}`} />
      <p>Tổng tiền phòng: {tienVnd(hoaDon.tongTienPhong)}</p>
      <p>Tiền cọc đã trừ: {tienVnd(hoaDon.tienCocDaTru || hoaDon.tienCoc)}</p>
      <h2>Các khoản thanh toán</h2>
      {(hoaDon.danhSachThanhToan || []).map((t, i) => (
        <p key={i}>
          {t.phuongThuc} · {tienVnd(t.soTien)} · {ngayHienThi(t.thoiDiem)}
        </p>
      ))}
      <button className="NutPhu NutIn" onClick={() => window.print()}>
        In hóa đơn
      </button>
    </section>
  );
}
