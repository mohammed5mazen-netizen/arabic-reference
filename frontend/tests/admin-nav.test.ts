import assert from "node:assert/strict";
import test from "node:test";
import { loginErrorMessage, logoutAdmin, requestLogin, storeAdminTokens } from "../lib/admin-api.ts";
import { visibleAdminNav } from "../lib/admin-nav.ts";

test("navigation follows held permissions", () => {
  const labels = visibleAdminNav(["admin.audit.view"]).map((item) => item.label);
  assert.deepEqual(labels, ["الرئيسية", "سجل التدقيق"]);
  assert.equal(visibleAdminNav([]).some((item) => item.href === "/admin/users"), false);
});

test("invalid admin login explains the failure without a public account", async () => {
  const fetchImpl = async () =>
    new Response(JSON.stringify({ code: "INVALID_CREDENTIALS", message: "The username or password is incorrect." }), {
      status: 401,
      headers: { "Content-Type": "application/json" },
    });
  await assert.rejects(
    () => requestLogin("http://127.0.0.1:8080", "owner", "wrong", fetchImpl as typeof fetch),
    /بيانات الدخول غير صحيحة/,
  );
  assert.equal(loginErrorMessage("ACCOUNT_LOCKED").includes("حساب"), true);
});

test("logout revokes the local admin session", async () => {
  const values = new Map<string, string>();
  const storage = {
    getItem: (key: string) => values.get(key) ?? null,
    setItem: (key: string, value: string) => values.set(key, value),
    removeItem: (key: string) => values.delete(key),
  };
  storeAdminTokens(storage, { accessToken: "access", refreshToken: "refresh", mustChangePassword: false });
  let logoutCalled = false;
  await logoutAdmin(storage, async (input, init) => {
    logoutCalled = String(input).endsWith("/api/v1/admin/auth/logout") && init?.method === "POST";
    return new Response(null, { status: 204 });
  });
  assert.equal(logoutCalled, true);
  assert.equal(storage.getItem("ar.admin.accessToken"), null);
  assert.equal(storage.getItem("ar.admin.refreshToken"), null);
});
