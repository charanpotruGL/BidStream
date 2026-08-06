/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL?: string;
  readonly VITE_ENV?: string;
  readonly VITE_ENABLE_NOTIFICATIONS?: string;
  readonly VITE_ENABLE_REAL_TIME_UPDATES?: string;
  readonly VITE_NOTIFICATION_POLL_INTERVAL?: string;
  readonly VITE_BID_POLL_INTERVAL?: string;
  readonly VITE_AUCTION_POLL_INTERVAL?: string;
  readonly VITE_WS_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
