package com.reven.project.admin.sa;

// 관리자 설문 미리보기 버튼과 공개 폼 읽기 전용 분기를 검증하는 테스트

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SASurveyPreviewViewTest {

    @Test
    void detailTemplateIncludesPreviewLinkForSavedSurvey() throws IOException {
        String html = readClasspath("/templates/admin/survey/detail.html");

        assertThat(html).contains("/admin/surveys/preview.do");
        assertThat(html).contains("target=\"_blank\"");
        assertThat(html).contains("rel=\"noopener noreferrer\"");
        assertThat(html).contains("미리보기");
    }

    @Test
    void detailTemplateSendsSurveyUidOnlyForNewSurvey() throws IOException {
        String html = readClasspath("/templates/admin/survey/detail.html");

        assertThat(html).contains("th:if=\"${survey?.surveySeq == null}\"");
        assertThat(html.indexOf("name=\"surveyUid\"")).isEqualTo(html.lastIndexOf("name=\"surveyUid\""));
    }

    @Test
    void publicSurveyFormBlocksSubmitInPreviewMode() throws IOException {
        String html = readClasspath("/templates/client/survey/form.html");

        assertThat(html).contains("관리자 미리보기 화면입니다. 응답은 저장되지 않습니다.");
        assertThat(html).contains("previewMode == true");
        assertThat(html).contains("/admin/surveys/preview.do");
        assertThat(html).contains("return false");
        assertThat(html).contains("th:if=\"${previewMode != true}\"");
    }

    @Test
    void publicSurveyFieldFragmentDisablesInputsInPreviewMode() throws IOException {
        String html = readClasspath("/templates/client/survey/field.html");

        assertThat(html).contains("th:disabled=\"${previewMode == true}\"");
        assertThat(html).contains("th:case=\"textarea\"");
        assertThat(html).contains("th:case=\"select\"");
        assertThat(html).contains("type=\"radio\"");
        assertThat(html).contains("type=\"checkbox\"");
    }

    @Test
    void publicSurveyFieldFragmentRendersControlNumberAndLabelInOrder() throws IOException {
        String html = readClasspath("/templates/client/survey/field.html");

        assertThat(html).contains("survey-option-number");
        assertThat(html).contains("survey-option-label");
        assertThat(html).contains("th:text=\"${optStat.count}\"");

        int radioInput = html.indexOf("th:case=\"radio\"");
        int radioControl = html.indexOf("type=\"radio\"", radioInput);
        int radioNumber = html.indexOf("survey-option-number", radioControl);
        int radioLabel = html.indexOf("survey-option-label", radioNumber);
        assertThat(radioControl).isLessThan(radioNumber);
        assertThat(radioNumber).isLessThan(radioLabel);

        int checkboxInput = html.indexOf("type=\"checkbox\"");
        int checkboxNumber = html.indexOf("survey-option-number", checkboxInput);
        int checkboxLabel = html.indexOf("survey-option-label", checkboxNumber);
        assertThat(checkboxInput).isLessThan(checkboxNumber);
        assertThat(checkboxNumber).isLessThan(checkboxLabel);
    }

    private String readClasspath(String path) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(input).as("classpath resource: %s", path).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
