/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE?: string;
  readonly VITE_FRONTEND_BASE?: string;
  readonly VITE_ADMIN_PORT?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
