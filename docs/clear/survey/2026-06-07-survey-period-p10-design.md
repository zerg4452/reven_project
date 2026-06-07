<!-- 설문 P10 기간 관리 구현 설계서 / created: 2026-06-07 -->

# 설문 P10 — 기간 관리

- 상태. 완료 (completed)
- 작성일. 2026-06-07
- 관련 체크리스트. `docs/checklist.md` → "관리자 설문 우선순위" P10
- 선행. P9 제출 스냅샷 보강

## 1. 배경과 목적

P10은 설문 마스터에 접수 기간을 추가하고, 사용자 화면에서 기간에 따라 설문 참여 가능 여부를 제어하는 작업이다.

현재 설문 공개 여부는 `sa_survey_mst.use_yn`만 기준으로 판단한다. 이 값은 수동 사용 여부라서 "언제부터 언제까지 접수할지"를 표현하지 못한다.

### 목표

1. 관리자가 설문별 접수 시작일과 종료일을 저장할 수 있다.
2. 사용자 설문 목록에는 예정/마감 설문도 비활성 카드로 보여준다.
3. 사용자 메인, 상세 진입, 제출 저장은 접수중 설문만 허용한다.
4. 기존 설문은 기간 제한 없이 기존처럼 동작한다.

## 2. 설계 결정

- 기간 컬럼은 설문 마스터(`sa_survey_mst`)에 `start_date`, `end_date`로 추가한다.
- 두 컬럼은 모두 `NULL` 허용이다. `NULL`은 해당 방향의 기간 제한이 없다는 뜻이다.
- 날짜 판단은 프로젝트 기준 시간대인 `Asia/Seoul`의 오늘 날짜로 한다.
- 접수 판단 날짜는 단일 소스로 한다. 서비스가 `LocalDate.now(Asia/Seoul)`로 `today`를 계산해 매퍼 bind 파라미터로 넘기고, DTO 판정도 같은 `Asia/Seoul` 오늘을 쓴다. SQL에서 `current_date()`를 직접 쓰지 않는다. `current_date()`는 DB 세션 시간대에 의존해 자정 부근에 Seoul 날짜와 어긋날 수 있기 때문이다. 기존 `SurveySearchWhere`가 `#{startDate}`를 넘기는 방식과 같다.
- 종료일은 해당 날짜 전체를 포함한다. 즉 `end_date = 2026-06-07`이면 2026-06-07까지 접수중이다.
- `use_yn = 'N'`이면 기간과 무관하게 접수 불가다.
- 관리자 미리보기는 기간과 무관하게 계속 허용한다.
- 제출 POST에서도 기간을 다시 검사해, 오래 열어둔 form이나 직접 POST로 기간 밖 제출이 저장되지 않게 한다.

### 공개 화면 정책

- 사용자 설문 목록(`/surveys/list.do`)은 삭제되지 않은 설문을 카드로 보여준다.
- 카드 상태는 `접수중`, `예정`, `마감`으로 나눈다.
- `접수중` 카드만 `작성하기` 링크를 노출한다.
- `예정`, `마감`, `미사용` 설문은 비활성 카드로 표시하고 제출 링크를 숨긴다.
- 사용자 메인(`진행중인 설문`)은 `접수중` 설문만 노출한다.
- 상세 화면(`/surveys/detail.do`, `/surveys/write.do`)은 접수중이 아니면 목록으로 이동한다.

## 3. 변경 파일

### 3.1 스키마 — `src/main/resources/schema.sql`

`sa_survey_mst`에 기간 컬럼을 추가한다.

```sql
ALTER TABLE sa_survey_mst ADD COLUMN IF NOT EXISTS start_date DATE NULL COMMENT '설문 접수 시작일' AFTER description;
ALTER TABLE sa_survey_mst ADD COLUMN IF NOT EXISTS end_date DATE NULL COMMENT '설문 접수 종료일' AFTER start_date;
```

신규 생성 테이블 정의에도 같은 컬럼을 반영한다. 기존 row는 NULL로 남기며 별도 백필은 하지 않는다.

### 3.2 DTO — `src/main/java/com/reven/project/service/sa/dto/SASurveyDto.java`

- `SurveyDetail`에 `LocalDate startDate`, `LocalDate endDate`를 추가한다.
- `SurveyListItem`에 `LocalDate startDate`, `LocalDate endDate`를 추가한다.
- `SurveySaveRequest`에 `LocalDate startDate`, `LocalDate endDate`를 추가한다.
- `SurveyDetail`과 `SurveyListItem`에 접수 가능 여부와 상태 표시용 메서드를 추가한다.
- 두 DTO가 같은 기간 로직을 복붙하지 않도록 판정은 `SASurveyDto`의 static helper에 모으고, 각 DTO 메서드는 helper에 위임한다.
- 날짜는 helper 호출부에서 `LocalDate.now(Asia/Seoul)`로 계산해 SQL bind와 같은 시간대를 쓴다.

