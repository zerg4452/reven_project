package com.reven.project.client.sa;

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.SASurveyService;
import com.reven.project.service.sa.SASurveySubmitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    @GetMapping("/surveys/list.do")
    public String list(Model model) {
        model.addAttribute("surveys", surveyService.findPublicSurveyCards());
        return "client/survey/list";
    }

    /**
     * 사용자가 선택한 설문 작성 화면을 조회한다.
     */
    @GetMapping({"/surveys/detail.do", "/surveys/write.do"})
    public String detail(@RequestParam String surveyUid, Model model) {
        var survey = surveyService.findSurvey(surveyUid);
        if (!survey.isEnabled()) {
            return "redirect:/surveys/list.do";
        }
        model.addAttribute("survey", survey);
        return "client/survey/form";
    }

    /**
     * 사용자 제출 form 값을 설문 답변 DTO로 변환한 뒤 제출 이력으로 저장한다.
     */
    @PostMapping("/surveys/submit.do")
    public String submit(@RequestParam String surveyUid,
                         @RequestParam MultiValueMap<String, String> params,
                         HttpServletRequest servletRequest) {
        SASurveyDto.SurveySubmitRequest request = new SASurveyDto.SurveySubmitRequest();
        request.submitterName = params.getFirst("submitterName");
        request.phone = params.getFirst("phone");
        request.email = params.getFirst("email");
        request.answers = new ArrayList<>();
        Map<String, List<String>> groupedAnswers = new LinkedHashMap<>();
        // 동적 문항은 answers[fieldKey] 형태로 넘어오므로 fieldKey 기준으로 여러 값을 보존해 평탄화한다.
        params.forEach((key, values) -> {
            if (!key.startsWith("answers[")) {
                return;
            }
            String fieldKey = key.substring("answers[".length(), key.length() - 1);
            groupedAnswers.put(fieldKey, new ArrayList<>(values));
        });
        groupedAnswers.forEach((fieldKey, values) -> {
            SASurveyDto.AnswerRequest answer = new SASurveyDto.AnswerRequest();
            answer.fieldKey = fieldKey;
            answer.values = values;
            request.answers.add(answer);
        });
        submitService.submit(surveyUid, request, servletRequest.getRemoteAddr());
        return "redirect:/surveys/thanks.do";
    }

    /**
     * 설문 제출 완료 안내 화면을 표시한다.
     */
    @GetMapping("/surveys/thanks.do")
    public String thanks() {
        return "client/survey/thanks";
    }
}
