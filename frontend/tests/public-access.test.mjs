import assert from "node:assert/strict";
import { spawn } from "node:child_process";
import { existsSync, readdirSync, readFileSync, statSync } from "node:fs";
import path from "node:path";
import test from "node:test";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const port = 3456;

const forbidden = [
  "تسجيل الدخول",
  "إنشاء حساب",
  "تسجيل حساب",
  'href="/login"',
  'href="/register"',
  'href="/auth"',
  'redirect("/login")',
  "redirect('/login')",
  'redirect("/auth")',
  "redirect('/auth')",
];

function sourceFiles(directory) {
  const entries = readdirSync(directory);
  const files = [];
  for (const entry of entries) {
    const fullPath = path.join(directory, entry);
    if (entry === "node_modules" || entry === ".next" || entry === "tests") {
      continue;
    }
    const stats = statSync(fullPath);
    if (stats.isDirectory()) {
      files.push(...sourceFiles(fullPath));
    } else if (/\.(tsx|ts|jsx|js|mjs)$/.test(entry)) {
      files.push(fullPath);
    }
  }
  return files;
}

test("public pages have no authentication gate", () => {
  assert.equal(existsSync(path.join(root, "middleware.ts")), false);
  const files = sourceFiles(root);
  const corpus = files.map((file) => readFileSync(file, "utf8")).join("\n");
  for (const phrase of forbidden) {
    assert.equal(corpus.includes(phrase), false, `found forbidden auth marker: ${phrase}`);
  }
});

test("GET / is public and renders the Arabic homepage", async () => {
  const nextBin = path.join(root, "node_modules", "next", "dist", "bin", "next");
  const child = spawn(process.execPath, [nextBin, "start", "-H", "127.0.0.1", "-p", String(port)], {
    cwd: root,
    env: { ...process.env, NODE_ENV: "production" },
    stdio: ["ignore", "pipe", "pipe"],
  });

  let logs = "";
  child.stdout.on("data", (chunk) => {
    logs += chunk.toString();
  });
  child.stderr.on("data", (chunk) => {
    logs += chunk.toString();
  });

  try {
    const response = await waitForOk(`http://127.0.0.1:${port}/`);
    assert.equal(response.status, 200);
    assert.equal(response.headers.get("location"), null);
    const html = await response.text();
    assert.match(html, /lang="ar"/);
    assert.match(html, /dir="rtl"/);
    assert.match(html, /المرجع العربي/);
    assert.match(html, /بوابتك الشاملة إلى اللغة العربية/);
    assert.match(html, /ابحث عن كلمة، معنى، جذر، قاعدة لغوية/);
    assert.equal(html.includes("تسجيل الدخول"), false);
    assert.equal(html.includes("إنشاء حساب"), false);
    assert.equal(html.includes('href="/login"'), false);
  } catch (error) {
    throw new Error(`${error instanceof Error ? error.message : error}\n${logs}`);
  } finally {
    await stop(child);
  }
});

async function waitForOk(url) {
  const started = Date.now();
  let lastError = "server did not start";
  while (Date.now() - started < 30000) {
    try {
      const response = await fetch(url);
      if (response.status === 200) {
        return response;
      }
      lastError = `status ${response.status}`;
    } catch (error) {
      lastError = error instanceof Error ? error.message : String(error);
    }
    await new Promise((resolve) => setTimeout(resolve, 400));
  }
  throw new Error(lastError);
}

async function stop(child) {
  if (process.platform === "win32" && child.pid) {
    spawn("taskkill", ["/pid", String(child.pid), "/t", "/f"], { stdio: "ignore" });
  } else {
    child.kill();
  }
  await new Promise((resolve) => child.once("exit", resolve));
}
