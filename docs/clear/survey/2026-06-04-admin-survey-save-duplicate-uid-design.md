# 관리자 설문 상세 저장 오류 대응 설계

- 대상 화면은 `/admin/surveys/detail.do`의 설문 상세 저장 흐름이다.
- 현상은 기존 설문을 수정 저장할 때 `/admin/surveys/update.do`에서 500이 발생하는 것이다.
- 현재 화면은 수정용 form action에 `surveyUid`를 query param으로 넣고, form body에도 hidden `surveyUid`를 함께 전송한다.
- 수정 저장에서는 `surveyUid`를 한 번만 전송하도록 정리하고, 신규 등록에서만 hidden `surveyUid`를 유지한다.
- 검증은 렌더링 결과에서 update form이 중복 `surveyUid`를 만들지 않는지 확인하고, 실제 저장이 목록으로 302 redirect 되는지 확인한다.
