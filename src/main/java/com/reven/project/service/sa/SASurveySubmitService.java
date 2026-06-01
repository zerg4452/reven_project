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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SASurveySubmitService {
    private final SASurveyService surveyService;
    private final SASurveySubmitMapper submitMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        Map<String, SASurveyDto.AnswerRequest> answersByFieldKey = new LinkedHashMap<>();
        for (SASurveyDto.AnswerRequest source : request.answers) {
            answersByFieldKey.put(source.fieldKey, source);
        }

        // 제출 이력 상세가 설문 수정 후에도 변하지 않도록 현재 문항 정보를 답변 row에 snapshot으로 복사한다.
        for (SASurveyDto.SurveyField field : survey.fields) {
            SASurveyDto.AnswerRequest source = answersByFieldKey.get(field.fieldKey);
            if (source == null) {
                continue;
            }

            List<String> values = source.values == null ? List.of() : source.values;
            SASurveyDto.AnswerInsert answer = new SASurveyDto.AnswerInsert();
            answer.submitSeq = submission.submitSeq;
            answer.fieldSeq = field.fieldSeq;
            answer.fieldKey = field.fieldKey;
            answer.fieldLabel = field.label;
            answer.fieldType = field.fieldType;
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

    private String buildAnswerValue(SASurveyDto.SurveyField field, List<String> values) {
        List<String> normalizedValues = values == null ? List.of() : values;
        if (normalizedValues.isEmpty()) {
            return null;
        }

        String renderType = field.getRenderType();
        if ("checkbox".equals(renderType)) {
            return normalizedValues.stream()
                    .map(value -> resolveOptionLabel(field, value))
                    .collect(Collectors.joining(", "));
        }

        String value = normalizedValues.get(0);
        if ("select".equals(renderType) || "radio".equals(renderType)) {
            return resolveOptionLabel(field, value);
        }
        return value;
    }

    private String buildAnswerJson(SASurveyDto.SurveyField field, List<String> values) {
        if (!"checkbox".equals(field.getRenderType())) {
            return null;
        }
        List<String> normalizedValues = values == null ? List.of() : values;
        if (normalizedValues.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(normalizedValues);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize checkbox answer values.", ex);
        }
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
