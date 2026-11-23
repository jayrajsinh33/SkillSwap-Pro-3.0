# Test evidence — SkillSwap 2.1

## Completed in the authoring environment

- TypeScript/TSX parser: 36 frontend source files, zero syntax parse diagnostics.
- Java 21 compiler frontend: checked all Java sources for parse errors; no Java grammar errors were reported. Full semantic compile could not proceed due to missing Spring Boot dependencies.
- Route inventory: 25 Next.js pages including new lesson reader and in-app video classroom. Only one `/profile` page exists.
- ZIP integrity: checked after creation.

## Not completed here

- `npm install`, Next.js full build and runtime browser interactions (required NPM registry/dependencies unavailable in the authoring environment).
- Java Maven compile, Spring test suite or live database tests (Maven/Spring dependencies unavailable here).
- Jitsi video camera/microphone call involving real users (external service cannot be certified here).
- Live Razorpay and real bank withdrawals/GST accuracy (credentials, compliance and reconciliations not configured).

**Do not describe this release as "100% tested" or "production ready" based on the syntax checks.** Use `test-local-windows.ps1` and the checklist below on the real Windows computer. Capture any failing HTTP/terminal output for an exact code patch.

## Manual page checklist

1. Sign in with `student@skillswap.local` / `Demo12345!` (demo mode only).
2. Open sidebar Profile and top avatar — both should open **the same** `/profile`.
3. Dashboard → Courses → Course detail → Wallet checkout; confirm wallet is debited and enrollment appears under My learning.
4. My learning → Open lessons; select a lesson, mark it complete, refresh, confirm progress persists.
5. Sign in as `mentor1@skillswap.local` / `Demo12345!`; Mentor studio → Lessons → edit an existing lesson → reload and confirm saved notes.
6. Student → Book a personal class for a future time. Sign in as the assigned mentor or admin; accept it under Bookings. Student → Join classroom.
7. Confirm the classroom iframe renders or use the "Open externally" fallback; grant browser camera/mic permissions. Test with two separate browser sessions if Jitsi is reachable.
8. Student → Group classes → Register after course purchase → Enter classroom.
9. Messages → Find people → start chat → send a message and PDF → confirm recipient sees it.
10. Wallet → Add demo credits → verify ledger and balance; mentor account → Earnings → withdrawal request limited to eligible teaching income.
11. Admin → review user/mentor, publish a course, view bookings/settlements/withdrawals and support tickets.
12. Desktop & mobile layouts, back/forward navigation, refresh every route, sign out/in, account settings and notifications.
