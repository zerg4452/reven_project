package com.reven.project.service.co;

import com.reven.project.service.co.dto.COAdminMapperSearchRequestDto;
import com.reven.project.service.co.dto.COAdminSessionDto;
import com.reven.project.service.co.mapper.COAdminMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class COAdminAuthService {
    private static final String FALLBACK_SUPER_LOGIN_ID = "admin";
    private static final String FALLBACK_SUPER_PASSWORD = "{noop}admin123";
    private static final COAdminSessionDto FALLBACK_SUPER_ADMIN = new COAdminSessionDto(
            0L,
            FALLBACK_SUPER_LOGIN_ID,
            FALLBACK_SUPER_PASSWORD,
            "슈퍼 관리자",
            "SUPER,ADMIN",
            null
    );

    private final COAdminMapper adminMapper;

    public COAdminAuthService(COAdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    /**
     * Spring Security 로그인에 사용할 관리자 계정을 조회한다.
     */
    public COAdminSessionDto findAdminForLogin(String loginId) {
        if (FALLBACK_SUPER_LOGIN_ID.equals(loginId)) {
            try {
                COAdminSessionDto admin = adminMapper.selectAdminByLoginId(new COAdminMapperSearchRequestDto(loginId));
                return admin == null ? FALLBACK_SUPER_ADMIN : admin;
            } catch (DataAccessException ex) {
                // DB 초기화 전에도 최초 접근이 가능하도록 내장 슈퍼 계정을 fallback으로 제공한다.
                return FALLBACK_SUPER_ADMIN;
            }
        }
        return adminMapper.selectAdminByLoginId(new COAdminMapperSearchRequestDto(loginId));
    }
}
