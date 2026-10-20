# Admin UI (SP1-05)

Built on `feature/fe-init` at `4d7d97c`. All changes are on `feature/sp1-05-admin-layout-catalogs`.

## Run

```sh
npm ci
npm run dev
```

Routes (BAN_QUAN_LY only): `/admin/accounts`, `/admin/catalogs/faculties`, `/admin/catalogs/classes`, `/admin/catalogs/disease-groups`, `/admin/catalogs/medicines`.

The existing backend currently implements authentication only. Live admin mode is the default and displays a retryable error when the pending APIs are unavailable. To preview synthetic CRUD data, add `VITE_ADMIN_DEMO=true` to `.env.local` and restart Vite. This does not bypass authentication: log in with a management account through the backend. Demo edits persist under `unicare.admin-demo.v1` in localStorage. Demo password reset does not change real credentials. Remove that key to reset sample records. Never put real account or medical data in demo storage.

## Pending API contract — coordinate with BE2 / BE3

Endpoint names below are proposals, not existing backend endpoints. Update only `src/api/adminApi.js` when the agreed contract is available. Axios continues to attach the existing Bearer token.

| Operation               | Proposed endpoint                                                        |
| ----------------------- | ------------------------------------------------------------------------ |
| List / create users     | GET / POST `/admin/users`                                                |
| Update user             | PUT `/admin/users/:id`                                                   |
| Lock / unlock           | PATCH `/admin/users/:id/status`, body `{ status: 'ACTIVE' or 'LOCKED' }` |
| Reset password          | POST `/admin/users/:id/reset-password`                                   |
| List / create catalog   | GET / POST `/catalogs/:resource`                                         |
| Update / delete catalog | PUT / DELETE `/catalogs/:resource/:id`                                   |

List responses: array or `{ data: array }`, containing the complete collection (UI filters and paginates locally). Do not return only one server page; add server pagination/filtering to the adapter and hook before using paginated APIs. Mutation responses can be raw objects or `{ data: object }`. Errors should expose `message`; HTTP 409 is recommended for duplicates or referenced catalog deletion.

Account: `id`, `username`, `fullName`, `role`, `status`. Creating an account leaves default-password generation to the backend. The UI never stores passwords. Catalog common fields: `id`, `code`, `name`; faculty and disease group add `description`; class adds `facultyId`, `cohort`; medicine adds `unit`, `strength`, `description`.

The server must enforce roles, uniqueness, foreign keys, prevention of self-lock/demotion, password policy and audit logging. Frontend guards are navigation controls, not a security boundary. Ban quản lý cannot open `/medical-list`.

## Maintenance

- `config/roles.js`: role labels and landing paths.
- `config/catalogs.js`: fields for each catalog.
- `components/layout/`: shared responsive shell extending FE1.
- `components/admin/AdminUI.jsx`: table, form, confirmation, feedback.
- `hooks/useResource.js`: loading and retry lifecycle.
- `api/adminApi.js`: live/demo data adapters.
- `admin.css`: layout and admin component styling using `global.css` theme tokens.

Mobile uses a native dialog drawer (Escape, focus containment and focus restoration). Tables scroll within their own region at 360px. Record dialogs use native focus containment. Notification popover is an explicit empty state until the notification service exists.

## Design and extension guidance

This extends the existing navy navigation, blue actions and light dashboard surfaces. `src/main.jsx` loads `global.css` before `admin.css`; the latter is global, including on non-admin routes. Keep shared theme changes deliberate rather than adding page-specific root overrides.

| Effective token | Value | Use |
| --- | --- | --- |
| `--primary-navy` | `#15243c` | Navigation and drawer |
| `--primary-blue` / `--primary-blue-hover` | `#3062ee` / `#244fd0` | Primary actions and selected navigation |
| `--bg-main` | `#f5f7fb` | Application canvas |
| `--border-light` | `#dfe5ef` | Panel and control boundaries |
| `--text-main` / `--text-muted` | `#0f172a` / `#64748b` | Main and supporting text, inherited from FE1 |

The first five color values override FE1 defaults in `admin.css`. White panels currently use literal `white`; `--surface: #fff` is declared but not consumed. Keep the system font stack from `global.css`. The shell uses 14px text, page titles 26px/600 (24px on mobile), and table/control labels generally 12px. Panels have 9px corners, buttons 7px, inputs 6px and dialogs 12px. Panels use borders; elevated shadows belong to popovers and dialogs.

- Reuse `MainLayout`, `Sidebar` and `Header` for all three roles; role navigation changes inside the shared shell. Desktop navigation is 246px wide or 78px collapsed, with a 74px header. Content is centered up to 1560px with 32px/30px padding.
- At 1100px and below, content padding tightens and toolbars wrap. At 767px and below, navigation becomes a modal drawer, the header is 64px high, content padding is 24px/16px, headings stack, and catalog links form two columns. Keep the CSS boundary aligned with the desktop rule starting at 768px.
- Preserve `DataTable`'s focusable, labeled horizontal-scroll region and mobile scrolling hint. Mobile tables retain a 690px minimum width inside that region; do not hide action columns or make the whole page scroll horizontally. Loading, retry, empty results and pagination are shared behavior.
- Compose screens with `PageHeading`, `SearchField`, `DataNotice`, `Feedback`, `DataTable`, `RecordForm` and `ConfirmAction` from `components/admin/AdminUI.jsx`. Add catalog fields through `config/catalogs.js`; reuse `Modal` for forms and confirmations so focus restoration, Escape handling and busy-state protection stay consistent.
- Keep visible keyboard focus, labeled icon actions and text alongside role/status colors. Feedback already uses `status` or `alert`; preserve those announcements. The 150ms hover transitions are disabled for reduced-motion preferences.

When extending these screens, check desktop and 360px layouts, keyboard access to the table and drawer, and long Vietnamese labels. These checks describe the existing component contract; API integration remains governed by the adapter contract above.

## Verification performed

- `npm run lint`, `npm run build`, and `git diff --check` pass.
- Chromium at desktop 1440px and mobile 360px: account create/edit/lock/unlock/reset, role/search filters, persistence; CRUD for all four catalogs; referenced-faculty deletion rejection; student/admin route denial; mobile drawer and dialog dismissal; no document overflow or uncaught runtime errors.
- Production preview with mocked API responses: Bearer header, wrapped list data, retry after 500, update/status/reset requests, inline 409 errors, 401/403 routing, and inline login failure. This verifies frontend behavior, not an actual BE2/BE3 deployment.
- Independent visual review: requested placeholder contrast correction and mobile table scroll hint both verified resolved.
