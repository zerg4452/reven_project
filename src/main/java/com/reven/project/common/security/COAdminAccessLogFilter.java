package com.reven.project.common.security;

import com.reven.project.service.co.COAccessLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class COAdminAccessLogFilter extends OncePerRequestFilter {

    private final COAccessLogService accessLogService;

    public COAdminAccessLogFilter(COAccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    /**
     * 요청 처리 후 인증된 관리자 접근만 접속 이력으로 남긴다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 화면 응답을 먼저 완료한 뒤 로그를 남겨, 로깅 실패가 관리자 사용 흐름을 막지 않게 한다.
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && request.getRequestURI().startsWith("/admin")) {
            try {
                accessLogService.recordAdminAccess(null, authentication.getName(), request);
            } catch (RuntimeException ignored) {
                // Access logging must not block the administrator workflow.
            }
        }
    }
}
