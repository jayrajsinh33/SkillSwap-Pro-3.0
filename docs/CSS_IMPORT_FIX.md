# CSS import fix — November 14, 2026

**Reported Next.js error**

`@import rules must precede all rules aside from @charset and @layer statements` at `frontend/app/globals.css`.

**Applied change**

- Removed the Google Fonts `@import url(...)` from `frontend/app/globals.css`.
- Added the same font stylesheet as an HTML `<link>` in the `<head>` of `frontend/app/layout.tsx`.
- Kept Tailwind's `@import 'tailwindcss';` at the beginning of `globals.css`.

**Update an existing copy**

Replace `frontend/app/globals.css` and `frontend/app/layout.tsx` using the matching paths
inside the hotfix ZIP. Any earlier changes made in these two files should be backed up.
Then stop (`Ctrl+C`) and restart the frontend using `npm run dev`.

**Scope**

This hotfix removes the known invalid CSS import ordering. Full `next build`, Maven
compilation, payment/video integrations, and end-to-end workflows are not claimed to
be verified in this environment.