권장 메서드.

```java
// 각 DTO. 템플릿에서 survey.accepting / survey.periodStatusText로 접근한다
public boolean isAccepting()
public String getPeriodStatusText() // 접수중 / 예정 / 마감

// SASurveyDto static helper. SurveyDetail·SurveyListItem 공용
static boolean accepting(LocalDate startDate, LocalDate endDate, LocalDate today)
static String periodStatusText(LocalDate startDate, LocalDate endDate, LocalDate today)
```

`getPeriodStatus()`(상태 코드)는 템플릿·정렬에 소비처가 없어 추가하지 않는다. 표시는 `getPeriodStatusText()`, 버튼 활성화는 `isAccepting()`으로 충분하다.

`isEnabled()`는 기존 의미인 `use_yn` 기준 수동 사용 여부로 유지한다. 관리자 목록의 사용여부 컬럼이 기간 상태로 바뀌지 않게 하기 위함이다.

### 3.3 매퍼 — `src/main/resources/mapper/sa/SASurveyMapper.xml`

아래 매퍼는 모두 이미 존재한다. 신규 추가가 아니라 기존 정의를 수정한다.

- `SurveyListItemMap`, `SurveyDetailMap`에 `start_date`, `end_date` 매핑을 추가한다.
- 관리자 목록 쿼리와 상세 조회 쿼리 SELECT에 기간 컬럼을 추가한다.
- `insertSurvey`, `updateSurvey`에 기간 컬럼을 추가한다.
- `selectPublicSurveyCardList`는 삭제되지 않은 설문을 모두 가져오되 정렬을 접수중→예정→마감 순으로 바꾼다.
- `selectPublicSurveySummaryList`는 SELECT 조건에 접수중 조건을 추가한다.
- 날짜 비교는 `current_date()` 대신 서비스가 넘기는 `#{today}` bind 파라미터를 쓴다(§2 참고).

접수중 SQL 조건.

```sql
s.use_yn = 'Y'
and (s.start_date is null or s.start_date <= #{today})
and (s.end_date is null or s.end_date >= #{today})
```

카드 목록 정렬. 상태 그룹(접수중 0, 예정 1, 마감·미사용 2)으로 1차 정렬한 뒤 기존 등록일 역순을 유지한다.

```sql
order by
  case
    when s.use_yn = 'Y'
     and (s.start_date is null or s.start_date <= #{today})
     and (s.end_date   is null or s.end_date   >= #{today}) then 0
    when s.use_yn = 'Y'
     and s.start_date is not null and s.start_date > #{today} then 1
    else 2
  end,
  s.reg_dtm desc, s.survey_seq desc
```

정렬용 SQL CASE와 카드의 `periodStatusText` 표시는 둘 다 `#{today}`/Seoul 오늘을 기준으로 해 서로 어긋나지 않는다.

### 3.4 서비스 — `SASurveyService`

- `newSurveyForm()`은 기간을 비워 둔다.
- `copySurveyForm()`은 원본 기간을 복사한다. 복사본은 기존처럼 `useYn = 'N'`이므로 즉시 공개되지 않는다. 단 원본이 이미 만료(end_date 과거)면 복사본을 `Y`로 바꾸는 즉시 마감 상태가 되므로, 만료 설문을 복제할 때는 관리자가 기간을 갱신해야 한다.
- `saveSurvey()`의 `toSurveyDetail()`에서 요청 기간을 마스터 DTO에 채운다.
- `findPublicSurveyCards()`, `findPublicSurveySummaries()`는 `LocalDate.now(Asia/Seoul)`로 `today`를 계산해 매퍼에 넘긴다.
- 접수 상태 판단은 DTO helper에 둔다(§3.2). 서비스는 공개 메인 조회와 저장 흐름을 기존 구조로 유지한다.

### 3.5 관리자 컨트롤러 — `SAAdminSurveyController`

- 기존 `LenientLocalDateEditor`를 그대로 사용해 잘못된 날짜 문자열은 null로 흡수한다.
- 저장 전 기간 검증을 추가한다.
- `startDate`와 `endDate`가 모두 있고 `endDate`가 `startDate`보다 빠르면 저장하지 않고 상세 화면을 재표시한다.
- 검증 실패 시 기존 `errors` map에 기간 오류를 넣어 화면 상단 알림에 표시한다.

오류 메시지.

