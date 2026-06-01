# 관리자 설문 사용자 렌더링 P1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Render objective and subjective survey questions correctly on the public survey form and persist checkbox answers without losing multiple selected values.

**Architecture:** Keep the public survey page server-rendered. Add a normalized render type helper to `SASurveyDto.SurveyField`, render each question through a dedicated Thymeleaf fragment, and change public submission binding to preserve repeated checkbox values. The submit service should translate raw option values into stored display values and JSON snapshots using the existing survey metadata.

**Tech Stack:** Spring Boot MVC, Thymeleaf, MyBatis, JUnit 5, Mockito, MockMvc, Jackson.

---

### Task 1: Write the failing tests first.

**Files:**
- Modify: `src/test/java/com/reven/project/service/sa/SASurveyServiceTest.java`
- Create: `src/test/java/com/reven/project/service/sa/SASurveySubmitServiceTest.java`
- Create: `src/test/java/com/reven/project/client/sa/SAPublicSurveyControllerTest.java`

- [ ] **Step 1: Add a render-type fallback test**

```java
@Test
void surveyFieldRenderTypeFallsBackBySurveyType() {
    SASurveyDto.SurveyField objectiveField = field(1L, 1L, "objective", "textarea");
    SASurveyDto.SurveyField subjectiveField = field(2L, 1L, "subjective", "radio");

    assertThat(objectiveField.getRenderType()).isEqualTo("select");
    assertThat(subjectiveField.getRenderType()).isEqualTo("text");
}
```

- [ ] **Step 2: Add a submit normalization test for single choice answers**

```java
@Test
void submitStoresSingleChoiceAnswerAsLabel() {
    SASurveyDto.SurveyDetail survey = survey(1L, "survey-uid");
    SASurveyDto.SurveyField field = field(10L, 1L, "objective", "select");
    field.options = List.of(option(100L, 10L, "Red", "red"), option(101L, 10L, "Blue", "blue"));
    survey.fields = List.of(field);
    when(surveyService.findSurvey("survey-uid")).thenReturn(survey);

    SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();
    SASurveyDto.AnswerRequest answer = new SASurveyDto.AnswerRequest();
    answer.fieldKey = "field-10";
    answer.values = List.of("blue");
    request.answers = List.of(answer);

    service.submit("survey-uid", request, "127.0.0.1");

    ArgumentCaptor<SASurveyDto.AnswerInsert> captor = ArgumentCaptor.forClass(SASurveyDto.AnswerInsert.class);
    verify(submitMapper).insertAnswer(captor.capture());
    assertThat(captor.getValue().answerValue).isEqualTo("Blue");
    assertThat(captor.getValue().answerJson).isNull();
}
```

- [ ] **Step 3: Add a submit normalization test for checkbox answers**

```java
@Test
void submitStoresCheckboxAnswerAsJoinedLabelAndJsonArray() {
    SASurveyDto.SurveyDetail survey = survey(1L, "survey-uid");
    SASurveyDto.SurveyField field = field(10L, 1L, "objective", "checkbox");
    field.options = List.of(option(100L, 10L, "Red", "red"), option(101L, 10L, "Blue", "blue"));
    survey.fields = List.of(field);
    when(surveyService.findSurvey("survey-uid")).thenReturn(survey);

    SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();
    SASurveyDto.AnswerRequest answer = new SASurveyDto.AnswerRequest();
    answer.fieldKey = "field-10";
    answer.values = List.of("red", "blue");
    request.answers = List.of(answer);

    service.submit("survey-uid", request, "127.0.0.1");

    ArgumentCaptor<SASurveyDto.AnswerInsert> captor = ArgumentCaptor.forClass(SASurveyDto.AnswerInsert.class);
    verify(submitMapper).insertAnswer(captor.capture());
    assertThat(captor.getValue().answerValue).isEqualTo("Red, Blue");
    assertThat(captor.getValue().answerJson).isEqualTo("[\"red\",\"blue\"]");
}
```

- [ ] **Step 4: Add a controller test for repeated checkbox params**

