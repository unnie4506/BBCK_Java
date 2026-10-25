import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const tomcat = new URL(env.VITE_DIA_CHI_TOMCAT || 'http://localhost:8080/homestay-api');
  const duongDanApi = (env.VITE_DUONG_DAN_API || `${tomcat.pathname.replace(/\/$/, '')}/api`).replace(/\/$/, '');
  return {
    // Dùng đường dẫn tương đối để có thể đặt bản build dưới context bất kỳ của Tomcat.
    // API production được suy ra lúc chạy từ đường dẫn /khach-hang.
    base: command === 'serve' ? '/' : './',
    // Bản build không sao chép bundle cũ nằm trong public; cổng khách chỉ chứa mã của chính nó.
    publicDir: false,
    plugins: [react()],
    build: { sourcemap: false, outDir: 'dist', emptyOutDir: true },
    server: {
      port: 5174,
      strictPort: true,
      proxy: {
        '/api': {
          target: tomcat.origin,
          changeOrigin: true,
          secure: false,
          // Tomcat mặc định giới hạn JSESSIONID theo context WAR; trên Vite,
          // trình duyệt gọi /api nên cần viết lại Path để cookie được gửi lại.
          cookiePathRewrite: '/',
          rewrite: (path) => path.replace(/^\/api/, duongDanApi),
        },
      },
    },
  };
});
