# Roadmap — Mid-level mobile portfolio (Japan)

**Owner:** Jian Wang  
**Applications window:** ~May 2027 (mid-level mobile roles in Japan)  
**Hard readiness date:** **March 2027** (feature freeze + interview packet)  
**Repo:** [wjdartmouth/mobile-dev-portfolio](https://github.com/wjdartmouth/mobile-dev-portfolio)

This roadmap turns the current five paired apps (iOS + Android) into work that reads as **experienced mid-level ownership**: tests, CI, accessibility, offline/sync depth, localization, performance, and polished bilingual docs.

---

## Success criteria (March 2027 checklist)

- [ ] All five iOS apps open via XcodeGen, build, and run on simulator with your Team
- [ ] All five Android apps `assembleDebug` (and preferably `assembleRelease` unsigned) green in CI
- [ ] Unit tests for ViewModels / services on both platforms for ≥3 apps; UI smoke tests for ≥2
- [ ] GitHub Actions: iOS (macOS runner or documented Mac workflow) + Android Gradle CI
- [ ] Real app icons + store-quality screenshots in `docs/screenshots/`
- [ ] Japanese + English strings for primary flows (or clear EN-only with JA hiring docs)
- [ ] Accessibility pass (VoiceOver / TalkBack labels, Dynamic Type / font scale)
- [ ] Sync Notes cloud path documented with a 5-minute enable guide (CloudKit / Firebase)
- [ ] Root README + this roadmap + per-app READMEs current; sample interview Q&A added
- [ ] Optional 6th “sophisticated” app (or major module) demonstrating senior-leaning depth
- [ ] Public repo looks actively maintained (weekly commits through the freeze)

---

## Phase 0 — Baseline (done as of Nov 2026)

| Item | Status |
|------|--------|
| Five SwiftUI apps under `Apps/` | ✅ |
| Five Compose apps under `Android/` | ✅ (`assembleDebug` verified) |
| Per-app feature READMEs | ✅ |
| Architecture notes | ✅ `docs/architecture.md` |
| Repo rename to `mobile-dev-portfolio` | ✅ |

---

## Phase 1 — Harden the five (Nov–Dec 2026)

**Theme:** Make existing apps look like someone who ships, not someone who demos.

### November 2026

| Work | Why mid-level JP hiring cares |
|------|-------------------------------|
| Replace placeholder app icons (iOS asset catalogs + Android mipmaps) | First visual signal in any screen share |
| Explicit empty / loading / error UI audit on every screen | Shows product sense, not only happy paths |
| Privacy / permission copy review (EN); draft JA strings file | Japan apps expect careful permission UX |
| Focus Timer: document App Group + widget / Glance setup end-to-end | Widgets are a common interview differentiator |
| Spot Journal: photo storage size limits + delete/export flows | Media apps fail interviews on edge cases |
| Add CONTRIBUTING-style “how I work in this repo” note (short) | Signals ownership habits |

### December 2026

| Work | Why |
|------|-----|
| **Unit tests**: timer math, weather decoding, ledger aggregates, note merge policy | Mid-level = tests without being asked |
| **Android**: WorkManager retry for weather refresh / note sync hooks | Background work is a classic Android interview topic |
| **iOS**: XCTest for services; Swift Testing where appropriate | Same bar on Apple stack |
| **GitHub Actions**: Android `assembleDebug` matrix for all five apps | Green badges on README |
| Accessibility: VoiceOver labels, TalkBack `contentDescription`, large text smoke | JP enterprise / consumer bars increasingly require a11y |
| Crash / non-fatal logging hooks (OSLog / Timber) behind a debug flag | Shows production instincts without adding Analytics PII |

**Exit criteria for Phase 1:** CI green for Android; test suites exist for ≥3 apps; icons not placeholders; a11y checklist started.

---

## Phase 2 — Depth: networking, offline, sync (+ optional 6th app) (Jan 2027)

**Theme:** Sophistication that mid-level candidates are expected to discuss fluently.

### Networking & offline

- Pulse Weather: stale-while-revalidate cache, connectivity banner, retry with backoff
- Shared networking errors mapped to user-visible copy (EN/JA)
- Certificate / ATS notes; no plaintext secrets

### Sync depth (Sync Notes)

- Document conflict policy (last-write-wins vs field merge) with a diagram in `docs/`
- iOS: CloudKit zone + subscription sketch; keep local fallback default
- Android: optional Firestore enable guide; Room as source of truth
- Multi-device demo script for interviews (2 simulators / emulator + device)

### Optional 6th sophisticated app (pick one)

Choose **one** to add under `Apps/` + `Android/` if bandwidth allows:

| Candidate | Mid-level signal |
|-----------|------------------|
| **Habit Coach** | Notifications scheduling, widgets, streaks, local analytics charts |
| **Receipt Capture** | Camera pipeline, on-device ML (Vision / ML Kit), structured OCR → Ledger |
| **Team Pulse** | Lightweight chat or status board with auth + realtime (Firebase / AppSync style) |
| **Modular Design System sample** | Shared UI kit module consumed by 2+ apps (package modularization) |

Prefer depth on one new app over shallow clones of the existing five.

### Platform craft

- iOS: modularization via local Swift packages for Shared models (Focus Timer already hints at this)
- Android: extract `:core:ui` / `:core:data` modules if a 6th app lands
- Design tokens (color / type / spacing) documented once, reused

**Exit criteria for Phase 2:** Offline/sync story interview-ready; either a serious 6th app **or** equivalent modularization + sync depth PRs.

---

## Phase 3 — Polish & interview packet (Feb 2027)

**Theme:** Make the repo and your narrative bilingual and defensible.

| Work | Deliverable |
|------|-------------|
| Performance pass | Scroll jank check on journal/ledger lists; image downsampling; Instruments / Macrobenchmark notes |
| Instrumentation | Optional Debug menu: env banner, fake clock for timer, mock weather |
| Bilingual docs | Root README JP section kept current; app store–style JA blurbs |
| `docs/interview-qa.md` | 15–20 Q&As mapped to these apps (MVVM, sync, widgets, Room vs SwiftData, etc.) |
| Screenshot pack | Light + dark, JP device locale where possible → `docs/screenshots/` |
| README badges | CI status, platforms, license |
| Security hygiene | `.gitignore` audit; no keys; Sync Notes Firebase instructions only |

**Exit criteria for Phase 3:** You can run a 45-minute technical interview using only this repo + `interview-qa.md`.

---

## Phase 4 — Freeze & ship the portfolio (March 2027)

**Theme:** Stop adding features. Package for applications.

| Week focus | Actions |
|------------|---------|
| Early March | Feature freeze; bugfix only; tag `portfolio-2027.03` |
| Mid March | Final screenshots; short release notes per app; update root README “Last verified” dates |
| Late March | Readiness checklist (top of this file) all checked; practice walkthroughs in JP + EN |
| After freeze | Only CI fixes / typo fixes until applications start (~May 2027) |

**Do not** start a 7th app in March. Depth and clarity beat novelty.

---

## Weekly cadence (routine)

Target: **1 meaningful commit series per week** through March 2027.

Suggested rotation (repeat):

1. **Week A — Quality:** tests, CI, lint, a11y  
2. **Week B — Product polish:** empty states, icons, copy (EN/JA), screenshots  
3. **Week C — Sophistication:** offline, sync, widgets, WorkManager, modularization, or 6th-app slice  
4. **Week D — Narrative:** README/ROADMAP/interview-qa updates reflecting what shipped  

If a week slips, prefer closing a quality debt over starting a new feature.

---

## Sophistication backlog (pick from as capacity allows)

### Already partially in place
- Focus Timer widgets (WidgetKit / Glance)
- Live Activity path (iOS)
- Open-Meteo networking without API keys
- CloudKit-ready + local fallback (iOS Sync Notes)
- Optional Firestore (Android Sync Notes)

### High value next
- [ ] GitHub Actions Android matrix  
- [ ] XCTest / JUnit for domain logic  
- [ ] Compose UI tests / XCUITest smoke  
- [ ] WorkManager scheduled refresh  
- [ ] Japanese `strings.xml` / `Localizable.xcstrings`  
- [ ] Design system module  
- [ ] Macrobenchmark / Instruments notes in `docs/performance.md`  
- [ ] App icon set + adaptive icons  
- [ ] Conflict-resolution diagram for Sync Notes  
- [ ] 6th sophisticated app (see Phase 2)

### Nice to have (post-freeze or stretch)
- [ ] Flutter or KMP experiment *as a separate folder* (only if it strengthens the story; not required for March)
- [ ] TestFlight / internal Play track links (private)
- [ ] Architecture Decision Records (ADRs) for persistence & sync choices

---

## Risks & mitigations

| Risk | Mitigation |
|------|------------|
| Mac/Xcode access gaps | Keep XcodeGen yml accurate; ask a friend/CI macOS runner for compile proofs; document last-verified date |
| CloudKit/Firebase friction | Local-first demos by default; cloud as optional appendix |
| Scope creep past March | Freeze rule; park ideas in this backlog |
| Docs drift from code | Weekly narrative rotation (Week D) |

---

## Related docs

- [README.md](README.md) — hiring-facing overview (EN + JP summary)
- [docs/architecture.md](docs/architecture.md) — shared patterns
- [Android/README.md](Android/README.md) — Android matrix & Firebase notes
- Per-app READMEs under `Apps/*` and `Android/*`

---

*Living document. Update the checklist as work lands. Last plan revision: November 2026.*
