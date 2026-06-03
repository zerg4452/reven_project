# 관리자 설문 검색 조건 강화 P6 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 설문 관리에 사용여부 필터를 추가하고, 두 검색 화면의 잘못된 검색 파라미터(날짜 바인딩 실패, 허용값 외 keywordType/useYn)를 기본값으로 보정해 목록이 항상 정상 렌더링되게 한다.

**Architecture:** 잘못된 `LocalDate` 문자열을 null로 흡수하는 공용 `PropertyEditor`를 두 컨트롤러 `@InitBinder`에 등록하고, 각 컨트롤러의 정규화 로직에서 null 날짜를 기본값으로 채우며 `keywordType`/`useYn`을 허용값 기준으로 보정한다. SQL/DB 스키마는 그대로 두고 화면·컨트롤러·mapper 의미만 맞춘다.

**Tech Stack:** Spring MVC (`@ModelAttribute` + direct field access DTO), MyBatis, Thymeleaf + Layout Dialect, JUnit5 + MockMvc standaloneSetup + Mockito.

---

## File Structure

- Create: `src/main/java/com/reven/project/common/web/LenientLocalDateEditor.java` — 잘못된 날짜 문자열을 null로 흡수하는 PropertyEditor.
- Create: `src/test/java/com/reven/project/common/web/LenientLocalDateEditorTest.java`
- Modify: `src/main/java/com/reven/project/admin/sa/SAAdminSurveyController.java` — InitBinder 날짜 editor 등록, 검색 정규화, `useYn` model 추가.
- Modify: `src/main/java/com/reven/project/admin/sa/SAAdminSurveySubmissionController.java` — InitBinder 날짜 editor 등록, `normalizeSearch`에 keywordType 보정 및 날짜 null 보정 추가.
- Modify: `src/main/resources/templates/admin/survey/list.html` — 사용여부 select 추가.
- Modify (test): `src/test/java/com/reven/project/admin/sa/SAAdminSurveyControllerTest.java`
- Modify (test): `src/test/java/com/reven/project/admin/sa/SAAdminSurveySubmissionControllerTest.java`
- Modify (docs): `docs/checklist.md`, `docs/context-notes.md`, `docs/worklog.md`

`SASurveyMapper.xml`의 `SurveySearchWhere`는 `useYn != null and useYn != ''`일 때만 `use_yn` 조건을 거는 기존 로직(line 54-56)을 그대로 쓴다. 컨트롤러가 잘못된 `useYn`을 null로 비워 보내므로 mapper 변경 불필요.

---

## Background (구현 전 반드시 읽을 것)

현재 동작 — 코드 확인 완료.

- `SASurveyDto.SurveySearchRequest` (`src/main/java/com/reven/project/service/sa/dto/SASurveyDto.java:29-35`): `startDate=now-60`, `endDate=now+1`, `keywordType="전체"`, `keyword`, `useYn` 필드. public field, lombok @Getter/@Setter.
- `SASurveyDto.SubmissionSearchRequest` (같은 파일 40-46): `startDate`, `endDate`, `keywordType="전체"`, `keyword`, `statuses=[]`.
- `SAAdminSurveyController.list()` (`SAAdminSurveyController.java:41-51`): `useYn`을 model에 안 넣음. `startDate`/`endDate`/`keywordType`/`keyword`만 넣음.
- `SAAdminSurveySubmissionController.normalizeSearch()` (`SAAdminSurveySubmissionController.java:107-118`): 빈 statuses → 전체 상태 보정. keywordType/날짜는 보정 안 함.
- `SASurveySubmitMapper.xml`의 `SubmissionSearchWhere` (line 69-84): keywordType `설문명`/`작성자명`/그 외(전체) `<choose>` 이미 구현됨. 변경 불필요.
- 설문 관리 키워드는 `SASurveyMapper.xml` `SurveySearchWhere` (line 57-59)에서 keywordType 무관하게 `title` like만 검색. `전체`/`설문명` 동일 SQL. 변경 불필요.

허용값.

- 설문 관리 `keywordType`: `전체`, `설문명`. (현재 `list.html:47-48` 옵션)
- 설문 이력 `keywordType`: `전체`, `설문명`, `작성자명`. (현재 `history-list.html:46-48` 옵션)
- `useYn`: `Y`, `N`. 그 외 → null(전체).
- 상태 코드: `new`, `reviewing`, `contacted`, `done`, `hold`. (`SAAdminSurveySubmissionController.java:124-130`)

