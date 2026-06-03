// 관리자 Layout Dialect 렌더링 결과에 shell과 CSS가 포함되는지 검증한다.
package com.reven.project.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.servlet.ServletContext;
import java.util.List;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AdminLayoutRenderIntegrationTest {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void adminHomeRendersLayoutShellAndStylesheet() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);

        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        request.setRequestURI("/admin/home.do");
        request.setContextPath("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        var webApplication = JakartaServletWebApplication.buildApplication(servletContext);
        var exchange = webApplication.buildExchange(request, response);
        var context = new WebContext(exchange, exchange.getLocale());
        context.setVariable("activeSurveyCount", 0);
        context.setVariable("todaySubmissionCount", 0);
        context.setVariable("submissionCount", 0);
        context.setVariable("recentSubmissions", List.of());

        String html = templateEngine.process("admin/home/index", context);

        assertThat(html)
                .contains("/common/css/app.css")
                .contains("admin-gnb")
                .contains("admin-shell")
                .contains("<main class=\"admin-content\">")
                .contains("site-footer")
                .doesNotContain("layout:decorate");

        int shellStart = html.indexOf("admin-shell");
        int asideEnd = html.indexOf("</aside>", shellStart);
        int mainStart = html.indexOf("<main class=\"admin-content\">", shellStart);
        int breadcrumbStart = html.indexOf("page-breadcrumb", shellStart);
        assertThat(mainStart).isGreaterThan(asideEnd);
        assertThat(breadcrumbStart).isGreaterThan(mainStart);
    }
}
