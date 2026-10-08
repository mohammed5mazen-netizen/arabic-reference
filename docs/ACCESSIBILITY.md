# Accessibility

The public baseline is WCAG 2.2 AA as a structural target. This document records what the code enforces. A human still needs the checklist in `docs/MANUAL_UI_CHECKLIST.md` before a visual sign-off.

## Document

- `lang="ar"` and `dir="rtl"` on the root
- Skip link: تجاوز إلى المحتوى, targeting `#content`
- Landmarks: `header`, `nav`, `main`, and `footer`
- One `h1` on each public page
- English identifiers and URLs can sit in `bdi` or `dir="ltr"`

## Interaction

- Visible `:focus-visible` using `--focus`
- Form fields keep their labels. Errors use `role="alert"` next to the field and do not clear the input
- Search suggestions are a combobox with a listbox
- The mobile menu is a modal dialog and can be closed from the keyboard
- Theme control has a text name, not an icon alone
- Progress uses a `progress` element plus a written count
- Quiz options are labeled radio buttons or checkboxes
- Touch targets on primary controls use at least `min-h-11` (44px)

## Color

Muted text, borders, and buttons use the semantic tokens. Provenance is a word inside a badge, not a color by itself. Contrast for the light muted color `#4e453c` on `#f3eee4`, and the dark muted color `#d7c7b4` on `#12110f`, is intended to meet AA for normal text. A contrast checker in a browser was not run in this environment.

## Motion

`prefers-reduced-motion: reduce` removes animation and transition.
