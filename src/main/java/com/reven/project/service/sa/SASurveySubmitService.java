package com.reven.project.service.sa;

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveySubmitMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SASurveySubmitService {
    private final SASurveyService surveyService;
    private final SASurveySubmitMapper submitMapper;

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

        // 제출 이력 상세가 설문 수정 후에도 변하지 않도록 현재 문항 정보를 답변 row에 snapshot으로 복사한다.
        Map<String, SASurveyDto.SurveyField> fieldsByKey = new LinkedHashMap<>();
        for (SASurveyDto.SurveyField field : survey.fields) {
            fieldsByKey.put(field.fieldKey, field);
        }
        for (SASurveyDto.AnswerRequest source : request.answers) {
            SASurveyDto.SurveyField field = fieldsByKey.get(source.fieldKey);
            if (field == null) {
                // 화면에 없는 fieldKey가 넘어온 경우 저장하지 않는다.
                continue;
            }
            SASurveyDto.AnswerInsert answer = new SASurveyDto.AnswerInsert();
            answer.submitSeq = submission.submitSeq;
            answer.fieldSeq = field.fieldSeq;
            answer.fieldKey = field.fieldKey;
            answer.fieldLabel = field.label;
            answer.fieldType = field.fieldType;
            answer.answerValue = source.answerValue;
            answer.answerJson = source.answerJson;
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
}