```java
@Test
void submitGroupsRepeatedCheckboxValuesIntoOneAnswerRequest() throws Exception {
    when(surveyService.findSurvey("survey-uid")).thenReturn(survey());
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new SAPublicSurveyController(surveyService, submitService)).build();

    mvc.perform(post("/surveys/submit.do")
                    .param("surveyUid", "survey-uid")
                    .param("submitterName", "홍길동")
                    .param("phone", "010-0000-0000")
                    .param("answers[field-10]", "red", "blue")
                    .param("answers[field-11]", "hello"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/surveys/thanks.do"));

    ArgumentCaptor<SASurveyDto.SurveySubmitRequest> captor = ArgumentCaptor.forClass(SASurveyDto.SurveySubmitRequest.class);
    verify(submitService).submit(eq("survey-uid"), captor.capture(), anyString());
    assertThat(captor.getValue().answers).extracting(answer -> answer.fieldKey).containsExactly("field-10", "field-11");
    assertThat(captor.getValue().answers.get(0).values).containsExactly("red", "blue");
}
```

- [ ] **Step 5: Run the red tests and confirm they fail for the missing P1 behavior**

Run:

```bash
./gradlew test --tests 'com.reven.project.service.sa.SASurveyServiceTest' --tests 'com.reven.project.service.sa.SASurveySubmitServiceTest' --tests 'com.reven.project.client.sa.SAPublicSurveyControllerTest'
```

Expected: compilation or assertion failures because `SurveyField.getRenderType()`, `AnswerRequest.values`, repeated checkbox grouping, and answer normalization are not implemented yet.

---

### Task 2: Implement request grouping and answer normalization.

**Files:**
- Modify: `src/main/java/com/reven/project/service/sa/dto/SASurveyDto.java`
- Modify: `src/main/java/com/reven/project/client/sa/SAPublicSurveyController.java`
- Modify: `src/main/java/com/reven/project/service/sa/SASurveySubmitService.java`

- [ ] **Step 1: Add `AnswerRequest.values` and `SurveyField.getRenderType()`**

```java
public static class SurveyField {
    public String getRenderType() {
        String surveyTypeValue = surveyType == null ? "" : surveyType.trim().toLowerCase();
        String fieldTypeValue = fieldType == null ? "" : fieldType.trim().toLowerCase();
        return switch (surveyTypeValue) {
            case "subjective" -> switch (fieldTypeValue) {
                case "textarea", "date", "number", "email" -> fieldTypeValue;
                default -> "text";
            };
            default -> switch (fieldTypeValue) {
                case "radio", "checkbox" -> fieldTypeValue;
                default -> "select";
            };
        };
    }
}

public static class AnswerRequest {
    public String fieldKey;
    public List<String> values = new ArrayList<>();
    public String answerValue;
    public String answerJson;
}
```

- [ ] **Step 2: Change the controller binding to preserve repeated parameter values**

```java
@PostMapping("/surveys/submit.do")
public String submit(@RequestParam String surveyUid,
                     @RequestParam MultiValueMap<String, String> params,
                     HttpServletRequest servletRequest) {
    SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();
    request.submitterName = params.getFirst("submitterName");
    request.phone = params.getFirst("phone");
    request.email = params.getFirst("email");
    request.answers = new ArrayList<>();

    Map<String, List<String>> groupedAnswers = new LinkedHashMap<>();
    params.forEach((key, values) -> {
        if (!key.startsWith("answers[")) {
            return;
        }
        String fieldKey = key.substring("answers[".length(), key.length() - 1);
        groupedAnswers.put(fieldKey, new ArrayList<>(values));
    });

    groupedAnswers.forEach((fieldKey, values) -> {
        SASurveyDto.AnswerRequest answer = new SASurveyDto.AnswerRequest();
        answer.fieldKey = fieldKey;
        answer.values = values;
        request.answers.add(answer);
    });

    submitService.submit(surveyUid, request, servletRequest.getRemoteAddr());
    return "redirect:/surveys/thanks.do";
}
```

- [ ] **Step 3: Normalize answers in the submit service using the survey field order**

