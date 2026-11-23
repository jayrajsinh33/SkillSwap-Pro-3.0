# SkillSwap Pro 2.1 — November update

**Please read [Release notes](docs/RELEASE_NOTES_2_1.md) and [Test evidence](docs/TEST_RESULTS_2_1.md) before running.** Updated navigation, in-app classroom, data-backed lessons and wallet transaction details are now included. Back up `backend/data` and `backend/uploads` before replacing files. This is a locally testable development codebase, **not a fully verified production service**.

---

# SkillSwap Pro — local full-stack marketplace starter

**Tagline:** Learn What You Love, Teach What You Know, and Earn What You Deserve.

This ZIP contains an editable **Next.js 16 + TypeScript** web application and a **Java 21 / Spring Boot 3.5.9** REST API. It implements a substantial **LOCAL DEMO** of a paid learning marketplace with role-based accounts and persistent data. It is not a deployed or certified production payment platform.

## Quick start on Windows (VS Code)

**Install once:** Node.js 22+, Java JDK 21, Maven 3.9+. Internet is required for the first dependency install and to load the Unsplash sample pictures and external video rooms.

1. Extract the ZIP and **Open Folder** `SkillSwap-Pro` in VS Code.
2. Open **Terminal 1** (Command Prompt or PowerShell):

   ```powershell
   cd backend
   mvn spring-boot:run
   ```

   Wait for the Java API to start on port **8080**. `http://localhost:8080/api/health` should return `{"status":"ok","service":"skillswap-api"}`.

3. Open **Terminal 2**:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

4. Visit **http://localhost:3000**. Public marketing, courses and mentors show first. Sign in at `/login` to see your dashboard.

### Demo accounts (LOCAL ONLY)

| User type | Email | Password |
|---|---|---|
| Learner | `student@skillswap.local` | `Demo12345!` |
| Mentor | `mentor1@skillswap.local` | `Demo12345!` |
| Admin | `admin@skillswap.local` | `Demo12345!` |

You can also register new learner accounts. The demo student starts with ₹3,500 in **fake test credits**. Other demo users have no cash value in their wallet. All seeded mentor identities, course records and wallet amounts are illustrative. Portraits/course photos load from Unsplash URLs; the portraits are not verified real mentors.

## Database options

The default quick-start uses **file-backed H2 in PostgreSQL compatibility mode** at `backend/data/skillswap.mv.db`. It is a real persistent local database, so data survives backend restarts. It needs **no Docker and no PostgreSQL installed**.

To switch to a proper **PostgreSQL 16** database, install Docker Desktop, start the database in the project root:

```powershell
docker compose up -d postgres
```

Then from `backend` run:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

PostgreSQL runs on localhost port **5433**, database `skillswap`, user `skillswap`, password `skillswap_local_only`. See `docker-compose.yml`. You can inspect it using **pgAdmin** / DBeaver. Tables include `app_users`, `courses`, `enrollments`, `bookings`, `group_classes`, `conversations`, `chat_messages`, `material_files`, `wallet_entries`, `settlements`, `withdrawals`, `topup_orders`, `reviews` and `support_tickets`.

**Important:** H2 and PostgreSQL are separate databases. Switching modes does not automatically migrate local data. Spring JPA uses `ddl-auto=update` for development convenience only. For production use Flyway migrations, backups and stricter deployment practices.

## What works in the local demo

- Public marketing home, responsive learner dashboard, search/discovery, category filters, top mentors, mentor details and course details.
- Register/login using hashed passwords and signed bearer tokens, learner + mentor on the same account, profile editor and password-change endpoint.
- Mentor application and administrator approval; mentor courses created as drafts for admin publication approval.
- Wallet balance, fake test credits, course enrollment purchase with a database transaction, double-purchase prevention and backend-calculated example GST, commission and mentor net.
- Teaching earnings ledger and withdrawal requests, which administrators manually mark paid or rejected (rejection refunds wallet hold).
- Enrolled course library, progress updates, learner review after purchase, mentor-uploaded resources and downloads with ownership checks.
- One-to-one class booking, mentor confirm / complete / cancel, and time-gated class join links.
- Group workshop creation, enrollment-only registration, attendee limits and group video links.
- Direct message and course community chat backed by the database; 5-second polling. PDF, text, DOCX and image attachments up to 12MB saved on the server.
- Notification/activity screen sourced from persisted booking and wallet events; support ticket submission and admin review.
- Admin overview, users/mentor approvals, course publication, bookings, per-sale settlements, withdrawal queue and support tickets.

