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

**검증 결과 (부분):**
- ✅ 상세 페이지 200 응답, 관리 폼 렌더링 확인
- ✅ 상태 변경 + 메모 POST → 302 PRG 리다이렉트 정상
- ⬜ 리다이렉트 후 변경값 반영 확인 (미완료, 인터럽트)
- ⬜ 엣지케이스 검증 미완료 (빈 status, 2000자 초과, 잘못된 status값)

---

## 미완료 — 기존 버그

### `client/survey/form.html` 500 에러
- **위치:** `form.html:20`
- **원인:** `<th:block th:each="field" th:replace="...">` — Thymeleaf에서 `th:replace` 우선순위(1)가 `th:each` 우선순위(2)보다 높아 fragment 호출 시 `field` 변수가 null로 전달됨
- **증상:** 공개 설문 폼 GET 요청 500, 제출 검증 실패 시 폼 재렌더링 500
- **수정 방법:** `th:each`를 outer 요소에, `th:insert`(또는 별도 inner 요소)로 분리
  ```html
  <!-- 현재 (broken) -->
  <th:block th:each="field : ${survey?.fields}"
            th:replace="~{client/survey/field :: field(field=${field})}">
  </th:block>

  <!-- 수정안 -->
  <th:block th:each="field : ${survey?.fields}">
      <th:block th:replace="~{client/survey/field :: field(field=${field})}"></th:block>
  </th:block>
  ```

---

## 앞으로 할 일 (우선순위 순)

| 순위 | 항목 | 비고 |
|------|------|------|
| 즉시 | `client/survey/form.html` 버그 수정 | 공개 설문 폼 전체 불능 |
| 즉시 | P3 검증 나머지 완료 | 변경값 반영 확인, 엣지케이스 |
| P4 | 문항 순서 변경 기능 | drag-and-drop 또는 위/아래 버튼 |
| P5 | 설문 미리보기 화면 | 관리자에서 사용자 폼 미리 보기 |
| P6 | 목록/이력 검색 조건 강화 | 날짜범위, 상태 필터 등 |
| P7 | 설문 복사 기능 | |
| P8 | 응답 통계/집계 화면 | |
| P9 | 제출 스냅샷 상세화 | |
| P10 | 설문 기간 관리 | |
