# SkillSwap — Page and navigation map

All account pages require an authenticated login. Role controls are enforced by the Spring API, not merely hidden navigation links.

| Route | Purpose | Access |
|---|---|---|
| `/` | Public SkillSwap landing page | Public |
| `/register`, `/login` | Create account / sign in | Public |
| `/terms`, `/privacy` | Platform information | Public |
| `/dashboard` | Learner overview & personal shortcuts | Logged-in users |
| `/discover` | Skills and instructor discovery | Logged-in users |
| `/courses` | Catalogue and course search | Logged-in users |
| `/courses/[id]` | Course overview, price quote, syllabus and payment | Logged-in users |
| `/mentors` | Instructor listings | Logged-in users |
| `/mentors/[id]` | Mentor bio and published courses | Logged-in users |
| `/my-learning` | Purchased courses and progress | Logged-in learners |
| `/learning/[courseId]` | Persistent lesson reader/completion and mentor editing | Enrolled learner, owner mentor or admin |
| `/classes` | One-to-one bookings, group sessions and join links | Logged-in users |
| `/classroom/booking/[id]` | Authorized one-to-one Jitsi video classroom | Assigned participants or admin; confirmed booking |
| `/classroom/group/[id]` | Authorized group Jitsi video classroom | Registered learner, host or admin |
| `/bookings` | Accept/complete/cancel personal appointments | Logged-in users; permission-checked actions |
| `/messages` | Personal chats, course communities and attachments | Logged-in users; per-conversation permission checks |
| `/wallet` | Balance, transactions, top-ups, payout status | Logged-in users; owner-only data |
| `/earnings` | Mentor sales, commission and withdrawal requests | Approved mentors |
| `/mentor-studio` | Apply as mentor, manage courses, materials and lessons | Logged-in users; edits require mentor approval |
| `/profile` | **The single My Profile page**: identity and account quicklinks | Logged-in users |
| `/settings` | Password and account controls | Logged-in users |
| `/notifications` | Account activity feed | Logged-in users |
| `/support` | Help and support tickets | Logged-in users |
| `/admin` | Manage users, courses, payments, bookings, support | Admin only |

Workflow: **discover → course details → wallet checkout → my learning → lessons → class booking / group registration → classroom → messages → profile**.

Do not share demo account credentials or the local demo JWT secret in a public deployment.
