# 설문 제출 이력 상태 변경 + 관리자 메모 저장 설계

작성일: 2026-06-02  
검토: 멀티 에이전트 리뷰 반영 (보수 + 실용 관점 절충)

## 목표

관리자가 설문 이력 상세 화면에서 제출 상태를 변경하고 메모를 남길 수 있도록 한다.

## 현재 상태

- `sa_survey_submit_mst.status`, `admin_memo` 컬럼 존재
- `SubmissionDetail` DTO에 `status`, `adminMemo` 필드 존재 (조회 전용)
- `selectSubmission` 쿼리에서 두 컬럼 SELECT 중
- `history-detail.html`: 읽기 전용, 저장 UI 없음
- Controller: GET 엔드포인트만 있음
- Service/Mapper: update 메서드 없음
- `SubmissionDetail.getStatusText()` 없어 기존 템플릿 `statusText` 표현식이 null 반환 (기존 버그)

## 가정

- 상태 목록은 컨트롤러의 `statusOptions()` 기준 고정 5개: new, reviewing, contacted, done, hold
- 저장 성공 시 PRG 패턴으로 같은 상세 URL로 리다이렉트
- 저장 실패(유효성 오류) 시 상세 화면 재렌더링 + 오류 메시지 표시
- `admin_memo`는 최대 2000자 (서버 검증)
- `mod_id`는 기존 코드베이스 패턴에 따라 'system'으로 고정
- 관리자 전용 화면이므로 별도 권한 분기 없음
- 동시 편집 충돌 처리 없음 (저트래픽 관리 도구, 나중에 필요 시 P9 이후 고려)

## 작업 범위

### 1. DTO (`SASurveyDto.java`)

**`SubmissionUpdateRequest` 추가 — 상태와 메모 저장 요청.**

```java
@Getter
@Setter
public static class SubmissionUpdateRequest {
    @NotBlank
    public String status;
    @Size(max = 2000)
    public String adminMemo;
}
```

허용 상태값 검증은 서비스 레이어에서 처리 (아래 참조).

**`SubmissionDetail`에 `getStatusText()` 추가 — 기존 템플릿 표현식 `statusText` 수정.**

`SubmissionListItem.getStatusText()`와 동일 로직.

### 2. Mapper 인터페이스 (`SASurveySubmitMapper.java`)

```java
void updateSubmission(@Param("submitUid") String submitUid,
                      @Param("status") String status,
                      @Param("adminMemo") String adminMemo);
```

### 3. Mapper XML (`SASurveySubmitMapper.xml`)

```xml
<update id="updateSubmission">
    update sa_survey_submit_mst
    set status     = #{status},
        admin_memo = #{adminMemo},
        mod_dtm    = now(),
        mod_id     = 'system'
    where submit_uid = #{submitUid}
</update>
```

### 4. Service (`SASurveySubmitService.java`)

```java
private static final Set<String> ALLOWED_STATUSES =
    Set.of("new", "reviewing", "contacted", "done", "hold");

@Transactional
public void updateSubmission(String submitUid, SASurveyDto.SubmissionUpdateRequest request) {
    if (!ALLOWED_STATUSES.contains(request.status)) {
        throw new IllegalArgumentException("허용되지 않는 상태값: " + request.status);
    }
    findSubmission(submitUid); // 존재 확인 (없으면 IllegalArgumentException)
    submitMapper.updateSubmission(submitUid, request.status, request.adminMemo);
}
```

### 5. Controller (`SAAdminSurveySubmissionController.java`)

기존 import에 `PostMapping`, `Valid`, `BindingResult` 추가.

```java
@PostMapping("/admin/survey-submissions/update.do")
public String update(
        @RequestParam String submitUid,
        @Valid @ModelAttribute SASurveyDto.SubmissionUpdateRequest request,
        BindingResult bindingResult,
        Model model
) {
    if (bindingResult.hasErrors()) {
        model.addAttribute("submission", submitService.findSubmission(submitUid));
        model.addAttribute("updateErrors", bindingResult);
        return "admin/survey/history-detail";
    }
    submitService.updateSubmission(submitUid, request);
    return "redirect:/admin/survey-submissions/detail.do?submitUid=" + submitUid;
}
```

### 6. UI (`history-detail.html`)

기존 읽기 전용 패널(설문명/제출자/상태/날짜) 유지. 답변 섹션 아래에 관리 폼 추가. 기존 `detail-action-bar`를 폼 안으로 이동.

```html
<form class="form-stack" method="post"
      th:action="@{/admin/survey-submissions/update.do(submitUid=${submission.submitUid})}">
    <input type="hidden" th:if="${_csrf != null}" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
    <section class="content-panel">
        <div class="panel-head"><h2>관리</h2></div>
        <!-- 오류 메시지 -->
        <div th:if="${updateErrors != null}" class="form-error-summary">
            <span th:each="err : ${updateErrors.allErrors}" th:text="${err.defaultMessage}"></span>
        </div>
        <label>상태
            <select class="form-control" name="status">
                <option value="new"       th:selected="${submission.status == 'new'}">신규</option>
                <option value="reviewing" th:selected="${submission.status == 'reviewing'}">확인중</option>
                <option value="contacted" th:selected="${submission.status == 'contacted'}">연락완료</option>
                <option value="done"      th:selected="${submission.status == 'done'}">처리완료</option>
                <option value="hold"      th:selected="${submission.status == 'hold'}">보류</option>
            </select>
        </label>
        <label>메모
            <textarea class="form-control" name="adminMemo" rows="4"
                      maxlength="2000" th:text="${submission.adminMemo}"></textarea>
        </label>
    </section>
    <div class="detail-action-bar">
        <a class="btn btn-neutral" th:href="@{/admin/survey-submissions/list.do}">목록</a>
        <button class="btn btn-edit" type="submit">저장</button>
    </div>
</form>
```

## 검증 기준

- 관리자 이력 상세 화면에서 상태 드롭다운과 메모 textarea가 표시된다.
- 상태 badge가 현재 status 기준으로 한글 텍스트를 올바르게 표시한다.
- 저장 버튼 클릭 시 상태와 메모가 DB에 반영된다.
- 저장 후 같은 상세 화면으로 리다이렉트된다 (PRG).
- 상태 빈 값 제출 시 폼 오류 메시지가 표시된다.
- 메모 2000자 초과 시 서버 유효성 오류 메시지가 표시된다.
- 허용되지 않는 상태값 제출 시 서버에서 거부된다.

## 비범위

- 상태 변경 이력(audit log) — P9 이후 고려
- 이메일/알림 발송
- 이력 목록 인라인 상태 변경 — P6 강화 범위
