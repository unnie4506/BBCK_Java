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
  layMotPhong,
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

export function TrangPhong() {
  const [danhSachLoai, setDanhSachLoai] = useState([]);
  const [phongDangQuanLy, setPhongDangQuanLy] = useState(null);
  const [danhSachAnh, setDanhSachAnh] = useState([]);
  const [dangTaiAnh, setDangTaiAnh] = useState(false);
  const [loiTaiAnh, setLoiTaiAnh] = useState("");
  const [loiTaiLoai, setLoiTaiLoai] = useState("");
  useEffect(() => {
    layLoaiPhong({ dangHoatDong: true })
      .then((r) => {
        setDanhSachLoai(layDanhSach(r));
        setLoiTaiLoai("");
      })
      .catch((e) => setLoiTaiLoai(layThongBao(e)));
  }, []);
  async function moQuanLyAnh(phong) {
    setPhongDangQuanLy(phong);
    setDanhSachAnh([]);
    setLoiTaiAnh("");
    setDangTaiAnh(true);
    try {
      const duLieu = await layMotPhong(phong.maPhong);
      setDanhSachAnh(duLieu?.danhSachAnh || []);
    } catch (e) {
      setLoiTaiAnh(layThongBao(e));
    } finally {
      setDangTaiAnh(false);
    }
  }
  async function napLaiAnh() {
    if (!phongDangQuanLy) return;
    try {
      const duLieu = await layMotPhong(phongDangQuanLy.maPhong);
      setDanhSachAnh(duLieu?.danhSachAnh || []);
    } catch (e) {
      setLoiTaiAnh(layThongBao(e));
    }
  }
  const duongDanAnh = (anh) => {
    const duongDan = anh.duongDanAnh || anh.duongDan || anh.tenFile || "";
    if (duongDan.startsWith("http"))
      return duongDan;
    if (duongDan.startsWith("/api/")) {
      const gocApi = import.meta.env.PROD
        ? import.meta.env.VITE_DUONG_DAN_API || "/api"
        : "/api";
      return `${gocApi}${duongDan.slice(4)}`;
    }
    if (duongDan.startsWith("/")) return duongDan;
    const tenFile = duongDan.split("/").pop();
    const duongDanApi = import.meta.env.PROD
      ? import.meta.env.VITE_DUONG_DAN_API || "/api"
      : "/api";
    return `${duongDanApi}/cong-khai/anh-phong/${encodeURIComponent(tenFile)}`;
  };
  return (
    <>
      <TrangDuLieu
        tieuDe="Phòng"
        hamTai={({ tuKhoa }) => layPhong({ tuKhoa })}
        nutThem="Thêm phòng"
        cot={[
          { title: "Số phòng", dataIndex: "soPhong" },
          { title: "Tầng", dataIndex: "tang" },
          { title: "Loại phòng", dataIndex: "tenLoaiPhong" },
          { title: "Trạng thái", dataIndex: "trangThai" },
          {
            title: "Hoạt động",
            dataIndex: "dangHoatDong",
            render: (v) => (v ? "Đang dùng" : "Ngừng dùng"),
          },
        ]}
        formTruong={
          <>
            <Form.Item
              name="soPhong"
              label="Số phòng"
              rules={[{ required: true }]}
            >
              <Input />
            </Form.Item>
            <Form.Item name="tang" label="Tầng" rules={[{ required: true }]}>
              <InputNumber min={1} className="RongDay" />
            </Form.Item>
            <Form.Item
              name="maLoaiPhong"
              label="Loại phòng"
              rules={[{ required: true }]}
            >
              <Select
                options={danhSachLoai.map((r) => ({
                  value: r.maLoaiPhong,
                  label: r.ten,
                }))}
              />
            </Form.Item>
            <Form.Item name="moTa" label="Mô tả">
              <Input.TextArea />
            </Form.Item>
            <Form.Item
              name="danhSachMaTienNghi"
              label="Mã tiện nghi, phân cách bằng dấu phẩy"
            >
              <Input />
            </Form.Item>
          </>
        }
        hamLuu={(d) =>
          luuPhong(null, {
            ...d,
            danhSachMaTienNghi: String(d.danhSachMaTienNghi || "")
              .split(",")
              .map(Number)
              .filter(Boolean),
          })
        }
        hamSua={(ma, d) =>
          luuPhong(ma, {
            ...d,
            danhSachMaTienNghi: String(d.danhSachMaTienNghi || "")
              .split(",")
              .map(Number)
              .filter(Boolean),
          })
        }
        thaoTac={(phong, tai) => (
          <Space wrap>
            <Select
              aria-label="Trạng thái phòng"
              disabled={phong.trangThai === "DANG_O"}
              defaultValue={
                phong.trangThai === "DANG_O" ? "TRONG" : phong.trangThai
              }
              options={[
                { value: "TRONG", label: "Trống" },
                { value: "DANG_DON", label: "Đang dọn" },
                { value: "BAO_TRI", label: "Bảo trì" },
              ]}
              onChange={async (trangThai) => {
                try {
                  await doiTrangThaiPhong(phong.maPhong, trangThai);
                  message.success("Đã cập nhật trạng thái.");
                  tai();
                } catch (e) {
                  message.error(layThongBao(e));
                }
              }}
            />
            <Button
              danger={phong.dangHoatDong}
              onClick={() =>
                Modal.confirm({
                  title: phong.dangHoatDong ? "Ngừng sử dụng phòng?" : "Sử dụng lại phòng?",
                  content: phong.dangHoatDong
                    ? "Phòng sẽ không được đặt mới. Dữ liệu và lịch sử đơn cũ vẫn được giữ."
                    : "Phòng sẽ xuất hiện lại trong sơ đồ và tìm phòng trống.",
                  okText: phong.dangHoatDong ? "Ngừng sử dụng" : "Sử dụng lại",
                  cancelText: "Quay lại",
                  onOk: async () => {
                    try {
                      await doiHoatDongPhong(phong.maPhong, !phong.dangHoatDong);
                      message.success(phong.dangHoatDong ? "Đã ngừng sử dụng phòng." : "Đã sử dụng lại phòng.");
                      await tai();
                    } catch (e) {
                      message.error(layThongBao(e));
                    }
                  },
                })
              }
            >
              {phong.dangHoatDong ? "Ngừng sử dụng" : "Sử dụng lại"}
            </Button>
            <Button
              onClick={() => {
                const tep = document.createElement("input");
                tep.type = "file";
                tep.multiple = true;
                tep.accept = ".jpg,.jpeg,.png";
                tep.style.position = "fixed";
                tep.style.left = "-10000px";
                tep.style.top = "0";
                tep.onchange = async () => {
                  const danhSachTep = [...tep.files];
                  tep.remove();
                  const tepSai = danhSachTep.find(
                    (tepAnh) =>
                      !/\.(jpg|jpeg|png)$/i.test(tepAnh.name) ||
                      tepAnh.size > 5 * 1024 * 1024,
                  );
                  if (tepSai) {
                    message.error(
                      "Chỉ nhận ảnh JPG, JPEG hoặc PNG tối đa 5 MB mỗi ảnh.",
                    );
                    return;
                  }
                  try {
                    await taiAnhPhong(phong.maPhong, danhSachTep);
                    message.success("Đã tải và lưu ảnh phòng.");
                    await moQuanLyAnh(phong);
                  } catch (e) {
                    message.error(layThongBao(e));
                  }
                };
                tep.oncancel = () => tep.remove();
                document.body.appendChild(tep);
                tep.click();
              }}
            >
              Tải ảnh
            </Button>
            <Button onClick={() => moQuanLyAnh(phong)}>Quản lý ảnh</Button>
          </Space>
        )}
      />
      {loiTaiLoai && (
        <Alert
          className="KhoangTren"
          type="error"
          showIcon
          message="Không thể tải danh sách loại phòng"
          description={loiTaiLoai}
        />
      )}
      <Modal
        title={`Ảnh phòng ${phongDangQuanLy?.soPhong || ""}`}
        open={Boolean(phongDangQuanLy)}
        onCancel={() => {
          setPhongDangQuanLy(null);
          setLoiTaiAnh("");
        }}
        footer={null}
        width={800}
      >
        {dangTaiAnh ? (
          <Spin tip="Đang tải danh sách ảnh..." />
        ) : loiTaiAnh ? (
          <Alert type="error" showIcon message={loiTaiAnh} />
        ) : (
          <>
            <div className="LuoiAnhPhong">
              {danhSachAnh.map((anh) => (
            <Card
              key={anh.maAnhPhong || anh.maAnh}
              cover={
                <img
                  className="AnhQuanLy"
                  src={duongDanAnh(anh)}
                  alt={`Ảnh phòng ${phongDangQuanLy?.soPhong || ""}`}
                  loading="lazy"
                  onError={(suKien) => {
                    suKien.currentTarget.onerror = null;
                    suKien.currentTarget.src =
                      "data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22600%22 height=%22360%22 viewBox=%220 0 600 360%22%3E%3Crect width=%22600%22 height=%22360%22 fill=%22%23e8eee9%22/%3E%3Cpath d=%22M0 290 180 145l110 90 90-120 220 175v70H0z%22 fill=%22%23bfd1c5%22/%3E%3Ctext x=%22300%22 y=%22325%22 text-anchor=%22middle%22 fill=%22%2326352a%22 font-size=%2224%22 font-family=%22Arial%22%3EChua co tep anh%3C/text%3E%3C/svg%3E";
                  }}
                />
              }
            >
              <Space wrap>
                <Button
                  onClick={async () => {
                    try {
                      await datAnhDaiDien(
                        phongDangQuanLy.maPhong,
                        anh.maAnhPhong || anh.maAnh,
                      );
                      message.success("Đã đặt ảnh đại diện.");
                      await napLaiAnh();
                    } catch (e) {
                      message.error(layThongBao(e));
                    }
                  }}
                >
                  Đặt ảnh đại diện
                </Button>
                <Button
                  danger
                  onClick={() =>
                    Modal.confirm({
                      title: "Xóa ảnh phòng?",
                      content: "Thao tác này không thể hoàn tác.",
                      okText: "Xóa ảnh",
                      cancelText: "Hủy",
                      onOk: async () => {
                        try {
                          await xoaAnhPhong(
                            phongDangQuanLy.maPhong,
                            anh.maAnhPhong || anh.maAnh,
                          );
                          message.success("Đã xóa ảnh phòng.");
                          await napLaiAnh();
                        } catch (e) {
                          message.error(layThongBao(e));
                        }
                      },
                    })
                  }
                >
                  Xóa ảnh
                </Button>
              </Space>
            </Card>
              ))}
            </div>
            {!danhSachAnh.length && <p>Phòng chưa có ảnh.</p>}
          </>
        )}
      </Modal>
    </>
  );
}
