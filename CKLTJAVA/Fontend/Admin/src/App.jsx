import { ConfigProvider, Spin } from "antd";
import viVN from "antd/locale/vi_VN";
import { HashRouter, Navigate, Route, Routes } from "react-router-dom";
import { XacThucProvider, useXacThuc } from "./contexts/XacThucContext.jsx";
import { TrangDangNhap } from "./pages/TrangDangNhap.jsx";
import { BoCucQuanLy } from "./pages/BoCucQuanLy.jsx";
import { Trang404 } from "./pages/Trang404.jsx";
import { TrangDuLieu } from "./pages/TrangDuLieu.jsx";
import { TrangSoDo } from "./pages/TrangSoDo.jsx";
import { TrangDonDatPhong } from "./pages/TrangDonDatPhong.jsx";
import { TrangKhachHang } from "./pages/TrangKhachHang.jsx";
import { TrangLoaiPhong } from "./pages/TrangLoaiPhong.jsx";
import { TrangTienNghi } from "./pages/TrangTienNghi.jsx";
import { TrangPhong } from "./pages/TrangPhong.jsx";
import { TrangChinhSach } from "./pages/TrangChinhSach.jsx";
import { TrangHoanCoc } from "./pages/TrangHoanCoc.jsx";
import { TrangChoXacNhanCoc } from "./pages/TrangChoXacNhanCoc.jsx";
import "./Admin.css";
const duongDanQuanLy = (import.meta.env.VITE_DUONG_DAN_QUAN_LY || "").replace(
  /^\/+|\/+$/g,
  "",
);
function UngDungQuanLy() {
  const { nguoiDung, dangKiemTra } = useXacThuc();
  if (!duongDanQuanLy) return <Trang404 />;
  if (dangKiemTra)
    return (
      <div className="CanGiua">
        <Spin size="large" />
      </div>
    );
  return (
    <Routes>
      <Route
        path="/dang-nhap"
        element={
          nguoiDung ? <Navigate to="/so-do-phong" replace /> : <TrangDangNhap />
        }
      />
      <Route
        path="/"
        element={
          nguoiDung ? (
            <BoCucQuanLy />
          ) : (
            <Trang404 />
          )
        }
      >
        <Route index element={<Navigate to="/so-do-phong" replace />} />
        <Route path="so-do-phong" element={<TrangSoDo />} />
        <Route path="loai-phong" element={<TrangLoaiPhong />} />
        <Route path="tien-nghi" element={<TrangTienNghi />} />
        <Route path="phong" element={<TrangPhong />} />
        <Route path="khach-hang" element={<TrangKhachHang />} />
        <Route path="don-dat-phong" element={<TrangDonDatPhong />} />
        <Route path="cho-xac-nhan-coc" element={<TrangChoXacNhanCoc />} />
        <Route path="chinh-sach-hoan-huy" element={<TrangChinhSach />} />
        <Route path="hoan-coc" element={<TrangHoanCoc />} />
        <Route path="*" element={<Trang404 />} />
      </Route>
      <Route path="*" element={<Trang404 />} />
    </Routes>
  );
}
export default function App() {
  return (
    <ConfigProvider
      locale={viVN}
      theme={{
        token: {
          colorPrimary: "#C8D9B4",
          colorText: "#26352A",
          colorBgLayout: "#F7F5ED",
          colorBgContainer: "#FFFEFA",
          borderRadius: 10,
        },
        components: {
          Button: {
            primaryColor: "#26352A",
          },
        },
      }}
    >
      <HashRouter>
        <XacThucProvider
          chuyenTrang={(giaTri) => {
            window.location.hash =
              giaTri === "het-phien"
                ? "#/dang-nhap?thongBao=het-phien"
                : "#/" + giaTri;
          }}
        >
          <UngDungQuanLy />
        </XacThucProvider>
      </HashRouter>
    </ConfigProvider>
  );
}
