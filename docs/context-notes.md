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

## 2026-06-02 관리자 설문 P4 설계

- P4는 관리자 설문 상세에서 **문항 순서 변경**이다. drag-and-drop 대신 **위/아래 버튼**으로 확정했다.
- DB `sort_ord`, DTO `sortOrd`, `insertChildren` fallback, 조회 `ORDER BY sort_ord`는 이미 있으므로 백엔드 변경은 없다.
- 순서는 DOM row swap → `refreshRows()`로 `fields[n].*` 재부여 → 기존 수정/등록 POST로 저장한다. hidden `sortOrd`나 전용 reorder API는 쓰지 않는다.
- 즉시 저장 없음. 사용자가 하단 수정/등록 버튼으로 full form save한다.
- 설문 정의 순서 변경은 이후 신규 제출에만 적용되고, 기존 제출 이력의 answer `sort_ord` 스냅샷은 유지한다.
- 수정 저장 시 문항 delete/reinsert v1 정책은 그대로 둔다. `field_seq` 안정화는 P4 비범위다.
- 설계서: `docs/clear/survey/2026-06-02-admin-survey-field-reorder-p4-design.md`

## 2026-06-02 관리자 설문 P4 구현

- 관리자 설문 상세에 ▲/▼ 버튼으로 문항 순서를 바꾸고, 기존 수정/등록 POST로 `sort_ord`에 반영한다.
- 백엔드 변경 없음. DOM row 순서 → `fields[n]` 재부여 → `insertChildren` fallback.
- TDD로 서비스·컨트롤러·뷰/JS 존재 테스트를 먼저 추가한 뒤 UI/JS/CSS를 구현했다.

## 2026-06-02 bd 파일 저장 상수 공통화

- `service.bd.support.BDFileStorageConstants`에 저장 경로 포맷(`yyyy/MM/dd`), `THUMB`/`ATTACH` 코드, 이미지 확장자, 공지 첨부·포토보드 확장자를 모았다.
- 포토보드 전용 업로드 메시지는 `BDPhotoBoardService`에 유지했다. 공지 첨부 확장자(문서·zip 등)는 공지 전용으로 상수 클래스에만 둔다.

## 2026-06-02 docs 정리

- `docs/` 루트에는 기록·할 일만 유지한다. `checklist.md`, `progress.md`, `context-notes.md`, `worklog.md`.
- 완료된 설계·구현 계획은 `docs/clear/` 아래 분류별로 이동했다. `migration/`, `survey/`, `photo-board/`, `user-main/`, `notice-board/`.
- `docs/superpowers/` 디렉터리는 제거했다. 목록은 `docs/clear/README.md` 참고.

## 2026-06-03 관리자 AI News 상태 선택

- AI News 편집 화면의 `status` 셀렉트는 현재 값 기준 `th:selected`가 없어서 브라우저가 첫 번째 옵션을 고르는 문제가 있었다.
- 처음엔 `Y`/`N` 두 값만 자동 선택하도록 정리했지만, 이후 상태 체계 자체를 `P/Y/E`로 다시 맞췄다.
- 템플릿 소스 회귀 테스트를 추가했고 `./gradlew test`로 통과를 확인했다.

## 2026-06-03 관리자 AI News 상태 정리

- AI News에서 `N`은 파일 수집 전 단계의 큐 상태라서 DB와 관리자 UI에 그대로 노출할 필요가 없다.
- Spring 쪽은 JSON 수집 시 `N`을 `P`로 정규화하고, 목록/상세/검색은 `P/Y/E`만 보이도록 맞췄다.
- 관리자 편집 화면도 `P/Y/E`만 선택 가능하게 바꾸고, 상태 라벨은 `처리중/완료/에러`로 정리했다.
- 기존 `N` 데이터가 조회될 수 있는 경로는 mapper에서 `P`로 정규화했다.

## 2026-06-03 관리자 AI News 크롤링 재수집 방지

- 현재 Spring 크롤러는 레거시처럼 `N` 파일만 한 번 처리하고 성공 후 소스 JSON을 소비 상태로 바꾸는 로직이 아직 없다.
- 그래서 이미 `Y`로 게시된 글도 같은 slug의 `N` JSON이 다시 들어오면 `P`로 내려갈 수 있다.
- 해결 방향은 크롤링 성공 후 JSON 상태를 갱신하고, 이미 `Y`인 DB 글은 재크롤링에서 건드리지 않는 것이다.

## 2026-06-03 관리자 AI News 재수집 방지 구현 결과

- Spring AI News 크롤러는 이제 `N` 상태 파일만 집계한다.
- 성공한 JSON은 `P` 처리중 상태로 갱신하고 `inserted_at`과 `news_seq`를 기록한다.
- 같은 slug의 DB 글이 이미 `Y`이면 DB를 건드리지 않고 JSON만 소비 처리한다.
- 회귀 테스트로 새 파일 import, 게시 글 보존, 이미 소비된 파일 스킵을 확인했다.

## 2026-06-03 관리자 AI News 크롤링 제목 접두사

- 크롤링으로 DB에 들어가는 제목은 JSON 내부 `published_at` 날짜를 기준으로 `[yyyy-MM-dd] 제목` 형식으로 저장한다.
- 접두사는 저장 직전에만 붙이고, 수동 등록/수정 화면에는 적용하지 않는다.
- 이미 같은 접두사가 붙은 제목은 중복 prefix를 피하도록 그대로 둔다.

