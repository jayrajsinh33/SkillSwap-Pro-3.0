# SkillSwap 3.0 — Accessibility and marketplace upgrade

## Scope of this update

The existing Next.js/React/TypeScript frontend and Java 21/Spring Boot backend are preserved. The original ink/teal visual identity was kept, with a new horizontal top navigation, mobile bottom shortcuts, smaller cards, more predictable labels, category-and-topic filters, and page-specific layouts.

**Frontend updated:** home/login with Indian education photography; dashboards; Explore skills; 20 seeded courses and detailed filters; mentors; public learner/mentor profiles; My learning; per-lesson video/PDF viewer and mentor upload; Live classes; bookings; familiar conversation UI with search and attachments; wallet and withdrawal requests; mentor studio and application; notifications; settings; help/support; administration. The existing course/mentor catalogue still uses remote demo images elsewhere.

**Backend updated:** recorded lesson MP4/PDF attachment IDs; course-specific permissions; 5% commission default; one-time first free 1:1 lecture; flexible 1–8 hours split into separate 60-minute bookings with 15-minute breaks; lecture identifiers for private and group sessions; report submission and manual evidence review; mentor score rules (100 -> 96 / ₹2,000; 96 -> 93 / ₹5,000; third violation -> ban / ₹10,000); student/mentor complaints; one-time refund of eligible *paid private lectures* when a mentor is found at fault; mentor identity-document upload, bank details submission, versioned electronic agreement and admin approval gates; logout and new-login token revocation. A duplicate Booking model field was removed and the GroupClass model now contains lectureId.

## IMPORTANT — what is NOT fully implemented

1. **Real-time AI voice/video lecture moderation is NOT running.** Reports go to admin for human review. AI inference, consent screens, recording storage, moderation confidence thresholds, appeal workflow and privacy policies require a separately configured provider and security review. AI must never automatically fine or ban someone based only on a model flag.
2. **Hosted live-video access and role enforcement are NOT production-secure.** This build uses an example Jitsi room with UI-level mentor/student permissions; a public room service cannot guarantee that a student never screen-shares or that room URLs cannot be reused. Select a video provider with signed, role-scoped room tokens and enforce screen-share privileges server-side.
3. **Real payouts and automated bank KYC are NOT integrated.** The prototype stores bank last four digits + IFSC and requires manual admin verification. Withdrawals are admin-reviewed ledger requests, not transfers. A real payment processor/KYC service, encryption at rest and compliance checks are needed before accepting real customers.
4. **Payment/GST is illustrative.** The default GST_BPS=1800 and COMMISSION_BPS=500 are configuration examples. A qualified Indian tax/payment specialist must confirm who supplies the service, invoicing, TDS/GST treatment, collections, escrow/refunds and bank reconciliations. Refunds currently credit the learner wallet; provider settlement clawbacks and dispute accounting must be designed before real-money launch. Seeded demo wallet credits have no cash value.
5. **Seeded lessons are text-first; not every lesson has an existing video/PDF.** Mentors must upload an MP4 and separate PDF notes for each topic. This update adds the actual upload, secured download and video playback workflow; it cannot invent source course recordings.
6. **Security hardening is partial.** A new login invalidates the last login token. Production still needs HTTPS, HttpOnly+Secure cookies instead of client-stored bearer tokens, access/audit logs, rate limiting, MFA, penetration testing, secure object storage, backups, secure room authorization, production secrets, and deployment-specific CSP. All newly deployed environments must set DEMO_MODE=false and a long random JWT_SECRET.
7. **Existing live group classes** that predate the new lectureId field may have an empty ID until migrated; new sessions are assigned unique IDs. Migration scripts should be reviewed for production databases.
8. **Smart TV and all browser/device UX** have not been physically tested; responsive CSS has been added but field testing is still necessary.

## How to run locally in Windows VS Code (no Docker)

Requirements: Java JDK 21, Maven 3.9+, Node.js 22 LTS or current supported release, npm, and working internet during first dependency installation.

In VS Code open the extracted `SkillSwap-Pro` folder. Open **two separate terminals**.

Terminal 1 — API:

```powershell
cd backend
mvn clean test
mvn spring-boot:run
```

Terminal 2 — web:

```powershell
cd frontend
Copy-Item .env.example .env.local
npm install
npm run lint:types
npm run dev
```

Visit `http://localhost:3000`. Set `NEXT_PUBLIC_DEMO_MODE=true` in `frontend/.env.local` only for demo/testing to prefill demo login; otherwise enter one of the documented demo accounts manually. The backend creates seeded demo accounts only when `DEMO_MODE=true`.

Demo credentials, provided by the original project: `student@skillswap.local` / `Demo12345!`; `admin@skillswap.local` / `Demo12345!`; `mentor1@skillswap.local` / `Demo12345!`. Never deploy these seeded accounts publicly.

**Windows troubleshooting:** `mvn` not found -> install Maven and add its `bin` directory to PATH, restart PowerShell. `next` not recognized -> run `npm install` *inside frontend*, check the registry connection, then `npm run dev`. Do not run `npm run dev` at the ZIP root; there is no root package.json. If a prior H2 database is already populated, back it up and test schema upgrades on a copy first.

Production settings: `DEMO_MODE=false`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGIN`, `STORAGE_DIR`, `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET` (if used), `COMMISSION_BPS=500`, and legally verified `GST_BPS`. Disable demo accounts and credit creation.

## Suggested manual acceptance tests

- Open all pages and horizontal nav links on 360px, 768px, 1280px, large-screen/TV browser; check keyboard focus and zoom at 200%.
- Search course/mentor categories, switch programming-language topic, clear filters, and view profile bio/skills.
- Register a learner, buy a course with demo funds, open learning page, upload MP4/PDF as course mentor, verify enrolled learner playback/download, verify anonymous access is blocked.
- Request 1, 2, and 8 hour live bookings; compare quote and ledger, first free entitlement, breaks/lecture IDs; mentor confirms a session and students join.
- Start individual/group chats, search and switch conversations, send and download PDF; refresh and check persistence.
- Submit mentor ID, bank details, contract; verify that application and admin approval are blocked until requirements pass; ensure bad documents rejected.
- Report a lecture, investigate, confirm/reject with evidence, check score/penalty, duplicate report prevention, refund-once behavior, and third-strike account disabling; test learner false reports and group reports separately.
- Log in on two devices and confirm first session becomes invalid; log out and test old token; verify every API route enforces authorization.
- Exercise wallet top-up (demo only), withdrawals, admin review and tax breakdown; never mistake ledger credits for cash.

## Image attribution

- Home hero: Sanket Mishra / Unsplash, photo `LCwo5opgr9M` (Indian college students, New Delhi), free Unsplash License.
- Login hero: Haseeb Modi / Unsplash, photo `LKySOH0mr38` (computer classroom, Chennai), free Unsplash License.

Images are loaded from the Unsplash CDN; an online connection is needed to see them. The design/layout code is original but seeded catalogue imagery is from the original project.
