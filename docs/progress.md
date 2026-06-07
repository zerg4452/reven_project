# 진행 상황 및 할 일

작성일: 2026-06-07

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

### 설문 P3: 이력 상태 변경 + 관리자 메모 (전체 완료)

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

### 설문 P6: 목록/이력 검색 조건 강화 (전체 완료)

**구현 완료 내용:**
- 설문 관리와 설문 이력 검색에서 잘못된 날짜 문자열을 400/500 대신 기본 날짜로 보정
- 설문 관리 검색 조건에 사용여부 필터 추가
- 설문 관리 `keywordType`, `useYn`과 설문 이력 `statuses` 허용값 보정
- 설문 이력 전체 검색은 설문명 OR 작성자명 기준 유지

**검증 결과:**
- ✅ 컨트롤러와 이력 검색 회귀 테스트 추가
- ✅ `./gradlew test` BUILD SUCCESSFUL

---

### 설문 P7: 설문 복사 기능 (전체 완료)

**구현 완료 내용:**
- 설문 관리 목록 행에 `복사` 링크 추가
- `/admin/surveys/copy.do` 진입 시 원본 설문을 신규 등록 화면에 prefill
- 복사 클릭만으로는 DB INSERT하지 않고, 운영자가 검토 후 등록 저장
- 복제본은 새 `surveyUid`, 제목 `원본 + ' 사본'`, 사용여부 `N`, 모든 seq null 상태로 구성
- 잘못된 `surveyUid`와 누락된 `surveyUid`는 목록 리다이렉트 + `비정상적인 접근입니다.` 알림 처리

**리뷰 반영:**
- P7 범위 밖이던 공개 제출 이메일 형식 검증 변경 제거
- `copy.do`의 `surveyUid` 누락 400 문제 수정
- `관리` 컬럼 추가는 기능 이상이 아니고 복사 버튼 노출 요구에 맞는 UI라 유지

**검증 결과:**
- ✅ 서비스, 컨트롤러, 목록 뷰 회귀 테스트 추가
- ✅ `SASurveySubmitServiceTest`, `SAAdminSurveyControllerTest`, `SASurveyServiceTest`, `SASurveyCopyViewTest` focused test BUILD SUCCESSFUL

---

### 설문 P8: 응답 통계와 집계 화면 (전체 완료)

**구현 완료 내용:**
- 설문 관리 목록 행에 `통계` 링크 추가
- `/admin/surveys/{surveyUid}/statistics.do` 통계 화면 추가
- 상태별 제출 건수, 최근 60일 일자별 제출 추이, 객관식 보기별 빈도, 주관식 최근 답변 표시
- 문항별 통계 집계를 현재 `field_seq`가 아니라 제출 답변 스냅샷의 `field_key_snapshot` 기준으로 보강
- 설문 수정 후 문항/보기가 재생성되어도 과거 제출 응답이 통계에 남도록 수정
- 체크박스 통계는 `answer_json` 배열 값을 SQL에서 펼쳐 집계해 쉼표 포함 보기 라벨도 안전하게 처리
- 객관식 보기별 빈도는 SQL `group by`, 주관식 최근 답변은 SQL `limit 20`으로 처리
- 문항 없는 설문의 빈 상태 메시지 추가

**검증 결과:**
- ✅ 서비스와 컨트롤러/템플릿 회귀 테스트 보강
- ✅ `./gradlew test --tests 'com.reven.project.service.sa.SASurveyStatisticsMapperIntegrationTest'` BUILD SUCCESSFUL
- ✅ `./gradlew test --tests 'com.reven.project.service.sa.SASurveyStatisticsServiceTest'` BUILD SUCCESSFUL
- ✅ `./gradlew test --tests 'com.reven.project.admin.sa.SAAdminSurveyStatisticsControllerTest'` BUILD SUCCESSFUL
- ✅ `./gradlew test` BUILD SUCCESSFUL

---

### 설문 P9: 제출 스냅샷 보강 (전체 완료)

