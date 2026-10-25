import { mayChu, docDuLieu } from './axios.js';

export async function laySoDo(ngay) { return docDuLieu(await mayChu.get('/quan-ly/so-do-phong', { params: { ngay } })); }
export async function layLoaiPhong(thamSo = {}) { return docDuLieu(await mayChu.get('/quan-ly/loai-phong', { params: thamSo })); }
export async function layMotLoaiPhong(ma) { return docDuLieu(await mayChu.get(`/quan-ly/loai-phong/${ma}`)); }
export async function themLoaiPhong(duLieu) { return mayChu.post('/quan-ly/loai-phong', duLieu); }
export async function suaLoaiPhong(ma, duLieu) { return mayChu.put(`/quan-ly/loai-phong/${ma}`, duLieu); }
export async function doiHoatDongLoaiPhong(ma, hoatDong) { return mayChu.post(`/quan-ly/loai-phong/${ma}/${hoatDong ? 'su-dung-lai' : 'ngung-su-dung'}`, {}); }
export async function layAnhLoaiPhong(ma) { return docDuLieu(await mayChu.get(`/quan-ly/loai-phong/${ma}/anh`)); }
export async function taiAnhLoaiPhong(ma, tep) {
  const duLieu = new FormData();
  tep.forEach((anh) => duLieu.append('anh', anh));
  return mayChu.post(`/quan-ly/loai-phong/${ma}/anh`, duLieu);
}
export async function xoaAnhLoaiPhong(ma, maAnh) { return mayChu.delete(`/quan-ly/loai-phong/${ma}/anh/${maAnh}`, { headers: { 'Content-Type': 'application/json' }, data: {} }); }
export async function datAnhDaiDienLoaiPhong(ma, maAnh) { return mayChu.put(`/quan-ly/loai-phong/${ma}/anh/${maAnh}/dai-dien`, {}); }

export async function layTienNghi() { return docDuLieu(await mayChu.get('/quan-ly/tien-nghi')); }
export async function luuTienNghi(ma, ten) { return ma ? mayChu.put(`/quan-ly/tien-nghi/${ma}`, { ten }) : mayChu.post('/quan-ly/tien-nghi', { ten }); }
export async function xoaTienNghi(ma) { return mayChu.delete(`/quan-ly/tien-nghi/${ma}`, { headers: { 'Content-Type': 'application/json' }, data: {} }); }
export async function doiHoatDongTienNghi(ma, hoatDong) { return mayChu.post(`/quan-ly/tien-nghi/${ma}/${hoatDong ? 'su-dung-lai' : 'ngung-su-dung'}`, {}); }

export async function layPhong(thamSo = {}) { return docDuLieu(await mayChu.get('/quan-ly/phong', { params: thamSo })); }
export async function layMotPhong(ma) { return docDuLieu(await mayChu.get(`/quan-ly/phong/${ma}`)); }
export async function luuPhong(ma, duLieu) { return ma ? mayChu.put(`/quan-ly/phong/${ma}`, duLieu) : mayChu.post('/quan-ly/phong', duLieu); }
export async function doiHoatDongPhong(ma, hoatDong) { return mayChu.post(`/quan-ly/phong/${ma}/${hoatDong ? 'su-dung-lai' : 'ngung-su-dung'}`, {}); }
export async function doiTrangThaiPhong(ma, trangThai) { return mayChu.put(`/quan-ly/phong/${ma}/trang-thai`, { trangThai }); }
export async function taiAnhPhong(ma, tep) {
  const duLieu = new FormData();
  tep.forEach((anh) => duLieu.append('anh', anh));
  return mayChu.post(`/quan-ly/phong/${ma}/anh`, duLieu);
}
export async function xoaAnhPhong(ma, maAnh) { return mayChu.delete(`/quan-ly/phong/${ma}/anh/${maAnh}`, { headers: { 'Content-Type': 'application/json' }, data: {} }); }
export async function datAnhDaiDien(ma, maAnh) { return mayChu.put(`/quan-ly/phong/${ma}/anh/${maAnh}/dai-dien`, {}); }

export async function layKhach(thamSo = {}) { return docDuLieu(await mayChu.get('/quan-ly/khach-hang', { params: thamSo })); }
export async function layMotKhach(ma) { return docDuLieu(await mayChu.get(`/quan-ly/khach-hang/${ma}`)); }
export async function luuKhach(ma, duLieu) { return ma ? mayChu.put(`/quan-ly/khach-hang/${ma}`, duLieu) : mayChu.post('/quan-ly/khach-hang', duLieu); }
export async function doiHoatDongKhach(ma, hoatDong) { return mayChu.post(`/quan-ly/khach-hang/${ma}/${hoatDong ? 'su-dung-lai' : 'ngung-su-dung'}`, {}); }
export async function layLichSuKhach(ma, trang = 1) { return docDuLieu(await mayChu.get(`/quan-ly/khach-hang/${ma}/lich-su-luu-tru`, { params: { trang } })); }

