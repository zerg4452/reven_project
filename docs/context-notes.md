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

## 2026-06-06 설문 P7 복사 기능

- 복사 동작은 즉시 DB INSERT가 아니라 신규 등록 화면 prefill 방식으로 결정했다. 운영자가 검토·수정 후 직접 저장한다.
- 복제본 모든 seq(surveySeq/fieldSeq/optionSeq)를 null로 두는 것이 핵심이다. detail.html이 `surveySeq == null`이면 등록 모드로 렌더되고 기존 `insert.do` 저장 흐름이 전부 신규 INSERT로 처리한다. mapper/스키마 변경이 없다.
- 복사 버튼은 설문 관리 목록 행 액션에만 두고, 상세 화면에는 추가하지 않았다.
- 복제본 기본값은 제목 `원본 + ' 사본'`, 사용여부 미사용(N)으로 고정했다. 제목 중복 검사는 하지 않는다.
- 잘못된 surveyUid는 preview.do와 동일하게 목록 리다이렉트 + 비정상 접근 알림으로 처리했다.
- 보기 값(optionValue)은 관리 화면 폼이 라벨만 전송하므로 저장 시 라벨에서 재생성된다. 따라서 복사뿐 아니라 일반 수정 저장에서도 value는 항상 label과 같아진다. distinct optionValue 소비처가 없어 label 기반 정책으로 명시했다.

## 2026-06-06 설문 P7 리뷰 반영

- P7 diff에 섞여 있던 공개 설문 이메일 문항 형식 검증은 복사 기능과 무관한 동작 변경이라 제거했다.
- `/admin/surveys/copy.do`는 `surveyUid`가 빠져도 400으로 끝나지 않게 하고, null/blank를 비정상 접근 알림 후 목록 이동으로 처리한다.
- 설문 목록의 `관리` 컬럼은 프로젝트 규칙의 목록 컬럼과 형식상 차이가 있지만, 기능 이상은 아니고 복사 버튼 노출 요구를 만족하는 UI라 이번 수정 범위에서는 유지한다.
- 최신 focused 검증은 `SASurveySubmitServiceTest`, `SAAdminSurveyControllerTest`, `SASurveyServiceTest`, `SASurveyCopyViewTest`로 통과했다.

## 2026-06-07 설문 P8 리뷰 수정

- P8 통계 집계는 현재 설문 정의의 `field_seq`와 현재 보기 테이블을 기준으로 하면 안 된다.
- 현재 설문 저장 정책은 수정 시 문항과 보기를 삭제 후 재생성하므로, 과거 제출 답변의 `field_seq`와 현재 문항의 `field_seq`가 달라질 수 있다.
- 통계 문항은 제출 당시 답변 스냅샷의 `field_key_snapshot`, `field_label_snapshot`, `field_type_snapshot`, `answer_value`, `answer_json`, `sort_ord`를 기준으로 만든다.
- 구현은 현재 문항을 0건 보기 표시용으로 먼저 넣고, 제출 스냅샷에만 남은 삭제 문항은 `selectStatisticFields` 결과로 추가 병합한다.
- 문항별 객관식 빈도는 `fieldSeq` 대신 `fieldKey`로 `selectObjectiveOptionFrequencies`를 조회한다.
- 문항별 주관식 최근 답변은 `fieldSeq` 대신 `fieldKey`로 `selectRecentTextAnswers`를 조회한다.
- 체크박스 라벨에 쉼표가 들어가는 경우는 `answer_json`을 SQL에서 펼쳐 집계하므로 더 이상 `answer_value` 쉼표 분해에 의존하지 않는다.
- P9의 더 자세한 보기 스냅샷 저장 전까지는 현재 보기 매핑에 없는 과거 값이 JSON raw 값으로 표시될 수 있다.
- 회귀 테스트는 설문 수정 후 `fieldSeq`가 달라져도 과거 객관식 답변이 남는 경우, 삭제된 문항 스냅샷 유지, 체크박스 JSON 값 라벨 매핑, 주관식 최근 답변 20건 제한, 통계 화면 빈 상태를 다룬다.
- MyBatis 통합 테스트는 실제 `sa_survey_submit_mst`, `sa_survey_answer_dtl` row를 넣고 `selectStatisticFields`, `selectObjectiveOptionFrequencies`, `selectRecentTextAnswers`가 `field_key_snapshot` 기준으로 읽는지 검증한다.
- 추가 리뷰에서 체크박스 `answer_value` 쉼표 분해가 콤마 포함 보기 라벨을 오집계할 수 있음을 확인했다.
- 체크박스 통계는 `answer_json` 배열 값을 SQL에서 펼쳐 집계하고, 서비스에서 현재 보기 `optionValue -> optionLabel` 매핑으로 표시 라벨을 복원한다. 매핑이 없으면 JSON 값을 그대로 표시한다.
- 객관식 빈도는 SQL `group by`, 주관식 최근 답변은 SQL `limit 20`으로 처리해 대량 제출 설문에서 문항별 전건을 Java 메모리에 적재하지 않게 한다.

