import api from './axios.js';
export const congKhaiApi = {
  loaiPhong: () => api.get('/cong-khai/loai-phong'),
  chiTietLoaiPhong: (ma) => api.get(`/cong-khai/loai-phong/${encodeURIComponent(ma)}`),
  phongTrong: (thamSo) => api.get('/cong-khai/phong-trong', { params: thamSo }),
};


