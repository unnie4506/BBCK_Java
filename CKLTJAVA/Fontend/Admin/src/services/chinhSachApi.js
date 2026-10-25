import { mayChu } from './axios.js';
export const chinhSachApi = {
  xem: () => mayChu.get('/quan-ly/chinh-sach-hoan-huy'),
  capNhat: (danhSachBac) => mayChu.put('/quan-ly/chinh-sach-hoan-huy', { danhSachBac }),
};
