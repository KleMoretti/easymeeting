import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:6060',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:6061',
        ws: true,
        changeOrigin: true,
      },
    },
  },
});
