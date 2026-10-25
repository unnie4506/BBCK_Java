export const TRANG_THAI_PHONG = {
  TRONG: { ten: 'Trống', mau: 'green' }, DA_DAT: { ten: 'Đã đặt', mau: 'blue' },
  DANG_O: { ten: 'Đang ở', mau: 'purple' }, DANG_DON: { ten: 'Đang dọn', mau: 'orange' }, BAO_TRI: { ten: 'Bảo trì', mau: 'red' },
};
export const DUONG_DAN_GOC = `/${(import.meta.env.VITE_DUONG_DAN_QUAN_LY || '').replace(/^\/+|\/+$/g, '')}`;
export const DUONG_DAN_DANG_NHAP = `${DUONG_DAN_GOC}/dang-nhap`;

