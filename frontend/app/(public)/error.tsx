"use client";

import { ErrorState } from "@/components/ui/error-state";
import { Button } from "@/components/ui/button";

export default function PublicSegmentError({ reset }: { error: Error; reset: () => void }) {
  return (
    <main id="content" className="mx-auto w-full max-w-3xl px-5 py-16">
      <ErrorState
        title="تعذر فتح الصفحة"
        description="الاتصال لم يكتمل. أعد المحاولة. لا نعرض تفاصيل تقنية."
        action={<Button onClick={reset}>إعادة المحاولة</Button>}
      />
    </main>
  );
}
