import api from './axios.js';
export const hoSoApi = {
  xem: () => api.get('/khach-hang/ho-so'),
  capNhat: (duLieu) => api.put('/khach-hang/ho-so', duLieu),
};


