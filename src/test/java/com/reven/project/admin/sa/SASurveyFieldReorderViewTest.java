package com.reven.project.admin.sa;

// 관리자 설문 상세 화면의 문항 순서 변경 UI 마크업·스크립트 존재를 검증하는 테스트

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SASurveyFieldReorderViewTest {

    @Test
    void detailTemplateIncludesFieldReorderControls() throws IOException {
        String html = readClasspath("/templates/admin/survey/detail.html");

        assertThat(html).contains("data-move-field-up");
        assertThat(html).contains("data-move-field-down");
        assertThat(html).contains("field-row-actions");
        assertThat(html).contains("data-add-option");
        assertThat(html).contains("data-option-template");
    }

    @Test
    void fieldEditorScriptHandlesFieldReorder() throws IOException {
        String script = readClasspath("/static/admin/js/survey-field-editor.js");

        assertThat(script).contains("data-move-field-up");
        assertThat(script).contains("data-move-field-down");
        assertThat(script).contains("insertAdjacentElement");
        assertThat(script).contains("data-add-option");
    }

    private String readClasspath(String path) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(input).as("classpath resource: %s", path).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
