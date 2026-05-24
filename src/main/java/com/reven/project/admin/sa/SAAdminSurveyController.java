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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    @GetMapping
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
    @GetMapping("/new")
    public String newSurvey(Model model) {
        model.addAttribute("survey", surveyService.newSurveyForm());
        return "admin/survey/detail";
    }

    /**
     * 설문 상세/수정 화면을 조회한다.
     */
    @GetMapping("/{surveyUid}")
    public String detail(@PathVariable String surveyUid, Model model) {
        model.addAttribute("survey", surveyService.findSurvey(surveyUid));
        return "admin/survey/detail";
    }

    /**
     * 신규 설문과 문항/보기 정보를 저장한다.
     */
    @PostMapping
    public String create(@Valid @ModelAttribute SADto.SurveySaveRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("survey", surveyService.newSurveyForm());
            return "admin/survey/detail";
        }
        SADto.SurveyDetail saved = surveyService.saveSurvey(null, request);
        return "redirect:/admin/surveys/" + saved.surveyUid;
    }

    /**
     * 기존 설문 마스터와 하위 문항/보기를 수정한다.
     */
    @PostMapping("/{surveyUid}")
    public String update(@PathVariable String surveyUid,
                         @Valid @ModelAttribute SADto.SurveySaveRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("survey", surveyService.findSurvey(surveyUid));
            return "admin/survey/detail";
        }
        SADto.SurveyDetail saved = surveyService.saveSurvey(surveyUid, request);
        return "redirect:/admin/surveys/" + saved.surveyUid;
    }
}
