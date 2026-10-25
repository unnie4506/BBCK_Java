import * as NenTang from "../NenTangGiaoDien.jsx";
import { Outlet } from "react-router-dom";
import { Badge } from "antd";

const { Button, Layout, Menu, useEffect, useNavigate, useState, useXacThuc, donDatPhongApi } = NenTang;
const { Header, Sider, Content } = Layout;
const MENUS = [
  ["so-do-phong", "Sơ đồ phòng"],
  ["don-dat-phong", "Đơn đặt phòng"],
  ["cho-xac-nhan-coc", "Chờ xác nhận cọc"],
  ["khach-hang", "Khách hàng"],
  ["loai-phong", "Loại phòng và giá"],
  ["phong", "Phòng"],
  ["tien-nghi", "Tiện nghi"],
  ["chinh-sach-hoan-huy", "Chính sách hoàn hủy"],
  ["hoan-coc", "Hoàn cọc chờ xử lý"],
];

export function BoCucQuanLy({ children }) {
  const { nguoiDung, dangXuat } = useXacThuc();
  const chuyenTrang = useNavigate();
  const [dangRa, setDangRa] = useState(false);
  const [soChoCoc, setSoChoCoc] = useState(0);

  useEffect(() => {
    let conHieuLuc = true;
    const tai = async () => {
      try {
        const r = await donDatPhongApi.choXacNhanCoc();
        if (conHieuLuc) setSoChoCoc(Number(r.data?.duLieu?.soDangCho || 0));
      } catch { if (conHieuLuc) setSoChoCoc(0); }
    };
    tai();
    const timer = window.setInterval(tai, 15000);
    return () => { conHieuLuc = false; window.clearInterval(timer); };
  }, []);

  async function xuLyDangXuat() {
    setDangRa(true);
    try {
      await dangXuat();
    } finally {
      chuyenTrang("/404", { replace: true });
      setDangRa(false);
    }
  }

  return (
    <Layout className="KhungQuanLy">
      <Sider breakpoint="lg" collapsedWidth="0" className="ThanhBen">
        <div className="ThuongHieu">Nhà Mây</div>
        <Menu
          theme="light"
          mode="inline"
          items={MENUS.map(([key, label]) => ({
            key,
            label: key === "cho-xac-nhan-coc" ? <Badge count={soChoCoc} offset={[8, 0]}>{label}</Badge> : label,
          }))}
          onClick={({ key }) => chuyenTrang(`/${key}`)}
        />
      </Sider>
      <Layout>
        <Header className="ThanhTren">
          <span>{nguoiDung?.tenHienThi || nguoiDung?.hoTen || "Xin chào"}</span>
          <Button onClick={xuLyDangXuat} loading={dangRa}>
            Đăng xuất
          </Button>
        </Header>
        <Content className="NoiDung">{children || <Outlet />}</Content>
      </Layout>
    </Layout>
  );
}

export default BoCucQuanLy;
