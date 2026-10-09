import Link from "next/link";
import { contentTypeLabel, recordHref, severityLabel, statusLabel, type QueueItem } from "@/lib/editorial";
import { EmptyState } from "@/components/ui/empty-state";

export function EditorialQueue({
  items,
  emptyTitle,
}: {
  items: QueueItem[];
  emptyTitle: string;
}) {
  if (items.length === 0) {
    return <EmptyState title={emptyTitle} />;
  }
  return (
    <div className="overflow-x-auto rounded-[var(--radius)] border border-line bg-raised">
      <table className="w-full min-w-[48rem] text-right">
        <caption className="sr-only">قائمة التحرير</caption>
        <thead>
          <tr className="border-b border-line text-sm text-muted">
            <th scope="col" className="px-4 py-3">النوع</th>
            <th scope="col" className="px-4 py-3">العنوان</th>
            <th scope="col" className="px-4 py-3">الحالة</th>
            <th scope="col" className="px-4 py-3">آخر تحديث</th>
            <th scope="col" className="px-4 py-3">الجودة</th>
            <th scope="col" className="px-4 py-3">إجراء</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr key={`${item.contentType}-${item.id}`} className="border-b border-line">
              <td className="px-4 py-3">{contentTypeLabel(item.contentType)}</td>
              <td className="px-4 py-3">{item.title}</td>
              <td className="px-4 py-3">{statusLabel(item.status)}</td>
              <td className="px-4 py-3">
                {item.updatedAt ? <time dateTime={item.updatedAt}>{item.updatedAt}</time> : "—"}
              </td>
              <td className="px-4 py-3">{item.blockers > 0 ? `${severityLabel("BLOCKER")} (${item.blockers})` : "لا مانع"}</td>
              <td className="px-4 py-3">
                <Link className="underline" href={recordHref(item.contentType, item.id)}>
                  فتح السجل
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
