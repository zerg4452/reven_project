// 관리자 템플릿 Layout Dialect 전환 상태를 검증한다.
package com.reven.project.admin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class AdminLayoutTemplateTest {

    static Stream<Path> adminTemplates() throws IOException {
        Path root = Path.of("src/main/resources/templates/admin");
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(path -> path.toString().endsWith(".html")).toList().stream();
        }
    }

    @ParameterizedTest
    @MethodSource("adminTemplates")
    void adminTemplateUsesLayoutDecorator(Path templatePath) throws IOException {
        String html = Files.readString(templatePath);
        if (templatePath.toString().contains("/auth/login.html")) {
            assertThat(html).contains("layout:decorate=\"~{layouts/auth}\"");
        } else {
            assertThat(html).contains("layout:decorate=\"~{layouts/admin}\"");
        }
        assertThat(html).contains("layout:fragment=\"content\"");
        assertThat(html).doesNotContain("fragments/layout :: gnb");
        assertThat(html).doesNotContain("fragments/layout :: footer");
    }

    @Test
    void adminLayoutDefinesSharedShell() throws IOException {
        String html = readClasspath("/templates/layouts/admin.html");
        assertThat(html).contains("layout:fragment=\"content\"");
        assertThat(html).contains("layout:fragment=\"pageScripts\"");
        assertThat(html).contains("layout:fragment=\"pageExtras\"");
    }

    private String readClasspath(String path) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(input).as("classpath resource: %s", path).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
