# Agent Behavior

Cursor loads this file for cross-project agent habits in this repository.
**Project rules** live in `.cursor/rules/` (e.g. `survey-rebuild-project.mdc`, `java-method-layout.mdc`, `java-record-schema.mdc`).

**Tradeoff:** These rules bias toward caution over speed. For trivial tasks (typo, one-line fix), use judgment.

---

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — do not pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what is confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No flexibility or configurability that was not requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Do not improve adjacent code, comments, or formatting.
- Do not refactor things that are not broken.
- Match existing style, even if you would do it differently.
- If you notice unrelated dead code, mention it — do not delete it unless asked.

When your changes create orphans:

- Remove imports, variables, or functions that **your** changes made unused.
- Do not remove pre-existing dead code unless asked.

Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" → tests for invalid inputs, then make them pass
- "Fix the bug" → test that reproduces it, then make it pass
- "Refactor X" → tests pass before and after

For multi-step tasks, state a brief plan:

```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") need clarification.

## 5. Korean Output — No Closing Colons

When the user writes in Korean, reply in Korean:

- Do not end sentences with `:` even if the next line is a list or example.
- Every Korean sentence should end with `.`, `?`, or `!` — not `:`.
- Colons are fine inside code, key-value pairs, or labels — not as sentence enders.

## 6. File Header Comments (Korean)

**First line of every new source file: a one-line Korean comment stating its role.**

Examples:

- Java: `// 설문 마스터 목록 조회 서비스`
- HTML / Thymeleaf: `<!-- 설문 이력 상세 읽기 전용 화면 -->`
- SQL: `-- 제출 설문 스냅샷 저장 테이블`
- TypeScript: `// 사용자 인증 상태를 관리하는 Context Provider`

Place directly under required directives (`'use client'`, package declaration, etc.).
Skip config files (`*.config.*`, `package.json`, `application.yml`, Gradle files, etc.).

## 7. Plan, Checklist, and Context Notes

Before any **non-trivial** task, produce three artifacts. Do not start coding without them.

| Artifact | Path | Purpose |
|----------|------|---------|
| Plan | Short summary in chat or `docs/superpowers/specs/YYYY-MM-DD-<topic>-design.md` | What we build and why |
| Checklist | `docs/checklist.md` | Concrete checkbox tasks; tick as you go |
| Context notes | `docs/context-notes.md` | Decisions and reasoning; append continuously |

If the user gives only a plan and asks to code, ask whether to create the checklist and context notes first.
The next session (human or agent) should resume without re-deriving decisions.

Trivial fixes (typo, single obvious line) skip this section.

## 8. Verify Before "Done"

**If you touched code, run verification before claiming completion.**

- Tests: `./gradlew test` (optionally scoped, e.g. `./gradlew test --tests 'com.reven.project.service.bd.*'`)
- Build: `./gradlew build` when compile or packaging matters
- Report pass/fail with evidence; if tests fail, fix and re-run
- Run proactively before the user says "끝", "완료", or "다 됐어"
- After substantive edits, check lints on touched files when the IDE reports issues

No test setup for a change? At minimum confirm the project compiles.

## 9. Git Commits

**Commit only when the user explicitly asks.**

- Still group work into **logical units** so you can suggest separate commits ("auth fix" vs "UI tweak").
- Good commit subject: one clear sentence describing why.
- Bad: one commit mixing unrelated auth, UI, and bugfix — split when the user asks to commit.
- Do not commit secrets (`.env`, credentials, local DB dumps).
- Follow repository commit message style from recent `git log`.

## 10. Read Errors, Don't Guess

When something fails:

- Read the full error message and stack trace.
- Check actual log output, not what you assume it should say.
- Do not apply a "common fix" before confirming the cause.
- If unclear, add a log or inspect state — then fix.
