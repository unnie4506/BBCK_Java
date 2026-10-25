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

export function TrangDuLieu({
  tieuDe,
  hamTai,
  cot,
  thaoTac,
  nutThem,
  formTruong,
  hamLuu,
  hamSua,
}) {
  const [duLieu, setDuLieu] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState("");
  const [dangGui, setDangGui] = useState(false);
  const [mo, setMo] = useState(false);
  const [banGhi, setBanGhi] = useState(null);
  const [tuKhoa, setTuKhoa] = useState("");
  const [trang, setTrang] = useState(1);
  const [kichThuoc, setKichThuoc] = useState(10);
  const [tongSoBanGhi, setTongSoBanGhi] = useState(0);
  const [form] = Form.useForm();
  async function taiLai(tuyChon = {}) {
    const trangCanTai = tuyChon.trang ?? trang;
    const kichThuocCanTai = tuyChon.kichThuoc ?? kichThuoc;
    setDangTai(true);
    setLoi("");
    try {
      const ketQua = await hamTai({ tuKhoa, trang: trangCanTai, kichThuoc: kichThuocCanTai });
      const ds = layDanhSach(ketQua);
      setDuLieu(ds);
      setTrang(trangCanTai);
      setKichThuoc(kichThuocCanTai);
      setTongSoBanGhi(Number(ketQua?.tongSoBanGhi ?? ketQua?.tong ?? ds.length));
    } catch (e) {
      setLoi(layThongBao(e));
    } finally {
      setDangTai(false);
    }
  }
  useEffect(() => {
    taiLai();
  }, []);
  async function luu(duLieuMoi) {
    setDangGui(true);
    try {
      await (banGhi && hamSua ? hamSua(banGhi, duLieuMoi) : hamLuu(duLieuMoi));
      message.success("Đã lưu dữ liệu.");
      setMo(false);
      await taiLai();
    } catch (e) {
      const danhSachTrung =
        e.response?.status === 409 ? layDanhSach(e.response?.data?.duLieu) : [];
      if (!banGhi && tieuDe === "Khách hàng" && danhSachTrung.length) {
        Modal.confirm({
          title: "Hồ sơ có thông tin trùng",
          content: (
            <div>
              <p>Hãy kiểm tra hồ sơ trùng trước khi tiếp tục.</p>
              {danhSachTrung.map((khach, i) => (
                <p key={khach.maKhachHang || i}>
                  {khach.hoTen} · {khach.soDienThoai} · {khach.soGiayTo}
                </p>
              ))}
            </div>
          ),
          okText: "Vẫn tạo hồ sơ mới",
          cancelText: "Dùng hồ sơ có sẵn",
          onOk: async () => {
            try {
              await hamLuu({ ...duLieuMoi, xacNhanTaoMoi: true });
              setMo(false);
              await taiLai();
            } catch (loiTao) {
              message.error(layThongBao(loiTao));
            }
          },
        });
      } else message.error(layThongBao(e));
    } finally {
      setDangGui(false);
    }
  }
  function moForm(mau = null) {
    setBanGhi(
      mau?.maLoaiPhong ||
        mau?.maPhong ||
        mau?.maKhachHang ||
        mau?.maTienNghi ||
        null,
    );
    form.setFieldsValue(mau || {});
    setMo(true);
  }
  const cotDayDu = [
    ...cot,
    ...(thaoTac || hamSua
      ? [
          {
            title: "Thao tác",
            key: "thaoTac",
            render: (_, banGhiHienTai) => (
              <Space>
                {hamSua && (
                  <Button onClick={() => moForm(banGhiHienTai)}>Sửa</Button>
                )}
                {thaoTac?.(banGhiHienTai, taiLai, moForm)}
              </Space>
            ),
          },
        ]
      : []),
  ];
  return (
    <section>
      <div className="HangTieuDe">
        <div>
          <h1>{tieuDe}</h1>
          <p>Quản lý dữ liệu và trạng thái theo nghiệp vụ.</p>
        </div>
        <Space>
        <Button onClick={() => taiLai()}>Tải lại</Button>
          {nutThem && (
            <Button type="primary" onClick={() => moForm()}>
              {nutThem}
            </Button>
          )}
        </Space>
      </div>
      <Input.Search
        className="TimKiem"
        placeholder={`Tìm trong ${tieuDe.toLowerCase()}`}
        value={tuKhoa}
        onChange={(e) => setTuKhoa(e.target.value)}
        onSearch={() => taiLai({ trang: 1 })}
      />
      {loi && <Alert type="error" message={loi} className="KhoangDuoi" />}
      <Card>
        {dangTai ? (
          <Spin />
        ) : (
          <Table
            rowKey={(r) =>
              r.maPhong ||
              r.maLoaiPhong ||
              r.maKhachHang ||
              r.maTienNghi ||
              r.maDonDatPhong ||
              JSON.stringify(r)
            }
            dataSource={duLieu}
            columns={cotDayDu}
            pagination={{
              current: trang,
              pageSize: kichThuoc,
              total: tongSoBanGhi,
              showSizeChanger: true,
              onChange: (trangMoi, kichThuocMoi) => taiLai({ trang: trangMoi, kichThuoc: kichThuocMoi }),
            }}
            locale={{ emptyText: "Chưa có dữ liệu." }}
          />
        )}
      </Card>
      <Modal
        title={
          banGhi
            ? `Sửa ${tieuDe.toLowerCase()}`
            : `Thêm ${tieuDe.toLowerCase()}`
        }
        open={mo}
        onCancel={() => setMo(false)}
        onOk={() => form.submit()}
        confirmLoading={dangGui}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={luu}>
          {formTruong}
        </Form>
      </Modal>
    </section>
  );
}