```java
public SASurveyDto.SurveySubmitResponse submit(String surveyUid, SASurveyDto.SurveySubmitRequest request, String ip) {
    SASurveyDto.SurveyDetail survey = surveyService.findSurvey(surveyUid);
    Map<String, SASurveyDto.AnswerRequest> answersByFieldKey = new LinkedHashMap<>();
    for (SASurveyDto.AnswerRequest answer : request.answers) {
        answersByFieldKey.put(answer.fieldKey, answer);
    }

    for (SASurveyDto.SurveyField field : survey.fields) {
        SASurveyDto.AnswerRequest source = answersByFieldKey.get(field.fieldKey);
        if (source == null) {
            continue;
        }

        List<String> values = source.values == null || source.values.isEmpty()
                ? List.of(source.answerValue == null ? "" : source.answerValue)
                : source.values;
        SASurveyDto.AnswerInsert answer = new SASurveyDto.AnswerInsert();
        answer.submitSeq = submission.submitSeq;
        answer.fieldSeq = field.fieldSeq;
        answer.fieldKey = field.fieldKey;
        answer.fieldLabel = field.label;
        answer.fieldType = field.fieldType;
        answer.answerValue = buildAnswerValue(field, values);
        answer.answerJson = buildAnswerJson(field, values);
        answer.sortOrd = field.sortOrd;
        submitMapper.insertAnswer(answer);
    }
}
```

- [ ] **Step 4: Add helper methods for label lookup and JSON output**

```java
private String buildAnswerValue(SASurveyDto.SurveyField field, List<String> values) {
    List<String> normalizedValues = values == null ? List.of() : values;
    String renderType = field.getRenderType();
    if ("checkbox".equals(renderType)) {
        return normalizedValues.stream()
                .map(value -> resolveOptionLabel(field, value))
                .collect(Collectors.joining(", "));
    }
    if (normalizedValues.isEmpty()) {
        return null;
    }
    String value = normalizedValues.get(0);
    if ("select".equals(renderType) || "radio".equals(renderType)) {
        return resolveOptionLabel(field, value);
    }
    return value;
}

private String buildAnswerJson(SASurveyDto.SurveyField field, List<String> values) {
    if (!"checkbox".equals(field.getRenderType())) {
        return null;
    }
    try {
        return objectMapper.writeValueAsString(values == null ? List.of() : values);
    } catch (JsonProcessingException ex) {
        throw new IllegalStateException("Failed to serialize checkbox answer values.", ex);
    }
}

private String resolveOptionLabel(SASurveyDto.SurveyField field, String value) {
    for (SASurveyDto.SurveyOption option : field.options) {
        if (Objects.equals(option.optionValue, value)) {
            return option.optionLabel;
        }
    }
    return value;
}
```

Use the option `optionValue -> optionLabel` mapping from `field.options`. For single-choice fields store the resolved label in `answer_value` and leave `answer_json` null. For checkbox fields store the joined labels in `answer_value` and the raw value array as JSON in `answer_json`. For subjective fields store the raw text in `answer_value` and leave `answer_json` null.

- [ ] **Step 5: Run the targeted tests until they pass**

Run:

```bash
./gradlew test --tests 'com.reven.project.service.sa.SASurveyServiceTest' --tests 'com.reven.project.service.sa.SASurveySubmitServiceTest' --tests 'com.reven.project.client.sa.SAPublicSurveyControllerTest'
```

Expected: all three test classes pass.

---

### Task 3: Render the public survey form with a dedicated fragment.

**Files:**
- Modify: `src/main/resources/templates/client/survey/form.html`
- Create: `src/main/resources/templates/client/survey/field.html`
- Modify: `src/main/resources/static/common/css/app.css`

- [ ] **Step 1: Replace inline question markup with a fragment include**

`form.html` should loop over `survey.fields` and delegate each question to `client/survey/field :: field(field=${field})` so the page shell stays simple.

- [ ] **Step 2: Implement the fragment with render-type switching**

