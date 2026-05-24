package com.reven.project.admin.sa;

import com.reven.project.service.sa.dto.SADto;
import com.reven.project.service.sa.SASurveyCsvService;
import com.reven.project.service.sa.SASurveySubmitService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

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
    @GetMapping("/admin/survey-submissions")
    public String list(@ModelAttribute SADto.SubmissionSearchRequest request, Model model) {
        List<SADto.SubmissionListItem> submissions = submitService.findSubmissions(request);
        model.addAttribute("submissions", submissions);
        model.addAttribute("totalCount", submissions.size());
        model.addAttribute("dateFrom", request.startDate);
        model.addAttribute("dateTo", request.endDate);
        model.addAttribute("keywordType", request.keywordType);
        model.addAttribute("keyword", request.keyword);
        return "admin/survey/history-list";
    }

    /**
     * 제출 당시 저장된 snapshot 기준으로 설문 이력 상세를 조회한다.
     */
    @GetMapping("/admin/survey-submissions/{submitUid}")
    public String detail(@PathVariable String submitUid, Model model) {
        model.addAttribute("submission", submitService.findSubmission(submitUid));
        return "admin/survey/history-detail";
    }

    /**
     * 현재 검색 조건에 맞는 설문 이력을 CSV 파일로 내려준다.
     */
    @GetMapping("/admin/survey-submissions.csv")
    public ResponseEntity<ByteArrayResource> csv(@ModelAttribute SADto.SubmissionSearchRequest request) {
        byte[] csv = csvService.createSubmissionCsv(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"survey-submissions.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .contentLength(csv.length)
                .body(new ByteArrayResource(csv));
    }
}
