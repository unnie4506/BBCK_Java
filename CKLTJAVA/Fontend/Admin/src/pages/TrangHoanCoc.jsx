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

export function TrangHoanCoc() {
  const [ds, setDs] = useState([]);
  const [khoanDangXuLy, setKhoanDangXuLy] = useState(null);
  const [soTienThucHoan, setSoTienThucHoan] = useState(0);
  const [dangXacNhan, setDangXacNhan] = useState(false);
  async function tai() {
    try {
      const r = await hoanCocApi.danhSach({ trangThai: "CHO_XU_LY" });
      setDs(layDanhSach(r.data?.duLieu));
    } catch (e) {
      message.error(layThongBao(e));
    }
  }
  useEffect(() => {
    tai();
  }, []);
  return (
    <section>
      <div className="HangTieuDe">
        <div>
          <h1>Hoàn cọc chờ xử lý</h1>
          <p>Xác nhận số tiền đã hoàn thực tế.</p>
        </div>
        <Button onClick={tai}>Tải lại</Button>
      </div>
      <Table
        rowKey="maThanhToan"
        dataSource={ds}
        columns={[
          { title: "Mã khoản hoàn", dataIndex: "maThanhToan" },
          { title: "Mã đơn", dataIndex: "maDonDatPhong" },
          { title: "Mã tra cứu", dataIndex: "maTraCuuDon" },
          { title: "Khách hàng", dataIndex: "hoTen" },
          {
            title: "Số tiền đề xuất",
            dataIndex: "soTien",
            render: dinhDangTien,
          },
          {
            title: "Thao tác",
            render: (_, r) => (
              <Button
                onClick={() => {
                  setSoTienThucHoan(Number(r.soTien));
                  setKhoanDangXuLy(r);
                }}
              >
                Xác nhận đã hoàn
              </Button>
            ),
          },
        ]}
        locale={{ emptyText: "Không có khoản chờ xử lý." }}
      />
      <Modal
        title="Xác nhận hoàn cọc"
        open={Boolean(khoanDangXuLy)}
        confirmLoading={dangXacNhan}
        okText="Xác nhận đã hoàn"
        cancelText="Quay lại"
        onCancel={() => setKhoanDangXuLy(null)}
        onOk={async () => {
          if (!khoanDangXuLy) return;
          setDangXacNhan(true);
          try {
            await hoanCocApi.xacNhan(khoanDangXuLy.maThanhToan, { soTienThucHoan });
            message.success("Đã ghi nhận hoàn cọc.");
            setKhoanDangXuLy(null);
            await tai();
          } catch (e) {
            message.error(layThongBao(e));
          } finally {
            setDangXacNhan(false);
          }
        }}
      >
        <p>Mã khoản hoàn: {khoanDangXuLy?.maThanhToan}</p>
        <p>Số tiền đề xuất: {dinhDangTien(khoanDangXuLy?.soTien)}</p>
        <InputNumber
          min={0}
          max={Number(khoanDangXuLy?.soTien || 0)}
          value={soTienThucHoan}
          onChange={(v) => setSoTienThucHoan(Number(v || 0))}
          className="RongDay"
          addonAfter="₫"
        />
      </Modal>
    </section>
  );
}
