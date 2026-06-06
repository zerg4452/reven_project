package com.reven.project.service.sa;

import com.reven.project.service.sa.dto.SASurveyDto;
import com.reven.project.service.sa.mapper.SASurveyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SASurveyService {
    private final SASurveyMapper surveyMapper;

    public SASurveyService(SASurveyMapper surveyMapper) {
        this.surveyMapper = surveyMapper;
    }

    /**
     * 관리자 설문 관리 목록을 검색 조건으로 조회한다.
     */
    public List<SASurveyDto.SurveyListItem> findAdminSurveys(SASurveyDto.SurveySearchRequest request) {
        return surveyMapper.selectSurveyList(request);
    }

    /**
     * 사용자 화면에 노출할 사용 중 설문만 조회한다.
     */
    public List<SASurveyDto.SurveyListItem> findPublicSurveys() {
        return surveyMapper.selectPublicSurveyList();
    }

    /**
     * 사용자 설문 목록 카드에 표시할 설문을 진행/마감 상태와 함께 조회한다.
     */
    public List<SASurveyDto.SurveyListItem> findPublicSurveyCards() {
        return surveyMapper.selectPublicSurveyCardList();
    }

    /**
     * 사용자 메인 화면에 노출할 진행중인 설문 최신 목록을 제한 개수만큼 조회한다.
     */
    public List<SASurveyDto.SurveyListItem> findPublicSurveySummaries(int limit) {
        return surveyMapper.selectPublicSurveySummaryList(Math.max(1, limit));
    }

    /**
     * 설문 마스터와 문항/보기 전체 구조를 조회한다.
     */
    public SASurveyDto.SurveyDetail findSurvey(String surveyUid) {
        SASurveyDto.SurveyDetail survey = surveyMapper.selectSurvey(surveyUid);
        if (survey == null) {
            throw new IllegalArgumentException("Survey not found: " + surveyUid);
        }
        attachChildren(survey);
        return survey;
    }

    /**
     * 설문 등록 화면에 표시할 새 설문 기본값을 만든다.
     */
    public SASurveyDto.SurveyDetail newSurveyForm() {
        SASurveyDto.SurveyDetail survey = new SASurveyDto.SurveyDetail();
        survey.surveyUid = newUid();
        return survey;
    }

    /**
     * 기존 설문 구조를 복제해 신규 설문 등록 화면에 채울 form 데이터를 만든다.
     * 모든 seq를 비우고 새 UID를 부여해 저장 시 신규 설문으로 INSERT되게 한다. DB 쓰기는 하지 않는다.
     * 보기 값(optionValue)은 관리 화면 폼이 보기 라벨만 전송하므로 저장 시 라벨에서 재생성된다.
     * 따라서 복사본 저장 후 optionValue는 항상 optionLabel과 같아진다.
     */
    public SASurveyDto.SurveyDetail copySurveyForm(String surveyUid) {
        SASurveyDto.SurveyDetail source = findSurvey(surveyUid);
        SASurveyDto.SurveyDetail copy = new SASurveyDto.SurveyDetail();
        copy.surveyUid = newUid();
        copy.surveySeq = null;
        copy.title = source.title + " 사본";
        copy.description = source.description;
        copy.useYn = "N";
        for (SASurveyDto.SurveyField sourceField : source.fields) {
            SASurveyDto.SurveyField field = new SASurveyDto.SurveyField();
            field.fieldSeq = null;
            field.surveySeq = null;
            field.fieldKey = sourceField.fieldKey;
            field.label = sourceField.label;
            field.surveyType = sourceField.surveyType;
            field.fieldType = sourceField.fieldType;
            field.requiredYn = sourceField.requiredYn;
            field.sortOrd = sourceField.sortOrd;
            for (SASurveyDto.SurveyOption sourceOption : sourceField.options) {
                SASurveyDto.SurveyOption option = new SASurveyDto.SurveyOption();
                option.optionSeq = null;
                option.fieldSeq = null;
                option.optionLabel = sourceOption.optionLabel;
                option.optionValue = sourceOption.optionValue;
                option.sortOrd = sourceOption.sortOrd;
                field.options.add(option);
            }
            copy.fields.add(field);
        }
        return copy;
    }

    /**
     * 설문 마스터를 저장하고 하위 문항/보기는 현재 요청 기준으로 다시 구성한다.
     */
    @Transactional
    public SASurveyDto.SurveyDetail saveSurvey(String surveyUid, SASurveyDto.SurveySaveRequest request) {
        SASurveyDto.SurveyDetail survey = toSurveyDetail(surveyUid, request);
        SASurveyDto.SurveyDetail existing = surveyMapper.selectSurvey(survey.surveyUid);
        if (existing == null) {
            surveyMapper.insertSurvey(survey);
        } else {
            survey.surveySeq = existing.surveySeq;
            surveyMapper.updateSurvey(survey);
            // v1에서는 문항 순서 변경/삭제 처리를 단순화하기 위해 하위 문항과 보기를 전체 재생성한다.
            surveyMapper.deleteSurveyOptions(existing.surveySeq);
            surveyMapper.deleteSurveyFields(existing.surveySeq);
        }
        insertChildren(survey, request.fields);
        return findSurvey(survey.surveyUid);
    }

    /**
     * 설문 마스터와 하위 문항/보기를 삭제 상태로 전환한다.
     */
    @Transactional
    public void deleteSurvey(String surveyUid) {
        SASurveyDto.SurveyDetail existing = findSurvey(surveyUid);
        surveyMapper.deleteSurveyOptions(existing.surveySeq);
        surveyMapper.deleteSurveyFields(existing.surveySeq);
        surveyMapper.deleteSurvey(existing.surveySeq);
    }

    private void attachChildren(SASurveyDto.SurveyDetail survey) {
        List<SASurveyDto.SurveyField> fields = surveyMapper.selectSurveyFields(survey.surveySeq);
        List<SASurveyDto.SurveyOption> options = surveyMapper.selectSurveyOptions(survey.surveySeq);
        // 옵션을 문항에 안정적으로 붙이기 위해 fieldSeq를 기준으로 문항 map을 먼저 구성한다.
        Map<Long, SASurveyDto.SurveyField> fieldMap = new LinkedHashMap<>();
        for (SASurveyDto.SurveyField field : fields) {
            fieldMap.put(field.fieldSeq, field);
            survey.fields.add(field);
        }
        for (SASurveyDto.SurveyOption option : options) {
            SASurveyDto.SurveyField field = fieldMap.get(option.fieldSeq);
            if (field != null) {
                field.options.add(option);
            }
        }
        for (SASurveyDto.SurveyField field : survey.fields) {
            field.surveyType = defaultSurveyType(field.surveyType);
        }
    }

    private SASurveyDto.SurveyDetail toSurveyDetail(String surveyUid, SASurveyDto.SurveySaveRequest request) {
        SASurveyDto.SurveyDetail survey = new SASurveyDto.SurveyDetail();
        survey.surveyUid = surveyUid != null && !surveyUid.isBlank()
                ? surveyUid
                : request.surveyUid;
        if (survey.surveyUid == null || survey.surveyUid.isBlank()) {
            survey.surveyUid = newUid();
        }
        survey.title = request.title;
        survey.description = request.description;
        survey.useYn = defaultYn(request.useYn, "Y");
        return survey;
    }

    private void insertChildren(SASurveyDto.SurveyDetail survey, List<SASurveyDto.SurveyFieldSaveRequest> fields) {
        int fieldOrder = 1;
        for (SASurveyDto.SurveyFieldSaveRequest source : fields) {
            SASurveyDto.SurveyField field = new SASurveyDto.SurveyField();
            field.surveySeq = survey.surveySeq;
            field.fieldKey = source.fieldKey == null || source.fieldKey.isBlank()
                    ? "field_" + fieldOrder
                    : source.fieldKey;
            field.label = source.label;
            field.surveyType = defaultSurveyType(source.surveyType);
            field.fieldType = source.fieldType;
            field.requiredYn = defaultYn(source.requiredYn, "N");
            field.sortOrd = source.sortOrd > 0 ? source.sortOrd : fieldOrder;
            surveyMapper.insertSurveyField(field);

            int optionOrder = 1;
            // 관리 화면에서는 옵션을 줄바꿈 텍스트로 입력할 수 있으므로 DTO에서 정규화한 옵션 목록을 사용한다.
            for (SASurveyDto.SurveyOptionSaveRequest sourceOption : source.normalizedOptions()) {
                SASurveyDto.SurveyOption option = new SASurveyDto.SurveyOption();
                option.fieldSeq = field.fieldSeq;
                option.optionLabel = sourceOption.optionLabel;
                option.optionValue = sourceOption.optionValue == null || sourceOption.optionValue.isBlank()
                        ? sourceOption.optionLabel
                        : sourceOption.optionValue;
                option.sortOrd = sourceOption.sortOrd > 0 ? sourceOption.sortOrd : optionOrder;
                surveyMapper.insertSurveyOption(option);
                optionOrder++;
            }
            fieldOrder++;
        }
    }

    private String defaultYn(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return switch (value.toLowerCase()) {
            case "true", "on", "y" -> "Y";
            case "false", "off", "n" -> "N";
            default -> value;
        };
    }

    private String defaultSurveyType(String value) {
        if (value == null || value.isBlank()) {
            return "objective";
        }
        return switch (value.trim().toLowerCase()) {
            case "objective", "o", "객관식" -> "objective";
            case "subjective", "s", "주관식" -> "subjective";
            default -> "objective";
        };
    }

    private String newUid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
