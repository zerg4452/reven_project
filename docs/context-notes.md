# 컨텍스트 노트

## 2026-05-30 사용자 포토 게시판

- 사용자는 관리자에 구현된 포토 게시판을 사용자 화면에도 추가하려고 한다.
- 사용자 GNB 기준으로 기존 `게시판 > AI News` 구조를 `게시판 > AI News / 포토 게시판`으로 확장한다.
- 포토 목록은 카드형이며 한 화면 9개씩 표시한다.
- 카드에는 대표 썸네일, 제목, 등록일을 표시한다.
- 대표 썸네일은 첫 번째 첨부 파일을 사용한다.
- 검색은 제목 키워드와 `이미지 포함`, `동영상 포함` 체크박스를 사용한다.
- 이미지와 동영상 체크박스를 모두 선택하면 OR 조건이다.
- 파일 유형은 새 컬럼 없이 기존 `bd_photo_board_file_dtl.content_type`으로 판단한다.
- 상세 화면은 필요하다.
- 상세 첨부 클릭 시 딤드 레이어 팝업으로 이미지 확대 또는 동영상 재생을 제공한다.
- 상세에서 비게시, 삭제, 존재하지 않는 글 등 비정상 접근은 `비정상적인 접근입니다.` 알럿 후 `/board/photo/list.do`로 이동한다.
- 파일 리소스 직접 접근은 알럿 대신 404로 차단한다.

## 2026-06-01 관리자 설문 등록 수정

- 사용자는 관리자 설문 등록 화면에서 설문 유형을 객관식/주관식 정도의 플래그로 선택할 수 있어야 한다고 요청했다.
- 신규 설문 화면은 `surveyUid`가 미리 생성되더라도 아직 저장 전이므로, 신규/기존 구분은 `surveySeq` 기준으로 보는 것이 안전하다.
- 저장 에러는 설문 유형 컬럼의 위치와 신규 화면 판별 오류를 함께 점검해야 한다.
- 실제 구현은 `sa_survey_field_dtl.survey_type` 컬럼을 추가하고, 문항별로 `objective`/`subjective` 값을 저장하도록 맞췄다.
- 관리자 설문 상세 화면의 새/기존 판별은 `surveySeq` 기준으로 바꿔 신규 등록 시 insert, 기존 수정 시 update가 제대로 분기되도록 했다.
- 회귀 테스트는 설문 서비스 저장 경로와 문항별 설문 유형 기본값을 직접 검증하는 방식으로 추가했다.

## 2026-06-01 관리자 설문 우선순위 정리

- 설문 기능은 한 번에 크게 묶지 않고, 우선순위 리스트를 기준으로 하나씩 설계 문서와 구현 계획을 분리한다.
- P1은 사용자 설문 화면의 문항 타입 렌더링이다. 이 항목이 사용자 입력 가능 여부를 먼저 결정한다.
- P2는 선택형 문항의 옵션 검증과 노출 규칙이다. 렌더링 다음에 와야 실제 입력 구조를 안정적으로 정리할 수 있다.
- P3는 설문 제출 이력의 상태 변경과 관리자 메모 저장이다. 운영 처리 기능이기 때문에 초반에 같이 잡는 것이 좋다.
- 설문 복사 기능은 P7로 내리고, 통계나 기간 관리는 그 뒤로 배치한다.
- 구현이 끝난 항목은 `docs/checklist.md`에서 하나씩 체크 해제한다.

## 2026-06-01 관리자 설문 P1 설계

- P1 설계서는 사용자 설문 화면을 서버 렌더링으로 유지하면서, 문항 단위 fragment로 분리하는 방향으로 잡았다.
- 객관식은 `select`, `radio`, `checkbox`로, 주관식은 `text`, `textarea`, `date`, `number`, `email`로 나눈다.
- 체크박스는 단일 문자열로 평탄화하면 안 되므로, 제출 컨트롤러에서 다중 값을 읽는 구조로 바꿔야 한다.
- 저장은 `answer_value`와 `answer_json`의 기존 컬럼만 사용하고, 별도 스키마 변경은 하지 않는다.
- 관리자 이력과 CSV는 `answer_value`를 계속 사람이 읽을 수 있게 유지하면 별도 보정 없이 이어갈 수 있다.

## 2026-06-01 관리자 설문 P1 구현

- `SASurveyDto.AnswerRequest.values`를 raw 제출값 리스트로 추가했다.
- `SASurveyDto.SurveyField.getRenderType()`로 객관식/주관식 렌더링 fallback을 DTO에서 처리했다.
- `SAPublicSurveyController.submit()`은 `MultiValueMap`을 받아 같은 `answers[fieldKey]` 파라미터를 모두 보존한다.
- `SASurveySubmitService.submit()`은 survey field 순서를 기준으로 `answer_value`와 `answer_json`을 계산한다.
- 단일 객관식은 보기 라벨을 `answer_value`에 저장하고, 체크박스는 라벨 조인 문자열과 raw value JSON 배열을 함께 저장한다.
- 사용자 설문 문항은 `client/survey/field.html` fragment로 분리했다.

## 2026-06-02 관리자 설문 P2 계획

- P2는 저장 검증과 제출 검증을 분리하되, 둘 다 서버에서 막는 방식으로 간다.
- 저장 검증은 `SAAdminSurveyController`가 `BindingResult`를 재사용해서 객관식 옵션 0개와 라벨 중복을 막는다.
- 공개 제출 검증은 `SASurveySubmitService`가 먼저 수행하고, 실패하면 `SAPublicSurveyController`가 같은 설문 화면을 다시 렌더링한다.
- 제출 실패 화면은 입력값 복원보다 문항별 오류 메시지 노출을 우선한다.
- `select`와 `radio`는 하나의 값만 쓰고, `checkbox`만 다중 값을 유지한다.

## 2026-06-02 관리자 설문 P2 구현

- 관리자 저장 검증은 `SAAdminSurveyController`에서 `objective` 문항의 옵션 비어 있음과 중복 라벨을 `BindingResult`로 거른다.
- 공개 제출 검증은 `SASurveySubmitService`가 저장 전에 수행하고, 실패 시 `SubmissionValidationException`을 던진다.
- 공개 컨트롤러는 제출 검증 실패를 잡아서 `client/survey/form`을 다시 렌더링하고 `errors` 맵을 넘긴다.
- 비필수 문항은 값이 비어 있으면 답변 row를 만들지 않는다.
- 문항별 오류 표시는 관리자 설문 상세와 사용자 설문 fragment에 각각 추가했다.
- `./gradlew test`로 전체 회귀를 확인했다.
