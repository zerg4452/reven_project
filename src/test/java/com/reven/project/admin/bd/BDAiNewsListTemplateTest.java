// AI News 목록 화면의 저장/삭제 알럿 마크업을 검증한다.
package com.reven.project.admin.bd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BDAiNewsListTemplateTest {

    @Test
    void listTemplateShowsAlertForSavedMessages() throws IOException {
        String html = readClasspath("/templates/admin/news/list.html");

        assertThat(html).contains("th:if=\"${aiNewsSavedMessage != null}\"");
        assertThat(html).contains("alert(/*[[${aiNewsSavedMessage}]]*/'저장되었습니다.');");
    }

    private String readClasspath(String path) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(input).as("classpath resource: %s", path).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
