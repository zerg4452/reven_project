package com.reven.project.client.main;

import com.reven.project.service.bd.BDAiNewsService;
import com.reven.project.service.sa.SASurveyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class COMainController {
    private final SASurveyService surveyService;
    private final BDAiNewsService aiNewsService;

    public COMainController(SASurveyService surveyService, BDAiNewsService aiNewsService) {
        this.surveyService = surveyService;
        this.aiNewsService = aiNewsService;
    }

    /**
     * 사용자 메인 화면에 진행중인 설문과 게시 뉴스 최신 글을 표시한다.
     */
    @GetMapping({"/", "/main.do", "/index.do"})
    public String main(Model model) {
        model.addAttribute("surveys", surveyService.findPublicSurveySummaries(3));
        model.addAttribute("newsList", aiNewsService.findPublishedAiNews(3));
        return "client/main/index";
    }
}
