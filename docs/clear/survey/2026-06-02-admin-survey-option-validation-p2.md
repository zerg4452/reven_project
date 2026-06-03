# 관리자 설문 옵션 검증 P2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent invalid objective options from being saved and block invalid required survey submissions before they are persisted.

**Architecture:** Keep the existing admin and public survey flows server-rendered. Add semantic save validation in the admin controller before `saveSurvey()`, and add server-side submit validation inside `SASurveySubmitService` so invalid public submissions never reach the insert path. Re-render the same Thymeleaf forms with field-level error messages instead of introducing a new client-side form layer.

**Tech Stack:** Spring Boot MVC, Thymeleaf, MyBatis, JUnit 5, Mockito, MockMvc.

---

### Task 1: Write the failing validation tests.

**Files:**
- Modify: `src/test/java/com/reven/project/admin/sa/SAAdminSurveyControllerTest.java`
- Modify: `src/test/java/com/reven/project/service/sa/SASurveySubmitServiceTest.java`
- Modify: `src/test/java/com/reven/project/client/sa/SAPublicSurveyControllerTest.java`

- [ ] **Step 1: Add a controller test for objective option validation on save.**

```java
@Test
void saveSurveyRejectsObjectiveFieldWithoutOptions() throws Exception {
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAAdminSurveyController(surveyService)).build();

    mvc.perform(post("/admin/surveys/insert.do")
                    .param("title", "설문")
                    .param("useYn", "Y")
                    .param("fields[0].label", "질문 1")
                    .param("fields[0].surveyType", "objective")
                    .param("fields[0].fieldType", "select")
                    .param("fields[0].optionsText", "   "))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/survey/detail"))
            .andExpect(model().attributeExists("errors"));

    verify(surveyService, never()).saveSurvey(any(), any());
}
```

- [ ] **Step 2: Add a service test for blank required objective submissions.**

```java
@Test
void submitRejectsRequiredObjectiveWhenNothingIsSelected() {
    SASurveyDto.SurveyDetail survey = survey(1L, "survey-uid");
    SASurveyDto.SurveyField field = field(10L, 1L, "objective", "select");
    field.requiredYn = "Y";
    field.options = List.of(option(100L, 10L, "Red", "red"));
    survey.fields = List.of(field);
    when(surveyService.findSurvey("survey-uid")).thenReturn(survey);

    SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();
    SASurveyDto.AnswerRequest answer = new SASurveyDto.AnswerRequest();
    answer.fieldKey = "field-10";
    answer.values = List.of("   ");
    request.answers = List.of(answer);

    assertThatThrownBy(() -> service.submit("survey-uid", request, "127.0.0.1"))
            .isInstanceOf(SASurveySubmitService.SubmissionValidationException.class);
}
```

- [ ] **Step 3: Add a controller test for submit validation failure redisplay.**

```java
@Test
void submitRedisplaysFormWithFieldErrorsWhenValidationFails() throws Exception {
    SASurveyDto.SurveyDetail survey = survey();
    when(surveyService.findSurvey("survey-uid")).thenReturn(survey);
    when(submitService.submit(anyString(), any(), anyString()))
            .thenThrow(new SASurveySubmitService.SubmissionValidationException(Map.of("field-10", "필수 문항에 응답해 주세요.")));

    MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAPublicSurveyController(surveyService, submitService)).build();

    mvc.perform(post("/surveys/submit.do")
                    .param("surveyUid", "survey-uid")
                    .param("submitterName", "홍길동")
                    .param("phone", "010-0000-0000"))
            .andExpect(status().isOk())
            .andExpect(view().name("client/survey/form"))
            .andExpect(model().attributeExists("errors"));
}
```

- [ ] **Step 4: Run the red tests and confirm they fail for the missing P2 behavior.**

Run:

```bash
./gradlew test --tests 'com.reven.project.admin.sa.SAAdminSurveyControllerTest' --tests 'com.reven.project.service.sa.SASurveySubmitServiceTest' --tests 'com.reven.project.client.sa.SAPublicSurveyControllerTest'
```

Expected: compilation or assertion failures because semantic save validation, submit validation, and validation redisplay are not implemented yet.

### Task 2: Implement admin save validation and error rendering.

**Files:**
- Modify: `src/main/java/com/reven/project/admin/sa/SAAdminSurveyController.java`
- Modify: `src/main/resources/templates/admin/survey/detail.html`
- Modify: `src/test/java/com/reven/project/admin/sa/SAAdminSurveyControllerTest.java`

- [ ] **Step 1: Add objective option validation before `saveSurvey()`.**

