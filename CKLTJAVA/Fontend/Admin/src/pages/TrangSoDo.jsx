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

export function TrangSoDo() {
  const [ngay, setNgay] = useState(dayjs());
  const [duLieu, setDuLieu] = useState(null);
  const [loi, setLoi] = useState("");
  const [dangTai, setDangTai] = useState(false);
  const [phongChon, setPhongChon] = useState(null);
  const [trangThaiMoi, setTrangThaiMoi] = useState("TRONG");
  async function taiLai() {
    setDangTai(true);
    setLoi("");
    try {
      const r = await laySoDo(ngay.format("YYYY-MM-DD"));
      setDuLieu(r);
    } catch (e) {
      setLoi(layThongBao(e));
    } finally {
      setDangTai(false);
    }
  }
  async function capNhatTrangThai() {
    try {
      await doiTrangThaiPhong(phongChon.maPhong, trangThaiMoi);
      message.success("Đã cập nhật trạng thái phòng.");
      setPhongChon(null);
      await taiLai();
    } catch (e) {
      message.error(layThongBao(e));
    }
  }
  useEffect(() => {
    taiLai();
  }, [ngay]);
  const tang = duLieu?.danhSachTang || duLieu?.tang || [];
  const lopTrangThai = (trangThai) =>
    ({
      TRONG: "Trong",
      DA_DAT: "DaDat",
      DANG_O: "DangO",
      DANG_DON: "DangDon",
      BAO_TRI: "BaoTri",
    })[trangThai] || "Trong";
  return (
    <section>
      <div className="HangTieuDe">
        <div>
          <h1>Sơ đồ phòng</h1>
          <p>Trạng thái phòng theo ngày {dinhDangNgay(ngay)}.</p>
        </div>
        <DatePicker
          value={ngay}
          onChange={(m) => m && setNgay(m)}
          format="DD/MM/YYYY"
        />
      </div>
      {loi && <Alert type="error" message={loi} />}
      <Spin spinning={dangTai}>
        <div className="HangTheThongKe">
          {["TRONG", "DA_DAT", "DANG_O", "DANG_DON", "BAO_TRI"].map(
            (trangThai) => (
              <Card key={trangThai}>
                <span>{trangThai}</span>
                <h2>{duLieu?.tongTheoTrangThai?.[trangThai] ?? 0}</h2>
              </Card>
            ),
          )}
        </div>
        {tang.map((motTang) => (
          <Card
            key={motTang.soTang}
            title={`Tầng ${motTang.soTang} · ${motTang.tongSoPhong} phòng`}
            className="KhoangDuoi"
          >
            <div className="LuoiPhong">
              {(motTang.danhSachPhong || []).map((phong) => (
                <button
                  type="button"
                  className={`OPhong OPhong${lopTrangThai(phong.trangThaiHienThi)}`}
                  key={phong.maPhong}
                  onClick={() => {
                    setPhongChon(phong);
                    setTrangThaiMoi(
                      phong.trangThaiHienThi === "DA_DAT"
                        ? "TRONG"
                        : phong.trangThaiHienThi,
                    );
                  }}
                >
                  <b>{phong.soPhong}</b>
                  <span>{phong.tenLoaiPhong}</span>
                  <small>{phong.trangThaiHienThi}</small>
                </button>
              ))}
            </div>
          </Card>
        ))}
      </Spin>
      <Modal
        title={`Phòng ${phongChon?.soPhong || ""}`}
        open={Boolean(phongChon)}
        onCancel={() => setPhongChon(null)}
        onOk={capNhatTrangThai}
        okButtonProps={{ disabled: phongChon?.trangThaiHienThi === "DANG_O" }}
        okText="Lưu trạng thái"
        cancelText="Đóng"
      >
        {phongChon?.trangThaiHienThi === "DANG_O" ? (
          <Alert
            type="warning"
            message="Phòng đang có khách lưu trú, không thể đổi trạng thái bằng tay."
          />
        ) : (
          <>
            <p>
              {phongChon?.tenLoaiPhong} · trạng thái hiện tại:{" "}
              {phongChon?.trangThaiHienThi}
            </p>
            <Select
              className="RongDay"
              value={trangThaiMoi}
              onChange={setTrangThaiMoi}
              options={[
                { value: "TRONG", label: "Trống" },
                { value: "DANG_DON", label: "Đang dọn" },
                { value: "BAO_TRI", label: "Bảo trì" },
              ]}
            />
          </>
        )}
      </Modal>
    </section>
  );
}
