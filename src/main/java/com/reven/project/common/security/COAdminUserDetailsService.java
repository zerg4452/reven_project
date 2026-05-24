package com.reven.project.common.security;

import com.reven.project.service.co.dto.COAdminSessionDto;
import com.reven.project.service.co.COAdminAuthService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class COAdminUserDetailsService implements UserDetailsService {

    private final COAdminAuthService adminAuthService;

    public COAdminUserDetailsService(COAdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * Spring Security가 로그인 시 호출하는 관리자 계정 조회 메서드다.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        COAdminSessionDto admin = adminAuthService.findAdminForLogin(username);
        if (admin == null) {
            throw new UsernameNotFoundException("Admin account not found: " + username);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        // DB role 값은 SUPER처럼 저장될 수 있으므로 Spring Security 권한 형식인 ROLE_*로 보정한다.
        admin.roles().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .filter(authority -> !authorities.contains(authority))
                .forEach(authorities::add);

        return User.withUsername(admin.loginId())
                .password(admin.passwordHash())
                .authorities(authorities)
                .build();
    }
}