```text
종료일은 시작일보다 빠를 수 없습니다.
```

### 3.6 관리자 상세 화면 — `admin/survey/detail.html`

설문 기본 정보 영역에 기간 입력을 추가한다.

- 시작일. `<input type="date" name="startDate">`
- 종료일. `<input type="date" name="endDate">`

관리자 목록 컬럼은 프로젝트 규칙의 기존 컬럼 구성을 유지한다. 기간 확인과 수정은 상세 화면에서 한다.

### 3.7 공개 화면 — `client/survey/list.html`, `client/main/index.html`

- 공개 설문 목록은 `survey.accepting` 기준으로 버튼 활성화 여부를 결정한다.
- badge 문구는 `survey.periodStatusText`를 사용한다.
- `접수중`은 기존 `badge-success`, 그 외 상태는 `badge-muted`를 사용한다.
- 메인 화면은 접수중 설문만 받으므로 기존 `접수중` 표시를 유지한다.

### 3.8 공개 컨트롤러와 제출 서비스

- `SAPublicSurveyController.detail()`은 `survey.isAccepting()`이 아니면 목록으로 이동한다. 현재의 `isEnabled()` 검사를 교체한다.
- `SASurveySubmitService.submit()`은 `findSurvey` 직후, 어떤 insert보다 먼저 `survey.isAccepting()`을 검사해 아니면 예외를 던진다.
- `SAPublicSurveyController.submit()`은 현재 `SubmissionValidationException`만 catch하므로, 기간 불가 예외용 catch를 별도로 추가해 목록 redirect로 처리한다.

권장 예외.

```java
public static class SurveyNotAcceptingException extends RuntimeException
```

## 4. 비범위

- 설문 기간을 제출 이력에 스냅샷으로 저장하지 않는다.
- 관리자 목록에 기간 검색 조건을 추가하지 않는다.
- 관리자 목록 컬럼 구성을 바꾸지 않는다.
- 기간별 자동 알림, 예약 배치, 상태 자동 변경 배치는 추가하지 않는다.
- 시간 단위 접수는 지원하지 않는다. 날짜 단위만 지원한다.

## 5. 테스트 / 검증

- `SASurveyServiceTest`.
  - 신규 저장 시 `startDate`, `endDate`가 mapper DTO에 전달된다.
  - 수정 저장 시 기간이 유지/변경된다.
  - 복사 폼이 원본 기간을 복사하되 `useYn = 'N'`을 유지한다.
- `SAAdminSurveyControllerTest`.
  - 정상 기간 저장 요청이 `SurveySaveRequest`에 바인딩된다.
  - 종료일이 시작일보다 빠르면 상세 화면을 재표시하고 저장하지 않는다.
- MyBatis 통합 테스트 또는 mapper 테스트. 테스트는 실 MariaDB에서 돈다(H2·testcontainers 없음).
  - 공개 카드 목록은 접수중/예정/마감 설문을 포함하고 그 순서로 정렬된다.
  - 공개 메인 요약은 접수중 설문만 반환한다.
  - `#{today}`에 경계 날짜를 주입해 시작일 당일·종료일 당일이 접수중에 포함되는지 검증한다.
  - 기존 row 호환을 위해 `startDate = NULL`, `endDate = NULL`인 사용 설문이 접수중으로 조회되는지 검증한다.
- `SAPublicSurveyControllerTest`.
  - 기간 밖 상세 접근은 `/surveys/list.do`로 이동한다.
  - 기간 밖 제출 POST는 저장하지 않고 `/surveys/list.do`로 이동한다.
- 템플릿 테스트.
  - 관리자 상세에 `startDate`, `endDate` 입력이 있다.
  - 공개 목록은 `periodStatusText`와 `accepting` 기준으로 badge와 버튼을 렌더링한다.
- 실행 명령.
  - 부분. `./gradlew test --tests "*SASurvey*" --tests "*SAPublicSurvey*" --tests "*COMainControllerTest"`
  - 전체. `./gradlew test`

## 6. 작업 순서

1. schema.sql 기간 컬럼 추가.
2. DTO 기간 필드와 접수 상태 메서드 추가.
3. 매퍼 resultMap, SELECT, INSERT, UPDATE 수정. 공개 조회는 `#{today}` 조건/정렬 반영.
4. 서비스 저장/복사 흐름에 기간 필드 반영. 공개 조회 시 Seoul `today` 매퍼 전달.
5. 관리자 저장 검증과 상세 화면 기간 입력 추가.
6. 공개 목록/상세/제출/메인 노출 정책 반영.
7. 테스트 추가·보강 후 Gradle 검증.
8. 완료 후 본 문서를 `docs/clear/survey/`에 보관.
