// 공개 게시판 목록 페이징 마크업을 검증하는 테스트
package com.reven.project.client.bd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class BDPublicPaginationViewTest {

    @Test
    void noticeListRendersGroupedPaginationControls() throws IOException {
        String html = readClasspath("/templates/client/notice/list.html");

        assertThat(html).contains("public-pagination");
        assertThat(html).contains("page.pageNumbers()");
        assertThat(html).contains("page.hasPreviousGroup()");
        assertThat(html).contains("page.previousGroupPage()");
        assertThat(html).contains("page.hasNextGroup()");
        assertThat(html).contains("page.nextGroupPage()");
        assertThat(html).contains("&lt;&lt;");
        assertThat(html).contains("&gt;&gt;");
        assertThat(html).contains("aria-current=\"page\"");
    }

    @Test
    void photoListRendersGroupedPaginationControls() throws IOException {
        String html = readClasspath("/templates/client/photo/list.html");

        assertThat(html).contains("public-pagination");
        assertThat(html).contains("page.pageNumbers()");
        assertThat(html).contains("page.hasPreviousGroup()");
        assertThat(html).contains("page.previousGroupPage()");
        assertThat(html).contains("page.hasNextGroup()");
        assertThat(html).contains("page.nextGroupPage()");
        assertThat(html).contains("&lt;&lt;");
        assertThat(html).contains("&gt;&gt;");
        assertThat(html).contains("aria-current=\"page\"");
    }

    private String readClasspath(String path) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(path)) {
            assertThat(input).as("classpath resource: %s", path).isNotNull();
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