## 2026-06-07 설문 P9 제출 스냅샷 보강 설계

- P9 범위는 사용자 확정으로 `required_yn` 실제값 기록과 `survey_type_snapshot` 추가 두 가지로 한정했다. 옵션셋 스냅샷, 무응답 문항 기록, survey description 스냅샷은 비범위다.
- 현재 `insertAnswer`는 `required_yn_snapshot`에 리터럴 `'N'`을 넣어 제출 시점 필수 여부가 보존되지 않는다. 이 컬럼은 아직 읽는 곳이 없어 영향은 전방 데이터 수집에 한정된다.
- 답변 row에 survey_type 스냅샷 컬럼이 없어 P8 통계가 `field_type`에서 객관식/주관식을 파생한다. 제출 시점 유형을 권위 데이터로 남기기 위해 `survey_type_snapshot`을 추가한다.
- `survey_type_snapshot`은 NULL 허용으로 추가한다. `schema.sql`은 매 부팅마다 실행되므로 비멱등 백필 UPDATE를 피하고, 기존 row는 NULL로 두고 `selectStatisticFields`에서 `field_type_snapshot` 기반 COALESCE 파생으로 읽는다.
- `required_yn` 레거시 값은 복구 불가하다. 기존 row는 `'N'`으로 남고 신규 제출부터 정확히 기록한다.
- 이력 상세 화면은 현재 required/survey_type을 표시하지 않으므로 P9는 수집·보존만 한다. 화면 노출은 향후 작업이다.
- 설계서는 `docs/clear/survey/2026-06-07-survey-submission-snapshot-p9-design.md`로 옮겼다.
- 구현 완료. `survey_type_snapshot`(NULL) 컬럼 추가, `insertAnswer` 실제 `required_yn`/`survey_type` 저장, `selectStatisticFields` COALESCE 파생, 제출 서비스 `resolveSurveyType` 추가로 답변 스냅샷을 채웠다. 제출/통계 회귀와 `./gradlew test`가 통과했다.
- 코드 리뷰 반영. `AnswerSnapshotMap`에 추가한 `survey_type`/`required_yn` 매핑이 `selectSubmissionAnswers`에서 미선택이라 dead였던 문제를, 해당 쿼리 SELECT에 두 컬럼을 추가해 살렸다(이력 상세 화면 표시는 여전히 향후). 통계 통합 테스트에 객관식 레거시(`field_type=radio`, 스냅샷 NULL) → `objective` 파생 케이스를 추가했다.
- `resolveSurveyType`(제출)·`defaultSurveyType`(통계)·SQL CASE 세 곳이 같은 버킷 규칙을 중복하지만, 호출처 3곳이라 지금은 공통화하지 않는다. 네 번째가 생기면 공통 헬퍼로 추출한다.

## 2026-06-07 설문 P10 기간 관리 구현

