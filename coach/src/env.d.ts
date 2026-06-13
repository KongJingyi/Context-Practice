/// <reference types="vite/client" />

declare module "*.vue" {
  import type { DefineComponent } from "vue";
  const component: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>;
  export default component;
}

interface ImportMetaEnv {
  readonly VITE_API_BASE: string;
  readonly VITE_DEV_PROXY_TARGET: string;
  readonly VITE_DEV_PORT: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

declare module "trtc-js-sdk" {
  const TRTC: {
    createClient: (options: Record<string, unknown>) => {
      on: (event: string, handler: (...args: unknown[]) => void) => void;
      join: (options: { strRoomId?: string; roomId?: number }) => Promise<void>;
      leave: () => Promise<void>;
      publish: (stream: unknown) => Promise<void>;
      unpublish: (stream: unknown) => Promise<void>;
      subscribe: (stream: unknown) => Promise<void>;
    };
    createStream: (options: Record<string, unknown>) => {
      initialize: () => Promise<void>;
      getMediaStream?: () => MediaStream;
      muteAudio?: () => void;
      unmuteAudio?: () => void;
      muteVideo?: () => void;
      unmuteVideo?: () => void;
      close?: () => void;
      stop?: () => void;
    };
  };
  export default TRTC;
}