```html
<!-- 사용자 설문 문항 1건을 렌더링하는 fragment -->
<th:block xmlns:th="http://www.thymeleaf.org" th:fragment="field(field)">
    <section class="survey-question">
        <div class="survey-question-head">
            <span class="survey-question-title" th:text="${field.label}">문항</span>
            <span class="required" th:if="${field.required}">*</span>
        </div>
        <div class="survey-question-body" th:switch="${field.renderType}">
            <textarea th:case="textarea" class="form-control" rows="4"
                      th:name="|answers[${field.key}]|"
                      th:required="${field.required}"></textarea>

            <select th:case="select" class="form-control"
                    th:name="|answers[${field.key}]|"
                    th:required="${field.required}">
                <option value="">선택</option>
                <option th:each="option : ${field.options}"
                        th:value="${option.optionValue}"
                        th:text="${option.optionLabel}">보기</option>
            </select>

            <div th:case="radio" class="survey-option-list">
                <label th:each="option : ${field.options}" class="survey-option">
                    <input type="radio"
                           th:name="|answers[${field.key}]|"
                           th:value="${option.optionValue}"
                           th:required="${field.required}">
                    <span th:text="${option.optionLabel}">보기</span>
                </label>
            </div>

            <div th:case="checkbox" class="survey-option-list">
                <label th:each="option : ${field.options}" class="survey-option">
                    <input type="checkbox"
                           th:name="|answers[${field.key}]|"
                           th:value="${option.optionValue}">
                    <span th:text="${option.optionLabel}">보기</span>
                </label>
            </div>

            <input th:case="date" type="date" class="form-control"
                   th:name="|answers[${field.key}]|"
                   th:required="${field.required}">
            <input th:case="number" type="number" class="form-control"
                   th:name="|answers[${field.key}]|"
                   th:required="${field.required}">
            <input th:case="email" type="email" class="form-control"
                   th:name="|answers[${field.key}]|"
                   th:required="${field.required}">
            <input th:case="*" type="text" class="form-control"
                   th:name="|answers[${field.key}]|"
                   th:required="${field.required}">
        </div>
    </section>
</th:block>
```

- [ ] **Step 3: Add only the CSS needed to keep the fragment readable**

```css
.survey-question {
    display: grid;
    gap: 10px;
    padding: 18px 0;
    border-bottom: 1px solid #e3ebf2;
}

.survey-question-head {
    display: flex;
    align-items: center;
    gap: 6px;
}

.survey-question-title {
    color: #263849;
    font-weight: 800;
}

.survey-option-list {
    display: grid;
    gap: 10px;
}

.survey-option {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-weight: 700;
}
```

Do not refactor the rest of the public survey page styles.

- [ ] **Step 4: Run the full verification command**

Run:

```bash
./gradlew test
```

Expected: all tests pass, including the new public survey behavior tests.

---

### Task 4: Review, update tracking docs, and close out P1.

**Files:**
- Modify: `docs/checklist.md`
- Modify: `docs/context-notes.md`
- Modify: `docs/worklog.md` if the implementation touches public survey behavior materially

- [ ] **Step 1: Mark the P1 checklist item complete**

Only tick the P1 checklist item after the full Gradle test run is green and the controller/service tests cover both single-choice and checkbox paths.

- [ ] **Step 2: Record the implementation decision in the context notes**

Note the final decisions for:

```text
- `AnswerRequest.values` is the raw request carrier.
- `answer_value` stores the display label or joined display labels.
- `answer_json` stores the raw selected values for checkbox answers.
- Checkbox `required` is not enforced in P1.
```

- [ ] **Step 3: Re-run the full verification command if any doc-driven code edits were needed**

Run:

```bash
./gradlew test
```

Expected: clean pass before wrapping up.

## Self-Review

- Spec coverage is complete for public rendering, checkbox submission preservation, answer normalization, and fragment-based templating.
- Placeholder scan is clean. No `TBD`, `TODO`, or open-ended implementation steps remain in the plan.
- Type consistency is preserved across `SurveyField.getRenderType()`, `AnswerRequest.values`, and the controller/service method signatures used in later tasks.