- 완료 설계서는 `docs/clear/survey/2026-06-07-survey-period-p10-design.md`에 둔다.
- P10은 설문 마스터에 `start_date`/`end_date`를 추가해 날짜 단위 접수 기간을 관리한다. 두 컬럼은 NULL 허용이며, NULL은 해당 방향 기간 제한 없음으로 본다.
- 날짜 판단은 DB `current_date()`가 아니라 서비스와 DTO가 `Asia/Seoul` 기준 오늘을 사용한다. 공개 조회 mapper에는 `today`를 bind 파라미터로 넘긴다.
- 공개 설문 목록은 삭제되지 않은 설문을 모두 카드로 보여주고, 접수중/예정/마감 상태를 badge로 표시한다. `작성하기` 링크는 접수중 설문만 노출한다.
- 사용자 메인 요약, 상세 진입, 제출 저장은 접수중 설문만 허용한다. 상세 GET과 제출 POST를 둘 다 막아 오래 열린 form이나 직접 POST 저장을 차단한다.
- `use_yn`은 수동 사용 여부로 유지한다. `use_yn='N'`이면 기간과 무관하게 접수 불가이고, 관리자 목록의 사용여부 컬럼 의미도 바꾸지 않는다.
- 관리자 미리보기는 기간과 무관하게 계속 허용한다. P10에서는 기간을 제출 이력 스냅샷에 저장하지 않고, 기간 검색이나 예약 배치도 비범위다.
- 설문 복사는 원본 기간을 복사하지만 복사본은 기존 정책대로 `useYn = N`이므로 즉시 공개되지 않는다.
- 회귀 테스트는 저장/수정/복사 기간 전달, 관리자 기간 역전 검증, 공개 상세·제출 차단, 공개 카드/메인 mapper 조건, 템플릿 분기를 포함한다.
- focused 테스트와 `./gradlew test`가 모두 통과했다.
- 코드 리뷰 반영. mapper 통합 테스트가 고정 `today`와 DTO의 실제 오늘 getter를 섞어 날짜가 지나면 실패할 수 있던 점을 고쳤고, `startDate = NULL`, `endDate = NULL`인 기존 설문이 접수중으로 조회되는 호환 케이스를 추가했다. 공개 설문 목록 문구도 비활성 카드를 포함하는 실제 정책에 맞춰 `설문 목록`으로 정리했다.

## 2026-06-07 코드 리팩토링 중복 제거 (R1·R2)

- 리팩토링 후보 조사에서 두 가지 중복을 정리했다. 동작 보존 리팩토링이라 새 테스트는 추가하지 않고 기존 회귀로 검증했다.
- R1. `BDNoticeService`·`BDPhotoBoardService`에 복붙돼 있던 파일 디스크 저장·경로 해석·커밋 후 삭제 로직을 `service/bd/support/BDFileStorageSupport`로 추출했다. `rootPath`가 서비스마다 달라 Spring 빈이 아니라 서비스별 인스턴스로 생성한다. 허용 확장자·MIME 검증은 서비스마다 달라 그대로 두고, 디스크 기록(`writeToDisk`)·경로 해석·삭제 스케줄링(`StoredFileRef` 기반)만 공통화했다. 두 서비스 합쳐 약 150줄 감소.
- R2. 4개 서비스(`BDNotice`/`BDPhotoBoard`/`BDAiNews`/`COAdminMenu`)에 바이트 단위로 동일하던 `firstText(String...)`를 `common/util/TextUtils`로 추출하고 static import로 전환했다. 호출처 40곳은 수정하지 않았다.
- R3 보류. `SASurveyDto`(531줄)는 nested static DTO 약 20개 컨테이너다. 분리해도 로직 이득이 없고 import·참조 경로 churn만 커서 분리하지 않기로 했다. 한 도메인 DTO가 한 파일에 모여 탐색이 오히려 쉽다.
- R4 액션 없음. `BDNoticeService.normalizedAdminSearch`(public)는 `BDNoticeAdminController`가 호출하는 살아 있는 API라 private `normalizeAdminSearch`와의 thin wrapper 쌍이 의도적이다. 정리 대상 아니다.
- 후속 후보. `COAdminManagementService`의 `firstText(String)`·`firstText(String, String)` 오버로드는 시그니처가 달라 이번 범위에 넣지 않았다. 의미가 같으면 향후 `TextUtils.firstText`로 흡수할 수 있다.
- 계획서는 완료 후 `docs/clear/refactor/2026-06-07-bd-refactor-dedup-plan.md`로 옮겼다. `./gradlew test` 전체 통과(150 tests).
- 코드 리뷰. 별도 리뷰어 검토 Critical/Important 0건·머지 가능 판정. 추출 헬퍼 byte-identical, `StoredFileRef` 인자 순서·`COAdminManagementService.firstText`(trim 오버로드) 함정 모두 정상 처리 확인. `refactor/bd-dedup` → main fast-forward 머지.
- 후속 테스트 갭 2건을 별도 태스크로 분리. `BDNoticeServiceTest`는 파일 로직을 직접 커버하지 않고(공유 헬퍼 통해 `BDPhotoBoardServiceTest`가 간접 커버), `resolveStoredFilePath` 경로 탈출 음성 테스트가 없다. 둘 다 이번 리팩토링이 만든 게 아닌 기존 갭이다. 계획서 Context/Verification의 안전망 문구도 이 사실에 맞게 정정했다.
