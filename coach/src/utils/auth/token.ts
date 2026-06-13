const TOKEN_KEY = "token";

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function setTokenStorage(token: string): void {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}
