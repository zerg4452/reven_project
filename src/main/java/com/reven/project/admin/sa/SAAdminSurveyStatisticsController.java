package com.reven.project.admin.sa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reven.project.service.sa.SASurveyService;
import com.reven.project.service.sa.SASurveyStatisticsService;
import com.reven.project.service.sa.dto.SASurveyDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SAAdminSurveyStatisticsController {

    private final SASurveyService surveyService;
    private final SASurveyStatisticsService statisticsService;
    private final ObjectMapper objectMapper;

    public SAAdminSurveyStatisticsController(SASurveyService surveyService,
                                              SASurveyStatisticsService statisticsService,
                                              ObjectMapper objectMapper) {
        this.surveyService = surveyService;
        this.statisticsService = statisticsService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/admin/surveys/{surveyUid}/statistics.do")
    public String statistics(@PathVariable String surveyUid,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        SASurveyDto.SurveyDetail survey;
        try {
            survey = surveyService.findSurvey(surveyUid);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("surveySavedMessage", "존재하지 않는 설문입니다.");
            return "redirect:/admin/surveys/list.do";
        }

        SASurveyDto.SurveyStatistics stats = statisticsService.getStatistics(survey);
        String statsJson;
        try {
            statsJson = objectMapper.writeValueAsString(stats);
        } catch (JsonProcessingException ex) {
            statsJson = "{}";
        }

        model.addAttribute("survey", survey);
        model.addAttribute("stats", stats);
        model.addAttribute("statsJson", statsJson);
        return "admin/survey/statistics";
    }
}