테스트 스타일 — 기존 컨트롤러 테스트는 `MockMvcBuilders.standaloneSetup(new Controller(mockService)).build()`. 뷰 렌더링 없이 model attribute와 service 전달 인자(ArgumentCaptor)로 검증. 동일 패턴 유지.

---

### Task 1: LenientLocalDateEditor 생성

**Files:**
- Create: `src/main/java/com/reven/project/common/web/LenientLocalDateEditor.java`
- Test: `src/test/java/com/reven/project/common/web/LenientLocalDateEditorTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.reven.project.common.web;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LenientLocalDateEditorTest {

    @Test
    void parsesIsoDate() {
        LenientLocalDateEditor editor = new LenientLocalDateEditor();
        editor.setAsText("2026-06-03");
        assertThat(editor.getValue()).isEqualTo(LocalDate.of(2026, 6, 3));
    }

    @Test
    void returnsNullForBlank() {
        LenientLocalDateEditor editor = new LenientLocalDateEditor();
        editor.setAsText("   ");
        assertThat(editor.getValue()).isNull();
    }

    @Test
    void returnsNullForInvalidText() {
        LenientLocalDateEditor editor = new LenientLocalDateEditor();
        editor.setAsText("abc");
        assertThat(editor.getValue()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.reven.project.common.web.LenientLocalDateEditorTest"`
Expected: 컴파일 실패 — `LenientLocalDateEditor` 클래스 없음.

- [ ] **Step 3: Write minimal implementation**

