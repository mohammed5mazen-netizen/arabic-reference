import { emptyLookupMessage } from "@/lib/dictionary";

export default function WordNotFound() {
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-16">
      <h1 className="font-display text-5xl">الكلمة</h1>
      <p className="mt-6 text-lg">{emptyLookupMessage}</p>
    </main>
  );
}
