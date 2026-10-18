# SCOPE V0.2 — SIMPLIFIED STUDENT PROJECT (READ FIRST)

**Leader-approved on 2026-10-18. This is the authoritative change summary for v0.2.** The lecturer did not specifically require advanced workflow journaling, automatic crash recovery or elaborate concurrency infrastructure. This revision supersedes v0.1 in those areas only.

## Non-negotiable project requirements (unchanged)

- **ONE** shared Spring Boot/Java/Maven project, **ONE** GitHub repo, SIX fixed feature branches and exactly TWO roles (Student/Admin).
- Six original module assignments, each with Student + Admin page(s), at least C/R/U categories, meaningful `ArrayList`, models, OOP relationship(s), a repository interface and concrete TXT implementation, service, REST controller, JavaScript `fetch()`, custom exception and tests.
- TXT-only storage (`BufferedReader`, `BufferedWriter`, UTF-8, plain text) — **no MySQL, JPA or JDBC**. Keep the existing member TXT schemas, URL-encoded fields, UTC timestamps and stable IDs. Never commit live `data/*.txt` files.
- M3 prerequisite rule checking with published passing grades from M5; M4 **real FIFO Queue** and queue reconstruction after restart; M5 **manual Insertion Sort by registration timestamp**; M6 **DEMO/manual payment** checked by backend, invoices/receipts/refunds; M4 sole-writer ownership of enrollments.
- Preserve security basics: authenticated Student/Admin roles, password hashes, per-student data access restrictions, server-side validation and safe error responses. Test full student registration → course request → admin approval → invoice → verified DEMO payment → ACTIVE enrollment → results/GPA.

## Simplified / optional: NOT a prerequisite for member development

- No mandatory `WorkflowJournalStore`, `WorkflowRecoveryService`, `WorkflowPayloadCodec`, complex stage/payload journaling, `workflow_journal.txt`, `audit_log.txt`, automatic crash rollback or replay, or production-style recovery testing. Existing Step 9–14 lesson code may remain in the leader's local project if it compiles, but members **do not implement or depend on it**.
- Keep basic single-process synchronization for file read/modify/write and last-seat/payment safeguards. Reuse `StorageLockManager` and, if useful, existing `SharedWorkflowCoordinator` *only for simple critical sections*; no need to extend journal-based recovery.
- For a rare partial cross-file failure, stop processing, return a controlled error, and document a manual audit/reconciliation action. Do not claim this is crash-safe/ACID. No real money collection/card storage.
- Preserve the existing v0.1 public cross-module ports, APIs, model ownership and member file field orders unless a separate approved v0.3 explicitly changes them.

## Immediate practical curriculum (one testable step at a time)

1. **Baseline cleanup/check:** run existing Maven tests; isolate or disable only *optional* advanced tests if they break the simplified baseline, without altering actual member requirements. Verify `/hello` and that runtime data is ignored by Git.
2. **Publish common GitHub repo:** check Git installation, initialize/push *one* project to `main`, commit this v0.2 contract. Confirm a second computer can clone/build it.
3. **Start six members:** each clones same project, switches to assigned branch, builds Student+Admin UI and owned backend against the same v0.2 agreement; create stubs/mocks only as agreed.
4. **Integrate functional modules:** M1 + M2 → M3 → M4 → M6 → M5, with full browser/Postman tests and safe fake seed data. Work can overlap if ports are honored.

## How to use the pack

Replace your local `docs/` contents with the seven files in this ZIP; keep `FINAL_MASTER_PROMPT.txt` only as historic teaching context. **v0.2 contract docs take precedence over older v0.1 prompts and old chat instructions** wherever advanced journal/recovery scope conflicts. Do not silently overwrite teammate edits after repository work begins. Document any future changes and tell all six members.
