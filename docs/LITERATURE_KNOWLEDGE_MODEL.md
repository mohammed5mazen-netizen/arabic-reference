# Literature knowledge model

This module is a knowledge base about Arabic literature. It is not a book library and it does not store full copyrighted works.

## Records

- `LiteraryEra` has a name, slug, start and end descriptions, summary, historical context, display order, status, and version. Boundaries may be approximate, so the model does not require exact Gregorian dates.
- `LiteraryFigure` is one person who may be a poet and a writer. Aliases are rows (`NAME`, `LAQAB`, `KUNYA`, `NISBA`, `OTHER`). Roles are rows and a person may hold more than one.
- Birth and death use `HistoricalDate`: exact, year, approximate, or unknown, on a Gregorian, Hijri, or unspecified calendar. The system stores the entered value and does not convert calendars. An approximate year is shown as نحو سنة….
- `LiteraryWork` stores metadata: title, aliases, figures, genre, era, description, composition note, language, and rights. It is not a file store.
- `LiteraryGenre` and `LiterarySchool` are extensible catalogs. Schools may point at an era and at figures.

Relations are join rows, not comma-separated identifiers. A public figure or work page lists only related records that are themselves published.

Public routes: `/literature`, `/literature/eras/{slug}`, `/literature/figures/{slug}`, and `/literature/works/{slug}`.
