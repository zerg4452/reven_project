// AI News 상세 화면의 Layout Dialect 마크업을 검증한다.
package com.reven.project.admin.bd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BDAiNewsDetailTemplateTest {

    @Test
    void detailTemplateUsesAdminLayoutDecorator() throws IOException {
        String html = readClasspath("/templates/admin/news/detail.html");

        assertThat(html).contains("layout:decorate=\"~{layouts/admin}\"");
        assertThat(html).contains("layout:fragment=\"content\"");
        assertThat(html).doesNotContain("fragments/layout :: gnb");
        assertThat(html).doesNotContain("fragments/layout :: footer");
    }

    @Test
    void adminLayoutTemplateDefinesSharedShell() throws IOException {
        String html = readClasspath("/templates/layouts/admin.html");

        assertThat(html).contains("layout:fragment=\"content\"");
        assertThat(html).contains("fragments/layout :: gnb('_')");
        assertThat(html).contains("fragments/layout :: lnb('_')");
        assertThat(html).contains("fragments/layout :: footer");
    }

    private String readClasspath(String path) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(input).as("classpath resource: %s", path).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
