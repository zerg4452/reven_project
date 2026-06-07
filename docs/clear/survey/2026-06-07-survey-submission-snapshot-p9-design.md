<!-- 설문 P9 제출 스냅샷 보강(required_yn 실제값 + survey_type_snapshot) 구현 설계서 / created: 2026-06-07 -->

# 설문 P9 — 제출 스냅샷 보강 (required_yn + survey_type)

- 상태. 완료 (completed)
- 작성일. 2026-06-07
- 관련 체크리스트. `docs/checklist.md` → "관리자 설문 우선순위" P9
- 선행. P8 응답 통계 (스냅샷 기준 집계)

## 1. 배경과 목적

P9는 제출 이력 답변 row의 스냅샷을 더 충실하게 남기는 작업이다. 사용자가 확정한 범위는 두 가지다.

1. 제출 시점의 **필수 여부(required_yn)** 를 실제 값으로 저장한다.
2. 제출 시점의 **설문 유형(survey_type, 객관식/주관식)** 을 답변 row에 스냅샷한다.

### 현재 갭

- `sa_survey_answer_dtl.required_yn_snapshot`은 `NOT NULL DEFAULT 'N'`이지만, `insertAnswer` 매퍼가 항상 리터럴 `'N'`을 넣는다. 제출 시점 필수 여부가 전혀 보존되지 않는다. 현재 이 컬럼을 읽는 코드는 없고 쓰기만 한다.
- 답변 row에 survey_type 스냅샷 컬럼이 아예 없다. P8 통계는 `field_type`에서 객관식/주관식을 파생한다(`SASurveyStatisticsService.defaultSurveyType`와 `SASurveySubmitMapper.xml`의 `selectStatisticFields` CASE). 제출 시점 유형이 권위 데이터로 남지 않는다.

### 목표

- 신규 제출은 실제 `required_yn`, `survey_type`을 답변 row에 기록한다.
- P8 통계는 파생 대신 스냅샷 `survey_type`을 신뢰하되, 레거시 row(NULL)는 `field_type_snapshot` 기반 파생으로 fallback한다.

## 2. 설계 결정

- survey_type_snapshot 컬럼은 **NULL 허용**으로 추가한다. 기존 row는 NULL로 두고 읽기 경로에서 COALESCE로 파생한다. 이렇게 하면 매 부팅마다 도는 `schema.sql`에 비멱등 백필 UPDATE를 넣지 않아도 된다.
- required_yn 레거시 값은 복구 불가하다. 기존 row는 `'N'`으로 남는다. 신규 제출부터 정확히 기록한다. 한계로 명기한다.
- 이력 상세 화면(`history-detail.html`)은 현재 required/survey_type을 표시하지 않는다. P9는 **수집·보존만** 한다. 화면 노출은 향후 작업이며 비범위다.

## 3. 변경 파일

### 3.1 스키마 — `src/main/resources/schema.sql`

`sa_survey_answer_dtl`에 컬럼 추가. `required_yn_snapshot`은 이미 있으므로 DDL 없음.

```sql
ALTER TABLE sa_survey_answer_dtl ADD COLUMN IF NOT EXISTS survey_type_snapshot VARCHAR(20) NULL COMMENT '설문 유형 스냅샷' AFTER field_type_snapshot;
```

### 3.2 DTO — `src/main/java/com/reven/project/service/sa/dto/SASurveyDto.java`

- `AnswerInsert`에 `public String requiredYn;`, `public String surveyType;` 추가.
- `AnswerSnapshot`에 `public String requiredYn;`, `public String surveyType;` 추가.

### 3.3 매퍼 — `src/main/resources/mapper/sa/SASurveySubmitMapper.xml`

- `insertAnswer`. 컬럼 목록과 values에 `required_yn_snapshot = #{requiredYn}`(리터럴 `'N'` 제거), `survey_type_snapshot = #{surveyType}` 추가.
- `AnswerSnapshotMap`. `required_yn_snapshot → requiredYn`, `survey_type_snapshot → surveyType` 매핑 추가.
- `selectStatisticFields`. survey_type 산출을 `field_type` CASE 단독에서 COALESCE로 변경.

```sql
coalesce(survey_type_snapshot,
         case when lower(field_type_snapshot) in ('select','radio','checkbox')
              then 'objective' else 'subjective' end) as survey_type
```

신규 row는 스냅샷 값을, 레거시 NULL row는 파생 값을 쓴다.

### 3.4 제출 서비스 — `SASurveySubmitService.submit()`

답변 build 루프(현재 line 74~83)에서 두 필드를 채운다.

- `answer.requiredYn = field.isRequired() ? "Y" : "N";`
- `answer.surveyType =` field.surveyType이 비어 있으면 `field_type` 기반 파생, 아니면 그대로. `SurveyField.surveyType` 기본값이 "objective"라 대부분 그대로 들어가고 blank 방어만 추가한다.

### 3.5 P8 통계 서비스 — `SASurveyStatisticsService`

- 현존 문항 경로(`fromCurrentField`)는 이미 `field.surveyType`을 쓴다. 변경 없음.
- 스냅샷 전용(삭제된) 문항은 `selectStatisticFields`가 COALESCE로 채운 surveyType을 그대로 받는다. `defaultSurveyType` fallback은 무해하므로 유지한다. 추가 변경 없음.

## 4. 비범위

- 옵션셋 스냅샷(제출 시점 보기 전체 동결).
- 무응답 문항 row 생성.
- survey description 스냅샷.
- 기존 row 백필.
- 이력 상세 화면의 required/survey_type 표시.

## 5. 테스트 / 검증

- `SASurveySubmitServiceTest`. `insertAnswer`를 ArgumentCaptor로 잡아 검증한다.
  - 필수 + 주관식 문항 제출 → `requiredYn="Y"`, `surveyType="subjective"`.
  - 객관식 체크박스 → `surveyType="objective"`.
- `SASurveyStatisticsMapperIntegrationTest`(실DB) 보강.
  - 신규 제출 → `selectStatisticFields`가 스냅샷 survey_type 반환.
  - `survey_type_snapshot` NULL 레거시 row → COALESCE 파생 동작.
- 기존 P8 테스트(`SASurveyStatisticsServiceTest`, 통합, 컨트롤러) 회귀 그린 유지.
- 실행 명령.
  - 부분. `./gradlew test --tests "*SASurveySubmit*" --tests "*SASurveyStatistics*"` (MariaDB localhost:3307 기동 상태).
  - 전체. `./gradlew test`.

## 6. 작업 순서

1. schema.sql 컬럼 추가.
2. DTO 2곳 필드 추가.
3. 매퍼 insertAnswer / AnswerSnapshotMap / selectStatisticFields 수정.
4. 제출 서비스 필드 채우기.
5. 테스트 추가·보강 후 Gradle 검증.
6. 완료 시 본 문서를 `docs/clear/survey/`로 이동.
