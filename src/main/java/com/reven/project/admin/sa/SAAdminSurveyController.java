package com.reven.project.admin.sa;

import com.reven.project.common.web.LenientLocalDateEditor;
import com.reven.project.service.sa.SASurveyService;
import com.reven.project.service.sa.dto.SASurveyDto;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/surveys")
public class SAAdminSurveyController {
    private final SASurveyService surveyService;

    public SAAdminSurveyController(SASurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @InitBinder
    void initBinder(WebDataBinder binder) {
        // SASurveyDto는 화면 form binding을 위해 public field 기반 DTO를 사용하므로 직접 필드 접근을 활성화한다.
        binder.initDirectFieldAccess();
        // 잘못된 날짜 문자열은 BindException 대신 null로 흡수해 목록 진입 실패를 막는다.
        binder.registerCustomEditor(LocalDate.class, new LenientLocalDateEditor());
    }

    /**
     * 설문 관리 목록 화면을 조회한다.
     */
    @GetMapping("/list.do")
    public String list(@ModelAttribute SASurveyDto.SurveySearchRequest request, Model model) {
        normalizeSearch(request);
        List<SASurveyDto.SurveyListItem> surveys = surveyService.findAdminSurveys(request);
        model.addAttribute("surveys", surveys);
        model.addAttribute("totalCount", surveys.size());
        model.addAttribute("dateFrom", request.startDate);
        model.addAttribute("dateTo", request.endDate);
        model.addAttribute("keywordType", request.keywordType);
        model.addAttribute("keyword", request.keyword);
        model.addAttribute("useYn", request.useYn == null ? "" : request.useYn);
        return "admin/survey/list";
    }

    /**
     * 설문 관리 검색 조건을 허용값 기준으로 보정한다.
     */
    private void normalizeSearch(SASurveyDto.SurveySearchRequest request) {
        if (request.startDate == null) {
            request.startDate = LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).minusDays(60);
        }
        if (request.endDate == null) {
            request.endDate = LocalDate.now(java.time.ZoneId.of("Asia/Seoul")).plusDays(1);
        }
        if (!"설문명".equals(request.keywordType)) {
            request.keywordType = "전체";
        }
        if (!"Y".equals(request.useYn) && !"N".equals(request.useYn)) {
            request.useYn = null;
        }
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
     * 기존 설문을 복제해 신규 설문 등록 화면에 채워 보여준다.
     */
    @GetMapping("/copy.do")
    public String copy(@RequestParam(required = false) String surveyUid, Model model, RedirectAttributes redirectAttributes) {
        if (surveyUid == null || surveyUid.isBlank()) {
            redirectAttributes.addFlashAttribute("surveySavedMessage", "비정상적인 접근입니다.");
            return "redirect:/admin/surveys/list.do";
        }
        try {
            model.addAttribute("survey", surveyService.copySurveyForm(surveyUid));
            return "admin/survey/detail";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("surveySavedMessage", "비정상적인 접근입니다.");
            return "redirect:/admin/surveys/list.do";
        }
    }

    /**
     * 저장된 설문을 사용자 화면 형태로 미리 본다.
     */
    @GetMapping("/preview.do")
    public String preview(@RequestParam String surveyUid, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("survey", surveyService.findSurvey(surveyUid));
            model.addAttribute("previewMode", true);
            return "client/survey/form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("surveySavedMessage", "비정상적인 접근입니다.");
            return "redirect:/admin/surveys/list.do";
        }
    }

    /**
     * 설문 신규 등록 요청을 저장한다.
     */
    @PostMapping("/insert.do")
    public String insert(
            @Valid @ModelAttribute SASurveyDto.SurveySaveRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return saveSurvey(null, request, bindingResult, model, redirectAttributes);
    }

    /**
     * 설문 수정 요청을 저장한다.
     */
    @PostMapping("/update.do")
    public String update(
            @RequestParam(required = false) String surveyUid,
            @Valid @ModelAttribute SASurveyDto.SurveySaveRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        return saveSurvey(surveyUid, request, bindingResult, model, redirectAttributes);
    }

