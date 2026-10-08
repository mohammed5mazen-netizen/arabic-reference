import Link from "next/link";
import { footerLinks, uniqueLinks } from "@/lib/navigation";

export function SiteFooter() {
  const links = uniqueLinks(footerLinks);
  return (
    <footer className="relative z-10 mt-16 border-t border-line">
      <div className="mx-auto grid w-full max-w-6xl gap-8 px-5 py-10 sm:grid-cols-2">
        <div>
          <p className="font-display text-2xl">المرجع العربي</p>
          <p className="mt-3 max-w-md leading-8 text-muted">معرفة لغوية منشورة. القراءة لا تتطلب حسابًا. المعلومة الموثّقة تبقى الأصل، والتحليل المحتمل يبقى موسومًا.</p>
        </div>
        <nav aria-label="تذييل المرجع">
          <ul className="grid grid-cols-2 gap-2 text-sm">
            {links.map((link) => (
              <li key={link.href}>
                <Link href={link.href} className="inline-flex min-h-11 items-center">{link.label}</Link>
              </li>
            ))}
          </ul>
        </nav>
      </div>
    </footer>
  );
}
