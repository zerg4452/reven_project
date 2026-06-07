package com.reven.project.service.sa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveySubmitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SASurveySubmitService {
    private static final Set<String> ALLOWED_STATUSES =
            Set.of("new", "reviewing", "contacted", "done", "hold");

    private final SASurveyService surveyService;
    private final SASurveySubmitMapper submitMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class SubmissionValidationException extends RuntimeException {
        private final Map<String, String> errors;

        public SubmissionValidationException(Map<String, String> errors) {
            super("Survey submission validation failed");
            this.errors = Map.copyOf(errors);
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }

    public SASurveySubmitService(SASurveyService surveyService, SASurveySubmitMapper submitMapper) {
        this.surveyService = surveyService;
        this.submitMapper = submitMapper;
    }

    /**
     * 사용자 설문 제출을 저장하고 제출 UID를 반환한다.
     */
    @Transactional
    public SASurveyDto.SurveySubmitResponse submit(String surveyUid, SASurveyDto.SurveySubmitRequest request, String ip) {
        SASurveyDto.SurveyDetail survey = surveyService.findSurvey(surveyUid);
        Map<String, List<String>> normalizedAnswersByFieldKey = normalizeAnswers(request);
        Map<String, String> errors = validateSubmission(survey, normalizedAnswersByFieldKey);
        if (!errors.isEmpty()) {
            throw new SubmissionValidationException(errors);
        }

        SASurveyDto.SubmitInsert submission = new SASurveyDto.SubmitInsert();
        submission.surveySeq = survey.surveySeq;
        submission.surveyUid = survey.surveyUid;
        submission.submitUid = UUID.randomUUID().toString().replace("-", "");
        submission.surveyTitle = survey.title;
        submission.submitterName = request.submitterName;
        submission.phone = request.phone;
        submission.email = request.email;
        submission.ip = ip;
        submitMapper.insertSubmission(submission);

        // 제출 이력 상세가 설문 수정 후에도 변하지 않도록 현재 문항 정보를 답변 row에 snapshot으로 복사한다.
        for (SASurveyDto.SurveyField field : survey.fields) {
            List<String> values = normalizedAnswersByFieldKey.getOrDefault(field.fieldKey, List.of());
            if (values.isEmpty()) {
                continue;
            }
            SASurveyDto.AnswerInsert answer = new SASurveyDto.AnswerInsert();
            answer.submitSeq = submission.submitSeq;
            answer.fieldSeq = field.fieldSeq;
            answer.fieldKey = field.fieldKey;
            answer.fieldLabel = field.label;
            answer.fieldType = field.fieldType;
            answer.surveyType = resolveSurveyType(field);
            answer.requiredYn = field.isRequired() ? "Y" : "N";
            answer.answerValue = buildAnswerValue(field, values);
            answer.answerJson = buildAnswerJson(field, values);
            answer.sortOrd = field.sortOrd;
            submitMapper.insertAnswer(answer);
        }
        return new SASurveyDto.SurveySubmitResponse(submission.submitUid);
    }

    /**
     * 관리자 설문 이력 목록을 검색 조건으로 조회한다.
     */
    public List<SASurveyDto.SubmissionListItem> findSubmissions(SASurveyDto.SubmissionSearchRequest request) {
        return submitMapper.selectSubmissionList(request);
    }

    /**
     * 제출 UID 기준으로 설문 이력 상세와 답변 snapshot을 조회한다.
     */
    public SASurveyDto.SubmissionDetail findSubmission(String submitUid) {
        SASurveyDto.SubmissionDetail detail = submitMapper.selectSubmission(submitUid);
        if (detail == null) {
            throw new IllegalArgumentException("Survey submission not found: " + submitUid);
        }
        detail.answers = submitMapper.selectSubmissionAnswers(detail.submitSeq);
        return detail;
    }

    /**
     * 설문 이력의 상태와 관리자 메모를 변경한다.
     */
    @Transactional
    public void updateSubmission(String submitUid, SASurveyDto.SubmissionUpdateRequest request) {
        if (!ALLOWED_STATUSES.contains(request.status)) {
            throw new IllegalArgumentException("허용되지 않는 상태값: " + request.status);
        }
        findSubmission(submitUid);
        submitMapper.updateSubmission(submitUid, request.status, request.adminMemo);
    }

    private Map<String, List<String>> normalizeAnswers(SASurveyDto.SurveySubmitRequest request) {
        Map<String, List<String>> answersByFieldKey = new LinkedHashMap<>();
        List<SASurveyDto.AnswerRequest> answers = request.answers == null ? List.of() : request.answers;
        for (SASurveyDto.AnswerRequest source : answers) {
            answersByFieldKey.put(source.fieldKey, normalizeValues(source.values));
        }
        return answersByFieldKey;
    }

    private Map<String, String> validateSubmission(SASurveyDto.SurveyDetail survey, Map<String, List<String>> answersByFieldKey) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (SASurveyDto.SurveyField field : survey.fields) {
            List<String> values = answersByFieldKey.getOrDefault(field.fieldKey, List.of());
            if (field.isRequired() && values.isEmpty()) {
                errors.put(field.fieldKey, "필수 문항에 응답해 주세요.");
            }
        }
        return errors;
    }

    private List<String> normalizeValues(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String buildAnswerValue(SASurveyDto.SurveyField field, List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        String renderType = field.getRenderType();
        if ("checkbox".equals(renderType)) {
            return values.stream()
                    .map(value -> resolveOptionLabel(field, value))
                    .collect(Collectors.joining(", "));
        }

        String value = values.get(0);
        if ("select".equals(renderType) || "radio".equals(renderType)) {
            return resolveOptionLabel(field, value);
        }
        return value;
    }

    private String buildAnswerJson(SASurveyDto.SurveyField field, List<String> values) {
        if (!"checkbox".equals(field.getRenderType())) {
            return null;
        }
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize checkbox answer values.", ex);
        }
    }

    private String resolveSurveyType(SASurveyDto.SurveyField field) {
        if (field.surveyType != null && !field.surveyType.isBlank()) {
            return field.surveyType;
        }
        String normalizedFieldType = field.fieldType == null ? "" : field.fieldType.toLowerCase();
        return switch (normalizedFieldType) {
            case "select", "radio", "checkbox" -> "objective";
            default -> "subjective";
        };
    }

    private String resolveOptionLabel(SASurveyDto.SurveyField field, String value) {
        for (SASurveyDto.SurveyOption option : field.options) {
            if (Objects.equals(option.optionValue, value)) {
                return option.optionLabel;
            }
        }
        return value;
    }
}