```java
// 잘못되거나 빈 날짜 문자열을 예외 대신 null로 흡수하는 LocalDate 바인딩 editor
package com.reven.project.common.web;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class LenientLocalDateEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) {
        if (text == null || text.isBlank()) {
            setValue(null);
            return;
        }
        try {
            setValue(LocalDate.parse(text.trim()));
        } catch (DateTimeParseException ex) {
            setValue(null);
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "com.reven.project.common.web.LenientLocalDateEditorTest"`
Expected: PASS (3 tests)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/reven/project/common/web/LenientLocalDateEditor.java src/test/java/com/reven/project/common/web/LenientLocalDateEditorTest.java
git commit -m "feat: 잘못된 날짜 문자열을 null로 흡수하는 LenientLocalDateEditor 추가"
```

---

### Task 2: 설문 관리 컨트롤러 검색 정규화 + useYn model 추가

**Files:**
- Modify: `src/main/java/com/reven/project/admin/sa/SAAdminSurveyController.java`
- Test: `src/test/java/com/reven/project/admin/sa/SAAdminSurveyControllerTest.java`

- [ ] **Step 1: Write the failing tests**

`SAAdminSurveyControllerTest.java` 상단 import에 다음을 추가한다.

```java
import com.reven.project.service.sa.dto.SASurveyDto;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
```

(이미 존재하는 import는 중복 추가하지 않는다.)

클래스 안에 테스트 4개를 추가한다.

```java
    @Test
    void listKeepsDefaultsOnFirstEntry() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findAdminSurveys(any())).thenReturn(java.util.List.of());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/list.do"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/list"))
                .andExpect(model().attribute("keywordType", "전체"))
                .andExpect(model().attribute("useYn", ""));
    }

    @Test
    void listPassesUseYnYToService() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findAdminSurveys(any())).thenReturn(java.util.List.of());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/list.do").param("useYn", "Y"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("useYn", "Y"));

        ArgumentCaptor<SASurveyDto.SurveySearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SurveySearchRequest.class);
        verify(surveyService).findAdminSurveys(captor.capture());
        assertThat(captor.getValue().useYn).isEqualTo("Y");
    }

    @Test
    void listClearsInvalidUseYnAndKeywordType() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findAdminSurveys(any())).thenReturn(java.util.List.of());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/list.do")
                        .param("useYn", "X")
                        .param("keywordType", "이상한값"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("useYn", ""))
                .andExpect(model().attribute("keywordType", "전체"));

        ArgumentCaptor<SASurveyDto.SurveySearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SurveySearchRequest.class);
        verify(surveyService).findAdminSurveys(captor.capture());
        assertThat(captor.getValue().useYn).isNull();
        assertThat(captor.getValue().keywordType).isEqualTo("전체");
    }

    @Test
    void listFallsBackToDefaultDatesOnInvalidDate() throws Exception {
        SASurveyService surveyService = mock(SASurveyService.class);
        when(surveyService.findAdminSurveys(any())).thenReturn(java.util.List.of());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

        mvc.perform(get("/admin/surveys/list.do").param("startDate", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/list"));

        ArgumentCaptor<SASurveyDto.SurveySearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SurveySearchRequest.class);
        verify(surveyService).findAdminSurveys(captor.capture());
        assertThat(captor.getValue().startDate).isNotNull();
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "com.reven.project.admin.sa.SAAdminSurveyControllerTest"`
Expected: FAIL — `useYn` model attribute 없음 / 잘못된 날짜에서 `startDate` null (현재 editor 없어 BindException 또는 null).

- [ ] **Step 3: Implement controller changes**

`SAAdminSurveyController.java`에 import 추가.

```java
import com.reven.project.common.web.LenientLocalDateEditor;
import java.time.LocalDate;
```

`initBinder`를 다음으로 교체한다(기존 주석 유지하고 editor 등록만 추가).

```java
    @InitBinder
    void initBinder(WebDataBinder binder) {
        // SASurveyDto는 화면 form binding을 위해 public field 기반 DTO를 사용하므로 직접 필드 접근을 활성화한다.
        binder.initDirectFieldAccess();
        // 잘못된 날짜 문자열은 BindException 대신 null로 흡수해 목록 진입 실패를 막는다.
        binder.registerCustomEditor(LocalDate.class, new LenientLocalDateEditor());
    }
```

`list()` 메서드를 다음으로 교체한다.

```java
    /**
     * 설문 관리 목록 화면을 조회한다.
     */
    @GetMapping("/list.do")
    public String list(@ModelAttribute SASurveyDto.SurveySearchRequest request, Model model) {
        normalizeSearch(request);
        List<SASurveyDto.SurveyListItem> surveys = surveyService.findAdminSurveys(request);
        model.addAttribute("surveys", surveys);
        model.addAttribute("totalCount", surveys.size());
        model.addAttribute("dateFrom", request.startDate);
        model.addAttribute("dateTo", request.endDate);
        model.addAttribute("keywordType", request.keywordType);
        model.addAttribute("keyword", request.keyword);
        model.addAttribute("useYn", request.useYn == null ? "" : request.useYn);
        return "admin/survey/list";
    }

    /**
     * 설문 관리 검색 조건을 허용값 기준으로 보정한다.
     */
    private void normalizeSearch(SASurveyDto.SurveySearchRequest request) {
        if (request.startDate == null) {
            request.startDate = LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).minusDays(60);
        }
        if (request.endDate == null) {
            request.endDate = LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).plusDays(1);
        }
        if (!"설문명".equals(request.keywordType)) {
            request.keywordType = "전체";
        }
        if (!"Y".equals(request.useYn) && !"N".equals(request.useYn)) {
            request.useYn = null;
        }
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "com.reven.project.admin.sa.SAAdminSurveyControllerTest"`
Expected: PASS (기존 + 신규 4개)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/reven/project/admin/sa/SAAdminSurveyController.java src/test/java/com/reven/project/admin/sa/SAAdminSurveyControllerTest.java
git commit -m "feat: 설문 관리 검색 조건 정규화 및 useYn 화면 표시 추가"
```

---

### Task 3: 설문 관리 list.html 사용여부 select 추가

**Files:**
- Modify: `src/main/resources/templates/admin/survey/list.html`

`.cursor/rules/html-markup-layout.mdc`를 먼저 읽고 요소 단위 줄바꿈/구역 주석 규칙을 지킨다.

- [ ] **Step 1: 사용여부 select 추가**

`list.html`의 검색 키워드 행(현재 line 44-56) 시작 부분, `검색조건` label 앞에 사용여부 블록을 추가한다. 교체 대상은 `<div class="news-search-row news-search-keyword-row">` 여는 태그 바로 다음 줄이다. 다음 마크업을 `<label class="search-label" for="keywordType">검색조건</label>` 앞에 삽입한다.

```html
                <label class="search-label" for="useYn">사용여부</label>
                <select class="form-control news-search-select" id="useYn" name="useYn">
                    <option value="" th:selected="${useYn == null or useYn == ''}">전체</option>
                    <option value="Y" th:selected="${useYn == 'Y'}">사용</option>
                    <option value="N" th:selected="${useYn == 'N'}">미사용</option>
                </select>
```

- [ ] **Step 2: 컴파일/렌더 확인**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL (템플릿은 런타임 검증이므로 컴파일만 확인). Task 5 전체 테스트에서 기존 뷰 테스트가 깨지지 않는지 함께 확인한다.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/templates/admin/survey/list.html
git commit -m "feat: 설문 관리 목록에 사용여부 검색 select 추가"
```

---

### Task 4: 설문 이력 컨트롤러 normalizeSearch 보강

**Files:**
- Modify: `src/main/java/com/reven/project/admin/sa/SAAdminSurveySubmissionController.java`
- Test: `src/test/java/com/reven/project/admin/sa/SAAdminSurveySubmissionControllerTest.java`

- [ ] **Step 1: Write the failing tests**

`SAAdminSurveySubmissionControllerTest.java` import에 다음을 추가한다(중복 제외).

```java
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
```

클래스 안에 테스트 3개를 추가한다.

```java
    @Test
    void listAppliesAllStatusesByDefault() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(submitService.findSubmissions(any())).thenReturn(java.util.List.of());

        mvc(submitService).perform(get("/admin/survey-submissions/list.do"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/survey/history-list"));

        ArgumentCaptor<SASurveyDto.SubmissionSearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SubmissionSearchRequest.class);
        verify(submitService).findSubmissions(captor.capture());
        assertThat(captor.getValue().statuses)
                .containsExactly("new", "reviewing", "contacted", "done", "hold");
    }

    @Test
    void listKeepsSelectedStatusesOnly() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(submitService.findSubmissions(any())).thenReturn(java.util.List.of());

        mvc(submitService).perform(get("/admin/survey-submissions/list.do")
                        .param("statuses", "done")
                        .param("statuses", "hold"))
                .andExpect(status().isOk());

        ArgumentCaptor<SASurveyDto.SubmissionSearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SubmissionSearchRequest.class);
        verify(submitService).findSubmissions(captor.capture());
        assertThat(captor.getValue().statuses).containsExactly("done", "hold");
    }

    @Test
    void listClearsInvalidKeywordTypeAndInvalidDate() throws Exception {
        SASurveySubmitService submitService = mock(SASurveySubmitService.class);
        when(submitService.findSubmissions(any())).thenReturn(java.util.List.of());

        mvc(submitService).perform(get("/admin/survey-submissions/list.do")
                        .param("keywordType", "이상한값")
                        .param("startDate", "abc"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("keywordType", "전체"));

        ArgumentCaptor<SASurveyDto.SubmissionSearchRequest> captor =
                ArgumentCaptor.forClass(SASurveyDto.SubmissionSearchRequest.class);
        verify(submitService).findSubmissions(captor.capture());
        assertThat(captor.getValue().keywordType).isEqualTo("전체");
        assertThat(captor.getValue().startDate).isNotNull();
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "com.reven.project.admin.sa.SAAdminSurveySubmissionControllerTest"`
Expected: FAIL — keywordType 보정 없음 / 잘못된 날짜에서 startDate null.

- [ ] **Step 3: Implement controller changes**

import 추가.

```java
import com.reven.project.common.web.LenientLocalDateEditor;
import java.time.LocalDate;
import java.time.ZoneId;
```

`initBinder`에 editor 등록을 추가한다.

```java
    @InitBinder
    void initBinder(WebDataBinder binder) {
        // 검색 DTO가 public field 기반이라 Spring MVC가 setter 없이 값을 주입하도록 설정한다.
        binder.initDirectFieldAccess();
        // 잘못된 날짜 문자열은 BindException 대신 null로 흡수해 목록 진입 실패를 막는다.
        binder.registerCustomEditor(LocalDate.class, new LenientLocalDateEditor());
    }
```

`normalizeSearch`를 다음으로 교체한다(날짜 null 보정 + keywordType 보정 추가).

```java
    /**
     * 설문 이력 검색 조건의 날짜/상태/검색조건 기본값을 보정한다.
     */
    private SASurveyDto.SubmissionSearchRequest normalizeSearch(SASurveyDto.SubmissionSearchRequest request) {
        List<String> allowedStatuses = statusOptions().stream()
                .map(SASurveyDto.SubmissionStatusOption::getCode)
                .toList();
        List<String> statuses = request.statuses == null || request.statuses.isEmpty()
                ? allowedStatuses
                : request.statuses.stream().filter(allowedStatuses::contains).toList();
        if (statuses.isEmpty()) {
            statuses = allowedStatuses;
        }
        SASurveyDto.SubmissionSearchRequest normalized = new SASurveyDto.SubmissionSearchRequest();
        normalized.startDate = request.startDate != null
                ? request.startDate
                : LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(60);
        normalized.endDate = request.endDate != null
                ? request.endDate
                : LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1);
        normalized.keywordType = isAllowedKeywordType(request.keywordType) ? request.keywordType : "전체";
        normalized.keyword = request.keyword;
        normalized.statuses = statuses;
        return normalized;
    }

    /**
     * 설문 이력 검색조건 허용값(전체/설문명/작성자명) 여부를 확인한다.
     */
    private boolean isAllowedKeywordType(String keywordType) {
        return "설문명".equals(keywordType) || "작성자명".equals(keywordType);
    }
```

참고 — 기존 `normalizeSearch`(line 107-118)에는 잘못된 status를 제거하는 로직이 없었다. 위 교체로 허용값 외 status도 함께 걸러진다(설계 보정 규칙 반영).

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "com.reven.project.admin.sa.SAAdminSurveySubmissionControllerTest"`
Expected: PASS (기존 + 신규 3개)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/reven/project/admin/sa/SAAdminSurveySubmissionController.java src/test/java/com/reven/project/admin/sa/SAAdminSurveySubmissionControllerTest.java
git commit -m "feat: 설문 이력 검색 날짜/검색조건/상태 정규화 보강"
```

---

### Task 5: 전체 테스트 + 문서 기록

**Files:**
- Modify: `docs/checklist.md`, `docs/context-notes.md`, `docs/worklog.md`

- [ ] **Step 1: 전체 테스트 실행**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL. 기존 공개 설문/작성/제출/미리보기 뷰 테스트 포함 전부 통과. 실패 시 `superpowers:systematic-debugging`로 원인 확인 후 수정.

- [ ] **Step 2: 문서 기록**

`docs/checklist.md`에 P6 항목을 체크 완료로 추가하고, `docs/context-notes.md`에 다음 결정을 append한다.

- 잘못된 날짜 문자열은 `LenientLocalDateEditor`로 null 흡수 후 컨트롤러에서 기본 날짜로 fallback (BindException 회피).
- `keywordType`/`useYn`/`statuses` 허용값 외 입력은 보정·무시 (500이 아니라 표시·조회 정합성 목적).
- 설문 관리 `keywordType` `전체`/`설문명`은 동일 SQL(title like), 설문 이력 `전체`는 설문명 OR 작성자명(mapper 기존 구현).

`docs/worklog.md`에 P6 구현 결과 1줄 기록.

- [ ] **Step 3: Commit**

```bash
git add docs/checklist.md docs/context-notes.md docs/worklog.md
git commit -m "docs: 설문 검색 강화 P6 진행 기록"
```

---

## Self-Review 결과

- **Spec coverage**: 설계서 각 항목 대응 확인.
  - 사용여부 필터(전체/사용/미사용) → Task 2(컨트롤러), Task 3(html).
  - keywordType 전체/설문명 동일 SQL → mapper 무변경, Task 2 보정.
  - 설문 이력 상태 기본값/일부선택/전체 키워드 → Task 4 + mapper 기존 구현.
  - 날짜 바인딩 fallback → Task 1 + Task 2/4 InitBinder.
  - CSV 동일 검색 조건 공유 → `csv()`가 `normalizeSearch` 호출(기존 line 96) 유지, Task 4가 같은 메서드를 강화하므로 자동 반영. 별도 작업 불필요.
  - 기존 흐름 무영향 → Task 5 전체 테스트.
- **Placeholder scan**: 모든 코드 스텝에 실제 코드 포함. TBD/TODO 없음.
- **Type consistency**: `SurveySearchRequest.useYn`(String), `SubmissionSearchRequest.statuses`(List<String>), `normalizeSearch` 시그니처(이력 반환형 유지, 관리 void) 일관. 허용 status 코드 5종 일치.
