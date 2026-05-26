package com.reven.project.common.config;

import com.reven.project.common.security.COAdminAccessLogFilter;
import com.reven.project.common.security.COAdminUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class COSecurityConfig {

    private final COAdminUserDetailsService adminUserDetailsService;
    private final COAdminAccessLogFilter adminAccessLogFilter;

    public COSecurityConfig(
            COAdminUserDetailsService adminUserDetailsService,
            COAdminAccessLogFilter adminAccessLogFilter
    ) {
        this.adminUserDetailsService = adminUserDetailsService;
        this.adminAccessLogFilter = adminAccessLogFilter;
    }

    /**
     * 관리자 URL 보호, 로그인/로그아웃 경로, 접속 로그 필터를 구성한다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(adminUserDetailsService)
                .authorizeHttpRequests(auth -> auth
                        // 사용자 화면과 정적 리소스는 로그인 없이 접근 가능하다.
                        .requestMatchers("/", "/main.do", "/index.do", "/news/**", "/surveys/**", "/admin/login.do", "/assets/**", "/common/**", "/admin/js/**", "/client/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login.do")
                        .loginProcessingUrl("/admin/login.do")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin/home.do", true)
                        .failureUrl("/admin/login.do?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout.do")
                        .logoutSuccessUrl("/admin/login.do?logout")
                )
                .addFilterAfter(adminAccessLogFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * {noop}, {bcrypt} 같은 Spring Security password id를 지원하는 encoder를 사용한다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