## Important demo limitations

- **Live video** opens an external `meet.jit.si` room and needs a working internet connection. Room names are random, and the API time-gates releasing the URL, but meeting membership is **not** protected by an independently provisioned video provider. This is not a private production-grade meeting system. Calls, group calls and screen sharing depend on the external provider. Recording and moderation are not implemented.
- **Wallet funds:** `DEMO_MODE=true` allows adding **fake credit only**. The application is not connected to a real bank and does not automatically disburse mentor payouts. Real payment capture is *optionally scaffolded* via Razorpay key/secret and captured-payment verification, but gateway provisioning, production webhooks, refunds, reconciliation and compliance must be completed before accepting money.
- **GST and platform commission** are configurable demonstration calculations (**18% GST and 15% commission**), not a substitute for GST advice, invoices or legally compliant settlement accounting. Tax treatment depends on seller and marketplace structure.
- A single account may be both learner and mentor after admin approval. The seeded demo uses fixed sample profiles; there is no identity verification or automated certificates.
- Chat uses REST polling (not WebSocket). No SMS or email delivery, Google/Facebook OAuth, password reset email, anti-abuse moderation or browser push notifications are included. Terms/privacy pages are educational placeholders, not reviewed legal documents.
- Password tokens are stored in the browser for this local demo. Configure more secure auth, HTTPS, rate limiting, secrets and monitoring for public deployment.
- `npm install` and Maven dependencies require internet for the first download. The ZIP intentionally **does not include node_modules, Maven cache or database binaries**.

## API structure and source locations

`frontend/app/` contains individual pages that can be replaced one by one; `frontend/components/` contains reusable cards and navigation; `frontend/lib/` contains API and authentication helpers.

`backend/src/main/java/com/skillswap/web/` contains Spring REST controllers; `model/` contains JPA database entities; `repo/` contains repositories; `service/WalletService.java` contains the core payment-ledger math.

| What to change | File |
|---|---|
| Theme and design tokens | `frontend/app/globals.css` |
| Landing page | `frontend/app/page.tsx` |
| Courses and checkout | `frontend/app/courses/[id]/page.tsx` |
| My learning | `frontend/app/my-learning/page.tsx` |
| Live classes | `frontend/app/classes/page.tsx` |
| Chat & attachments | `frontend/app/messages/page.tsx` |
| Mentor studio | `frontend/app/mentor-studio/page.tsx` |
| Wallet | `frontend/app/wallet/page.tsx` |
| Admin console | `frontend/app/admin/page.tsx` |
| Backend checkout/commission | `backend/src/main/java/com/skillswap/service/WalletService.java` |
| Fee rates & database | `backend/src/main/resources/application.properties` |

This preserves your requested **page-by-page replacement workflow**. If you change an API response contract, you may also need to update the corresponding frontend types and backend handler.

## Test commands

Run after installing dependencies:

```powershell
cd backend
mvn test
```

```powershell
cd frontend
npm run lint:types
npm run build
```

Then manually verify: login as learner, add demo wallet funds, purchase a published course, check both users' transactions, book a class, confirm from mentor account, send message and file, create group class, publish a newly created course as admin, and test withdrawal approval/rejection.

**Testing transparency:** Source-level checks were performed while preparing the archive, but this environment had no Maven executable and could not resolve the npm registry. Consequently end-to-end browser/runtime tests and the Maven/Next production builds **could not be executed here**. Do not describe this archive as fully tested or production ready until you have run the commands above on your machine.

## Configuration before real use

By default `JWT_SECRET` is a visible development placeholder and `DEMO_MODE=true`. **Never put the default settings on a public server.** Set a strong private `JWT_SECRET`, set `DEMO_MODE=false`, configure `CORS_ORIGIN`, PostgreSQL DB variables, application firewall and HTTPS. Razorpay configuration is read from `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` environment variables. Review actual marketplace money movement, tax, refund and payout obligations with appropriate providers before switching to real transactions.

For help modifying a page later, tell me its exact filename and the changes required; the files are intentionally kept separate.

## CSS import hotfix (November 14, 2026)

