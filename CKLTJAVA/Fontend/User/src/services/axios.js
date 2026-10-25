import axios from 'axios';

export function layGocApi() {
  if (import.meta.env.DEV) return '/api';
  return `${window.location.pathname.replace(/\/khach-hang\/?$/, '')}/api`;
}

const api = axios.create({
  baseURL: layGocApi(),
  withCredentials: true,
  headers: { Accept: 'application/json' },
});

let xuLyHetPhien = null;
let yeuCauKiemTraPhien = null;
export function datXuLyHetPhien(ham) { xuLyHetPhien = ham; }

api.interceptors.response.use(
  (phanHoi) => phanHoi,
  async (loi) => {
    const yeuCau = loi.config;
    const laDangNhap = yeuCau?.url?.includes("/cong-khai/xac-thuc/dang-nhap");

    if (
      loi.response?.status === 401 &&
      !yeuCau?._boQuaKiemTraPhien &&
      !laDangNhap
    ) {
      if (!yeuCauKiemTraPhien) {
        // Dùng endpoint hồ sơ xác thực làm phép kiểm tra phiên duy nhất.
        yeuCauKiemTraPhien = api
          .get("/khach-hang/xac-thuc/ho-so", {
            _boQuaKiemTraPhien: true,
          })
          .then(() => false)
          .catch((loiKiemTra) => loiKiemTra.response?.status === 401)
          .finally(() => {
            yeuCauKiemTraPhien = null;
          });
      }

      // Chỉ xóa trạng thái khi backend xác nhận phiên không còn hợp lệ.
      // Lỗi mạng, lỗi máy chủ hoặc sai quyền không được biến thành đăng xuất.
      if (await yeuCauKiemTraPhien) {
        xuLyHetPhien?.();
      }
    }

    return Promise.reject(loi);
  },
);

export function layThongBao(loi, macDinh = 'Không kết nối được máy chủ, thử lại.') {
  if (loi?.response?.data?.thongBao) return loi.response.data.thongBao;
  if (!loi?.response) return macDinh;
  return loi.message || macDinh;
}
export default api;


