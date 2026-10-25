import api from './axios.js';
export const lichSuApi = {
  danhSach: (trang = 1) => api.get('/khach-hang/lich-su-luu-tru', { params: { trang } }),
  chiTiet: (ma) => api.get(`/khach-hang/lich-su-luu-tru/${encodeURIComponent(ma)}`),
};
