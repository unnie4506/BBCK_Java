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

export function TrangDangNhap() {
  const { dangNhap, nguoiDung, dangKiemTra } = useXacThuc();
  const chuyenTrang = useNavigate();
  const [dangGui, setDangGui] = useState(false);
  const [loi, setLoi] = useState("");
  const [form] = Form.useForm();
  useEffect(() => {
    if (nguoiDung) chuyenTrang("/so-do-phong", { replace: true });
  }, [nguoiDung, chuyenTrang]);
  async function xuLyDangNhap(duLieu) {
    setDangGui(true);
    setLoi("");
    try {
      await dangNhap(duLieu.tenDangNhap, duLieu.matKhau);
      chuyenTrang("/so-do-phong", { replace: true });
    } catch (e) {
      const trangThai = e.response?.status;
      if (trangThai === 423) {
        setLoi(
          `Tài khoản đang bị khóa. Thời gian chờ: ${e.response?.data?.duLieu?.thoiGianChoGiay || e.response?.headers?.["retry-after"] || "hãy thử lại sau"} giây.`,
        );
      } else if (trangThai === 401) {
        setLoi("Sai tài khoản hoặc mật khẩu.");
      } else if (!e.response) {
        setLoi("Không kết nối được máy chủ. Kiểm tra Tomcat và kết nối API.");
      } else {
        setLoi(e.response.data?.thongBao || `Máy chủ trả lỗi (${trangThai}).`);
      }
    } finally {
      setDangGui(false);
    }
  }
  return (
    <main className="TrangDangNhap">
      <Card className="TheDangNhap" title="Đăng nhập khu vực làm việc">
        <Form form={form} layout="vertical" onFinish={xuLyDangNhap}>
          <Form.Item
            label="Tên đăng nhập"
            name="tenDangNhap"
            rules={[
              { required: true, message: "Vui lòng nhập tên đăng nhập." },
            ]}
          >
            <Input autoComplete="username" />
          </Form.Item>
          <Form.Item
            label="Mật khẩu"
            name="matKhau"
            rules={[{ required: true, message: "Vui lòng nhập mật khẩu." }]}
          >
            <Input.Password autoComplete="current-password" />
          </Form.Item>
          {loi && (
            <Alert type="error" showIcon message={loi} className="KhoangDuoi" />
          )}
          <Button
            type="primary"
            htmlType="submit"
            loading={dangGui || dangKiemTra}
            block
          >
            Đăng nhập
          </Button>
        </Form>
      </Card>
    </main>
  );
}
