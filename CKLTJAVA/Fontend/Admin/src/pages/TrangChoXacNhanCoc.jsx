import * as NenTang from "../NenTangGiaoDien.jsx";
const {
  useEffect,
  useState,
  Alert,
  Button,
  Input,
  InputNumber,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  message,
  donDatPhongApi,
  layDanhSach,
  layThongBao,
  dinhDangTien,
} = NenTang;

const gioPhut = (ms) => ms ? new Date(Number(ms)).toLocaleString("vi-VN") : "—";

export function TrangChoXacNhanCoc() {
  const [ds, setDs] = useState([]);
  const [dangTai, setDangTai] = useState(false);
  const [don, setDon] = useState(null);
  const [dangTaiChiTiet, setDangTaiChiTiet] = useState(false);
  const [soTienThucNhan, setSoTienThucNhan] = useState(0);
  const [phuongThuc, setPhuongThuc] = useState("CHUYEN_KHOAN");
  const [maGiaoDich, setMaGiaoDich] = useState("");
  const [cheDoTuChoi, setCheDoTuChoi] = useState(false);
  const [lyDo, setLyDo] = useState("");
  const [dangXuLy, setDangXuLy] = useState(false);

  async function tai() {
    setDangTai(true);
    try {
      const r = await donDatPhongApi.choXacNhanCoc();
      setDs(layDanhSach(r.data?.duLieu));
    } catch (e) { message.error(layThongBao(e)); }
    finally { setDangTai(false); }
  }

  useEffect(() => {
    tai();
    const timer = window.setInterval(tai, 15000);
    return () => window.clearInterval(timer);
  }, []);

  async function moDon(r) {
    setDangTaiChiTiet(true);
    setCheDoTuChoi(false);
    setLyDo("");
    setMaGiaoDich("");
    try {
      const detail = await donDatPhongApi.chiTiet(r.maDonDatPhong);
      const v = detail.data?.duLieu || detail.data;
      setDon(v);
      setSoTienThucNhan(Math.max(0, Number(v.tienCocYeuCau || 0) - Number(v.tienCocDaNhan || 0)));
    } catch (e) { message.error(layThongBao(e)); }
    finally { setDangTaiChiTiet(false); }
  }

  async function xacNhan() {
    if (!don || Number(soTienThucNhan) <= 0) { message.warning("Nhập số tiền thực nhận lớn hơn 0."); return; }
    setDangXuLy(true);
    try {
      const r = await donDatPhongApi.xacNhanCoc(don.maDonDatPhong, {
        soTienThucNhan: Number(soTienThucNhan), phuongThuc, maGiaoDich: maGiaoDich.trim() || null,
      });
      const v = r.data?.duLieu || {};
      if (v.trangThai === "DA_DAT") message.success("Đã nhận đủ cọc; đơn chuyển sang Đã đặt.");
      else if (v.trangThai === "DA_HUY") message.warning("Phòng đã có người đặt; đơn bị hủy và tiền nhận đã vào danh sách hoàn cọc.");
      else message.success("Đã ghi nhận một phần tiền cọc. Đơn vẫn chờ xác nhận thêm.");
      setDon(null);
      await tai();
    } catch (e) { message.error(layThongBao(e)); }
    finally { setDangXuLy(false); }
  }

  async function tuChoi() {
    if (!don || !lyDo.trim()) { message.warning("Nhập lý do từ chối."); return; }
    setDangXuLy(true);
    try {
      const r = await donDatPhongApi.tuChoiCoc(don.maDonDatPhong, { lyDo: lyDo.trim() });
      const v = r.data?.duLieu || {};
      message.success(v.soTienHoanChoXuLy > 0 ? "Đơn đã hủy; khoản hoàn tiền đã chờ xử lý." : "Đã từ chối cọc và nhả lịch phòng.");
      setDon(null);
      await tai();
    } catch (e) { message.error(layThongBao(e)); }
    finally { setDangXuLy(false); }
  }

  const conHan = Boolean(don?.conHan);
  return <section>
    <div className="HangTieuDe">
      <div><h1>Chờ xác nhận cọc</h1><p>Đối chiếu nội dung chuyển khoản và số tiền trên sao kê. Khách báo đã chuyển được ưu tiên lên đầu.</p></div>
      <Button onClick={tai} loading={dangTai}>Tải lại</Button>
    </div>
    <Alert type="info" showIcon message="Chỉ ghi nhận tiền sau khi đã đối chiếu sao kê ngân hàng. Nếu nhận thiếu, xác nhận phần đã nhận; đơn sẽ tiếp tục chờ cọc." />
    <Table
      rowKey="maDonDatPhong"
      loading={dangTai}
      dataSource={ds}
      onRow={(r) => ({ onClick: () => moDon(r), style: { cursor: "pointer" } })}
      columns={[
        { title: "Mã đơn", dataIndex: "maDonDatPhong" },
        { title: "Mã chuyển khoản", render: (_, r) => `HS${r.maDonDatPhong}` },
        { title: "Khách hàng", dataIndex: "hoTen" },
        { title: "Trạng thái", render: (_, r) => r.conHan ? <Tag color="blue">Chờ cọc</Tag> : <Tag color="orange">Hết hạn trong 48 giờ</Tag> },
        { title: "Cọc cần nhận", dataIndex: "tienCocYeuCau", render: dinhDangTien },
        { title: "Đã nhận", dataIndex: "tienCocDaNhan", render: dinhDangTien },
        { title: "Hạn giữ chỗ", dataIndex: "hanGiuChoMs", render: gioPhut },
        { title: "Khách báo chuyển", dataIndex: "khachBaoDaChuyenLuc", render: (v) => v ? <Tag color="green">Đã báo</Tag> : "Chưa báo" },
        { title: "Thao tác", render: (_, r) => <Button onClick={(e) => { e.stopPropagation(); moDon(r); }}>Đối soát</Button> },
      ]}
      locale={{ emptyText: "Không có đơn chờ xác nhận cọc." }}
    />

    <Modal
      title={don ? `Đối soát đơn ${don.maTraCuuDon || don.maDonDatPhong}` : "Đang tải đơn"}
      open={Boolean(don) || dangTaiChiTiet}
      onCancel={() => { if (!dangXuLy) setDon(null); }}
      footer={null}
      width={680}
      destroyOnClose
    >
      {dangTaiChiTiet && <p>Đang tải thông tin đơn…</p>}
      {don && <>
        {!conHan && <Alert type="warning" showIcon message="Đơn đã hết hạn. Khi xác nhận tiền, hệ thống sẽ kiểm tra lại toàn bộ lịch phòng." />}
        <p>Khách: <b>{don.hoTen}</b> · {don.soDienThoai}</p>
        <p>Nội dung chuyển khoản: <b>HS{don.maDonDatPhong}</b></p>
        <p>Tiền cọc yêu cầu: <b>{dinhDangTien(don.tienCocYeuCau)}</b> · Đã ghi nhận: <b>{dinhDangTien(don.tienCocDaNhan)}</b></p>
        <Table size="small" pagination={false} rowKey={(r, i) => r.maChiTietDatPhong || i} dataSource={don.danhSachPhong || []} columns={[
          { title: "Phòng", dataIndex: "soPhong" }, { title: "Nhận", dataIndex: "ngayNhan" }, { title: "Trả", dataIndex: "ngayTra" }, { title: "Tiền phòng", dataIndex: "tienPhong", render: dinhDangTien },
        ]} />
        {(don.danhSachTienCoc || []).length > 0 && <>
          <h3>Lần nhận cọc trước</h3>
          {(don.danhSachTienCoc || []).map((p) => <p key={p.maThanhToan}>{dinhDangTien(p.soTien)} · {p.phuongThuc} · {p.maGiaoDich || "Không có mã giao dịch"}</p>)}
        </>}
        {!cheDoTuChoi ? <>
          <h3>Ghi nhận tiền vừa đối soát</h3>
          <Space direction="vertical" className="RongDay" size="middle">
            <InputNumber min={1} precision={0} value={soTienThucNhan} onChange={(v) => setSoTienThucNhan(Number(v || 0))} className="RongDay" addonAfter="₫" />
            <Select value={phuongThuc} onChange={setPhuongThuc} className="RongDay" options={[
              { value: "CHUYEN_KHOAN", label: "Chuyển khoản" }, { value: "TIEN_MAT", label: "Tiền mặt" }, { value: "VI_DIEN_TU", label: "Ví điện tử" },
            ]} />
            <Input value={maGiaoDich} onChange={(e) => setMaGiaoDich(e.target.value)} maxLength={100} placeholder="Mã giao dịch ngân hàng (nếu có)" />
          </Space>
          <Space className="NutDoiSoatCoc">
            <Button type="primary" loading={dangXuLy} onClick={xacNhan}>Xác nhận số tiền này</Button>
            <Button danger onClick={() => setCheDoTuChoi(true)}>Từ chối</Button>
          </Space>
        </> : <>
          <h3>Từ chối xác nhận cọc</h3>
          <Input.TextArea value={lyDo} onChange={(e) => setLyDo(e.target.value)} maxLength={1000} rows={3} placeholder="Nhập lý do; nếu đơn đã có tiền được ghi nhận, khoản đó sẽ chờ hoàn." />
          <Space className="NutDoiSoatCoc"><Button danger loading={dangXuLy} onClick={tuChoi}>Từ chối và hủy đơn</Button><Button onClick={() => setCheDoTuChoi(false)}>Quay lại</Button></Space>
        </>}
      </>}
    </Modal>
  </section>;
}
