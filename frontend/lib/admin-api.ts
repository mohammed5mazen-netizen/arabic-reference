export const adminApiBase = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

const ACCESS_KEY = "ar.admin.accessToken";
const REFRESH_KEY = "ar.admin.refreshToken";

export type TokenPair = {
  accessToken: string;
  refreshToken: string;
  mustChangePassword: boolean;
};

export type AdminSession = {
  id: string;
  username: string;
  displayName: string;
  mustChangePassword: boolean;
  roles: string[];
  permissions: string[];
};

type ErrorBody = { code?: string; message?: string };

export function loginErrorMessage(code: string | undefined, fallback?: string): string {
  if (code === "INVALID_CREDENTIALS") return "بيانات الدخول غير صحيحة.";
  if (code === "ACCOUNT_LOCKED") return "الحساب مقفل مؤقتًا.";
  if (code === "ACCOUNT_DISABLED") return "الحساب معطّل.";
  if (code === "RATE_LIMITED") return "محاولات كثيرة. انتظر ثم أعد المحاولة.";
  return fallback || "تعذر الدخول.";
}

export function storeAdminTokens(storage: Pick<Storage, "setItem">, tokens: TokenPair): void {
  storage.setItem(ACCESS_KEY, tokens.accessToken);
  storage.setItem(REFRESH_KEY, tokens.refreshToken);
}

export function clearAdminTokens(storage: Pick<Storage, "removeItem">): void {
  storage.removeItem(ACCESS_KEY);
  storage.removeItem(REFRESH_KEY);
}

export function readAdminAccessToken(storage: Pick<Storage, "getItem">): string | null {
  return storage.getItem(ACCESS_KEY);
}

export async function requestLogin(
  base: string,
  username: string,
  password: string,
  fetchImpl: typeof fetch = fetch,
): Promise<TokenPair> {
  const response = await fetchImpl(`${base}/api/v1/admin/auth/login`, {
    method: "POST",
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  const body = (await response.json()) as { data?: TokenPair } & ErrorBody;
  if (!response.ok || !body.data) {
    throw new Error(loginErrorMessage(body.code, body.message));
  }
  return body.data;
}

export async function adminFetch<T>(path: string, init: RequestInit = {}, retried = false): Promise<T> {
  const token = sessionStorage.getItem(ACCESS_KEY);
  const response = await fetch(`${adminApiBase}${path}`, {
    ...init,
    headers: {
      Accept: "application/json",
      ...(init.body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  });
  if (response.status === 401 && !retried && sessionStorage.getItem(REFRESH_KEY)) {
    const refreshed = await refreshAdminSession();
    if (refreshed) {
      return adminFetch(path, init, true);
    }
  }
  const body = (await response.json()) as { data?: T } & ErrorBody;
  if (!response.ok) {
    throw new Error(body.message || "تعذر تنفيذ الطلب.");
  }
  return body.data as T;
}

async function refreshAdminSession(): Promise<boolean> {
  const refreshToken = sessionStorage.getItem(REFRESH_KEY);
  if (!refreshToken) return false;
  const response = await fetch(`${adminApiBase}/api/v1/admin/auth/refresh`, {
    method: "POST",
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });
  if (!response.ok) {
    clearAdminTokens(sessionStorage);
    return false;
  }
  const body = (await response.json()) as { data: TokenPair };
  storeAdminTokens(sessionStorage, body.data);
  return true;
}

export async function logoutAdmin(
  storage: Pick<Storage, "getItem" | "removeItem"> = globalThis.sessionStorage,
  fetchImpl: typeof fetch = fetch,
): Promise<void> {
  const token = storage.getItem(ACCESS_KEY);
  if (token) {
    await fetchImpl(`${adminApiBase}/api/v1/admin/auth/logout`, {
      method: "POST",
      headers: { Accept: "application/json", Authorization: `Bearer ${token}` },
    }).catch(() => undefined);
  }
  clearAdminTokens(storage);
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("ar-admin-session"));
  }
}
