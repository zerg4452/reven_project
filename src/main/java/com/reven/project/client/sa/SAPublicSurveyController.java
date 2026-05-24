package com.reven.project.client.sa;

import com.reven.project.service.sa.dto.SADto;
import com.reven.project.service.sa.SASurveyService;
import com.reven.project.service.sa.SASurveySubmitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Map;

@Controller
public class SAPublicSurveyController {
    private final SASurveyService surveyService;
    private final SASurveySubmitService submitService;

    public SAPublicSurveyController(SASurveyService surveyService, SASurveySubmitService submitService) {
        this.surveyService = surveyService;
        this.submitService = submitService;
    }

    /**
     * 사용자가 접수 가능한 설문 목록을 조회한다.
     */
    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("surveys", surveyService.findPublicSurveys());
        return "client/survey/list";
    }

    /**
     * 사용자가 선택한 설문 작성 화면을 조회한다.
     */
    @GetMapping("/surveys/{surveyUid}")
    public String write(@PathVariable String surveyUid, Model model) {
        model.addAttribute("survey", surveyService.findSurvey(surveyUid));
        return "client/survey/form";
    }

    /**
     * 사용자 제출 form 값을 설문 답변 DTO로 변환한 뒤 제출 이력으로 저장한다.
     */
    @PostMapping("/surveys/{surveyUid}/submit")
    public String submit(@PathVariable String surveyUid,
                                             @RequestParam Map<String, String> params,
                                             HttpServletRequest servletRequest) {
        SADto.SurveySubmitRequest request = new SADto.SurveySubmitRequest();
        request.submitterName = params.get("submitterName");
        request.phone = params.get("phone");
        request.email = params.get("email");
        request.answers = new ArrayList<>();
        // 동적 문항은 answers[fieldKey] 형태로 넘어오므로 fieldKey를 잘라 답변 목록으로 평탄화한다.
        params.forEach((key, value) -> {
            if (key.startsWith("answers[")) {
                SADto.AnswerRequest answer = new SADto.AnswerRequest();
                answer.fieldKey = key.substring("answers[".length(), key.length() - 1);
                answer.answerValue = value;
                request.answers.add(answer);
            }
        });
        submitService.submit(surveyUid, request, servletRequest.getRemoteAddr());
        return "redirect:/surveys/thanks";
    }

    /**
     * 설문 제출 완료 안내 화면을 표시한다.
     */
    @GetMapping("/surveys/thanks")
    public String thanks() {
        return "client/survey/thanks";
    }
}
