import { mayChu, docDuLieu } from './axios.js';

export async function dangNhap(tenDangNhap, matKhau) {
  const phanHoi = await mayChu.post('/quan-ly/xac-thuc/dang-nhap', { tenDangNhap, matKhau });
  return docDuLieu(phanHoi);
}
export async function layHoSoPhien() {
  const phanHoi = await mayChu.get('/quan-ly/xac-thuc/ho-so');
  return docDuLieu(phanHoi);
}
export async function dangXuat() { return mayChu.post('/quan-ly/xac-thuc/dang-xuat', {}); }

