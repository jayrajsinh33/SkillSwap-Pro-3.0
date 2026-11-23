# SkillSwap Pro 2.1 — Navigation, Learning, Wallet & Classroom Update

Date: 14 November 2026.

## What changed in this source update

- **Single profile page:** `/profile` is the sole personal profile route. The sidebar contains one "My profile" link. The sidebar footer is now account identification, not another profile button. The avatar in the top bar is a shortcut to the same page.
- **Connected navigation:** Dashboard, Explore, Courses, Mentors, My learning, Live classes, Bookings, Messages, Wallet, Earnings, Mentor studio, Profile, Settings, Notifications, Support and the role-gated Admin console remain separate connected pages. Mobile navigation is preserved.
- **Database-backed lessons:** new `course_lessons` and `lesson_completions` tables and `/learning/[courseId]` page; demo courses receive an editable lesson starter curriculum on startup. Learners can mark individual lessons complete. Mentor owners can edit lessons and add new content.
- **Classroom inside SkillSwap:** `/classroom/booking/[id]` and `/classroom/group/[id]` request a backend-authorized room name and show an embedded Jitsi iframe after click, with "Open externally" fallback. The video service itself is third-party and needs internet and permissions.
- **Bookings:** confirmed appointments have direct "Join classroom" links. Mentors can still accept/complete appointments; learners can cancel.
- **Wallet:** balance, eligible mentor income, payouts, transaction types and status are clearer; mentor withdrawal requests exclude demo top-ups from the eligible earning calculation. Razorpay requires real configured keys; demo credits are noncash.
- **Live workshops:** if previous demo workshops all expired, fresh future demonstration workshops can be seeded on startup without clearing previous data. Past public workshops no longer clutter the upcoming list.
- **Course detail:** shows database-backed lesson curriculum previews and links directly to the lessons when enrolled.
- **UI:** improved readability, focus states, course & classroom layouts, mobile breakpoints and calmer matte finish.
- **Local origin fix:** in DEMO_MODE, frontend can connect to Spring API from localhost/127.0.0.1 on different local development ports. Production remains explicitly origin-restricted.

## Existing user data

Keep the existing `backend/data` folder (H2 database files), `backend/uploads` folder and any `.env` files. Neither this ZIP nor the update script deletes those folders. Newly added tables are created with the configured development `ddl-auto=update` setting. **Back up your DB before upgrading.** Never overwrite an active database file while the backend is running.

## Important distinctions

| Workflow | Implementation scope |
|---|---|
| Navigation, lesson editor, progress records, course purchase, demo wallet ledger, bookings, user directory, chat and file attachments | Source implementation included; actual end-to-end run on your computer required |
| Video lessons | Embedded Jitsi test-room UI and authorization. Jitsi may require moderator sign-in or may change public-hosting policy; provider availability is not controlled by this source. |
| Real money transfers, secure payouts, GST compliance | **Not production-ready.** Live gateway onboarding, verification, regulatory checks and reconciliation are required. |
| Notifications | Derived from persisted booking and wallet activity. Not a push notification system. |
| Uploaded course content | Mentor can upload PDFs/files. Preseeded text is clearly demo starter content, not a purchased curriculum from an external provider. |
| Realtime messaging | Chat polling while page is open; not WebSocket-based push messaging. |

## Starting on Windows

Use `start-local-windows.bat` (two windows) or:

```powershell
cd 'C:\Users\Het\OneDrive\Desktop\SkillSwap-Pro\backend'
& 'C:\Tools\apache-maven\apache-maven-3.10.0\bin\mvn.cmd' spring-boot:run
```

In another terminal:

```powershell
cd 'C:\Users\Het\OneDrive\Desktop\SkillSwap-Pro\frontend'
npm install
npm run dev
```

Open `http://localhost:3000`. Backend health: `http://localhost:8080/api/health`.

## Testing

Once the backend is running, run from project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\test-local-windows.ps1
```

It creates a disposable QA learner in the **local demo** database, credits DEMO money, buys a demo course, verifies persisted lessons and completion, checks the group room authorization, sends chat messages, verifies admin access, confirms a personal booking and fetches its video room name. This is a REST API smoke test, **not** a browser/video/payment end-to-end test.

Also run `npm run lint:types`, `npm run build`, and `mvn test` with a connected dev machine before concluding the project is fully running.
