import * as NenTang from "../NenTangGiaoDien.jsx";
const {
  useState,
  Alert,
  Button,
  Card,
  Form,
  Input,
  InputNumber,
  Modal,
  Space,
  Spin,
  message,
  layLoaiPhong,
  themLoaiPhong,
  suaLoaiPhong,
  doiHoatDongLoaiPhong,
  layAnhLoaiPhong,
  taiAnhLoaiPhong,
  xoaAnhLoaiPhong,
  datAnhDaiDienLoaiPhong,
  layThongBao,
  layDanhSach,
  dinhDangTien,
} = NenTang;
import { TrangDuLieu } from "./TrangDuLieu.jsx";

export function TrangLoaiPhong() {
  const [loaiDangQuanLy, setLoaiDangQuanLy] = useState(null);
  const [danhSachAnh, setDanhSachAnh] = useState([]);
  const [dangTaiAnh, setDangTaiAnh] = useState(false);
  const [dangGuiAnh, setDangGuiAnh] = useState(false);
  const [loiTaiAnh, setLoiTaiAnh] = useState("");

  async function napAnh(ma = loaiDangQuanLy?.maLoaiPhong) {
    if (!ma) return;
    setDangTaiAnh(true);
    setLoiTaiAnh("");
    try {
      setDanhSachAnh(layDanhSach(await layAnhLoaiPhong(ma)));
    } catch (e) {
      setLoiTaiAnh(layThongBao(e));
    } finally {
      setDangTaiAnh(false);
    }
  }

  async function moQuanLyAnh(loai) {
    setLoaiDangQuanLy(loai);
    setDanhSachAnh([]);
    await napAnh(loai.maLoaiPhong);
  }

  function duongDanAnh(anh) {
    const duongDan = anh.duongDanAnh || anh.duongDan || "";
    if (duongDan.startsWith("http")) return duongDan;
    const gocApi = import.meta.env.PROD
      ? import.meta.env.VITE_DUONG_DAN_API || "/api"
      : "/api";
    if (duongDan.startsWith("/api/")) return `${gocApi}${duongDan.slice(4)}`;
    if (duongDan.startsWith("/")) return duongDan;
    return `${gocApi}/cong-khai/anh-phong/${encodeURIComponent(duongDan.split("/").pop())}`;
  }

  function chonAnhDeTai() {
    const input = document.createElement("input");
    input.type = "file";
    input.multiple = true;
    input.accept = ".jpg,.jpeg,.png,image/jpeg,image/png";
    input.style.position = "fixed";
    input.style.left = "-10000px";
    input.onchange = async () => {
      const tep = [...(input.files || [])];
      input.remove();
      if (!tep.length || !loaiDangQuanLy) return;
      const tepSai = tep.find((anh) =>
        !/\.(jpg|jpeg|png)$/i.test(anh.name) || anh.size > 5 * 1024 * 1024,
      );
      if (tepSai) {
        message.error("Chỉ nhận ảnh JPG, JPEG hoặc PNG tối đa 5 MB mỗi ảnh.");
        return;
      }
      setDangGuiAnh(true);
      try {
        await taiAnhLoaiPhong(loaiDangQuanLy.maLoaiPhong, tep);
        message.success("Đã tải và lưu ảnh loại phòng.");
        await napAnh();
      } catch (e) {
        message.error(layThongBao(e));
      } finally {
        setDangGuiAnh(false);
      }
    };
    input.oncancel = () => input.remove();
    document.body.appendChild(input);
    input.click();
  }

  return (
    <>
      <TrangDuLieu
        tieuDe="Loại phòng và giá"
        hamTai={({ tuKhoa }) => layLoaiPhong({ tuKhoa })}
        nutThem="Thêm loại phòng"
        cot={[
          { title: "Tên loại", dataIndex: "ten" },
          { title: "Sức chứa", dataIndex: "sucChua" },
          { title: "Giá ngày thường", dataIndex: "giaNgayThuong", render: dinhDangTien },
          { title: "Giá cuối tuần", dataIndex: "giaCuoiTuan", render: dinhDangTien },
          { title: "Hoạt động", dataIndex: "dangHoatDong", render: (v) => (v ? "Đang dùng" : "Ngừng dùng") },
        ]}
        formTruong={
          <>
            <Form.Item name="ten" label="Tên loại phòng" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item name="moTa" label="Mô tả"><Input.TextArea rows={3} /></Form.Item>
            <Form.Item name="sucChua" label="Sức chứa" rules={[{ required: true }]}><InputNumber min={1} className="RongDay" /></Form.Item>
            <Form.Item name="giaNgayThuong" label="Giá ngày thường" rules={[{ required: true }]}><InputNumber min={1} className="RongDay" /></Form.Item>
            <Form.Item name="giaCuoiTuan" label="Giá cuối tuần" rules={[{ required: true }]}><InputNumber min={1} className="RongDay" /></Form.Item>
          </>
        }
        hamLuu={(d) => themLoaiPhong(d)}
        hamSua={(ma, d) => suaLoaiPhong(ma, d)}
        thaoTac={(r, tai) => (
          <Space wrap>
            <Button onClick={() => moQuanLyAnh(r)}>Quản lý ảnh</Button>
            <Button
              danger={r.dangHoatDong}
              onClick={() => Modal.confirm({
                title: r.dangHoatDong ? "Ngừng sử dụng loại phòng?" : "Sử dụng lại loại phòng?",
                content: r.dangHoatDong
                  ? "Loại phòng sẽ không được dùng cho đơn mới. Dữ liệu và đơn cũ vẫn được giữ."
                  : "Loại phòng sẽ xuất hiện lại trong các chức năng tạo đơn.",
                okText: r.dangHoatDong ? "Ngừng sử dụng" : "Sử dụng lại",
                cancelText: "Quay lại",
                onOk: async () => {
                  try {
                    await doiHoatDongLoaiPhong(r.maLoaiPhong, !r.dangHoatDong);
                    message.success(r.dangHoatDong ? "Đã ngừng sử dụng loại phòng." : "Đã sử dụng lại loại phòng.");
                    await tai();
                  } catch (e) {
                    message.error(layThongBao(e));
                  }
                },
              })}
            >
              {r.dangHoatDong ? "Ngừng sử dụng" : "Sử dụng lại"}
            </Button>
          </Space>
        )}
      />
      <Modal
        title={`Ảnh loại phòng ${loaiDangQuanLy?.ten || ""}`}
        open={Boolean(loaiDangQuanLy)}
        onCancel={() => {
          setLoaiDangQuanLy(null);
          setLoiTaiAnh("");
        }}
        footer={null}
        width={800}
      >
        <Space className="KhoangDuoi" wrap>
          <Button type="primary" loading={dangGuiAnh} onClick={chonAnhDeTai}>Tải ảnh</Button>
          <Button onClick={() => napAnh()} disabled={dangTaiAnh}>Tải lại ảnh</Button>
          <span>Chọn một hay nhiều ảnh JPG, JPEG hoặc PNG; tối đa 5 MB mỗi ảnh.</span>
        </Space>
        {loiTaiAnh ? (
          <Alert type="error" showIcon message={loiTaiAnh} />
        ) : dangTaiAnh ? (
          <Spin tip="Đang tải danh sách ảnh..." />
        ) : (
          <>
            <div className="LuoiAnhPhong">
              {danhSachAnh.map((anh) => (
                <Card
                  key={anh.maAnhLoaiPhong}
                  cover={<img className="AnhQuanLy" src={duongDanAnh(anh)} alt={loaiDangQuanLy?.ten || "Ảnh loại phòng"} loading="lazy" />}
                >
                  <Space wrap>
                    {Number(anh.thuTuHienThi) === 0 && <strong>Ảnh đại diện</strong>}
                    {Number(anh.thuTuHienThi) !== 0 && (
                      <Button onClick={async () => {
                        try {
                          await datAnhDaiDienLoaiPhong(loaiDangQuanLy.maLoaiPhong, anh.maAnhLoaiPhong);
                          message.success("Đã đặt ảnh đại diện.");
                          await napAnh();
                        } catch (e) { message.error(layThongBao(e)); }
                      }}>Đặt ảnh đại diện</Button>
                    )}
                    <Button danger onClick={() => Modal.confirm({
                      title: "Xóa ảnh loại phòng?",
                      content: "Ảnh này sẽ bị gỡ khỏi loại phòng và trang khách.",
                      okText: "Xóa ảnh",
                      cancelText: "Quay lại",
                      onOk: async () => {
                        try {
                          await xoaAnhLoaiPhong(loaiDangQuanLy.maLoaiPhong, anh.maAnhLoaiPhong);
                          message.success("Đã xóa ảnh loại phòng.");
                          await napAnh();
                        } catch (e) { message.error(layThongBao(e)); }
                      },
                    })}>Xóa ảnh</Button>
                  </Space>
                </Card>
              ))}
            </div>
            {!danhSachAnh.length && <p>Loại phòng chưa có ảnh.</p>}
          </>
        )}
      </Modal>
    </>
  );
}
