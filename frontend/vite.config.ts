import { reactRouter } from "@react-router/dev/vite";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
  plugins: [tailwindcss(), reactRouter(), tsconfigPaths()],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080/api",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
      "/oauth2": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/admin": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/user": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/matches": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/bots": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/tournaments/detail": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/tournaments/join": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/tournament-images": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/bot-images": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
      "/user-images": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
