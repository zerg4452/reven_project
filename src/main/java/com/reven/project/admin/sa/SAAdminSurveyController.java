package com.reven.project.admin.sa;

import com.reven.project.service.sa.dto.SADto;
import com.reven.project.service.sa.SASurveyService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/surveys")
public class SAAdminSurveyController {
    private final SASurveyService surveyService;

    public SAAdminSurveyController(SASurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @InitBinder
    void initBinder(WebDataBinder binder) {
        // SADto는 화면 form binding을 위해 public field 기반 DTO를 사용하므로 직접 필드 접근을 활성화한다.
        binder.initDirectFieldAccess();
    }

    /**
     * 설문 관리 목록 화면을 조회한다.
     */
    @GetMapping("/list.do")
    public String list(@ModelAttribute SADto.SurveySearchRequest request, Model model) {
        List<SADto.SurveyListItem> surveys = surveyService.findAdminSurveys(request);
        model.addAttribute("surveys", surveys);
        model.addAttribute("totalCount", surveys.size());
        model.addAttribute("dateFrom", request.startDate);
        model.addAttribute("dateTo", request.endDate);
        model.addAttribute("keywordType", request.keywordType);
        model.addAttribute("keyword", request.keyword);
        return "admin/survey/list";
    }

    /**
     * 신규 설문 등록 화면에 필요한 기본 DTO를 만든다.
     */
    @GetMapping({"/write.do", "/detail.do"})
    public String writeForm(@RequestParam(required = false) String surveyUid, Model model) {
        surveyUid = surveyUid == null || surveyUid.isBlank() ? null : surveyUid;
        model.addAttribute("survey", surveyUid == null ? surveyService.newSurveyForm() : surveyService.findSurvey(surveyUid));
        return "admin/survey/detail";
    }

    /**
     * 설문 신규 등록 요청을 저장한다.
     */
    @PostMapping("/insert.do")
    public String insert(
            @RequestParam(required = false) String surveyUid,
            @Valid @ModelAttribute SADto.SurveySaveRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        return saveSurvey(null, request, bindingResult, model);
    }

    /**
     * 설문 수정 요청을 저장한다.
     */
    @PostMapping("/update.do")
    public String update(
            @RequestParam(required = false) String surveyUid,
            @Valid @ModelAttribute SADto.SurveySaveRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        return saveSurvey(surveyUid, request, bindingResult, model);
    }

    /**
     * 설문 삭제 요청을 처리한다.
     */
    @PostMapping("/delete.do")
    public String delete(@RequestParam String surveyUid) {
        surveyService.deleteSurvey(surveyUid);
        return "redirect:/admin/surveys/list.do";
    }

    private String saveSurvey(
            String surveyUid,
            SADto.SurveySaveRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        surveyUid = surveyUid == null || surveyUid.isBlank() ? null : surveyUid;
        if (bindingResult.hasErrors()) {
            model.addAttribute("survey", surveyUid == null ? surveyService.newSurveyForm() : surveyService.findSurvey(surveyUid));
            return "admin/survey/detail";
        }
        SADto.SurveyDetail saved = surveyService.saveSurvey(surveyUid, request);
        return "redirect:/admin/surveys/write.do?surveyUid=" + saved.surveyUid;
    }
}
