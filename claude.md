# CLAUDE.md

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

## 5. No Closing Colons (Korean Output)

**End Korean sentences with a period, not a colon.**

When the user writes in Korean, your output is also Korean:
- Don't end sentences with `:` even if the next line is a list or example.
- LLMs trained on English docs leak the colon habit into Korean. Catch it.
- The test: every Korean sentence terminator should be `.`, `?`, or `!` — not `:`.
- Colons are fine inside code, key-value pairs, or labels. Not as sentence enders.

## 6. File Header Comments in Korean

**First line of every new source file: a one-line Korean comment stating its role.**

When creating a new file:
- TypeScript/JavaScript: `// 사용자 인증 상태를 관리하는 Context Provider`
- Python: `# KIS API 호출을 비동기로 래핑하는 클라이언트`
- SQL: `-- 일별 집계 결과를 저장하는 머티리얼라이즈드 뷰`
- Place it directly under required directives (`'use client'`, `'use server'`, shebang).
- Skip config files (`*.config.ts`, `package.json`, etc.).

Why: agents read files selectively, not whole codebases. A one-line Korean header gives instant context so the next session (human or agent) can navigate without re-reading the entire file.

## 7. Plan + Checklist + Context Notes

**Before any non-trivial task, produce three artifacts. Don't start coding without them.**

- **Plan** — what we're building and why.
- **Checklist** (`checklist.md`) — concrete tasks as checkboxes. Tick as you go.
- **Context Notes** (`context-notes.md`) — decisions made during the work and the reasoning behind them. Append continuously.

If the user gives only a plan and asks you to start coding, stop and ask: "Should I create the checklist and context notes first?" The next session — yours or someone else's — needs the notes to pick up where you left off without re-deriving every decision.

## 8. Run Tests Before Marking Complete

**If you touched code, run the tests before saying "done".**

- `npm test`, `pytest`, `cargo test`, whatever the project uses — run it.
- If tests pass, report results. If they fail, fix and re-run.
- No test setup? At minimum, verify the project builds/compiles.
- Run tests proactively, before the user signals "끝", "완료", "다 됐어" — not after.

This is the step LLMs skip most often. Treat it as non-negotiable.

## 9. Semantic Commits

**Commit when one logical change is complete. Don't wait for the user to ask.**

- The test: "Can I describe this commit in one sentence?" If yes, commit. If no, the changes are still mixed — split them.
- Good: "auth 미들웨어 추가". Bad: "auth 추가하고 UI도 고치고 버그도 수정" (split into 3).
- Don't accumulate 20 unrelated edits and lose the ability to roll back individually.
- Don't commit just to commit — meaningful units only.

Note: For solo prototypes or throwaway scripts, group commits loosely if it slows you down. The point is reversibility, not ceremony.

## 10. Read Errors, Don't Guess

**Read the actual error/log line. Don't pattern-match from memory.**

When something fails:
- Read the full error message and stack trace.
- Check the actual log output, not what you assume it should say.
- Don't apply a "common fix" before confirming the cause.
- If unclear, add a print/log to verify state — then fix.

This is the step LLMs skip most often after "run tests". They guess from error keywords and apply the most-recent-pattern fix. That's how a one-line bug becomes a three-file refactor.

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

---

## 11. Cursor Rules (`.cursor/rules/*.mdc`)

**세션 시작 시, 그리고 관련 파일 편집 전 반드시 해당 rule을 읽고 준수한다.**

각 `.mdc` 파일 상단 frontmatter의 두 필드를 기준으로 적용 범위를 판단한다.

| 필드 | 값 | 동작 |
|------|----|------|
| `alwaysApply` | `true` | 파일 종류 무관, 항상 적용 |
| `alwaysApply` | `false` | `globs` 패턴에 매칭되는 파일 편집 시에만 적용 |
| `globs` | 패턴 (예: `**/*.java`) | 해당 파일 편집 시 rule을 먼저 읽는다 |

현재 등록된 rules.

- `.cursor/rules/survey-rebuild-project.mdc` — `alwaysApply: true`. 설문 재구축 프로젝트 전반 규칙 (용어, UI, 검색, 데이터, 작업로그). 모든 작업에 적용.
- `.cursor/rules/java-method-layout.mdc` — `globs: **/*.java`. Java 메서드 시그니처·블록 레이아웃. `.java` 파일 편집 시 적용.
- `.cursor/rules/java-record-schema.mdc` — `globs: **/*.java`. Java record `@Schema` 어노테이션 및 컴포넌트 간격. `.java` 파일 편집 시 적용.
- `.cursor/rules/html-markup-layout.mdc` — `globs: **/*.html`. HTML·Thymeleaf 요소 단위 줄바꿈, 큰 블록 구분용 빈 줄, 구역 시작·끝 주석. `.html` 파일 편집 시 적용.
- `.cursor/rules/thymeleaf-layout-dialect.mdc` — `globs: **/*.html`. Layout Dialect `layouts/*` shell, `layout:decorate`, controller layout model. `.html` 파일 편집 시 적용.

새 `.mdc` 파일이 추가되면 이 목록도 함께 갱신한다.

## 12. Superpowers Skills

**세션 시작 시 `using-superpowers` skill을 먼저 확인하고, 작업 성격에 맞는 skill을 반드시 사용한다.**

주요 skill 목록 (전체 목록은 시스템 프롬프트의 available skills 참고).

| Trigger | Skill |
|---------|-------|
| 새 기능·컴포넌트 구현 전 | `superpowers:brainstorming` |
| 버그·테스트 실패 발생 시 | `superpowers:systematic-debugging` |
| 구현 완료 후 검증 전 | `superpowers:verification-before-completion` |
| 멀티스텝 구현 계획 수립 | `superpowers:writing-plans` |
| 계획 실행 | `superpowers:executing-plans` |
| 브랜치 통합 결정 시 | `superpowers:finishing-a-development-branch` |
| 코드리뷰 수신 시 | `superpowers:receiving-code-review` |

"1%라도 관련 skill이 있을 것 같으면 반드시 호출한다"는 원칙을 따른다.