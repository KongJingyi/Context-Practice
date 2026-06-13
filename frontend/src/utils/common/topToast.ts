type ToastType = "error" | "success" | "info";

let handler: ((msg: string, type: ToastType) => void) | null = null;

export function registerTopToast(fn: (msg: string, type: ToastType) => void) {
  handler = fn;
}

export function showTopToast(message: string, type: ToastType = "error") {
  if (handler) {
    handler(message, type);
    return;
  }
  uni.showToast({ title: message, icon: type === "success" ? "success" : "none" });
}
