# Design system

The public reference uses one set of semantic tokens in `frontend/app/globals.css`. Components use those tokens through the existing Tailwind names (`bg-raised`, `text-ink`, `border-line`, `text-library`) and the CSS variables `--success`, `--warning`, `--destructive`, `--info`, and `--focus`.

Light and dark themes set the same variable names. A blocking script in the root layout applies the saved theme before paint. The preference key is `arabic-reference-theme` in `localStorage`. When nothing is saved, the site follows `prefers-color-scheme`.

## Typography

- Display and headings: Amiri, then Noto Naskh Arabic, then a serif fallback. Weights 400 and 700.
- Body, labels, and UI: IBM Plex Sans Arabic, then Segoe UI, then sans-serif. Weights 400, 500, and 600.
- Both families are loaded with `next/font` and `display: swap`.
- Reading measure: `.reading` caps a paragraph near 42rem with a loose line height.
- `.type-display`, `.type-label`, and `.type-meta` name the roles. Pages may also use the Tailwind display sizes already in the templates.

## Numerals

Public counts use Latin digits through `formatNumber` (`ar-EG` with the `latn` numbering system). Arabic words stay Arabic.

## Shared components

Only components that a public page renders are in `frontend/components/ui`:

- `Button` and `LinkButton`: primary, secondary, ghost, destructive, link; small, medium, large; disabled state
- `Card`
- `ProvenanceBadge`: موثق، منشور، تحليل محتمل، نتيجة قريبة، مثال تعليمي
- `EmptyState`, `ErrorState`
- `PageHeader`
- `SourceCard`
- `RelatedContent`

Icons in the header mark are decorative (`aria-hidden`). There is one icon drawing, not an icon library.

## Motion

`prefers-reduced-motion: reduce` disables animation and transition on the page.
