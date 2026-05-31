package com.reven.project.service.sa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 설문 도메인에서 화면, 서비스, MyBatis 입출력에 사용하는 DTO 묶음이다.
 */
public final class SASurveyDto {
    private static final String DEFAULT_KEYWORD_TYPE = "전체";
    private static final ZoneId PROJECT_ZONE = ZoneId.of("Asia/Seoul");

    private SASurveyDto() {
    }

    @Getter
    @Setter
    /** 설문 관리 목록 검색 조건이다. */
    public static class SurveySearchRequest {
        public LocalDate startDate = LocalDate.now(PROJECT_ZONE).minusDays(60);
        public LocalDate endDate = LocalDate.now(PROJECT_ZONE).plusDays(1);
        public String keywordType = DEFAULT_KEYWORD_TYPE;
        public String keyword;
        public String useYn;
    }

    @Getter
    @Setter
    /** 설문 이력 목록 검색 조건이다. */
    public static class SubmissionSearchRequest {
        public LocalDate startDate = LocalDate.now(PROJECT_ZONE).minusDays(60);
        public LocalDate endDate = LocalDate.now(PROJECT_ZONE).plusDays(1);
        public String keywordType = DEFAULT_KEYWORD_TYPE;
        public String keyword;
        public List<String> statuses = new ArrayList<>();
    }

    @Getter
    @Setter
    /** 설문 이력 검색 화면에서 상태 체크박스 옵션을 표현한다. */
    public static class SubmissionStatusOption {
        public String code;
        public String label;
    }

    @Getter
    @Setter
    /** 설문 관리/사용자 설문 목록의 한 행을 표현한다. */
    public static class SurveyListItem {
        public long rowNo;
        public Long surveySeq;
        public String surveyUid;
        public String title;
        public int fieldCount;
        public String useYn;
        public LocalDate regDate;
        public LocalDate modDate;

        /** Thymeleaf 공통 템플릿에서 id라는 이름으로 접근할 수 있도록 공개 UID를 반환한다. */
        public String getId() {
            return surveyUid;
        }

        public boolean isEnabled() {
            return "Y".equalsIgnoreCase(useYn);
        }

        public LocalDate getCreatedDate() {
            return regDate;
        }

        public LocalDate getUpdatedDate() {
            return modDate;
        }
    }

    @Getter
    @Setter
    /** 설문 마스터와 하위 문항/보기 전체 구조를 표현한다. */
    public static class SurveyDetail {
        public Long surveySeq;
        public String surveyUid;
        public String title;
        public String description;
        public String useYn = "Y";
        public LocalDate regDate;
        public LocalDate modDate;
        public List<SurveyField> fields = new ArrayList<>();

        public String getId() {
            return surveyUid;
        }

        public boolean isEnabled() {
            return "Y".equalsIgnoreCase(useYn);
        }
    }

    @Getter
    @Setter
    /** 설문 문항 상세와 선택형 보기 목록을 표현한다. */
    public static class SurveyField {
        public Long fieldSeq;
        public Long surveySeq;
        public String fieldKey;
        public String label;
        public String fieldType;
        public String requiredYn = "N";
        public int sortOrd;
        public List<SurveyOption> options = new ArrayList<>();

        /** 기존 템플릿 표현식에서 key라는 짧은 이름으로 접근하기 위한 편의 메서드다. */
        public String getKey() {
            return fieldKey;
        }

        public String getType() {
            return fieldType;
        }

        public boolean isRequired() {
            return "Y".equalsIgnoreCase(requiredYn);
        }

        /** 관리 화면 textarea에 보기 목록을 줄바꿈 텍스트로 표시한다. */
        public String getOptionsText() {
            return options.stream()
                    .map(option -> option.optionLabel)
                    .reduce((left, right) -> left + "\n" + right)
                    .orElse("");
        }
    }

    @Getter
    @Setter
    /** 설문 문항의 선택형 보기 한 건을 표현한다. */
    public static class SurveyOption {
        public Long optionSeq;
        public Long fieldSeq;
        public String optionLabel;
        public String optionValue;
        public int sortOrd;
    }

    @Getter
    @Setter
    /** 설문 등록/수정 form 요청 DTO다. */
    public static class SurveySaveRequest {
        public String surveyUid;
        @NotBlank
        public String title;
        public String description;
        public String useYn = "Y";
        @Valid
        public List<SurveyFieldSaveRequest> fields = new ArrayList<>();
    }

    @Getter
    @Setter
    /** 설문 등록/수정 form의 문항 요청 DTO다. */
    public static class SurveyFieldSaveRequest {
        public Long fieldSeq;
        public String fieldKey;
        @NotBlank
        public String label;
        @NotBlank
        public String fieldType;
        public String requiredYn = "N";
        public String optionsText;
        public int sortOrd;
        @Valid
        public List<SurveyOptionSaveRequest> options = new ArrayList<>();

