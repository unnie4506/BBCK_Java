import * as NenTang from "../NenTangGiaoDien.jsx";
const {
  useEffect,
  useState,
  HashRouter,
  Link,
  Navigate,
  Route,
  Routes,
  useNavigate,
  Alert,
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Layout,
  Menu,
  Modal,
  Select,
  Space,
  Spin,
  Table,
  Tag,
  message,
  ConfigProvider,
  viVN,
  dayjs,
  XacThucProvider,
  useXacThuc,
  DUONG_DAN_GOC,
  laySoDo,
  layLoaiPhong,
  themLoaiPhong,
  suaLoaiPhong,
  doiHoatDongLoaiPhong,
  layTienNghi,
  luuTienNghi,
  xoaTienNghi,
  layPhong,
  luuPhong,
  doiHoatDongPhong,
  doiTrangThaiPhong,
  taiAnhPhong,
  xoaAnhPhong,
  datAnhDaiDien,
  layKhach,
  luuKhach,
  doiHoatDongKhach,
  donDatPhongApi,
  chinhSachApi,
  hoanCocApi,
  Header,
  Sider,
  Content,
  DUONG_DAN,
  MENUS,
  layThongBao,
  layDanhSach,
  dinhDangTien,
  dinhDangNgay,
} = NenTang;
import { TrangDuLieu } from "./TrangDuLieu.jsx";

export function TrangKhachHang() {
  return (
    <TrangDuLieu
      tieuDe="Khách hàng"
      hamTai={({ tuKhoa, trang, kichThuoc }) =>
        layKhach({ tuKhoa, trang, kichThuoc })
      }
      nutThem="Thêm khách"
      cot={[
        { title: "Họ tên", dataIndex: "hoTen" },
        { title: "Số điện thoại", dataIndex: "soDienThoai" },
        { title: "Số giấy tờ", dataIndex: "soGiayTo" },
        { title: "Email", dataIndex: "thuDienTu" },
        {
          title: "Hoạt động",
          dataIndex: "dangHoatDong",
          render: (v) => (v ? "Đang dùng" : "Ngừng dùng"),
        },
      ]}
      formTruong={
        <>
          <Form.Item name="hoTen" label="Họ tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item
            name="soDienThoai"
            label="Số điện thoại"
            rules={[{ required: true }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="soGiayTo" label="Số giấy tờ">
            <Input />
          </Form.Item>
          <Form.Item name="thuDienTu" label="Email">
            <Input />
          </Form.Item>
        </>
      }
      hamLuu={(d) => luuKhach(null, d)}
      hamSua={(ma, d) => luuKhach(ma, d)}
      thaoTac={(r, tai) => (
        <Button
          danger={r.dangHoatDong}
          onClick={() =>
            Modal.confirm({
              title: r.dangHoatDong ? "Ngừng sử dụng hồ sơ khách?" : "Sử dụng lại hồ sơ khách?",
              content: r.dangHoatDong
                ? "Khách sẽ không thể dùng hồ sơ này để tạo đơn mới. Đơn và lịch sử cũ vẫn được giữ."
                : "Hồ sơ khách sẽ được phép dùng lại khi tạo đơn.",
              okText: r.dangHoatDong ? "Ngừng sử dụng" : "Sử dụng lại",
              cancelText: "Quay lại",
              onOk: async () => {
                try {
                  await doiHoatDongKhach(r.maKhachHang, !r.dangHoatDong);
                  message.success(r.dangHoatDong ? "Đã ngừng sử dụng hồ sơ khách." : "Đã sử dụng lại hồ sơ khách.");
                  await tai();
                } catch (e) {
                  message.error(layThongBao(e));
                }
              },
            })
          }
        >
          {r.dangHoatDong ? "Ngừng sử dụng" : "Sử dụng lại"}
        </Button>
      )}
    />
  );
}