The `globals.css` file no longer contains the late Google Fonts `@import` that caused
Next/PostCSS `@import rules must precede all rules`. The font stylesheet is now loaded
from `frontend/app/layout.tsx` via a `<link>` in the HTML head.

If you're replacing files inside an existing extracted project, copy **both**
`frontend/app/globals.css` and `frontend/app/layout.tsx` from this archive into your project,
overwriting those files. Restart `npm run dev`. This restores the packaged stylesheet;
back up local CSS customizations before overwriting.

This addresses the reported CSS parse error; it does not certify all remaining frontend
or backend flows as tested.

## Usability and API repair update (14 November 2026)

This edition applies the following fixes over the earlier CSS-hotfix ZIP:

- Fixes nested `<Link>` markup in login, register and legal pages (previously triggered `<a> cannot be a descendant of <a>` hydration warnings).
- Increases the desktop and mobile text, navigation, buttons, table cells and card spacing; retains the matte navy/teal design.
- Defers user-specific API requests until the login state is initialized. Returns a clearer HTTP 401 when a session is absent and displays protected-route errors rather than silently swallowing them in several pages.
- Makes wallet demo-credit entry validate the ₹100–₹20,000 range before posting; prevents misleading old success/error states.
- Adds member discovery and direct chats across active learners and mentors, purchased-course group chat shortcuts, and useful conversation filters.
- Adds **10 additional demo courses** on fresh and existing demo databases, bringing the initial catalogue to 20. Adds 3 starter workshops only if no group sessions exist. Existing wallets, saved chats and enrollments are not reset.
- In local demo mode, confirmed personal sessions and registered group sessions can open a Jitsi demonstration room outside the scheduled time; booking and membership authorization still apply. Production mode retains the join time window.
- Opens the video tab in the direct click handler before the API request to avoid popup blockers. Video connectivity still depends on Jitsi and your browser permissions.
- Includes separate Windows launchers that detect Maven in your previously shown `C:\Tools\apache-maven\apache-maven-3.10.0\bin` location.
- Includes `test-local-windows.ps1`, an automated local API smoke test using *fresh, randomly generated demo data*. **Do not run the smoke test in real-payments mode or a production environment.**

### Easiest way to start on your Windows PC

1. Back up your original `backend/data` and `backend/uploads` directories if you want to preserve your test records. These folders are not included in the new ZIP.
2. Extract the **entire** updated project folder. You may overwrite source files in your current `SkillSwap-Pro` folder; do **not** delete your existing database or uploads folders.
3. Double-click `start-local-windows.bat`, or open `start-backend-windows.bat` and `start-frontend-windows.bat` separately.
4. Confirm that `http://localhost:8080/api/health` returns `status: ok`; visit `http://localhost:3000`.
5. Open PowerShell inside the project root and execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\test-local-windows.ps1
```

The script checks health, registration, authentication, demo credit, checkout, enrollment, workshop registration/join, personal messages and admin access. It changes local **demo** data; never run it on production. If you see `401`, sign out and sign in again to refresh a stale demo login token. If you see `403`, verify the user's role or the protected action's permissions.

### What is *not* certified

While the source files and the ZIP are statically checked for syntax and structure, this authoring environment cannot download npm or Maven dependencies. Therefore the updated application **cannot be claimed fully runtime-tested or production-ready** here. Real payment settlement, video membership protection, recording, financial reconciliation, production GST accounting, and enterprise chat moderation remain separate production integrations. Review `docs/TEST_RESULTS.md` and run `mvn test` plus `npm run build` on your development PC.

---

## SkillSwap 3.0 update (Nov 2026)

The design/marketplace source changes are documented in [`docs/UPGRADE_3_0_STATUS.md`](docs/UPGRADE_3_0_STATUS.md). **Read that file first:** it identifies which features are built in source and which still require a video/AI/payment/KYC provider or real testing. The grammar test report is in [`docs/TEST_RESULTS_3_0.md`](docs/TEST_RESULTS_3_0.md) and extra commercial ideas in [`docs/REVENUE_AND_PARTNERSHIPS.md`](docs/REVENUE_AND_PARTNERSHIPS.md).

For local demo login convenience add `NEXT_PUBLIC_DEMO_MODE=true` to the frontend `.env.local`. Never set demo flags in production.
