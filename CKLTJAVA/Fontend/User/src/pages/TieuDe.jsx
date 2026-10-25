export function TieuDe({ nhan, tieuDe, moTa }) {
  return (
    <div className="TieuDeTrang">
      <p className="NhanTieuDe">{nhan || "KHÔNG GIAN NGHỈ"}</p>
      <h1>{tieuDe}</h1>
      {moTa && <p className="MoTaTieuDe">{moTa}</p>}
    </div>
  );
}

export default TieuDe;
