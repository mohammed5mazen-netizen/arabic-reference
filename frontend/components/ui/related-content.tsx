import Link from "next/link";

export function RelatedContent({ links }: { links: { href: string; label: string }[] }) {
  const visible = links.filter((link) => link.href && link.label);
  if (!visible.length) return null;
  return (
    <section className="mt-10" aria-labelledby="related-title">
      <h2 id="related-title" className="font-display text-3xl">محتوى ذو صلة</h2>
      <ul className="mt-3 space-y-2">
        {visible.map((link) => (
          <li key={link.href}>
            <Link href={link.href} className="text-library">{link.label}</Link>
          </li>
        ))}
      </ul>
    </section>
  );
}