**구현 완료 내용:**
- 답변 테이블 `sa_survey_answer_dtl`에 `survey_type_snapshot` 컬럼(NULL 허용)을 추가
- `insertAnswer`가 제출 시점의 실제 `required_yn`과 `survey_type`을 저장하도록 수정
- `selectStatisticFields`가 `survey_type_snapshot` 우선, 레거시 row는 `field_type_snapshot` 기반 COALESCE 파생으로 읽도록 보강
- 제출 서비스 `SASurveySubmitService`에 `resolveSurveyType`을 추가해 답변 스냅샷을 채움
- `SASurveySubmitServiceTest`와 `SASurveyStatisticsMapperIntegrationTest`에 스냅샷 캡처 및 레거시 파생 검증을 추가

**검증 결과:**
- ✅ focused 테스트 통과
- ✅ `./gradlew test` BUILD SUCCESSFUL

### 설문 P10: 기간 관리 (전체 완료)

**구현 완료 내용:**
- 설문 마스터 `sa_survey_mst`에 `start_date`, `end_date` 컬럼을 추가했다.
- 설문 DTO와 MyBatis 매퍼 저장/조회 경로에 접수 시작일과 종료일을 반영했다.
- 관리자 설문 상세 화면에서 기간을 입력하고, 종료일이 시작일보다 빠른 요청은 저장하지 않도록 검증했다.
- 공개 설문 목록은 예정/마감/미사용 설문도 비활성 카드로 표시하고, 접수중 설문만 `작성하기` 링크를 노출한다.
- 사용자 메인 요약, 상세 진입, 제출 저장은 `Asia/Seoul` 기준 오늘 날짜로 접수중인 설문만 허용한다.
- 설문 복사는 원본 기간을 복사하되 기존 정책대로 복사본 `useYn = N`을 유지한다.

**검증 결과:**
- ✅ 서비스, 컨트롤러, 제출 서비스, mapper 통합, 템플릿 회귀 테스트 보강
- ✅ 코드 리뷰 지적 반영. mapper 통합 테스트 날짜 의존성 제거, NULL 기간 기존 설문 호환 케이스 추가, 공개 목록 문구 정리
- ✅ `./gradlew test --tests '*SASurvey*' --tests '*SAPublicSurvey*' --tests '*COMainControllerTest'` BUILD SUCCESSFUL
- ✅ `./gradlew test` BUILD SUCCESSFUL

### 코드 리팩토링: 중복 제거 (전체 완료)

**구현 완료 내용:**
- R1. `BDNoticeService`·`BDPhotoBoardService`의 중복 파일 저장 헬퍼(디스크 기록·경로 해석·커밋 후 삭제)를 `service/bd/support/BDFileStorageSupport`로 추출. 서비스마다 다른 `rootPath`로 인스턴스를 생성하고, 검증 규칙(확장자·MIME)은 서비스별로 유지.
- R2. 4개 서비스(`BDNotice`/`BDPhotoBoard`/`BDAiNews`/`COAdminMenu`)에 동일하던 `firstText(String...)`를 `common/util/TextUtils`로 통합하고 static import로 전환.
- R3. `SASurveyDto`(531줄) 분리는 churn 대비 이득이 낮아 보류.
- R4. `normalizedAdminSearch`는 컨트롤러가 쓰는 살아 있는 API라 유지.

**검증 결과:**
- ✅ 동작 보존 리팩토링. 기존 회귀로 검증, 두 BD 서비스 합쳐 약 150줄 감소
- ✅ 코드 리뷰 Critical/Important 0건, 머지 가능 판정 → `refactor/bd-dedup` main fast-forward 머지
- ✅ `./gradlew test` BUILD SUCCESSFUL (150 tests, 0 실패)
- 완료 계획서: `docs/clear/refactor/2026-06-07-bd-refactor-dedup-plan.md`

---

## 앞으로 할 일 (우선순위 순)

| 순위 | 항목 | 비고 |
|------|------|------|
| 후속 | BD 파일저장 테스트 갭 보강 | `BDNoticeServiceTest`가 파일 로직 미커버 + `resolveStoredFilePath` 경로 탈출 음성 테스트 부재. 둘 다 기존 갭 |