## 2026-06-03 관리자 AI News 편집 후 알럿

- AI News 편집에서 저장/삭제를 처리한 뒤에는 상세로 남기지 않고 목록으로 돌려보낸다.
- 저장/삭제 결과는 `aiNewsSavedMessage` flash attribute로 넘기고, 목록 화면에서 브라우저 `alert()`로 보여준다.
- 크롤링 완료 메시지는 기존처럼 목록 상단 알럿 영역으로 유지한다.

## 2026-06-03 관리자 AI News 수집 방식 견적

- 배치형은 이미 생성된 JSON을 스케줄러로 주기 import하는 방식으로 본다.
- 인앱 크롤링형은 원문 페이지나 API를 직접 읽어 파싱하는 별도 수집 계층으로 본다.
- 현재 프로젝트는 외부 자동화가 JSON을 만들고 Spring Boot가 소비하는 구조가 가장 단순하므로, 1차 권장안은 배치형이다.
- 인앱 크롤링형은 소스 파서, 페이지네이션, 재시도, 중복 제거, 운영 로그까지 포함하는 별도 서브시스템으로 보는 것이 맞다.
- 진행 예정 문서는 `docs/planned`, 완료 문서는 `docs/clear`에 둔다는 분류 규칙을 적용한다.

## 2026-06-03 관리자 설문 P5 미리보기 설계

- P5는 관리자 설문 상세에서 저장된 설문을 사용자 화면처럼 미리 보는 기능이다.
- 진입 방식은 새 창이나 새 탭으로 열리는 링크가 맞다.
- 미리보기는 저장된 설문만 대상이고, 신규 작성 중 설문은 제외한다.
- 화면은 공개 설문 UI를 그대로 재사용하고, 제출은 막는 읽기 전용 상태로 둔다.
- `window.open`보다 `target="_blank"` 링크가 단순하고 팝업 차단 영향을 덜 받으므로 이 방식을 쓴다.
- `target="_blank"` 링크에는 `rel="noopener noreferrer"`를 붙인다.
- 미리보기는 제출 버튼만 숨기지 않고 form submit 자체를 막는다. 공개 폼의 실제 `/surveys/submit.do` action이 살아 있으면 Enter 키나 스크립트 제출로 데이터가 저장될 수 있기 때문이다.
- 문항 입력은 `client/survey/field.html` fragment 안에 있으므로, `previewMode` 비활성화는 form 템플릿과 field fragment 양쪽에 반영해야 한다.
- 잘못된 `surveyUid`는 500 대신 관리자 목록 이동과 비정상 접근 알림으로 처리한다.

## 2026-06-03 관리자 설문 검색 강화 P6

- 잘못된 날짜 문자열은 `LenientLocalDateEditor`로 null 흡수 후 컨트롤러에서 기본 날짜로 fallback한다. BindException으로 목록 진입이 막히지 않게 한다.
- `keywordType`, `useYn`, `statuses`는 허용값 외 입력을 보정·무시한다. 500 방지보다 화면 select 표시와 조회 조건 정합성이 목적이다.
- 설문 관리 `keywordType` `전체`/`설문명`은 동일 SQL(title like)이며 mapper는 변경하지 않았다.
- 설문 이력 `전체` 키워드는 설문명 OR 작성자명으로 동작하며 `SASurveySubmitMapper.xml` 기존 구현을 유지한다.
- 설문 관리 목록에 사용여부 select(전체/사용/미사용)를 추가하고 `useYn`을 model에 넣었다.

## 2026-06-04 관리자 설문 상세 저장 오류

- 사용자는 관리자 설문 상세에서 저장하면 500이 난다고 했다.
- 브라우저에서 `/admin/surveys/update.do`를 직접 재현해 보니 Whitelabel Error Page와 Internal Server Error 500이 떴다.
- 관리자 설문 상세 form은 수정 시 `surveyUid`를 query param으로 보내면서 hidden input도 같은 이름으로 전송한다.
- 수정 저장은 `surveyUid`를 한 번만 전송하도록 정리하는 쪽이 안전하다. 신규 등록은 hidden `surveyUid`를 그대로 유지해야 한다.
- 이번 수정은 화면 마크업이 원인이므로, 컨트롤러가 아니라 템플릿부터 고치고 렌더링 회귀를 추가하는 방향이 맞다.
- 템플릿에서 기존 설문일 때만 hidden `surveyUid`를 제거했고, 8081 임시 서버에서 실제 저장 후 `/admin/surveys/list.do`로 302 리다이렉트되는 것을 확인했다.

## 2026-06-05 사용자 게시판 페이징

- 공개 공지사항과 포토 게시판의 현재 페이징은 이전/다음만 보여서, 요청한 `<< < 1 2 3 4 5 6 7 8 9 10 > >>` 형태와 맞지 않는다.
- 공개 목록 페이징은 10개 단위 번호 묶음을 보여 주고, `<<`/`<`는 첫 페이지와 이전 묶음, `>`/`>>`는 다음 묶음과 마지막 페이지를 가리키는 방식이 자연스럽다.
- 공지사항과 포토 게시판은 같은 공개 목록 패턴이므로, 두 템플릿을 같이 바꾸는 쪽이 일관적이다.
- 8081 임시 서버의 공지사항 목록에서 실제로 `<< < 1 2 3 4 5 6 7 > >>`가 렌더되고, 비활성 화살표와 페이지 링크가 분리되는 것을 확인했다.
