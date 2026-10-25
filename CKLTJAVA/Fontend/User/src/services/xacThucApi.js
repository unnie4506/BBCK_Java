import api from './axios.js';
export const xacThucApi = {
  dangKy: (duLieu) => api.post('/cong-khai/xac-thuc/dang-ky', duLieu),
  dangNhap: (duLieu) => api.post('/cong-khai/xac-thuc/dang-nhap', duLieu),
  dangXuat: () => api.post('/khach-hang/xac-thuc/dang-xuat', {}),
  hoSo: () => api.get('/khach-hang/xac-thuc/ho-so'),
};


