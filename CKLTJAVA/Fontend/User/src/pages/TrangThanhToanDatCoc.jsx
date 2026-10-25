import * as NenTang from "../NenTangGiaoDien.jsx";
const {
  useEffect,
  useState,
  Link,
  useNavigate,
  useParams,
  donDatPhongApi,
  goiApi,
  layDuLieu,
  tienVnd,
} = NenTang;
import { TieuDe } from "./TieuDe.jsx";
import { LoiBao } from "./LoiBao.jsx";
import { TheTai } from "./TheTai.jsx";

function demNguoc(soMs) {
  const s = Math.max(0, Math.floor(soMs / 1000));
  return `${String(Math.floor(s / 60)).padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`;
}

export function TrangThanhToanDatCoc() {
  const { ma } = useParams();
  const chuyenTrang = useNavigate();
  const [don, setDon] = useState(null);
  const [loi, setLoi] = useState("");
  const [dangBao, setDangBao] = useState(false);
  const [dangHuy, setDangHuy] = useState(false);
  const [moModalBao, setMoModalBao] = useState(false);
  const [dongHo, setDongHo] = useState({ server: 0, local: 0 });
  const [nhatKyDongHo, setNhatKyDongHo] = useState(0);

  async function tai() {
    try {
      const moi = layDuLieu(await donDatPhongApi.chiTiet(ma));
      setDon(moi);
      setLoi("");
      setDongHo({ server: Number(moi.thoiGianMayChuMs || Date.now()), local: Date.now() });
      if (moi.trangThai === "DA_DAT") chuyenTrang(`/dat-phong-thanh-cong/${ma}`, { replace: true });
    } catch (e) {
      setLoi(e.response?.status === 404 ? "Không tìm thấy đơn đặt phòng." : goiApi(e));
    }
  }

  useEffect(() => {
    if (moModalBao) return undefined;
    let conHieuLuc = true;
    const taiDinhKy = async () => { if (conHieuLuc) await tai(); };
    taiDinhKy();
    const poll = window.setInterval(taiDinhKy, 5000);
    const tick = window.setInterval(() => setNhatKyDongHo(Date.now()), 1000);
    return () => { conHieuLuc = false; window.clearInterval(poll); window.clearInterval(tick); };
  }, [ma, moModalBao]);

  async function baoDaChuyen() {
    setDangBao(true);
    setLoi("");
    try {
      await donDatPhongApi.baoDaChuyenKhoan(ma);
      setDon((donHienTai) => donHienTai ? { ...donHienTai, daBaoDaChuyenKhoan: true } : donHienTai);
      setMoModalBao(true);
    } catch (e) { setLoi(goiApi(e)); }
    finally { setDangBao(false); }
  }

  async function huyChoCoc() {
    if (!window.confirm("Hủy đơn đang chờ cọc? Lịch sẽ được nhả ngay; hệ thống không tạo khoản hoàn vì chưa ghi nhận tiền.")) return;
    setDangHuy(true);
    setLoi("");
    try {
      await donDatPhongApi.huy(ma, { lyDo: "Khách hủy khi đang chờ cọc." });
      chuyenTrang("/tim-phong", { replace: true });
    } catch (e) { setLoi(goiApi(e)); }
    finally { setDangHuy(false); }
  }

  if (!don && !loi) return <TheTai soLuong={1} />;
  if (!don) return <section className="KhungForm"><TieuDe tieuDe="Không tải được đơn đặt phòng" />{loi && <LoiBao loi={loi} />}<Link to="/tai-khoan/don-cua-toi">Xem đơn của tôi</Link></section>;

  const thoiGianMayChu = dongHo.server + Math.max(0, nhatKyDongHo - dongHo.local);
  const conLai = Math.max(0, Number(don.hanGiuChoMs || 0) - thoiGianMayChu);
  const dangCho = don.trangThai === "CHO_COC";
  const daBao = Boolean(don.daBaoDaChuyenKhoan);

  if (don.trangThai === "HET_HAN") return (
    <section className="KhungForm">
      <TieuDe tieuDe="Đơn đã hết hạn giữ chỗ" moTa={`Mã đơn ${don.maTraCuuDon || ma}`} />
      <p>Thời gian giữ phòng đã kết thúc. Hãy tìm phòng lại để kiểm tra lịch mới nhất.</p>
      <Link className="NutChinh NutLien" to="/tim-phong">Tìm phòng</Link>
    </section>
  );
  if (don.trangThai === "DA_HUY") return (
    <section className="KhungForm"><TieuDe tieuDe="Đơn đã hủy" moTa={`Mã đơn ${don.maTraCuuDon || ma}`} /><Link className="NutChinh NutLien" to="/tim-phong">Tìm phòng</Link></section>
  );

  return (
    <section className="KhungForm">
      <TieuDe tieuDe="Thanh toán đặt cọc" moTa={`Mã đơn ${don.maTraCuuDon || ma} · Trạng thái: ${don.trangThai}`} />
      {loi && <LoiBao loi={loi} />}
      <p>Số tiền cọc cần chuyển: <b>{tienVnd(don.tienCocYeuCau)}</b></p>
      {dangCho && <p>Thời gian giữ phòng còn lại: <b aria-live="polite">{demNguoc(conLai)}</b></p>}
      <div className="DongTomTat"><span>Ngân hàng</span><b>{don.nganHang || "Chưa cấu hình"}</b></div>
      <div className="DongTomTat"><span>Số tài khoản</span><b>{don.soTaiKhoan || "Chưa cấu hình"}</b></div>
      <div className="DongTomTat"><span>Chủ tài khoản</span><b>{don.chuTaiKhoan || "Chưa cấu hình"}</b></div>
      <div className="DongTomTat"><span>Nội dung chuyển khoản</span><b>{don.noiDungChuyenKhoan || `HS${ma}`}</b></div>
      <div className="DanhSachDong">
        {(don.danhSachPhong || []).map((p, i) => <div className="DongDon" key={p.maChiTietDatPhong || i}>
          <b>Phòng {p.soPhong}</b><span>{p.ngayNhan} – {p.ngayTra}</span><b>{tienVnd(p.tienPhong)}</b>
        </div>)}
      </div>
      {dangCho && <div className="NhomNutDatCoc">
        <button className="NutChinh" disabled={dangBao || daBao || conLai <= 0} onClick={baoDaChuyen}>
          {dangBao ? "Đang ghi nhận…" : daBao ? "Đã báo chuyển khoản" : "Tôi đã chuyển khoản"}
        </button>
        <button className="NutNguyHiem" disabled={dangHuy} onClick={huyChoCoc}>
          {dangHuy ? "Đang hủy…" : "Hủy đơn chờ cọc"}
        </button>
      </div>}
      <div className="LienKetDonDatCoc"><Link to="/tai-khoan/don-cua-toi">Xem đơn của tôi</Link></div>
      {moModalBao && <div className="NenHopThoai">
        <section className="HopThoai" role="dialog" aria-modal="true" aria-labelledby="TieuDeBaoChuyenKhoan">
          <p>Đã ghi nhận thời điểm bạn báo chuyển khoản. Đơn vẫn chờ Quản lý đối soát sao kê.</p>
          <div className="HangNut">
            <button className="NutChinh" onClick={() => chuyenTrang("/tai-khoan/don-cua-toi", { replace: true })}>Đóng</button>
          </div>
        </section>
      </div>}
    </section>
  );
}

export function TrangDatPhongThanhCong() {
  const { ma } = useParams();
  return <section className="KhungForm">
    <TieuDe tieuDe="Đặt phòng thành công" moTa={`Đơn ${ma} đã được Quản lý xác nhận cọc.`} />
    <p>Homestay sẽ xử lý nhận phòng theo lịch đã đặt. Bạn có thể xem trạng thái và thông tin đơn bất cứ lúc nào.</p>
    <Link className="NutChinh NutLien" to={`/tai-khoan/don-cua-toi/${ma}`}>Xem chi tiết đơn</Link>
  </section>;
}
