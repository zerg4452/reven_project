# 관리자 설문 문항 순서 변경 P4 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let admins reorder survey fields with up/down buttons on the survey detail screen and persist order via the existing full-form save into `sort_ord`.

**Architecture:** No backend changes. Extend `detail.html` and `survey-field-editor.js` to swap DOM rows and reindex `fields[n].*` names. Saving uses the existing POST to `insert.do` / `update.do`; `SASurveyService.insertChildren()` already assigns `sort_ord` from list order.

**Tech Stack:** Thymeleaf, vanilla JS, Spring Boot MVC (unchanged), JUnit 5, MockMvc, Mockito.

**Design spec:** [`2026-06-02-admin-survey-field-reorder-p4-design.md`](./2026-06-02-admin-survey-field-reorder-p4-design.md)

**Status (2026-06-02):** 구현·단위 테스트·문서(checklist/progress/worklog) 완료. 수동 브라우저 검증과 Optional Docker smoke는 미수행.

---

### Task 1: Add a failing service test for field order on save.

**Files:**
- Modify: `src/test/java/com/reven/project/service/sa/SASurveyServiceTest.java`

- [x] **Step 1: Write test that reversed field list order yields sort_ord 1, 2, 3 in insert order.**

```java
@Test
void saveSurveyPersistsFieldOrderFromRequestListOrder() {
    SASurveyMapper mapper = mock(SASurveyMapper.class);
    SASurveyDto.SurveyDetail persisted = survey(1L, "survey-uid");
    when(mapper.selectSurvey("survey-uid")).thenReturn(null, persisted);
    when(mapper.selectSurveyFields(1L)).thenReturn(List.of());
    when(mapper.selectSurveyOptions(1L)).thenReturn(List.of());

    SASurveyService service = new SASurveyService(mapper);
    SASurveyDto.SurveySaveRequest request = new SASurveyDto.SurveySaveRequest();
    request.surveyUid = "survey-uid";
    request.title = "순서 테스트";
    request.useYn = "Y";
    request.fields = List.of(
            fieldRequest("세 번째", "objective", "select", "A"),
            fieldRequest("첫 번째", "subjective", "text", null),
            fieldRequest("두 번째", "objective", "radio", "B\nC")
    );

    service.saveSurvey(null, request);

    ArgumentCaptor<SASurveyDto.SurveyField> captor =
            ArgumentCaptor.forClass(SASurveyDto.SurveyField.class);
    verify(mapper, times(3)).insertSurveyField(captor.capture());
    assertThat(captor.getAllValues())
            .extracting(f -> f.label, f -> f.sortOrd)
            .containsExactly(
                    tuple("세 번째", 1),
                    tuple("첫 번째", 2),
                    tuple("두 번째", 3)
            );
}
```

- [x] **Step 2: Run test and confirm it passes without code changes (existing behavior).**

Run: `./gradlew test --tests 'com.reven.project.service.sa.SASurveyServiceTest.saveSurveyPersistsFieldOrderFromRequestListOrder'`

Expected: PASS (documents current contract; no production change yet)

---

### Task 2: Add up/down buttons to admin survey detail template.

**Files:**
- Modify: `src/main/resources/templates/admin/survey/detail.html`

- [x] **Step 1: Update existing field rows** — wrap delete in `.field-row-actions` and add up/down buttons with `data-move-field-up` / `data-move-field-down`.

- [x] **Step 2: Update empty default row** (lines ~67–68) with the same head structure.

- [x] **Step 3: Update `<template data-field-template>`** with the same head structure.

- [ ] **Step 4: Manual check** — load admin survey detail in browser; buttons visible, form still submits. (수동 미수행)

---

### Task 3: Implement move up/down in survey-field-editor.js.

**Files:**
- Modify: `src/main/resources/static/admin/js/survey-field-editor.js`

- [x] **Step 1: Extend `refreshRows`** — disable up on first row, down on last row; leave single-row case with both disabled.

- [x] **Step 2: Add click delegation** for `data-move-field-up` and `data-move-field-down`:
  - Find current `[data-field-row]`
  - Swap with previous/next sibling via `insertAdjacentElement`
  - Call `refreshRows(editor)`

- [ ] **Step 3: Manual check** — reorder rows, confirm `fields[0]`, `fields[1]` names in devtools before submit. (수동 미수행)

---

### Task 4: Add minimal CSS for action button group.

**Files:**
- Modify: `src/main/resources/static/common/css/app.css`

- [x] **Step 1: Add styles near existing `.field-row-head` block:**

```css
.field-row-actions {
    display: flex;
    align-items: center;
    gap: 8px;
}

.btn-compact {
    min-width: 36px;
    padding: 6px 10px;
}
```

- [x] **Step 2: Verify layout on narrow admin viewport (existing responsive block ~2145).** `.field-row` responsive grid는 기존 블록 유지, `.field-row-actions`는 flex로 좁은 화면에서도 동작.

---

### Task 5: Integration verification and docs.

**Files:**
- Modify: `docs/checklist.md` — check P4 item when done
- Modify: `docs/progress.md` — move P4 to completed section
- Modify: `docs/worklog.md` — brief entry if behavior changed materially

- [x] **Step 1: Run full test suite.**

Run: `./gradlew test`

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Optional Docker smoke** — save reordered survey, open public form, confirm field order matches admin screen. (Optional, 미수행)

- [x] **Step 3: Update checklist and progress docs.**

---

## Verification checklist (from design spec)

- [ ] ▲/▼ reorder works in admin detail (수동 미수행)
- [ ] First/last row buttons disabled correctly (수동 미수행)
- [x] Save persists `sort_ord` 1..N matching screen order (`SASurveyServiceTest`, `SAAdminSurveyControllerTest`)
- [ ] Public `form.html` reflects saved order (수동/Docker 미수행)
- [ ] Add/remove field still works after reorder (수동 미수행)
- [x] P2 option validation regression passes (`./gradlew test`)

## Out of scope (do not implement in P4)

- Drag-and-drop
- Hidden `sortOrd` inputs
- Reorder-only API
- Option line reorder UI
- `field_seq` preservation on update
