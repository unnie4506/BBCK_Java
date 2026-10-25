import { mayChu } from './axios.js';
export const hoanCocApi = {
  danhSach: (thamSo = {}) => mayChu.get('/quan-ly/hoan-coc', { params: thamSo }),
  xacNhan: (ma, duLieu) => mayChu.post(`/quan-ly/hoan-coc/${ma}/xac-nhan`, duLieu),
};
