# Verification report — 14 November 2026

## Source-level checks completed in the authoring environment

- Java 21 compiler parser: **44 Java source/test files, zero parse diagnostics**. This is a syntax parse, not semantic compilation with Spring Boot dependencies.
- TypeScript parser: **35 TS/TSX files, zero syntax diagnostics**.
- HTML hydration markup: removed `<Link><Brand/></Link>` patterns from login/register/privacy/terms screens; `Brand` remains a single Next `<Link>`.
- Next stylesheet: only top-of-file `@import 'tailwindcss'` remains; the Google Fonts stylesheet is referenced in `layout.tsx`.
- New authenticated people directory, messages UI, 20-course seed upgrade, group workshops and popup-safe Jitsi join flow have been included in the source tree.
- Existing account data is retained when the upgraded code points at the original database; local demo content seeding checks for existing courses/workshops before inserting.
- PowerShell smoke test script is included for validation on a Windows PC where the backend is running.

## Not possible in this environment

- npm dependency installation and Next build, because npm registry resolution/network access were unavailable here.
- Maven compile/tests because Maven and Spring dependencies were unavailable here.
- Actual H2/Postgres runtime tests, real browser UI interaction, Jitsi camera/microphone connectivity and payment-gateway verification.

**This package is not certified fully working end-to-end.** Run `mvn test` / `mvn spring-boot:run`, `npm install`, `npm run lint:types`, `npm run build`, and `powershell -ExecutionPolicy Bypass -File .\test-local-windows.ps1` on a Windows machine with network access. If any test fails, preserve the exact terminal error so the corresponding backend or frontend file can be patched.

## Production readiness boundaries

Demo money is not real funds. Settlement and GST figures are illustrative. Jitsi meeting rooms are external and do not implement authenticated production room membership. Live Razorpay credentials, webhooks, refund policies, financial reconciliation, secure identity verification, moderation, HTTPS and other production controls are not provisioned by this ZIP.
