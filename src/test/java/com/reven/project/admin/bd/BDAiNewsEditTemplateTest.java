// AI News 편집 화면의 상태 셀렉트 마크업을 검증한다.
package com.reven.project.admin.bd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BDAiNewsEditTemplateTest {

    @Test
    void editTemplateSelectsCurrentStatus() throws IOException {
        String html = readClasspath("/templates/admin/news/edit.html");

        assertThat(html).contains("name=\"status\"");
        assertThat(html).contains("value=\"P\"");
        assertThat(html).contains("value=\"Y\"");
        assertThat(html).contains("value=\"E\"");
        assertThat(html).doesNotContain("value=\"N\"");
        assertThat(html).contains("th:selected=\"${(formStatus != null ? formStatus : news?.status) == null or (formStatus != null ? formStatus : news?.status) == 'P'}\"");
    }

    private String readClasspath(String path) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(input).as("classpath resource: %s", path).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
