import * as NenTang from "../NenTangGiaoDien.jsx";
import { LoiBao } from "./LoiBao.jsx";
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

export function ModalHuy({ mo, dong, xemTruoc, loi, xacNhan, dangGui, choCoc = false }) {
  if (!mo) return null;
  return (
    <div className="NenHopThoai">
      <section className="HopThoai" role="dialog" aria-modal="true">
        <h2>Xác nhận hủy đơn</h2>
        {choCoc ? (
          <p>Đơn chưa được Quản lý xác nhận tiền cọc nên không có khoản hoàn. Hủy sẽ nhả lịch phòng ngay.</p>
        ) : (
          <p>Tiền hoàn dự kiến: {tienVnd(xemTruoc?.soTienHoanDuKien ?? xemTruoc?.tongTienHoan)}</p>
        )}
        <p>Việc hoàn tiền thực tế do Quản lý thực hiện.</p>
        {loi ? <LoiBao loi={loi} /> : null}
        <label>
          Lý do (không bắt buộc)
          <textarea id="LyDoHuy" maxLength={1000} />
        </label>
        <div className="HangNut">
          <button className="NutPhu" onClick={dong}>
            Quay lại
          </button>
          <button className="NutNguyHiem" disabled={dangGui} onClick={xacNhan}>
            Xác nhận hủy
          </button>
        </div>
      </section>
    </div>
  );
}
