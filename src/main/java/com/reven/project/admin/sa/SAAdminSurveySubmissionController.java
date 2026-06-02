package com.reven.project.admin.sa;

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.SASurveyCsvService;
import com.reven.project.service.sa.SASurveySubmitService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class SAAdminSurveySubmissionController {
    private final SASurveySubmitService submitService;
    private final SASurveyCsvService csvService;

    public SAAdminSurveySubmissionController(SASurveySubmitService submitService, SASurveyCsvService csvService) {
        this.submitService = submitService;
        this.csvService = csvService;
    }

    @InitBinder
    void initBinder(WebDataBinder binder) {
        // 검색 DTO가 public field 기반이라 Spring MVC가 setter 없이 값을 주입하도록 설정한다.
        binder.initDirectFieldAccess();
    }

    /**
     * 설문 이력 관리 목록 화면을 조회한다.
     */
    @GetMapping("/admin/survey-submissions/list.do")
    public String list(@ModelAttribute SASurveyDto.SubmissionSearchRequest request, Model model) {
        SASurveyDto.SubmissionSearchRequest normalized = normalizeSearch(request);
        List<SASurveyDto.SubmissionListItem> submissions = submitService.findSubmissions(normalized);
        model.addAttribute("submissions", submissions);
        model.addAttribute("totalCount", submissions.size());
        model.addAttribute("dateFrom", normalized.startDate);
        model.addAttribute("dateTo", normalized.endDate);
        model.addAttribute("keywordType", normalized.keywordType);
        model.addAttribute("keyword", normalized.keyword);
        model.addAttribute("statuses", normalized.statuses);
        model.addAttribute("statusOptions", statusOptions());
        return "admin/survey/history-list";
    }

    /**
     * 제출 당시 저장된 snapshot 기준으로 설문 이력 상세를 조회한다.
     */
    @GetMapping("/admin/survey-submissions/detail.do")
    public String detail(@RequestParam String submitUid, Model model) {
        model.addAttribute("submission", submitService.findSubmission(submitUid));
        return "admin/survey/history-detail";
    }

    /**
     * 설문 이력의 상태와 관리자 메모를 저장한다.
     */
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

    /**
     * 현재 검색 조건에 맞는 설문 이력을 CSV 파일로 내려준다.
     */
    @GetMapping("/admin/survey-submissions/download.do")
    public ResponseEntity<ByteArrayResource> csv(@ModelAttribute SASurveyDto.SubmissionSearchRequest request) {
        byte[] csv = csvService.createSubmissionCsv(normalizeSearch(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"survey-submissions.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .contentLength(csv.length)
                .body(new ByteArrayResource(csv));
    }

    /**
     * 설문 이력 검색 조건의 날짜/상태 기본값을 보정한다.
     */
    private SASurveyDto.SubmissionSearchRequest normalizeSearch(SASurveyDto.SubmissionSearchRequest request) {
        List<String> statuses = request.statuses == null || request.statuses.isEmpty()
                ? statusOptions().stream().map(SASurveyDto.SubmissionStatusOption::getCode).toList()
                : request.statuses;
        SASurveyDto.SubmissionSearchRequest normalized = new SASurveyDto.SubmissionSearchRequest();
        normalized.startDate = request.startDate;
        normalized.endDate = request.endDate;
        normalized.keywordType = request.keywordType;
        normalized.keyword = request.keyword;
        normalized.statuses = statuses;
        return normalized;
    }

    /**
     * 설문 이력 상태 체크박스 옵션을 반환한다.
     */
    private List<SASurveyDto.SubmissionStatusOption> statusOptions() {
        return List.of(
                statusOption("new", "신규"),
                statusOption("reviewing", "확인중"),
                statusOption("contacted", "연락완료"),
                statusOption("done", "처리완료"),
                statusOption("hold", "보류")
        );
    }

    private SASurveyDto.SubmissionStatusOption statusOption(String code, String label) {
        SASurveyDto.SubmissionStatusOption option = new SASurveyDto.SubmissionStatusOption();
        option.code = code;
        option.label = label;
        return option;
    }
}
