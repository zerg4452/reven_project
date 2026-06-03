# 진행 상황 및 할 일

작성일: 2026-06-02

---

## 완료된 작업

### 사용자 포토 게시판 (전체 완료)
- 컨트롤러/서비스/매퍼/템플릿/CSS/JS 구현
- 테스트, Docker 기동 검증까지 완료

### 관리자 설문 등록 수정 (전체 완료)
- 문항별 `surveyType` (객관식/주관식) DB 컬럼 + DTO + mapper 추가
- 관리자 등록 화면 UI: 문항별 surveyType 선택 + 동적 fieldType 필터링 JS
- 신규/기존 판별 기준을 `surveyUid` → `surveySeq`로 수정 (신규 저장 버그 수정)
- 관련 테스트 추가

### 설문 P1/P2 (전체 완료)
- 사용자 설문 화면 객관식/주관식 렌더링 반영
- 선택형 문항 옵션 검증 + 제출 실패 표시
- 관리자/공개 저장/제출 검증 + 오류 메시지

### 설문 P3: 이력 상태 변경 + 관리자 메모 (구현 완료, 검증 중단)

**구현 완료 파일:**
- `SASurveyDto` — `SubmissionDetail.getStatusText()` 추가, `SubmissionUpdateRequest` 추가
- `SASurveySubmitMapper.java` + `SASurveySubmitMapper.xml` — `updateSubmission` 추가
- `SASurveySubmitService` — 상태값 검증 + `updateSubmission` 트랜잭션
- `SAAdminSurveySubmissionController` — POST `/admin/survey-submissions/update.do` 추가
- `history-detail.html` — 상태 드롭다운/메모 textarea/저장 폼 추가

**검증 중 발견한 버그 (수정 완료):**
- `history-detail.html` — `th:each`+`th:if` 우선순위 문제로 `updateErrors` null 시 NPE → `th:each`를 inner `<span>`으로 이동해 수정

**검증 결과:**
- ✅ 상세 페이지 200 응답, 관리 폼 렌더링 확인
- ✅ 상태 변경 + 메모 POST → 302 PRG 리다이렉트 정상
- ✅ 엣지케이스 검증 완료 (`SAAdminSurveySubmissionControllerTest` 추가)
  - 빈 status → `@NotBlank` → history-detail 재표시, 저장 안 함
  - 2000자 초과 메모 → `@Size(max=2000)` → 재표시, 저장 안 함
  - 잘못된 status값 → 컨트롤러에서 `statusOptions()` 멤버십 검사 후 `rejectValue` → graceful 재표시 (기존엔 서비스 `IllegalArgumentException`으로 500이던 것 수정)
- ✅ 리다이렉트 후 변경값 반영 (로직 검증). `updateSubmission` mapper SQL/`@Param` 정합 확인, redirect → `detail.do`가 `selectSubmission`으로 새 row 재조회. admin 로그인 end-to-end curl은 크리덴셜 없어 미수행

---

## 수정 완료 — 기존 버그

### `client/survey/form.html` 500 에러 (수정 완료)
- **위치:** `form.html:20`
- **원인:** `<th:block th:each="field" th:replace="...">` — Thymeleaf에서 `th:replace` 우선순위(1)가 `th:each` 우선순위(2)보다 높아 fragment 호출 시 `field` 변수가 null로 전달됨
- **증상:** 공개 설문 폼 GET 요청 500, 제출 검증 실패 시 폼 재렌더링 500
- **수정:** `th:each`를 outer `<th:block>`, fragment 호출을 inner `<th:block>`으로 분리 (아래 적용 완료)
  ```html
  <th:block th:each="field : ${survey?.fields}">
      <th:block th:replace="~{client/survey/field :: field(field=${field})}"></th:block>
  </th:block>
  ```
- ✅ 라이브 검증 완료. 앱 재기동 후 `GET /surveys/detail.do?surveyUid=...` → HTTP 200, 문항 4건 정상 렌더, exception 0 (이전 500 해소)

---

### 설문 P4: 문항 순서 변경 (전체 완료)

**구현 완료 파일:**
- `detail.html` — 문항 row 헤더에 ▲/▼ 순서 버튼 + `.field-row-actions` 추가
- `survey-field-editor.js` — move up/down, `refreshRows` disabled 갱신, DOM swap 후 `fields[n]` 재부여
- `app.css` — `.field-row-actions`, `.btn-compact` 스타일

**TDD 검증:**
- `SASurveyServiceTest.saveSurveyPersistsFieldOrderFromRequestListOrder` — POST 리스트 순서 → `sort_ord` 1..N
- `SAAdminSurveyControllerTest.saveSurveyPreservesPostedFieldOrder` — 컨트롤러 바인딩 순서 유지
- `SASurveyFieldReorderViewTest` — 템플릿·JS에 reorder 마크업/핸들러 존재
- `./gradlew test` BUILD SUCCESSFUL

---

### 설문 P5: 미리보기 화면 (전체 완료)

**구현 완료 파일:**
- `SAAdminSurveyController` — GET `/admin/surveys/preview.do` 추가
- `admin/survey/detail.html` — 저장된 설문에 새 창 미리보기 버튼 추가
- `client/survey/form.html` — `previewMode` 안내, 제출 차단, 제출 버튼 숨김
- `client/survey/field.html` — 미리보기 모드 입력 비활성화

**검증 결과:**
- ✅ 저장된 설문만 미리보기 버튼 노출
- ✅ 미리보기 새 창 링크 `target="_blank"` + `rel="noopener noreferrer"` 확인
- ✅ 미리보기 화면 제출 버튼 제거, form submit 차단, 입력 비활성화
- ✅ 잘못된 `surveyUid`는 관리자 설문 목록으로 이동하고 비정상 접근 알림 표시
- ✅ `SAAdminSurveyControllerTest`, `SASurveyPreviewViewTest` 추가
- ✅ `./gradlew test` BUILD SUCCESSFUL

---

## 앞으로 할 일 (우선순위 순)

| 순위 | 항목 | 비고 |
|------|------|------|
| P6 | 목록/이력 검색 조건 강화 | 날짜범위, 상태 필터 등 |
| P7 | 설문 복사 기능 | |
| P8 | 응답 통계/집계 화면 | |
| P9 | 제출 스냅샷 상세화 | |
| P10 | 설문 기간 관리 | |
