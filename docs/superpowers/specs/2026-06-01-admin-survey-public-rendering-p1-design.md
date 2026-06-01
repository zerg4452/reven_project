# 관리자 설문 사용자 렌더링 P1 설계

작성일: 2026-06-01

## 목표

관리자 설문 문항에 저장된 `surveyType`과 `fieldType`을 사용자 설문 작성 화면에 반영한다. 객관식 문항은 선택형 UI로, 주관식 문항은 입력형 UI로 렌더링하고, 다중 선택 응답도 현재 저장 구조 안에서 안전하게 저장되도록 한다.

## 현재 상태

- 관리자 문항 편집 화면은 이미 `objective`와 `subjective`를 상위 분류로 사용한다.
- `src/main/resources/static/admin/js/survey-field-editor.js`는 `surveyType`에 따라 허용되는 `fieldType`과 옵션 노출 여부를 제어한다.
- 사용자 설문 화면 `src/main/resources/templates/client/survey/form.html`은 아직 `textarea`, `select`, 기본 `input`만 분기한다.
- 제출 마스터와 답변 테이블에는 `answer_value`와 `answer_json` 컬럼이 이미 존재한다.
- `src/main/java/com/reven/project/service/sa/SASurveyService.java`는 `surveyType` 값을 `objective`와 `subjective`로 정규화한다.

## 선택한 방향

서버 렌더링을 유지하고, 사용자 설문 폼의 문항 렌더링만 분리한다. 템플릿에 모든 분기를 몰아넣지 않고, 문항 1건을 렌더링하는 전용 fragment를 두는 방식을 추천한다.

### 대안 비교

1. `client/survey/form.html` 안에서 모든 분기를 직접 처리한다.
   - 장점은 파일 수가 적다.
   - 단점은 문항 타입이 늘어날수록 템플릿이 빠르게 복잡해진다.

2. 문항 렌더링 fragment를 추가해서 `surveyType`과 `fieldType` 분기를 한 곳에 모은다.
   - 장점은 책임이 분리되고, 이후 P2에서 옵션 검증이나 표시 규칙을 붙이기 쉽다.
   - 단점은 템플릿 파일이 하나 더 늘어난다.

3. 클라이언트 JavaScript로 폼을 다시 그린다.
   - 장점은 초기에는 마크업을 단순하게 유지할 수 있다.
   - 단점은 제출값 정규화와 접근성, 서버 렌더링 일관성이 모두 나빠진다.

권장안은 2번이다. 현재 프로젝트가 서버 렌더링 중심이고, 설문 문항은 서버에서 내려오는 메타데이터를 그대로 쓰는 편이 가장 안정적이다.

## 렌더링 규칙

문항은 `surveyType`을 먼저 기준으로 나누고, 그 안에서 `fieldType`을 세부 위젯으로 해석한다.

| surveyType | fieldType | 화면 위젯 | 제출 형태 |
| --- | --- | --- | --- |
| `objective` | `select` | 단일 선택 `<select>` | 단일 문자열 |
| `objective` | `radio` | 라디오 그룹 | 단일 문자열 |
| `objective` | `checkbox` | 체크박스 그룹 | 다중 문자열 |
| `subjective` | `text` | 단일 텍스트 입력 | 단일 문자열 |
| `subjective` | `textarea` | 장문 입력 | 단일 문자열 |
| `subjective` | `date` | 날짜 입력 | 단일 문자열 |
| `subjective` | `number` | 숫자 입력 | 단일 문자열 |
| `subjective` | `email` | 이메일 입력 | 단일 문자열 |

### 객관식 문항 규칙

- 선택지는 `field.options`를 그대로 사용한다.
- `optionValue`를 HTML 제출값으로 사용하고, `optionLabel`은 화면에 보여준다.
- `checkbox`는 같은 문항 키로 여러 값을 제출할 수 있어야 한다.
- 선택형 문항에서 옵션이 비어 있으면 화면은 최소한으로 렌더링하되, 이후 우선순위 과제에서 저장 검증을 보강한다.
- 체크박스 그룹의 "최소 한 개 선택" 강제는 P1 범위가 아니다. HTML `required`는 체크박스 그룹에서 "모두 체크"를 강요하므로 객관식 체크박스 문항에는 `required` 속성을 걸지 않는다. 필수 객관식의 최소 선택 검증은 옵션 검증과 함께 P2로 미룬다.

