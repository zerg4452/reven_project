# 설문 DB 모델 초안

작성일: 2026-05-23

현재 PHP 구현의 `forms.json`, `submissions.json` 구조를 DB 저장 방식으로 바꾸기 위한 초안입니다. 실제 구현 전 컬럼명과 감사 컬럼 규칙은 한 번 더 확정합니다.

## 권장 테이블

### survey

설문 마스터입니다.

- survey_id 또는 survey_seq
- title
- description
- status: active/inactive
- reg_dtm
- reg_id
- mod_dtm
- mod_id

### survey_field

설문 문항입니다.

- field_id 또는 field_seq
- survey_id
- field_key
- label
- field_type: text/textarea/select/radio/checkbox/date/number/email
- required_yn
- sort_ord
- reg_dtm
- reg_id
- mod_dtm
- mod_id

### survey_field_option

선택형 문항의 보기입니다.

- option_id 또는 option_seq
- field_id
- option_label
- option_value
- sort_ord
- reg_dtm
- reg_id
- mod_dtm
- mod_id

### survey_submission

사용자 제출 마스터입니다.

- submission_id 또는 submission_seq
- survey_id
- survey_title_snapshot
- submitter_name
- phone
- email
- birthdate
- address
- status: new/checked/etc
- admin_memo
- submitted_at
- ip
- reg_dtm
- reg_id
- mod_dtm
- mod_id

### survey_answer

제출 답변입니다.

- answer_id 또는 answer_seq
- submission_id
- field_id nullable
- field_key_snapshot
- field_label_snapshot
- field_type_snapshot
- answer_value
- answer_json nullable
- sort_ord
- reg_dtm
- reg_id
- mod_dtm
- mod_id

## checkbox 저장 방식

checkbox 답변은 다중 값이므로 두 가지 중 하나를 선택합니다.

- 단순 구현: `answer_json`에 배열 JSON 저장
- 정규화 구현: `survey_answer_item` 별도 테이블 생성

초기 이관에서는 `answer_json` 방식이 빠르고, 통계/검색 요구가 커지면 별도 테이블을 검토합니다.

## 마이그레이션 메모

- 기존 설문 ID는 문자열 hex 값입니다. 새 DB에서 numeric seq를 쓰더라도 외부 링크 호환을 위해 public id 컬럼을 별도로 두는 것을 권장합니다.
- 제출 이력은 설문 제목/문항 라벨 snapshot을 보존해야 합니다. 설문 수정 후에도 과거 제출 상세가 변하지 않아야 합니다.
- 날짜 표시 규칙은 `yyyy-mm-dd`, timezone은 `Asia/Seoul`을 유지합니다.
