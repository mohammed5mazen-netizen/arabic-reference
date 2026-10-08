import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import test from "node:test";
import { assistantDisabled, assistantName, assistantPlaceholder, assistantPromise } from "../lib/assistant.ts";
import { adminNav } from "../lib/admin-nav.ts";

test("assistant copy stays reference-bound and disabled-safe", () => {
  assert.equal(assistantName, "المساعد اللغوي");
  assert.match(assistantPromise, /موثقة من محتوى المرجع|موثّقة من محتوى المرجع/);
  assert.match(assistantDisabled, /غير متاح/);
  assert.match(assistantPlaceholder, /همزة الوصل/);
  const panel = readFileSync(new URL("../components/assistant-panel.tsx", import.meta.url), "utf8");
  const page = readFileSync(new URL("../app/(public)/assistant/page.tsx", import.meta.url), "utf8");
  assert.equal(panel.includes("dangerouslySetInnerHTML"), false);
  assert.match(panel, /htmlFor=/);
  assert.match(panel, /aria-describedby/);
  assert.match(panel, /role="alert"/);
  assert.match(panel, /grid-cols-1/);
  assert.match(panel, /whitespace-pre-wrap/);
  assert.match(page, /canonical/);
  assert.match(page, /index: true/);
  const header = readFileSync(new URL("../components/site-header.tsx", import.meta.url), "utf8");
  const home = readFileSync(new URL("../app/(public)/page.tsx", import.meta.url), "utf8");
  assert.match(header, /href="\/assistant"/);
  assert.match(home, /href="\/assistant"/);
  assert.equal(adminNav.some((item) => item.href === "/admin/ai" && item.permission === "ai.admin.view"), true);
});
