# Publication readiness

`PublicationReadiness` is `READY` or `BLOCKED`. A record is ready only when its status is `VERIFIED`, the live quality rules report no blocker, and no open blocker finding remains. The response includes the reasons and a checklist: review completed, no quality blockers, rights and sources allow publication, and a snapshot can be built from a title.

The publishing inbox lists verified records and groups open blockers into rights, citation, and other publication blockers. Restricted rights, unknown rights, a missing citation, and an excerpt that the rights policy rejects appear there when a scan has recorded them.

The checklist is not the publication gate. The owning module's publish method still rejects an invalid record, and `PublicationChecks` rejects publish while an open `BLOCKER` finding exists for that type and id. Quality scan and publish can run together: publish reads the findings committed so far, and a scan does not change the content row.
