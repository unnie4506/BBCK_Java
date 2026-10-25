import axios from 'axios';

const diaChiApi = import.meta.env.PROD
  ? (import.meta.env.VITE_DUONG_DAN_API || '/api')
  : '/api';

export const mayChu = axios.create({
  baseURL: diaChiApi,
  withCredentials: true,
  headers: { Accept: 'application/json' },
});

let xuLyHetPhien = () => {};
let dangKiemTra = null;
export function datXuLyHetPhien(callback) { xuLyHetPhien = callback; }

mayChu.interceptors.response.use(
  (phanHoi) => phanHoi,
  async (loi) => {
    const cauHinh = loi.config || {};
    const ma = loi.response?.status;
    const laApiQuanLy = (cauHinh.url || '').includes('/quan-ly/');
    const laDangNhap = (cauHinh.url || '').includes('/xac-thuc/dang-nhap');
    const laMe = (cauHinh.url || '').includes('/xac-thuc/ho-so');
    if (laApiQuanLy && !laDangNhap && !laMe && !cauHinh._daKiemTraPhien && [401, 404].includes(ma)) {
      try {
        dangKiemTra ||= mayChu.get('/quan-ly/xac-thuc/ho-so', { _boQuaKiemTraPhien: true });
        await dangKiemTra;
      } catch {
        xuLyHetPhien();
      } finally {
        dangKiemTra = null;
      }
    }
    return Promise.reject(loi);
  },
);

export function docDuLieu(phanHoi) { return phanHoi.data?.duLieu; }
export function docThongBao(loi, macDinh = 'Không thể hoàn thành yêu cầu.') {
  return loi.response?.data?.thongBao || loi.response?.data?.message || macDinh;
}

