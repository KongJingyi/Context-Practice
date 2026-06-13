import path from "node:path";
import { defineConfig, loadEnv } from "vite";
import uni from "@dcloudio/vite-plugin-uni";

/**
 * Uni-app + Vite：同一套代码发布 H5 与微信小程序。
 * - H5 开发：`npm run dev:h5`
 * - 微信开发：`npm run dev:mp-weixin`，开发者工具打开 dist/dev/mp-weixin
 * - 发布 H5：`npm run build:h5`
 * - 发布小程序：`npm run build:mp-weixin`，上传 dist/build/mp-weixin
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const apiTarget = env.VITE_DEV_PROXY_TARGET || "http://localhost:8080";

  return {
    plugins: [uni()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"),
      },
    },
    server: {
      host: true,
      port: Number(env.VITE_DEV_PORT || 5173),
      proxy: {
        "/api": {
          target: apiTarget,
          changeOrigin: true,
        },
      },
    },
  };
});
