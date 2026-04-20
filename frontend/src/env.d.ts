/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE: string;
  readonly VITE_DEV_PROXY_TARGET?: string;
  readonly VITE_DEV_PORT?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

/** Pinia store（setup 写法），供 .vue 中类型推断 */
declare module "@/store/user" {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  export function useUserStore(): any;
}

declare module "@/utils/trainingBridge.js" {
  export function setPendingTraining(payload: {
    scenarioId: string;
    roomTitle: string;
  }): void;
  export function consumePendingTraining(): {
    scenarioId: string;
    roomTitle: string;
  } | null;
  export function peekPendingTraining(): {
    scenarioId: string;
    roomTitle: string;
  } | null;
}

declare module "*.vue" {
  import { DefineComponent } from "vue";
  // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
  const component: DefineComponent<{}, {}, any>;
  export default component;
}
