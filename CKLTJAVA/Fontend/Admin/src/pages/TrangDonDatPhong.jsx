import * as NenTang from "../NenTangGiaoDien.jsx";
import { docDuLieu } from "../services/axios.js";
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
  layMotKhach,
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

export function TrangDonDatPhong() {
  const [modal, modalContextHolder] = Modal.useModal();
  const [danhSach, setDanhSach] = useState([]);
  const [don, setDon] = useState(null);
  const [hoaDon, setHoaDon] = useState(null);
  const [moHoaDon, setMoHoaDon] = useState(false);
  const [xemTruocTra, setXemTruocTra] = useState(null);
  const [moTinhTien, setMoTinhTien] = useState(false);
  const [ngayTraThucTe, setNgayTraThucTe] = useState("");
  const [dangTaiTinhTien, setDangTaiTinhTien] = useState(false);
  const [loiTinhTien, setLoiTinhTien] = useState("");
  const [maChiTietDangNhan, setMaChiTietDangNhan] = useState(null);
  const [maChiTietDangHuy, setMaChiTietDangHuy] = useState(null);
  const [dangTaiXemTruocHuy, setDangTaiXemTruocHuy] = useState(false);
  const [dangHuyDon, setDangHuyDon] = useState(false);
  const [dangTraPhong, setDangTraPhong] = useState(false);
  const [dangThanhToan, setDangThanhToan] = useState(false);
  const [loi, setLoi] = useState("");
  const [tuKhoa, setTuKhoa] = useState("");
  const [trangThai, setTrangThai] = useState("");
  const [nguonDat, setNguonDat] = useState("");
  const [dangTai, setDangTai] = useState(false);
  const [loiThaoTac, setLoiThaoTac] = useState("");
  const [moTao, setMoTao] = useState(false);
  const [dangTao, setDangTao] = useState(false);
  const [loiTao, setLoiTao] = useState("");
  const [ngayNhanLoc, setNgayNhanLoc] = useState("");
  const [dongGiaHan, setDongGiaHan] = useState(null);
  const [moGiaHan, setMoGiaHan] = useState(false);
  const [dangGiaHan, setDangGiaHan] = useState(false);
  const [moHoSoKhach, setMoHoSoKhach] = useState(false);
  const [dangLuuHoSoKhach, setDangLuuHoSoKhach] = useState(false);
  const [danhSachKhach, setDanhSachKhach] = useState([]);
  const [phongTrongTheoDong, setPhongTrongTheoDong] = useState({});
  const [dangTaiPhongTheoDong, setDangTaiPhongTheoDong] = useState({});
  const [loiTaiPhongTheoDong, setLoiTaiPhongTheoDong] = useState({});
  const [formTao] = Form.useForm();
  const [formThanhToan] = Form.useForm();
  const [formGiaHan] = Form.useForm();
  const [formHoSoKhach] = Form.useForm();
  const cacDongTao = Form.useWatch("danhSachPhong", formTao) || [];
  const hinhThucKhach = Form.useWatch("hinhThucKhach", formTao) || "CO_SAN";
  const ngayHomNay = dayjs().format("YYYY-MM-DD");
  const khoaNgayDat = JSON.stringify(
    cacDongTao.map((dong) => [dong?.ngayNhan || "", dong?.ngayTra || ""]),
  );
  const tongTienDuKien = cacDongTao.reduce((tong, dong, viTri) => {
    const phong = (phongTrongTheoDong[viTri] || []).find(
      (muc) => Number(muc.maPhong) === Number(dong?.maPhong),
    );
    return tong + Number(phong?.tienPhongDuKien || 0);
  }, 0);

  useEffect(() => {
    if (!moTao) return undefined;
    let conHieuLuc = true;

    async function taiPhongTrongChoTungDong() {
      const ketQua = await Promise.all(
        cacDongTao.map(async (dong, viTri) => {
          if (!dong?.ngayNhan || !dong?.ngayTra || dong.ngayTra <= dong.ngayNhan) {
            return [viTri, { danhSach: [], loi: "" }];
          }

          try {
            const phanHoi = await donDatPhongApi.phongTrong({
              ngayNhan: dong.ngayNhan,
              ngayTra: dong.ngayTra,
            });
            return [viTri, { danhSach: docDuLieu(phanHoi) || [], loi: "" }];
          } catch (e) {
            return [viTri, { danhSach: [], loi: layThongBao(e) }];
          }
        }),
      );

      if (conHieuLuc) {
        setPhongTrongTheoDong(
          Object.fromEntries(ketQua.map(([viTri, kq]) => [viTri, kq.danhSach])),
        );
        setLoiTaiPhongTheoDong(
          Object.fromEntries(
            ketQua
              .filter(([, kq]) => kq.loi)
              .map(([viTri, kq]) => [viTri, kq.loi]),
          ),
        );
        setDangTaiPhongTheoDong({});
      }
    }

    setDangTaiPhongTheoDong(
      Object.fromEntries(
        cacDongTao
          .map((dong, viTri) => [
            viTri,
            Boolean(dong?.ngayNhan && dong?.ngayTra && dong.ngayTra > dong.ngayNhan),
          ])
          .filter(([, dangTai]) => dangTai),
      ),
    );
    setLoiTaiPhongTheoDong({});
    taiPhongTrongChoTungDong();
    return () => {
      conHieuLuc = false;
    };
  }, [moTao, khoaNgayDat]);
  async function tai() {
    setDangTai(true);
    try {
      const r = await donDatPhongApi.danhSach({
        tuKhoa,
        trangThai: trangThai || undefined,
        tuNgay: ngayNhanLoc || undefined,
        denNgay: ngayNhanLoc || undefined,
        nguonDat: nguonDat || undefined,
        trang: 1,
        kichThuoc: 20,
      });
      setDanhSach(layDanhSach(r.data?.duLieu));
      setLoi("");
    } catch (e) {
      setLoi(layThongBao(e));
    } finally {
      setDangTai(false);
    }
  }
  function doiNgayDat(viTri) {
    formTao.setFieldValue(["danhSachPhong", viTri, "maPhong"], undefined);
    setPhongTrongTheoDong((hienTai) => ({ ...hienTai, [viTri]: [] }));
    setLoiTaiPhongTheoDong((hienTai) => ({ ...hienTai, [viTri]: "" }));
  }
  async function moFormTao() {
    try {
      setDanhSachKhach(
        layDanhSach(await layKhach({ trang: 1, kichThuoc: 100 })).filter(
          (khach) => khach.dangHoatDong === true || khach.dangHoatDong === 1,
        ),
      );
    } catch (e) {
      message.error(layThongBao(e));
    }
    formTao.resetFields();
    formTao.setFieldsValue({
      hinhThucKhach: "CO_SAN",
      tienCoc: 0,
      danhSachPhong: [{ maPhong: null, ngayNhan: "", ngayTra: "" }],
    });
    setLoiTao("");
    setMoTao(true);
  }
  async function taoDon(duLieu) {
    setDangTao(true);
    setLoiTao("");
    try {
      const { hinhThucKhach: hinhThuc, hoTen, soDienThoai, soGiayTo, thuDienTu, ...duLieuDon } = duLieu;
      await donDatPhongApi.tao({
        ...duLieuDon,
        maKhachHang: hinhThuc === "VANG_LAI" ? null : Number(duLieu.maKhachHang),
        ...(hinhThuc === "VANG_LAI" ? { khachHangVangLai: { hoTen, soDienThoai, soGiayTo, thuDienTu } } : {}),
        danhSachPhong: duLieu.danhSachPhong.map((dong) => ({
          ...dong,
          maPhong: Number(dong.maPhong),
        })),
      });
      message.success("Đã tạo đơn đặt phòng.");
      setMoTao(false);
      await tai();
    } catch (e) {
      const thongBao = layThongBao(e);
      setLoiTao(thongBao);
      message.error(thongBao);
    } finally {
      setDangTao(false);
    }
  }
  function baoLoiFormTao({ errorFields }) {
    const loiDauTien = errorFields[0];
    const thongBao = loiDauTien?.errors?.[0] || "Vui lòng kiểm tra lại thông tin đặt phòng.";
    setLoiTao(thongBao);
    message.error(thongBao);
    if (loiDauTien) formTao.scrollToField(loiDauTien.name);
  }
  async function xem(ma) {
    try {
      const duLieuDon = docDuLieu(await donDatPhongApi.chiTiet(ma));
      setLoiThaoTac("");
      setDon(duLieuDon);
      setHoaDon(null);
      setMoHoaDon(false);

      // Hóa đơn chỉ xuất hiện sau khi Quản lý trả phòng cuối và hệ thống lập hóa đơn.
      try {
        const duLieuHoaDon = docDuLieu(await donDatPhongApi.hoaDonTheoDon(ma));
        setHoaDon(duLieuHoaDon);
        formThanhToan.setFieldsValue({
          phuongThuc: "TIEN_MAT",
          soTien: Number(duLieuHoaDon?.soTienConPhaiThu || 0),
        });
      } catch {
        setHoaDon(null);
      }
    } catch (e) {
      message.error(layThongBao(e));
    }
  }
  async function lamMoiDon() {
    if (don) await xem(don.maDonDatPhong);
    await tai();
  }
  async function nhanPhong(maChiTiet) {
    setMaChiTietDangNhan(maChiTiet);
    setLoiThaoTac("");
    try {
      await donDatPhongApi.nhanPhong(maChiTiet, { xacNhanNhanSom: false });
      message.success("Đã ghi nhận nhận phòng.");
      await lamMoiDon();
    } catch (e) {
      if (e.response?.data?.maLoi === "CAN_XAC_NHAN") {
        modal.confirm({
          title: "Nhận phòng sớm",
          content: "Xác nhận cho khách nhận phòng sớm?",
          okText: "Xác nhận",
          cancelText: "Quay lại",
          onOk: async () => {
            setMaChiTietDangNhan(maChiTiet);
            try {
              await donDatPhongApi.nhanPhong(maChiTiet, {
                xacNhanNhanSom: true,
              });
              message.success("Đã ghi nhận nhận phòng.");
              await lamMoiDon();
            } catch (loiNhan) {
              const thongBao = layThongBao(loiNhan);
              setLoiThaoTac(thongBao);
              message.error(thongBao);
              throw loiNhan;
            } finally {
              setMaChiTietDangNhan(null);
            }
          },
        });
      } else {
        const thongBao = layThongBao(e);
        setLoiThaoTac(thongBao);
        message.error(thongBao);
      }
    } finally {
      setMaChiTietDangNhan(null);
    }
  }
  function moFormGiaHan(r) {
    setDongGiaHan(r);
    formGiaHan.setFieldsValue({
      ngayTra: dayjs(r.ngayTra).add(1, "day").format("YYYY-MM-DD"),
    });
    setMoGiaHan(true);
  }
  async function giaHan(duLieu) {
    if (!dongGiaHan) return;
    setDangGiaHan(true);
    try {
      await donDatPhongApi.giaHan(dongGiaHan.maChiTietDatPhong || dongGiaHan.maChiTiet, {
        ngayTra: duLieu.ngayTra,
      });
      message.success(`Đã gia hạn phòng ${dongGiaHan.soPhong}.`);
      setMoGiaHan(false);
      setDongGiaHan(null);
      await lamMoiDon();
    } catch (e) {
      message.error(layThongBao(e));
    } finally {
      setDangGiaHan(false);
    }
  }
  async function moFormHoSoKhach() {
    if (!don?.maKhachHang) {
      message.error("Không tìm thấy mã hồ sơ khách của đơn này.");
      return;
    }
    try {
      formHoSoKhach.setFieldsValue(await layMotKhach(don.maKhachHang));
      setMoHoSoKhach(true);
    } catch (e) {
      message.error(layThongBao(e));
    }
  }
  async function luuHoSoKhach(duLieu) {
    if (!don?.maKhachHang) return;
    setDangLuuHoSoKhach(true);
    try {
      await luuKhach(don.maKhachHang, duLieu);
      message.success("Đã cập nhật hồ sơ khách.");
      setMoHoSoKhach(false);
      await xem(don.maDonDatPhong);
    } catch (e) {
      message.error(layThongBao(e));
    } finally {
      setDangLuuHoSoKhach(false);
    }
  }
  async function huyPhong(maChiTiet, soPhong) {
    if (!don) return;
    setLoiThaoTac("");
    setDangTaiXemTruocHuy(true);
    try {
      const phanHoi = await donDatPhongApi.xemTruocHuy(don.maDonDatPhong, maChiTiet);
      const duKien = docDuLieu(phanHoi);
      modal.confirm({
        title: `Hủy phòng ${soPhong || "đã chọn"}`,
        content: `Tiền cọc hoàn dự kiến cho phòng này: ${dinhDangTien(duKien?.soTienHoanDuKien || 0)}.`,
        okText: "Xác nhận hủy phòng",
        cancelText: "Quay lại",
        onOk: async () => {
          setMaChiTietDangHuy(maChiTiet);
          try {
            await donDatPhongApi.huyChiTiet(don.maDonDatPhong, maChiTiet, {
              lyDo: "Quản lý hủy phòng",
            });
            message.success(`Đã hủy phòng ${soPhong || "đã chọn"}.`);
            await lamMoiDon();
          } catch (e) {
            const thongBao = layThongBao(e);
            setLoiThaoTac(thongBao);
            message.error(thongBao);
            throw e;
          } finally {
            setMaChiTietDangHuy(null);
          }
        },
      });
    } catch (e) {
      const thongBao = layThongBao(e);
      setLoiThaoTac(thongBao);
      message.error(thongBao);
    } finally {
      setDangTaiXemTruocHuy(false);
    }
  }
  async function taiTinhTienTraPhong(maChiTiet, ngayTra = "") {
    setDangTaiTinhTien(true);
    setLoiTinhTien("");
    setXemTruocTra(null);
    try {
      const phanHoi = await donDatPhongApi.xemTruocTraPhong(maChiTiet, {
        ngayTraThucTe: ngayTra || null,
      });
      setXemTruocTra(docDuLieu(phanHoi));
    } catch (e) {
      const thongBao = layThongBao(e);
      setLoiTinhTien(thongBao);
      message.error(thongBao);
    } finally {
      setDangTaiTinhTien(false);
    }
  }

  async function moBangTinhTien(maChiTiet) {
    setNgayTraThucTe("");
    setXemTruocTra(null);
    setMoTinhTien(true);
    await taiTinhTienTraPhong(maChiTiet);
  }

  async function xacNhanTraPhong() {
    if (!xemTruocTra) return;
    setDangTraPhong(true);
    try {
      const phanHoi = await donDatPhongApi.traPhong(
        xemTruocTra.maChiTietDatPhong,
        { ngayTraThucTe: ngayTraThucTe || null },
      );
      const ketQua = docDuLieu(phanHoi);
      message.success(
        ketQua?.daLapHoaDon
          ? ketQua?.daHoanTatCheckOut
            ? "Đã trả phòng cuối, lập hóa đơn và thanh toán đủ."
            : `Đã lập hóa đơn. Cần thu thêm ${dinhDangTien(ketQua.soTienConPhaiThu)} để hoàn tất trả phòng cuối.`
          : "Đã trả phòng. Đơn còn phòng khác nên chưa lập hóa đơn.",
      );
      setMoTinhTien(false);
      await lamMoiDon();
    } catch (e) {
      message.error(layThongBao(e));
    } finally {
      setDangTraPhong(false);
    }
  }

  async function guiThanhToan(duLieu) {
    if (!hoaDon?.maHoaDon) return;
    setDangThanhToan(true);
    try {
      await donDatPhongApi.thanhToan(hoaDon.maHoaDon, {
        phuongThuc: duLieu.phuongThuc,
        soTien: Number(duLieu.soTien),
      });
      message.success("Đã ghi nhận khoản thanh toán.");
      const maDon = don.maDonDatPhong;
      await xem(maDon);
      setMoHoaDon(true);
      await tai();
    } catch (e) {
      message.error(layThongBao(e));
    } finally {
      setDangThanhToan(false);
    }
  }
  async function huyDon() {
    if (!don) return;
    setLoiThaoTac("");
    setDangTaiXemTruocHuy(true);
    try {
      const p = await donDatPhongApi.xemTruocHuy(don.maDonDatPhong);
      const x = docDuLieu(p);
      modal.confirm({
        title: "Hủy đơn đặt phòng",
        content: `Tiền hoàn dự kiến: ${dinhDangTien(x?.tongTienHoan || x?.soTienHoanDuKien)}.`,
        okText: "Hủy đơn",
        cancelText: "Quay lại",
        onOk: async () => {
          setDangHuyDon(true);
          try {
            await donDatPhongApi.huy(don.maDonDatPhong, {
              lyDo: "Quản lý hủy đơn",
            });
            message.success("Đã hủy đơn.");
            await lamMoiDon();
          } catch (e) {
            const thongBao = layThongBao(e);
            setLoiThaoTac(thongBao);
            message.error(thongBao);
            throw e;
          } finally {
            setDangHuyDon(false);
          }
        },
      });
    } catch (e) {
      const thongBao = layThongBao(e);
      setLoiThaoTac(thongBao);
      message.error(thongBao);
    } finally {
      setDangTaiXemTruocHuy(false);
    }
  }
  const coTheHuyToanDon =
    don?.trangThai === "DA_DAT" &&
    Array.isArray(don?.danhSachPhong) &&
    don.danhSachPhong.some((dong) => dong.trangThai === "DA_DAT") &&
    don.danhSachPhong.every((dong) => ["DA_DAT", "DA_HUY"].includes(dong.trangThai));

  useEffect(() => {
    tai();
    // Cập nhật danh sách và đơn đang mở để quản lý thấy ngay thay đổi do khách hủy.
    const boDem = window.setInterval(() => {
      if (document.visibilityState !== "visible") return;
      tai();
      if (don?.maDonDatPhong) xem(don.maDonDatPhong);
    }, 15000);
    return () => window.clearInterval(boDem);
  }, [trangThai, nguonDat, ngayNhanLoc, don?.maDonDatPhong]);
  const dong = don?.danhSachPhong || don?.danhSachChiTiet || [];
  return (
    <section>
      <div className="HangTieuDe">
        <div>
          <h1>Đơn đặt phòng</h1>
          <p>Đơn quản lý và đơn đặt trực tuyến dùng chung danh sách.</p>
        </div>
        <Space>
          <Button onClick={tai}>Tải lại</Button>
          <Button type="primary" onClick={moFormTao}>
            Tạo đơn
          </Button>
        </Space>
      </div>
      <Space wrap className="KhoangDuoi">
        <Input.Search
          className="TimKiem"
          placeholder="Mã đơn, tên khách hoặc số điện thoại"
          value={tuKhoa}
          onChange={(e) => setTuKhoa(e.target.value)}
          onSearch={tai}
        />
        <Input
          type="date"
          aria-label="Lọc theo ngày nhận phòng"
          value={ngayNhanLoc}
          onChange={(e) => setNgayNhanLoc(e.target.value)}
          style={{ width: 180 }}
        />
        <Button onClick={() => setNgayNhanLoc(ngayHomNay)}>Phòng đến nhận hôm nay</Button>
        {ngayNhanLoc && <Button onClick={() => setNgayNhanLoc("")}>Bỏ lọc ngày</Button>}
        <Select
          allowClear
          placeholder="Mọi trạng thái"
          value={trangThai || undefined}
          onChange={(v) => setTrangThai(v || "")}
          options={["DA_DAT", "DANG_O", "HOAN_TAT", "DA_HUY"].map((v) => ({
            value: v,
            label: v,
          }))}
        />
        <Select
          allowClear
          placeholder="Mọi nguồn đặt"
          value={nguonDat || undefined}
          onChange={(v) => setNguonDat(v || "")}
          options={[
            { value: "QUAN_LY", label: "Quản lý" },
            { value: "TRUC_TUYEN", label: "Đặt trực tuyến" },
          ]}
        />
      </Space>
      {loi && <Alert type="error" message={loi} className="KhoangDuoi" />}
      <Table
        rowKey="maDonDatPhong"
        loading={dangTai}
        dataSource={danhSach}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: "Chưa có đơn đặt phòng." }}
        columns={[
          {
            title: "Mã đơn",
            dataIndex: "maDonDatPhong",
            render: (ma) => (
              <Button type="link" onClick={() => xem(ma)}>
                {ma}
              </Button>
            ),
          },
          { title: "Khách hàng", dataIndex: "tenKhachHang" },
          {
            title: "Nguồn đặt",
            dataIndex: "nguonDat",
            render: (v) => (
              <Tag color={v === "TRUC_TUYEN" ? "blue" : "default"}>
                {v || "QUAN_LY"}
              </Tag>
            ),
          },
          { title: "Trạng thái", dataIndex: "trangThai" },
          { title: "Ngày nhận", dataIndex: "ngayNhan", render: dinhDangNgay },
          {
            title: "Tiền phòng",
            dataIndex: "tongTienPhong",
            render: dinhDangTien,
          },
          {
            title: "Thao tác",
            render: (_, r) => (
              <Button onClick={() => xem(r.maDonDatPhong)}>Chi tiết</Button>
            ),
          },
        ]}
      />
      {modalContextHolder}
      <Modal
        title={`Chi tiết đơn ${don?.maDonDatPhong || ""}`}
        open={Boolean(don)}
        onCancel={() => setDon(null)}
        footer={null}
        width={1400}
        destroyOnClose
      >
        {don && (
          <>
            {loiThaoTac ? (
              <Alert className="KhoangDuoi" type="error" showIcon message={loiThaoTac} />
            ) : null}
            <Card size="small" title="Thông tin khách hàng" className="KhoangDuoi">
              <Space direction="vertical" size="small">
                <span>
                  {don.hoTen || don.tenKhachHang || "Chưa có tên"} · {don.soDienThoai || "Chưa có số điện thoại"}
                </span>
                <span>
                  Số giấy tờ: {don.soGiayTo || <Tag color="red">Chưa bổ sung</Tag>}
                </span>
                {!String(don.soGiayTo || "").trim() && (
                  <Space wrap>
                    <Alert type="warning" showIcon message="Cần bổ sung số giấy tờ trước khi check-in." />
                    <Button onClick={moFormHoSoKhach}>Bổ sung hồ sơ khách</Button>
                  </Space>
                )}
              </Space>
            </Card>
            <p>
              Nguồn đặt: {don.nguonDat || (don.nguonTao === "QUAN_LY" ? "QUAN_LY" : "TRUC_TUYEN")} · Trạng thái: {don.trangThai} · Tiền cọc: {dinhDangTien(don.tienCoc ?? don.tienCocDaNhan)}
            </p>
            <p>Ghi chú: {don.ghiChu || "Không có ghi chú."}</p>
            <Table
              rowKey={(r) => r.maChiTietDatPhong || r.maChiTiet}
              dataSource={dong}
              scroll={{ x: 1500 }}
              pagination={false}
              columns={[
                { title: "Phòng", dataIndex: "soPhong", width: 90 },
                {
                  title: "Ngày nhận",
                  dataIndex: "ngayNhan",
                  render: dinhDangNgay,
                  width: 125,
                },
                {
                  title: "Ngày trả",
                  dataIndex: "ngayTra",
                  render: dinhDangNgay,
                  width: 125,
                },
                { title: "Số đêm", dataIndex: "soDem", width: 80 },
                {
                  title: "Giá ngày thường",
                  dataIndex: "giaNgayThuongLucDat",
                  render: dinhDangTien,
                  width: 145,
                },
                {
                  title: "Giá cuối tuần",
                  dataIndex: "giaCuoiTuanLucDat",
                  render: dinhDangTien,
                  width: 145,
                },
                { title: "Đêm thường", dataIndex: "soDemNgayThuong", width: 100 },
                { title: "Đêm cuối tuần", dataIndex: "soDemCuoiTuan", width: 110 },
                {
                  title: "Tiền phòng",
                  dataIndex: "tienPhong",
                  render: dinhDangTien,
                  width: 145,
                },
                { title: "Trạng thái", dataIndex: "trangThai", width: 110 },
                {
                  title: "Thao tác",
                  width: 250,
                  render: (_, r) => (
                    <Space>
                      {r.trangThai === "DA_DAT" && (
                        <>
                          <Button
                            loading={maChiTietDangNhan === (r.maChiTietDatPhong || r.maChiTiet)}
                            disabled={maChiTietDangNhan !== null || maChiTietDangHuy !== null}
                            onClick={() => nhanPhong(r.maChiTietDatPhong || r.maChiTiet)}
                          >
                            Nhận phòng
                          </Button>
                          <Button
                            danger
                            loading={maChiTietDangHuy === (r.maChiTietDatPhong || r.maChiTiet)}
                            disabled={maChiTietDangHuy !== null || maChiTietDangNhan !== null || dangTaiXemTruocHuy}
                            onClick={() => huyPhong(r.maChiTietDatPhong || r.maChiTiet, r.soPhong)}
                          >
                            Hủy phòng
                          </Button>
                        </>
                      )}
                      {r.trangThai === "DANG_O" && (
                        <>
                          {!hoaDon && (
                            <Button onClick={() => moFormGiaHan(r)}>Gia hạn</Button>
                          )}
                          <Button
                            onClick={() =>
                              hoaDon
                                ? setMoHoaDon(true)
                                : moBangTinhTien(r.maChiTietDatPhong || r.maChiTiet)
                            }
                          >
                            {hoaDon ? "Xem tiền và thu tiền" : "Tính tiền / trả phòng"}
                          </Button>
                        </>
                      )}
                    </Space>
                  ),
                },
              ]}
            />
            <Card size="small" className="KhoangTren">
              <Space size="large" wrap>
                <strong>Tổng tiền phòng: {dinhDangTien(don.tongTienPhong)}</strong>
                <span>Tiền cọc: {dinhDangTien(don.tienCoc ?? don.tienCocDaNhan)}</span>
              </Space>
            </Card>
            <Space className="KhoangTren">
              {hoaDon && (
                <Button type="primary" onClick={() => setMoHoaDon(true)}>
                  Xem hóa đơn {hoaDon.trangThai === "CHUA_THANH_TOAN" ? "· Chưa thanh toán đủ" : ""}
                </Button>
              )}
              {coTheHuyToanDon && (
                <Button
                  danger
                  loading={dangTaiXemTruocHuy || dangHuyDon}
                  disabled={dangTaiXemTruocHuy || dangHuyDon}
                  onClick={huyDon}
                >
                  Hủy đơn
                </Button>
              )}
            </Space>
          </>
        )}
      </Modal>
      <Modal
        title="Tính tiền khi trả phòng"
        open={moTinhTien}
        onCancel={() => setMoTinhTien(false)}
        maskClosable={false}
        onOk={xacNhanTraPhong}
        confirmLoading={dangTraPhong}
        okText="Xác nhận trả phòng"
        cancelText="Quay lại"
        okButtonProps={{ disabled: !xemTruocTra || dangTaiTinhTien || Boolean(loiTinhTien) }}
        width={700}
        destroyOnClose
      >
        {dangTaiTinhTien && <Spin />}
        {loiTinhTien && <Alert className="KhoangDuoi" type="error" showIcon message="Không thể tính tiền trả phòng" description={loiTinhTien} />}
        {xemTruocTra && (
          <>
            <p><strong>Phòng:</strong> {xemTruocTra.soPhong}</p>
            <p>
              <strong>Ngày nhận:</strong> {dinhDangNgay(xemTruocTra.ngayNhan)} ·{" "}
              <strong>Ngày trả dự kiến:</strong> {dinhDangNgay(xemTruocTra.ngayTraDuKien)}
            </p>
            <label htmlFor="NgayTraThucTe">Ngày trả thực tế (để trống nếu tính đủ ngày dự kiến)</label>
            <Input
              id="NgayTraThucTe"
              type="date"
              value={ngayTraThucTe}
              onChange={(e) => {
                const ngay = e.target.value;
                setNgayTraThucTe(ngay);
                taiTinhTienTraPhong(xemTruocTra.maChiTietDatPhong, ngay);
              }}
              className="KhoangDuoi"
            />
            <Card size="small" loading={dangTaiTinhTien}>
              <Space direction="vertical" size="small">
                <span>Số đêm: {xemTruocTra.soDem} (ngày thường {xemTruocTra.soDemNgayThuong}, cuối tuần {xemTruocTra.soDemCuoiTuan})</span>
                <span>Tiền phòng phòng này: {dinhDangTien(xemTruocTra.tienPhongPhongNay)}</span>
                {xemTruocTra.laPhongCuoi ? (
                  <>
                    <span>Tổng tiền phòng đơn: {dinhDangTien(xemTruocTra.tongTienPhongDon)}</span>
                    <span>Cọc được trừ: {dinhDangTien(xemTruocTra.tienCocDuocTru)}</span>
                    <strong>Còn phải thu: {dinhDangTien(xemTruocTra.soTienConPhaiThu)}</strong>
                  </>
                ) : (
                  <span>Đơn còn phòng khác chưa trả; hóa đơn sẽ lập khi trả phòng cuối.</span>
                )}
                <span>{xemTruocTra.thongBao}</span>
              </Space>
            </Card>
          </>
        )}
      </Modal>
      <Modal
        title={`Gia hạn phòng ${dongGiaHan?.soPhong || ""}`}
        open={moGiaHan}
        onCancel={() => {
          setMoGiaHan(false);
          setDongGiaHan(null);
        }}
        onOk={() => formGiaHan.submit()}
        confirmLoading={dangGiaHan}
        okText="Xác nhận gia hạn"
        cancelText="Quay lại"
        destroyOnClose
      >
        {dongGiaHan && (
          <>
            <p>Chỉ thay đổi ngày trả; phòng và ngày nhận sẽ được giữ nguyên.</p>
            <p>Ngày trả hiện tại: {dinhDangNgay(dongGiaHan.ngayTra)}</p>
            <Form form={formGiaHan} layout="vertical" onFinish={giaHan}>
              <Form.Item
                name="ngayTra"
                label="Ngày trả mới"
                rules={[
                  { required: true, message: "Chọn ngày trả mới." },
                  {
                    validator: (_, value) =>
                      !value || dayjs(value).isAfter(dayjs(dongGiaHan.ngayTra))
                        ? Promise.resolve()
                        : Promise.reject(new Error("Ngày trả mới phải sau ngày trả hiện tại.")),
                  },
                ]}
              >
                <Input
                  type="date"
                  min={dayjs(dongGiaHan.ngayTra).add(1, "day").format("YYYY-MM-DD")}
                />
              </Form.Item>
              <p>Hệ thống sẽ kiểm tra trùng lịch và tính lại tiền theo giá đã chốt khi đặt.</p>
            </Form>
          </>
        )}
      </Modal>
      <Modal
        title="Bổ sung hồ sơ khách hàng"
        open={moHoSoKhach}
        onCancel={() => setMoHoSoKhach(false)}
        onOk={() => formHoSoKhach.submit()}
        confirmLoading={dangLuuHoSoKhach}
        okText="Lưu hồ sơ"
        cancelText="Quay lại"
        destroyOnClose
      >
        <Form form={formHoSoKhach} layout="vertical" onFinish={luuHoSoKhach}>
          <Form.Item name="hoTen" label="Họ tên" rules={[{ required: true, message: "Nhập họ tên khách." }]}>
            <Input maxLength={120} />
          </Form.Item>
          <Form.Item name="soDienThoai" label="Số điện thoại" rules={[{ required: true, message: "Nhập số điện thoại." }]}>
            <Input />
          </Form.Item>
          <Form.Item name="soGiayTo" label="Số giấy tờ" rules={[{ required: true, whitespace: true, message: "Nhập số giấy tờ để check-in." }, { max: 30 }]}>
            <Input maxLength={30} />
          </Form.Item>
          <Form.Item name="thuDienTu" label="Email">
            <Input />
          </Form.Item>
          <Form.Item name="ghiChu" label="Ghi chú">
            <Input.TextArea maxLength={1000} rows={3} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title="Hóa đơn lưu trú"
        className="HoaDonIn"
        open={moHoaDon}
        onCancel={() => setMoHoaDon(false)}
        footer={(
          <Space>
            {hoaDon?.trangThai === "DA_THANH_TOAN" && (
              <Button onClick={() => window.print()}>In hóa đơn</Button>
            )}
            <Button onClick={() => setMoHoaDon(false)}>Đóng</Button>
          </Space>
        )}
        width={1000}
        destroyOnClose
      >
        {hoaDon && (
          <>
            <p><strong>Số hóa đơn:</strong> {hoaDon.soHoaDon || "—"}</p>
            <p><strong>Khách hàng:</strong> {hoaDon.hoTen || "—"} · {hoaDon.soDienThoai || "—"}</p>
            <p><strong>Trạng thái:</strong> {hoaDon.trangThai}</p>
            <Table
              rowKey="maChiTietDatPhong"
              dataSource={hoaDon.danhSachPhong || []}
              pagination={false}
              scroll={{ x: 650 }}
              locale={{ emptyText: "Hóa đơn chưa có dòng phòng." }}
              columns={[
                { title: "Phòng", dataIndex: "soPhong" },
                { title: "Loại phòng", dataIndex: "tenLoaiPhong" },
                { title: "Ngày nhận", dataIndex: "ngayNhan", render: dinhDangNgay },
                { title: "Ngày trả", dataIndex: "ngayTra", render: dinhDangNgay },
                { title: "Tiền phòng", dataIndex: "tienPhong", render: dinhDangTien },
              ]}
            />
            {hoaDon.trangThai === "CHUA_THANH_TOAN" && Number(hoaDon.soTienConPhaiThu) > 0 && (
              <Card title="Ghi nhận thanh toán" size="small" className="KhoangTren">
                <Form form={formThanhToan} layout="inline" onFinish={guiThanhToan}>
                  <Form.Item name="phuongThuc" rules={[{ required: true, message: "Chọn phương thức thanh toán." }]}>
                    <Select
                      aria-label="Phương thức thanh toán"
                      options={[
                        { value: "TIEN_MAT", label: "Tiền mặt" },
                        { value: "CHUYEN_KHOAN", label: "Chuyển khoản" },
                        { value: "VI_DIEN_TU", label: "Ví điện tử" },
                      ]}
                      style={{ width: 180 }}
                    />
                  </Form.Item>
                  <Form.Item
                    name="soTien"
                    rules={[
                      { required: true, message: "Nhập số tiền." },
                      { type: "number", min: 1, message: "Số tiền phải lớn hơn 0." },
                      {
                        type: "number",
                        max: Number(hoaDon.soTienConPhaiThu),
                        message: "Số tiền vượt khoản còn phải thu.",
                      },
                    ]}
                  >
                    <InputNumber min={1} max={Number(hoaDon.soTienConPhaiThu)} addonAfter="₫" />
                  </Form.Item>
                  <Button type="primary" htmlType="submit" loading={dangThanhToan}>
                    Thu tiền
                  </Button>
                </Form>
              </Card>
            )}
            <Card size="small" className="KhoangTren">
              <Space size="large" wrap>
                <span>Tiền phòng: {dinhDangTien(hoaDon.tienPhong)}</span>
                <span>Cọc được trừ: {dinhDangTien(hoaDon.tienCocDuocTru)}</span>
                <span>Đã thanh toán: {dinhDangTien(hoaDon.tongDaThanhToan)}</span>
                <strong>Còn phải thu: {dinhDangTien(hoaDon.soTienConPhaiThu)}</strong>
              </Space>
            </Card>
            <h3 className="KhoangTren">Các lần thanh toán</h3>
            <Table
              rowKey="maThanhToan"
              dataSource={hoaDon.danhSachThanhToan || []}
              pagination={false}
              scroll={{ x: 500 }}
              locale={{ emptyText: "Chưa ghi nhận thanh toán." }}
              columns={[
                { title: "Thời điểm", dataIndex: "taoLuc", render: (v) => v ? v.replace("T", " ") : "—" },
                { title: "Phương thức", dataIndex: "phuongThuc" },
                { title: "Số tiền", dataIndex: "soTien", render: dinhDangTien },
                { title: "Loại giao dịch", dataIndex: "loaiThanhToan" },
              ]}
            />
          </>
        )}
      </Modal>
      <Modal
        title="Tạo đơn đặt phòng"
        open={moTao}
        onCancel={() => setMoTao(false)}
        maskClosable={false}
        onOk={() => formTao.submit()}
        confirmLoading={dangTao}
        okText="Tạo đơn"
        cancelText="Hủy"
        width={760}
        destroyOnClose
      >
        <Form
          form={formTao}
          layout="vertical"
          onValuesChange={() => setLoiTao("")}
          onFinish={taoDon}
          onFinishFailed={baoLoiFormTao}
        >
          {loiTao ? (
            <Alert className="KhoangDuoi" type="error" showIcon message={loiTao} />
          ) : null}
          <Form.Item name="hinhThucKhach" label="Khách hàng">
            <Select getPopupContainer={(node) => node.parentElement || document.body} options={[
              { value: "CO_SAN", label: "Chọn hồ sơ khách hàng có sẵn" },
              { value: "VANG_LAI", label: "Khách vãng lai · nhập thông tin mới" },
            ]} onChange={(v) => {
              if (v === "VANG_LAI") formTao.setFieldValue("maKhachHang", undefined);
              else formTao.setFieldsValue({ hoTen: undefined, soDienThoai: undefined, soGiayTo: undefined, thuDienTu: undefined });
            }} />
          </Form.Item>
          {hinhThucKhach === "VANG_LAI" ? (
            <>
              <Form.Item name="hoTen" label="Họ tên khách" rules={[{ required: true, message: "Nhập họ tên khách." }, { max: 120 }]}>
                <Input />
              </Form.Item>
              <Form.Item name="soDienThoai" label="Số điện thoại" rules={[
                { required: true, whitespace: true, message: "Nhập số điện thoại." },
                {
                  validator: (_, value) => {
                    const sdt = String(value || "").trim().replace(/[ .-]/g, "").replace(/^\+84/, "0");
                    return !value || /^0[35789][0-9]{8}$/.test(sdt)
                      ? Promise.resolve()
                      : Promise.reject(new Error("Nhập số di động Việt Nam hợp lệ (10 số hoặc +84)."));
                  },
                },
              ]}>
                <Input />
              </Form.Item>
              <Form.Item name="soGiayTo" label="Số giấy tờ" rules={[{ required: true, message: "Cần giấy tờ để nhận phòng." }, { max: 30 }]}>
                <Input />
              </Form.Item>
              <Form.Item name="thuDienTu" label="Email (không bắt buộc)" rules={[{ type: "email", message: "Email không hợp lệ." }]}>
                <Input />
              </Form.Item>
            </>
          ) : (
            <Form.Item name="maKhachHang" label="Chọn khách hàng" rules={[{ required: true, message: "Vui lòng chọn khách hàng." }]}>
              <Select getPopupContainer={(node) => node.parentElement || document.body} showSearch optionFilterProp="label" placeholder={danhSachKhach.length ? "Chọn hồ sơ khách đang hoạt động" : "Không có khách đang hoạt động; chọn khách vãng lai"} options={danhSachKhach.map((khach) => ({
                value: khach.maKhachHang,
                label: `${khach.hoTen} · ${khach.soDienThoai}`,
              }))} />
            </Form.Item>
          )}
          <Form.List
            name="danhSachPhong"
            rules={[
              {
                validator: async (_, value) => {
                  if (value?.length) return;
                  throw new Error("Đơn phải có ít nhất một phòng.");
                },
              },
            ]}
          >
            {(fields, { add, remove }, { errors }) => (
              <>
                {fields.map((field, index) => (
                  <Card
                    key={field.key}
                    title={`Phòng ${index + 1}`}
                    className="KhoangDuoi"
                  >
                    <Space wrap>
                      <Form.Item
                        name={[field.name, "maPhong"]}
                        label="Phòng trống theo ngày đã chọn"
                        rules={[{ required: true, message: "Vui lòng chọn phòng." }]}
                      >
                        <Select
                          getPopupContainer={(node) => node.parentElement || document.body}
                          showSearch
                          optionFilterProp="label"
                          loading={dangTaiPhongTheoDong[index]}
                          disabled={dangTaiPhongTheoDong[index]}
                          placeholder="Chọn ngày để tìm phòng trống"
                          options={(phongTrongTheoDong[index] || []).map(
                            (phong) => ({
                              value: phong.maPhong,
                              label: `${phong.soPhong} · ${phong.loai} · ${dinhDangTien(phong.tienPhongDuKien)}`,
                            }),
                          )}
                        />
                      </Form.Item>
                      <Form.Item
                        name={[field.name, "ngayNhan"]}
                        label="Ngày nhận"
                        rules={[
                          { required: true, message: "Vui lòng chọn ngày nhận." },
                          {
                            validator: (_, value) =>
                              !value || value >= ngayHomNay
                                ? Promise.resolve()
                                : Promise.reject(new Error("Ngày nhận không được ở quá khứ.")),
                          },
                        ]}
                      >
                        <Input
                          type="date"
                          min={ngayHomNay}
                          onChange={() => doiNgayDat(field.name)}
                        />
                      </Form.Item>
                      <Form.Item
                        name={[field.name, "ngayTra"]}
                        label="Ngày trả"
                        rules={[
                          { required: true, message: "Vui lòng chọn ngày trả." },
                          {
                            validator: (_, value) => {
                              const ngayNhan = cacDongTao[index]?.ngayNhan;
                              if (!value || !ngayNhan) return Promise.resolve();
                              const soDem = dayjs(value).diff(dayjs(ngayNhan), "day");
                              if (soDem < 1) {
                                return Promise.reject(new Error("Ngày trả phải sau ngày nhận."));
                              }
                              if (soDem > 30) {
                                return Promise.reject(new Error("Mỗi phòng được đặt tối đa 30 đêm."));
                              }
                              return Promise.resolve();
                            },
                          },
                        ]}
                      >
                        <Input
                          type="date"
                          min={cacDongTao[index]?.ngayNhan || ngayHomNay}
                          onChange={() => doiNgayDat(field.name)}
                        />
                      </Form.Item>
                      <Button danger onClick={() => remove(field.name)}>
                        Bỏ dòng
                      </Button>
                    </Space>
                    {loiTaiPhongTheoDong[index] ? (
                      <Alert
                        className="KhoangTren"
                        type="error"
                        showIcon
                        message="Không tải được danh sách phòng trống"
                        description={loiTaiPhongTheoDong[index]}
                      />
                    ) : !dangTaiPhongTheoDong[index] &&
                      cacDongTao[index]?.ngayNhan &&
                      cacDongTao[index]?.ngayTra > cacDongTao[index]?.ngayNhan &&
                      !(phongTrongTheoDong[index] || []).length ? (
                      <Alert
                        className="KhoangTren"
                        type="warning"
                        showIcon
                        message="Không có phòng trống trong khoảng ngày đã chọn."
                      />
                    ) : null}
                  </Card>
                ))}
                <Button
                  onClick={() =>
                    add({ maPhong: null, ngayNhan: "", ngayTra: "" })
                  }
                >
                  Thêm phòng
                </Button>
                <Form.ErrorList errors={errors} />
              </>
            )}
          </Form.List>
          <Card size="small" className="KhoangTren">
            <strong>Tổng tiền phòng dự kiến: {dinhDangTien(tongTienDuKien)}</strong>
            <p className="GhiChuNho">
              Giá dự kiến được tính theo từng đêm từ dữ liệu phòng. Máy chủ sẽ
              tính và chốt lại giá khi tạo đơn.
            </p>
          </Card>
          <Form.Item name="tienCoc" label="Tiền cọc" initialValue={0}>
            <InputNumber min={0} className="RongDay" />
          </Form.Item>
          <Form.Item name="ghiChu" label="Ghi chú">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </section>
  );
}
