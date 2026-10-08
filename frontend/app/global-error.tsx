"use client";

export default function GlobalError({ reset }: { error: Error; reset: () => void }) {
  return (
    <html lang="ar" dir="rtl">
      <body style={{ margin: 0, padding: "2rem", fontFamily: "IBM Plex Sans Arabic, Segoe UI, sans-serif" }}>
        <main id="content">
          <h1>تعذر عرض الصفحة</h1>
          <p>حدث خلل مؤقت. أعد المحاولة.</p>
          <button type="button" onClick={reset}>إعادة المحاولة</button>
        </main>
      </body>
    </html>
  );
}