### 주관식 문항 규칙

- `text`, `textarea`, `date`, `number`, `email`을 기존 HTML 입력 타입과 연결한다.
- `options`는 표시하지 않는다.
- `required` 표시는 기존과 같은 방식으로 유지한다.

### 기본값과 예외 처리

- `surveyType`이 비어 있거나 알 수 없는 값이면 `objective`로 본다.
- `fieldType`이 `surveyType`과 맞지 않으면 각 타입군의 첫 번째 허용 타입(objective는 `select`, subjective는 `text`)으로 떨어뜨린다.
- 이 정규화 분기는 템플릿이 아니라 `SurveyField` DTO의 조회용 메서드(예: `getRenderType()`)나 서비스 계층에서 처리한다. Thymeleaf 표현식에 fallback 로직을 넣지 않는다.
- 기존 데이터는 이미 `SASurveyService`에서 `objective`와 `subjective`로 정규화하므로, 렌더링은 그 값을 그대로 신뢰한다.

## 제출 정규화

P1에서 가장 중요한 저장 규칙은 체크박스처럼 한 문항에 여러 값이 들어오는 경우를 깨지지 않게 저장하는 것이다.

### 폼 값 바인딩

- 현재 `SAPublicSurveyController.submit()`은 `@RequestParam Map<String, String> params`를 받는다. 이 구조는 같은 키(`answers[key]`)가 여러 번 오면 한 값으로 붕괴하므로 체크박스를 받을 수 없다.
- 바인딩을 `@RequestParam MultiValueMap<String, String> params`로 바꾼다. `MultiValueMap`은 같은 키의 모든 값을 `List<String>`으로 보존한다.
- `answers[fieldKey]` 키를 잘라 `fieldKey` → `List<String>` 형태로 그룹핑한 뒤, 문항 1건당 `AnswerRequest` 1건을 만든다. 단일 값 문항은 리스트 크기 1, 체크박스는 크기 N으로 들어온다.
- 다중 값을 담기 위해 `AnswerRequest`에 `List<String> values` 필드를 추가한다. 컨트롤러는 제출된 raw `optionValue`(주관식은 입력 원문) 목록을 이 필드에 채우기만 한다. 컨트롤러는 라벨 변환이나 JSON 직렬화를 하지 않는다.
- 기존 `answerValue`/`answerJson` 필드는 클라이언트 입력이 아니라 `SASurveySubmitService`가 `values`로부터 계산하는 결과값으로 역할을 바꾼다. 즉 전달은 `values` 단일 경로로 통일하고, `answer_value`/`answer_json` 컬럼 형식 결정은 전적으로 서비스가 맡는다.

### 제출값 → 저장값 변환

- 폼이 제출하는 값은 `optionValue`다(라벨 아님). 서비스는 `AnswerRequest.values`를 입력으로 받아 저장값을 계산한다. `optionValue`를 `optionLabel`로 변환하는 책임은 `SASurveySubmitService.submit()`에 둔다. 이 서비스는 이미 `surveyService.findSurvey()`로 문항·옵션 메타데이터를 로드하므로 추가 조회가 필요 없다.
- 변환 규칙은 문항의 `optionValue` → `optionLabel` 매핑을 만들어 적용한다. 제출된 value가 매핑에 없으면(stale 옵션이나 조작) 변환하지 않고 제출된 value 원문을 그대로 저장한다. 즉 "라벨이 있으면 라벨, 없으면 원본 value".
- 주관식 문항은 옵션이 없으므로 변환 없이 입력 원문을 그대로 저장한다.

### 컬럼별 저장 형식

- 단일 선택 문항(`select`, `radio`)은 `answer_value`에 선택된 보기의 **라벨**을 저장하고 `answer_json`은 비워 둔다.
- 체크박스 문항은 `answer_value`에 선택된 보기 **라벨**을 `, `로 연결한 문자열을 저장하고, `answer_json`에 선택된 **value 배열**을 JSON 문자열로 저장한다. 예: 보기 value가 `a`, `c`이면 `answer_json = ["a","c"]`. 라벨이 아니라 value를 보존하는 이유는 옵션 라벨이 나중에 바뀌어도 원본 선택을 복원할 수 있게 하기 위함이다.
- 주관식 문항은 `answer_value`에 사용자가 입력한 원문을 저장하고, `answer_json`은 비워 둔다.
- `answer_value`는 표시용이다. 기존 관리자 이력 화면과 CSV에서 바로 읽을 수 있도록 사람이 읽는 문자열을 유지한다. 라벨에 `, `가 포함되면 체크박스 조인 결과가 모호해질 수 있으나, 원본은 `answer_json`이 보존하므로 P1에서는 허용한다.
- `answer_json`은 멀티 선택의 원본 value를 보존하는 용도이며, 단일/주관식에서는 빈 값(null)으로 둔다.

