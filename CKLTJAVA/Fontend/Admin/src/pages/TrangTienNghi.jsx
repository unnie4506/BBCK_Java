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
  doiHoatDongTienNghi,
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

export function TrangTienNghi() {
  return (
    <TrangDuLieu
      tieuDe="Tiện nghi"
      hamTai={layTienNghi}
      nutThem="Thêm tiện nghi"
      cot={[
        { title: "Tên tiện nghi", dataIndex: "ten" },
        {
          title: "Hoạt động",
          dataIndex: "dangHoatDong",
          render: (v) => (v ? "Đang dùng" : "Ngừng dùng"),
        },
      ]}
      formTruong={
        <Form.Item
          name="ten"
          label="Tên tiện nghi"
          rules={[{ required: true }]}
        >
          <Input />
        </Form.Item>
      }
      hamLuu={(d) => luuTienNghi(null, d.ten)}
      hamSua={(ma, d) => luuTienNghi(ma, d.ten)}
      thaoTac={(r, tai) => (
        <Button
          danger={r.dangHoatDong}
          onClick={() =>
            Modal.confirm({
              title: r.dangHoatDong ? "Ngừng sử dụng tiện nghi?" : "Sử dụng lại tiện nghi?",
              content: r.dangHoatDong
                ? "Tiện nghi sẽ không được gán cho phòng mới. Các phòng hiện dùng tiện nghi này vẫn giữ nguyên dữ liệu."
                : "Tiện nghi sẽ có thể được chọn khi cập nhật phòng.",
              okText: r.dangHoatDong ? "Ngừng sử dụng" : "Sử dụng lại",
              cancelText: "Quay lại",
              onOk: async () => {
                try {
                  await doiHoatDongTienNghi(r.maTienNghi, !r.dangHoatDong);
                  message.success(r.dangHoatDong ? "Đã ngừng sử dụng tiện nghi." : "Đã sử dụng lại tiện nghi.");
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
