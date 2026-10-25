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

export function TrangChinhSach() {
  const [ds, setDs] = useState([]);
  const [loi, setLoi] = useState("");
  const [dangTai, setDangTai] = useState(true);
  const [dangLuu, setDangLuu] = useState(false);
  async function tai() {
    setDangTai(true);
    try {
      const r = await chinhSachApi.xem();
      const duLieu = r.data?.duLieu;
      // Backend hiện trả trực tiếp danh sách các bậc, không bọc trong danhSachBac.
      setDs(Array.isArray(duLieu) ? duLieu : duLieu?.danhSachBac || []);
      setLoi("");
    } catch (e) {
      setLoi(layThongBao(e));
    } finally {
      setDangTai(false);
    }
  }
  useEffect(() => {
    tai();
  }, []);
  return (
    <section>
      <div className="HangTieuDe">
        <div>
          <h1>Chính sách hoàn hủy</h1>
          <p>Thiết lập số ngày và tỷ lệ hoàn cọc.</p>
        </div>
        <Button
          type="primary"
          loading={dangLuu}
          disabled={dangTai || Boolean(loi) || ds.length === 0}
          onClick={async () => {
            setDangLuu(true);
            try {
              await chinhSachApi.capNhat(ds);
              message.success("Đã lưu chính sách.");
              await tai();
            } catch (e) {
              message.error(layThongBao(e));
            } finally {
              setDangLuu(false);
            }
          }}
        >
          Lưu thay đổi
        </Button>
      </div>
      {loi && <Alert type="error" message={loi} />}
      <Table
        loading={dangTai}
        rowKey={(_, i) => i}
        dataSource={ds}
        columns={[
          {
            title: "Số ngày tối thiểu",
            dataIndex: "soNgayToiThieu",
            render: (v, _, i) => (
              <InputNumber
                min={0}
                value={v}
                onChange={(x) =>
                  setDs(
                    ds.map((b, j) =>
                      j === i ? { ...b, soNgayToiThieu: x } : b,
                    ),
                  )
                }
              />
            ),
          },
          {
            title: "Tỷ lệ hoàn (%)",
            dataIndex: "tyLeHoan",
            render: (v, _, i) => (
              <InputNumber
                min={0}
                max={100}
                value={v}
                onChange={(x) =>
                  setDs(ds.map((b, j) => (j === i ? { ...b, tyLeHoan: x } : b)))
                }
              />
            ),
          },
        ]}
      />
    </section>
  );
}