이 방식이면 `sa_survey_answer_dtl`의 기존 컬럼만으로 P1을 처리할 수 있고, 별도 스키마 변경은 필요하지 않다.

## 변경 범위

### 수정 대상

- `src/main/resources/templates/client/survey/form.html`.
- `src/main/resources/templates/client/survey/field.html` 또는 동등한 전용 fragment 파일.
- `src/main/java/com/reven/project/client/sa/SAPublicSurveyController.java`.
- `src/main/java/com/reven/project/service/sa/SASurveySubmitService.java`.

### 필요하면 같이 보는 파일

- `src/main/java/com/reven/project/service/sa/dto/SASurveyDto.java`.
- `src/main/resources/templates/admin/survey/history-detail.html`.
- `src/main/resources/mapper/sa/SASurveySubmitMapper.xml`.

후자 세 개는 P1의 핵심은 아니지만, 제출 정규화 방식에 따라 보조 조정이 필요할 수 있다.

## 비범위

- 설문 문항 옵션의 비어 있음, 중복, 개수 제한 검증은 P2로 미룬다.
- 문항 순서 변경은 P4다.
- 미리보기는 P5다.
- 설문 복사는 P7이다.
- 응답 통계와 집계는 P8이다.
- 제출 스냅샷 확장은 P9다.
- 설문 기간 관리는 P10이다.
- 관리자 제출 이력 상태 수정과 메모 저장은 P3다.

## 테스트 기준

### 컨트롤러 수준

- `/surveys/detail.do?surveyUid=...`가 `objective`와 `subjective` 문항을 포함한 설문을 정상 렌더링한다.
- `/surveys/submit.do`가 단일 선택 응답을 정상 저장한다.
- `/surveys/submit.do`가 체크박스 응답을 정상 저장한다.

### 서비스 수준

- 객관식 문항의 체크박스 제출이 `answer_value`(라벨 조인)와 `answer_json`(value 배열) 둘 다 채워진 상태로 저장된다.
- 단일 선택 제출은 `optionValue`로 들어와 `answer_value`에 `optionLabel`로 변환되어 저장된다.
- 옵션에 없는 value가 제출되면 변환하지 않고 원본 value를 그대로 저장한다.
- 주관식 문항은 기존처럼 단일 문자열로 저장된다.
- 알 수 없는 `surveyType`은 `objective`로 정규화된다.

### 회귀 기준

- 기존 설문 목록과 등록 흐름은 영향을 받지 않는다.
- 관리자 이력 상세와 CSV 출력은 단일 선택 응답에 대해 기존과 동일하게 동작한다.
- 체크박스 응답도 읽기 가능한 문자열을 유지한다.

## 완료 기준

- 사용자 설문 화면에서 `objective`와 `subjective` 문항이 각각 알맞은 입력 UI로 보인다.
- 체크박스 응답이 여러 값으로 제출되어도 저장이 깨지지 않는다.
- 기존 단일 응답 설문은 동작 방식이 바뀌지 않는다.

## 재검토 메모

- (해결) 체크박스 다중 값 전달 형식. `AnswerRequest`에 `List<String> values` 필드를 두는 방식으로 확정했다. 컨트롤러는 raw `optionValue` 목록만 채우고, `answer_value`/`answer_json` 계산은 `SASurveySubmitService`가 담당한다. §폼 값 바인딩과 §제출값 → 저장값 변환 참고.
- (해결) `required` 체크박스 처리. 객관식 체크박스에는 HTML `required`를 걸지 않고, "최소 한 개 선택" 검증은 P2로 미룬다. 체크박스 렌더링 자체는 P1 범위에 포함한다. §객관식 문항 규칙 참고.