    /**
     * 설문 삭제 요청을 처리한다.
     */
    @PostMapping("/delete.do")
    public String delete(
            @RequestParam String surveyUid,
            RedirectAttributes redirectAttributes
    ) {
        surveyService.deleteSurvey(surveyUid);
        redirectAttributes.addFlashAttribute("surveySavedMessage", "삭제되었습니다.");
        return "redirect:/admin/surveys/list.do";
    }

    private String saveSurvey(
            String surveyUid,
            SASurveyDto.SurveySaveRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        boolean isNewSurvey = surveyUid == null || surveyUid.isBlank();
        surveyUid = isNewSurvey ? null : surveyUid;
        validateSurveyOptions(request, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("survey", requestToDetail(surveyUid, request));
            model.addAttribute("errors", collectFieldErrors(bindingResult));
            return "admin/survey/detail";
        }
        surveyService.saveSurvey(surveyUid, request);
        redirectAttributes.addFlashAttribute("surveySavedMessage", "저장되었습니다.");
        return "redirect:/admin/surveys/list.do";
    }

    private void validateSurveyOptions(SASurveyDto.SurveySaveRequest request, BindingResult bindingResult) {
        List<SASurveyDto.SurveyFieldSaveRequest> fields = request.fields == null ? List.of() : request.fields;
        for (int index = 0; index < fields.size(); index++) {
            SASurveyDto.SurveyFieldSaveRequest field = fields.get(index);
            if ("subjective".equalsIgnoreCase(field.surveyType)) {
                continue;
            }

            List<String> labels = new ArrayList<>();
            for (SASurveyDto.SurveyOptionSaveRequest option : field.normalizedOptions()) {
                String label = option.optionLabel == null ? "" : option.optionLabel.trim();
                if (label.isBlank()) {
                    continue;
                }
                if (labels.contains(label)) {
                    bindingResult.rejectValue("fields[" + index + "].options", "survey.option.duplicate", "보기 라벨이 중복되었습니다.");
                    break;
                }
                labels.add(label);
            }
            if (labels.isEmpty()) {
                bindingResult.rejectValue("fields[" + index + "].options", "survey.option.required", "객관식 문항에는 보기를 1개 이상 입력해야 합니다.");
            }
        }
    }

    /** 제출된 SurveySaveRequest를 SurveyDetail로 변환해 검증 실패 시 입력값을 화면에 보존한다. */
    private SASurveyDto.SurveyDetail requestToDetail(String surveyUid, SASurveyDto.SurveySaveRequest request) {
        SASurveyDto.SurveyDetail detail = new SASurveyDto.SurveyDetail();
        detail.surveyUid = surveyUid;
        detail.title = request.title;
        detail.description = request.description;
        detail.useYn = request.useYn;

        List<SASurveyDto.SurveyField> fields = new ArrayList<>();
        if (request.fields != null) {
            for (int i = 0; i < request.fields.size(); i++) {
                SASurveyDto.SurveyFieldSaveRequest src = request.fields.get(i);
                SASurveyDto.SurveyField field = new SASurveyDto.SurveyField();
                field.fieldSeq = src.fieldSeq;
                field.fieldKey = src.fieldKey;
                field.label = src.label;
                field.surveyType = src.surveyType;
                field.fieldType = src.fieldType;
                field.requiredYn = src.requiredYn;
                field.sortOrd = i;

                List<SASurveyDto.SurveyOption> options = new ArrayList<>();
                for (SASurveyDto.SurveyOptionSaveRequest opt : src.normalizedOptions()) {
                    SASurveyDto.SurveyOption o = new SASurveyDto.SurveyOption();
                    o.optionSeq = opt.optionSeq;
                    o.optionLabel = opt.optionLabel;
                    o.optionValue = opt.optionValue;
                    o.sortOrd = opt.sortOrd;
                    options.add(o);
                }
                field.options = options;
                fields.add(field);
            }
        }
        detail.fields = fields;

        return detail;
    }

    private Map<String, String> collectFieldErrors(BindingResult bindingResult) {
        Map<String, String> errors = new LinkedHashMap<>();
        bindingResult.getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return errors;
    }
}