        public void setType(String type) {
            this.fieldType = type;
        }

        public void setRequired(String required) {
            this.requiredYn = required;
        }

        /** 줄바꿈 textarea 입력을 선택형 보기 DTO 목록으로 변환한다. */
        public List<SurveyOptionSaveRequest> normalizedOptions() {
            if (options != null && !options.isEmpty()) {
                return options;
            }
            if (optionsText == null || optionsText.isBlank()) {
                return List.of();
            }
            return Arrays.stream(optionsText.split("\\R"))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(value -> {
                        SurveyOptionSaveRequest option = new SurveyOptionSaveRequest();
                        option.optionLabel = value;
                        option.optionValue = value;
                        return option;
                    })
                    .toList();
        }
    }

    @Getter
    @Setter
    /** 설문 등록/수정 form의 보기 요청 DTO다. */
    public static class SurveyOptionSaveRequest {
        public Long optionSeq;
        @NotBlank
        public String optionLabel;
        public String optionValue;
        public int sortOrd;
    }

    @Getter
    @Setter
    /** 사용자 설문 제출 요청 DTO다. */
    public static class SurveySubmitRequest {
        @NotBlank
        public String submitterName;
        @NotBlank
        public String phone;
        public String email;
        @NotEmpty
        @Valid
        public List<AnswerRequest> answers = new ArrayList<>();
    }

    @Getter
    @Setter
    /** 사용자 설문 제출 답변 DTO다. */
    public static class AnswerRequest {
        @NotBlank
        public String fieldKey;
        public String answerValue;
        public String answerJson;
    }

    @Getter
    @Setter
    /** 설문 제출 후 생성된 제출 UID를 반환하는 응답 DTO다. */
    public static class SurveySubmitResponse {
        public String submitUid;

        public SurveySubmitResponse(String submitUid) {
            this.submitUid = submitUid;
        }
    }

    @Getter
    @Setter
    /** 설문 이력 관리 목록의 한 행을 표현한다. */
    public static class SubmissionListItem {
        public long rowNo;
        public Long submitSeq;
        public String submitUid;
        public String surveyUid;
        public String surveyTitle;
        public String submitterName;
        public String phone;
        public String status;
        public LocalDate submittedDate;

        /** Thymeleaf 공통 링크 표현식에서 id라는 이름으로 접근할 수 있도록 제출 UID를 반환한다. */
        public String getId() {
            return submitUid;
        }

        public String getStatusText() {
            if ("new".equalsIgnoreCase(status) || "NEW".equalsIgnoreCase(status)) {
                return "신규";
            }
            if ("reviewing".equalsIgnoreCase(status)) {
                return "확인중";
            }
            if ("contacted".equalsIgnoreCase(status)) {
                return "연락완료";
            }
            if ("done".equalsIgnoreCase(status)) {
                return "처리완료";
            }
            if ("hold".equalsIgnoreCase(status)) {
                return "보류";
            }
            return status;
        }
    }

    @Getter
    @Setter
    /** 설문 이력 상세 화면의 제출자 정보와 답변 snapshot을 표현한다. */
    public static class SubmissionDetail {
        public Long submitSeq;
        public String submitUid;
        public String surveyUid;
        public String surveyTitle;
        public String submitterName;
        public String phone;
        public String email;
        public String status;
        public String adminMemo;
        public LocalDate submittedDate;
        public List<AnswerSnapshot> answers = new ArrayList<>();
    }

    @Getter
    @Setter
    /** 제출 당시 문항 라벨/유형과 답변값을 보존한 snapshot DTO다. */
    public static class AnswerSnapshot {
        public Long answerSeq;
        public Long submitSeq;
        public String fieldKey;
        public String fieldLabel;
        public String fieldType;
        public String answerValue;
        public String answerJson;
        public int sortOrd;
    }

    @Getter
    @Setter
    /** MyBatis 제출 마스터 insert에 사용하는 DTO다. */
    public static class SubmitInsert {
        public Long submitSeq;
        public Long surveySeq;
        public String surveyUid;
        public String submitUid;
        public String surveyTitle;
        public String submitterName;
        public String phone;
        public String email;
        public String status = "NEW";
        public String ip;
    }

    @Getter
    @Setter
    /** MyBatis 제출 답변 insert에 사용하는 DTO다. */
    public static class AnswerInsert {
        public Long answerSeq;
        public Long submitSeq;
        public Long fieldSeq;
        public String fieldKey;
        public String fieldLabel;
        public String fieldType;
        public String answerValue;
        public String answerJson;
        public int sortOrd;
    }

    @Getter
    @Setter
    /** 설문 이력 CSV 출력 row DTO다. */
    public static class CsvRow {
        public String submitUid;
        public String surveyTitle;
        public String submitterName;
        public String phone;
        public String email;
        public String status;
        public LocalDate submittedDate;
        public String fieldLabel;
        public String answerValue;
        public String answerJson;
        public int sortOrd;
    }
}
