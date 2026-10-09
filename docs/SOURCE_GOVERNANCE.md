# Source governance

Sources stay on the S2 `reference_source` record. S11 does not add a second source entity.

Each source stores an identity key of normalized title, author, and edition. The admin catalog shows title, author, edition, publisher, year, Arabic license label, status, citation count, and last citation time when those columns exist. `/admin/sources` also lists duplicate candidates that share an identity key. Nothing is merged automatically.

The source page lists where the source is cited: dictionary entries, grammar rules, spelling rules, rhetoric devices, literary works, and articles. Deleting a source that has citations returns `409`. A source that is no longer a draft also returns `409`; it should be archived through the existing workflow. Saving a source or changing its status writes `SOURCE_UPDATED` or `SOURCE_STATUS_CHANGED`. Adding a citation bumps the source version so a concurrent edit conflicts.

License labels are:

| Code | Label |
| --- | --- |
| `PUBLIC_DOMAIN` | ملكية عامة |
| `CC0` | ملكية عامة CC0 |
| `CC_BY` | نسب المصنف |
| `CC_BY_SA` | نسب المصنف - المشاركة بالمثل |
| `PERMISSION_GRANTED` | إذن ممنوح |
| `RESTRICTED` | مقيد |
| `UNKNOWN` | غير معروف |

Age alone does not mark a work as public domain. The earlier license and literature rights policies remain the decision.
