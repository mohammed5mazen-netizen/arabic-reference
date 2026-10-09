const apiBase = process.env.NEXT_PUBLIC_API_URL?.trim() || "http://localhost:8080";

function connectSources() {
  try {
    const origin = new URL(apiBase).origin;
    return origin && origin !== "null" ? `'self' ${origin}` : "'self'";
  } catch {
    return "'self'";
  }
}

export const contentSecurityPolicy = [
  "default-src 'self'",
  "script-src 'self' 'unsafe-inline'",
  "style-src 'self' 'unsafe-inline'",
  "img-src 'self' data: blob:",
  "font-src 'self'",
  `connect-src ${connectSources()}`,
  "frame-ancestors 'none'",
  "base-uri 'self'",
  "form-action 'self'",
  "object-src 'none'",
].join("; ");

export const securityHeaders: { key: string; value: string }[] = [
  { key: "Content-Security-Policy", value: contentSecurityPolicy },
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=()" },
  { key: "X-Frame-Options", value: "DENY" },
];

if (process.env.ENABLE_HSTS === "true") {
  securityHeaders.push({
    key: "Strict-Transport-Security",
    value: "max-age=15552000; includeSubDomains",
  });
}
