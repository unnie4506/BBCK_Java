import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const duongDanQuanLy = (env.VITE_DUONG_DAN_QUAN_LY || '').replace(/^\/+|\/+$/g, '');
  const tomcat = new URL(env.VITE_DIA_CHI_TOMCAT || 'http://localhost:8080/quan-ly-homestay-may-chu');
  const duongDanApi = (env.VITE_DUONG_DAN_API || `${tomcat.pathname.replace(/\/$/, '')}/api`).replace(/\/$/, '');

  if (!duongDanQuanLy) {
    throw new Error('Cần cấu hình VITE_DUONG_DAN_QUAN_LY trong file .env.');
  }

  return {
    // Dev và bản build đều dùng cùng đường dẫn bí mật để tránh lệch đường dẫn gốc.
    base: `/${duongDanQuanLy}/`,
    publicDir: false,
    plugins: [react()],
    build: {
      sourcemap: false,
      outDir: 'dist',
      emptyOutDir: true,
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (id.includes('/node_modules/react/') || id.includes('/node_modules/react-dom/') || id.includes('/node_modules/react-router/')) return 'react-vendor';
            if (id.includes('/node_modules/antd/') || id.includes('/node_modules/@ant-design/') || id.includes('/node_modules/@rc-component/')) return 'giao-dien-vendor';
            if (id.includes('/node_modules/dayjs/')) return 'ngay-vendor';
          },
        },
      },
    },
    server: {
      port: 5173,
      strictPort: true,
      proxy: {
        '/api': {
          target: tomcat.origin,
          changeOrigin: true,
          secure: false,
          // Cookie JSESSIONID của Tomcat mặc định giới hạn ở context WAR.
          // Trên cổng Vite, trình duyệt gọi API qua /api nên cần đổi Path về /.
          cookiePathRewrite: '/',
          rewrite: (path) => path.replace(/^\/api/, duongDanApi),
        },
      },
    },
  };
});
