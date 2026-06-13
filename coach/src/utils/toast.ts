type ToastType = "error" | "success" | "info";

let handler: ((msg: string, type: ToastType) => void) | null = null;

export function registerTopToast(fn: (msg: string, type: ToastType) => void): void {
  handler = fn;
}

export function showTopToast(message: string, type: ToastType = "error"): void {
  if (handler) {
    handler(message, type);
    return;
  }
  // Fallback for when no handler registered
  console.warn(`[Toast:${type}] ${message}`);
}
