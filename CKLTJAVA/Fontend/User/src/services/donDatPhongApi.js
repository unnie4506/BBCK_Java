import api from './axios.js';
export const donDatPhongApi = {
  xemTruoc: (duLieu) => api.post('/khach-hang/don-dat-phong/xem-truoc', duLieu),
  datPhong: (duLieu) => api.post('/khach-hang/don-dat-phong', duLieu),
  danhSach: (thamSo) => api.get('/khach-hang/don-dat-phong', { params: thamSo }),
  chiTiet: (ma) => api.get(`/khach-hang/don-dat-phong/${encodeURIComponent(ma)}`),
  xemTruocHuy: (ma) => api.get(`/khach-hang/don-dat-phong/${encodeURIComponent(ma)}/xem-truoc-huy`),
  huy: (ma, duLieu) => api.post(`/khach-hang/don-dat-phong/${encodeURIComponent(ma)}/huy`, duLieu),
  // Gửi JSON rỗng để MaHoaKyTuFilter chấp nhận POST này; endpoint chỉ ghi nhận thời điểm báo.
  baoDaChuyenKhoan: (ma) => api.post(
    `/khach-hang/don-dat-phong/${encodeURIComponent(ma)}/bao-da-chuyen-khoan`,
    {},
    { headers: { 'Content-Type': 'application/json' } },
  ),
};
