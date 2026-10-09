import assert from "node:assert/strict";
import test from "node:test";
import { contentSecurityPolicy, securityHeaders } from "../lib/security-headers.ts";

test("content security policy stays specific", () => {
  assert.equal(contentSecurityPolicy.includes("default-src *"), false);
  assert.match(contentSecurityPolicy, /default-src 'self'/);
  assert.match(contentSecurityPolicy, /script-src 'self' 'unsafe-inline'/);
  assert.match(contentSecurityPolicy, /frame-ancestors 'none'/);
  assert.match(contentSecurityPolicy, /object-src 'none'/);
});

test("hsts stays off unless explicitly enabled", () => {
  const names = securityHeaders.map((header) => header.key);
  assert.equal(names.includes("Strict-Transport-Security"), process.env.ENABLE_HSTS === "true");
  assert.ok(names.includes("X-Content-Type-Options"));
  assert.ok(names.includes("Referrer-Policy"));
  assert.ok(names.includes("Permissions-Policy"));
});
