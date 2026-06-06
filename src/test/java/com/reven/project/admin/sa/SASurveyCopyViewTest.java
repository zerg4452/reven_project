package com.reven.project.admin.sa;

// 관리자 설문 목록의 복사 링크 마크업을 검증하는 테스트

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SASurveyCopyViewTest {

    @Test
    void listTemplateIncludesCopyLinkPerRow() throws IOException {
        String html = readClasspath("/templates/admin/survey/list.html");

        assertThat(html).contains("/admin/surveys/copy.do");
        assertThat(html).contains("surveyUid=${survey.surveyUid}");
        assertThat(html).contains("복사");
    }

    private String readClasspath(String path) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(input).as("classpath resource: %s", path).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
