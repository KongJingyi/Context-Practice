import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "node:path";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, path.resolve(__dirname, ".."), "");
  const apiTarget = env.VITE_DEV_PROXY_TARGET || "http://localhost:8080";

  return {
    root: path.resolve(__dirname),
    plugins: [vue()],
    resolve: {
      alias: {
        "@admin": path.resolve(__dirname, "src"),
      },
    },
    server: {
      port: Number(env.VITE_ADMIN_PORT || 5176),
      proxy: {
        "/api": { target: apiTarget, changeOrigin: true },
      },
    },
    build: {
      outDir: path.resolve(__dirname, "../dist-admin"),
      emptyOutDir: true,
    },
  };
});