```java
private void validateSurveyOptions(SASurveyDto.SurveySaveRequest request, BindingResult bindingResult) {
    for (int index = 0; index < request.fields.size(); index++) {
        SASurveyDto.SurveyFieldSaveRequest field = request.fields.get(index);
        if ("subjective".equalsIgnoreCase(field.surveyType)) {
            continue;
        }

        List<String> labels = new ArrayList<>();
        for (SASurveyDto.SurveyOptionSaveRequest option : field.normalizedOptions()) {
            String label = option.optionLabel == null ? "" : option.optionLabel.trim();
            if (label.isBlank()) {
                continue;
            }
            if (labels.contains(label)) {
                bindingResult.rejectValue("fields[" + index + "].optionsText", "survey.option.duplicate", "보기 라벨이 중복되었습니다.");
                break;
            }
            labels.add(label);
        }
        if (labels.isEmpty()) {
            bindingResult.rejectValue("fields[" + index + "].optionsText", "survey.option.required", "객관식 문항에는 보기를 1개 이상 입력해야 합니다.");
        }
    }
}
```

- [ ] **Step 2: Rebuild the form model from the request when validation fails.**

```java
if (bindingResult.hasErrors()) {
    model.addAttribute("survey", toSurveyDetail(surveyUid, request));
    model.addAttribute("errors", collectFieldErrors(bindingResult));
    return "admin/survey/detail";
}
```

- [ ] **Step 3: Show admin form errors next to the options field and in a top alert.**

```html
<div class="alert" th:if="${errors != null and !errors.isEmpty()}">
    <ul>
        <li th:each="entry : ${errors.entrySet()}" th:text="${entry.value}"></li>
    </ul>
</div>
```

- [ ] **Step 4: Run the admin controller test and confirm it passes.**

Run:

```bash
./gradlew test --tests 'com.reven.project.admin.sa.SAAdminSurveyControllerTest'
```

Expected: the objective options validation test passes and the save path still works for valid surveys.

### Task 3: Implement submit validation and public error redisplay.

**Files:**
- Modify: `src/main/java/com/reven/project/service/sa/SASurveySubmitService.java`
- Modify: `src/main/java/com/reven/project/client/sa/SAPublicSurveyController.java`
- Modify: `src/main/resources/templates/client/survey/form.html`
- Modify: `src/main/resources/templates/client/survey/field.html`
- Modify: `src/test/java/com/reven/project/service/sa/SASurveySubmitServiceTest.java`
- Modify: `src/test/java/com/reven/project/client/sa/SAPublicSurveyControllerTest.java`

- [ ] **Step 1: Add a submission validation exception that carries field errors.**

```java
public static class SubmissionValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public SubmissionValidationException(Map<String, String> errors) {
        super("Survey submission validation failed");
        this.errors = Map.copyOf(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
```

- [ ] **Step 2: Normalize raw submission values before validation and insert.**

```java
private List<String> normalizeValues(List<String> values) {
    if (values == null) {
        return List.of();
    }
    return values.stream()
            .map(value -> value == null ? "" : value.trim())
            .filter(value -> !value.isBlank())
            .toList();
}
```

- [ ] **Step 3: Validate required fields in `submit()` before inserting any rows.**

```java
Map<String, String> errors = validateSubmission(survey, request);
if (!errors.isEmpty()) {
    throw new SubmissionValidationException(errors);
}
```

- [ ] **Step 4: Catch validation failures in the controller and re-render the form.**

```java
try {
    submitService.submit(surveyUid, request, servletRequest.getRemoteAddr());
    return "redirect:/surveys/thanks.do";
} catch (SASurveySubmitService.SubmissionValidationException ex) {
    model.addAttribute("survey", surveyService.findSurvey(surveyUid));
    model.addAttribute("errors", ex.getErrors());
    return "client/survey/form";
}
```

- [ ] **Step 5: Surface the field-level error next to each public survey question.**

```html
<div class="survey-question-error" th:if="${errors != null and errors[field.key] != null}" th:text="${errors[field.key]}"></div>
```

- [ ] **Step 6: Run the submit and controller tests and confirm the validation flow passes.**

Run:

```bash
./gradlew test --tests 'com.reven.project.service.sa.SASurveySubmitServiceTest' --tests 'com.reven.project.client.sa.SAPublicSurveyControllerTest'
```

Expected: required objective, required subjective, and blank checkbox submissions fail; valid submissions still persist.

### Task 4: Run the full test suite and review the checklist.

**Files:**
- Modify: `docs/checklist.md`
- Modify: `docs/context-notes.md`

- [ ] **Step 1: Run the full Gradle test suite.**

Run:

```bash
./gradlew test
```

Expected: all tests pass.

- [ ] **Step 2: Mark P2 complete in the checklist and add implementation notes.**

```markdown
- [x] P2 선택형 문항의 옵션 검증과 제출 실패 표시를 추가한다.
```

- [ ] **Step 3: Append the final implementation notes for future P3 work.**

```markdown
- 관리자 저장 검증은 `optionsText` 기반으로 객관식 옵션 최소 개수와 중복을 막는다.
- 공개 제출 검증은 서비스에서 먼저 막고, 컨트롤러가 같은 설문 화면을 다시 렌더링한다.
- 입력값 보존은 P2 범위 밖으로 둔다.
```

## Status

- Implemented and verified with `./gradlew test`.
- Checklist and context notes were updated to reflect completion.
