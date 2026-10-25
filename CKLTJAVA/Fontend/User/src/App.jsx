import { HashRouter, Link, Route, Routes } from "react-router-dom";
import { XacThucProvider } from "./contexts/XacThucContext.jsx";
import { GioPhongProvider } from "./contexts/GioPhongContext.jsx";
import { KhungKhach } from "./pages/KhungKhach.jsx";
import { LoiBao } from "./pages/LoiBao.jsx";
import { TheTai } from "./pages/TheTai.jsx";
import { AnhPhong } from "./pages/AnhPhong.jsx";
import { TheLoaiPhong } from "./pages/TheLoaiPhong.jsx";
import { TrangChu } from "./pages/TrangChu.jsx";
import { TrangLoaiPhong } from "./pages/TrangLoaiPhong.jsx";
import { TrangChiTietLoaiPhong } from "./pages/TrangChiTietLoaiPhong.jsx";
import { ThanhGio } from "./pages/ThanhGio.jsx";
import { TrangTimPhong } from "./pages/TrangTimPhong.jsx";
import { BaoVe } from "./pages/BaoVe.jsx";
import { TrangDangNhap } from "./pages/TrangDangNhap.jsx";
import { TrangDangKy } from "./pages/TrangDangKy.jsx";
import { TrangDatPhong } from "./pages/TrangDatPhong.jsx";
import { TrangThanhToanDatCoc, TrangDatPhongThanhCong } from "./pages/TrangThanhToanDatCoc.jsx";
import { TrangHoSo } from "./pages/TrangHoSo.jsx";
import { TrangDanhSachDon } from "./pages/TrangDanhSachDon.jsx";
import { TrangChiTietDon } from "./pages/TrangChiTietDon.jsx";
import { ModalHuy } from "./pages/ModalHuy.jsx";
import { TrangLichSu } from "./pages/TrangLichSu.jsx";
import { TrangChiTietLichSu } from "./pages/TrangChiTietLichSu.jsx";
import { TrangLoi404 } from "./pages/TrangLoi404.jsx";
import "./User.css";
function UngDungKhach() {
  return (
    <HashRouter>
      <XacThucProvider>
        <GioPhongProvider>
          <Routes>
            <Route element={<KhungKhach />}>
              <Route index element={<TrangChu />} />
              <Route path="phong" element={<TrangLoaiPhong />} />
              <Route path="phong/:ma" element={<TrangChiTietLoaiPhong />} />
              <Route path="tim-phong" element={<TrangTimPhong />} />
              <Route
                path="dat-phong"
                element={
                  <BaoVe>
                    <TrangDatPhong />
                  </BaoVe>
                }
              />
              <Route path="thanh-toan-dat-coc/:ma" element={<BaoVe><TrangThanhToanDatCoc /></BaoVe>} />
              <Route path="dat-phong-thanh-cong/:ma" element={<BaoVe><TrangDatPhongThanhCong /></BaoVe>} />
              <Route path="dang-ky" element={<TrangDangKy />} />
              <Route path="dang-nhap" element={<TrangDangNhap />} />
              <Route
                path="tai-khoan/ho-so"
                element={
                  <BaoVe>
                    <TrangHoSo />
                  </BaoVe>
                }
              />
              <Route
                path="tai-khoan/don-cua-toi"
                element={
                  <BaoVe>
                    <TrangDanhSachDon />
                  </BaoVe>
                }
              />
              <Route
                path="tai-khoan/don-cua-toi/:ma"
                element={
                  <BaoVe>
                    <TrangChiTietDon />
                  </BaoVe>
                }
              />
              <Route
                path="tai-khoan/lich-su-luu-tru"
                element={
                  <BaoVe>
                    <TrangLichSu />
                  </BaoVe>
                }
              />
              <Route
                path="tai-khoan/lich-su-luu-tru/:ma"
                element={
                  <BaoVe>
                    <TrangChiTietLichSu />
                  </BaoVe>
                }
              />
              <Route path="*" element={<TrangLoi404 />} />
            </Route>
          </Routes>
        </GioPhongProvider>
      </XacThucProvider>
    </HashRouter>
  );
}
export default function App() {
  return <UngDungKhach />;
}
