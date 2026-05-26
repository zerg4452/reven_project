package com.reven.project.service.co;

import com.reven.project.service.co.dto.COAdminDetailResponseDto;
import com.reven.project.service.co.dto.COAdminListItemResponseDto;
import com.reven.project.service.co.dto.COAdminManagementPageResponseDto;
import com.reven.project.service.co.dto.COAdminManagementSearchRequestDto;
import com.reven.project.service.co.dto.COAdminMapperSearchRequestDto;
import com.reven.project.service.co.dto.COAdminSessionDto;
import com.reven.project.service.co.dto.COAdminWriteRequestDto;
import com.reven.project.service.co.mapper.COAdminMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class COAdminManagementService {

    private static final Set<String> KEYWORD_TYPES = Set.of("all", "name", "login_id");

    private final COAdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    public COAdminManagementService(COAdminMapper adminMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 관리자 관리 목록을 검색 조건과 함께 조회한다.
     */
    public COAdminManagementPageResponseDto searchAdmins(COAdminManagementSearchRequestDto searchRequestDto) {
        COAdminManagementSearchRequestDto normalized = normalize(searchRequestDto);
        List<COAdminListItemResponseDto> admins = adminMapper.selectAdmins(normalized);
        return new COAdminManagementPageResponseDto(
                normalized,
                adminMapper.countAdmins(normalized),
                admins
        );
    }

    /**
     * 관리자 상세를 조회한다.
     */
    public COAdminDetailResponseDto findAdmin(Long adminSeq) {
        if (adminSeq == null) {
            return null;
        }
        return adminMapper.selectAdminBySeq(adminSeq);
    }

    /**
     * 관리자 계정을 등록하거나 수정한다.
     */
    @Transactional
    public Long saveAdmin(COAdminWriteRequestDto requestDto, String actorId) {
        COAdminDetailResponseDto existing = requestDto.adminSeq() == null
                ? null
                : adminMapper.selectAdminBySeq(requestDto.adminSeq());
        if (requestDto.adminSeq() != null && existing == null) {
            throw new IllegalArgumentException("관리자를 찾을 수 없습니다.");
        }
        if (existing != null && "super".equalsIgnoreCase(existing.role())) {
            throw new IllegalArgumentException("최고관리자는 수정할 수 없습니다.");
        }

        String loginId = requestDto.adminSeq() == null ? firstText(requestDto.loginId()) : firstText(existing.loginId());
        String name = firstText(requestDto.name());
        String status = "inactive".equalsIgnoreCase(requestDto.status()) ? "inactive" : "active";
        String password = requestDto.password() == null ? "" : requestDto.password().trim();

        if (loginId.isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해 주세요.");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("관리자명을 입력해 주세요.");
        }
        if (requestDto.adminSeq() == null && password.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }

        COAdminSessionDto duplicate = adminMapper.selectAdminByLoginIdAnyStatus(new COAdminMapperSearchRequestDto(loginId));
        if (duplicate != null && (requestDto.adminSeq() == null || !duplicate.adminSeq().equals(requestDto.adminSeq()))) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        String passwordHash = existing == null ? "" : existing.passwordHash();
        if (!password.isBlank()) {
            passwordHash = passwordEncoder.encode(password);
        }
        if (requestDto.adminSeq() == null && passwordHash.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }

        LocalDateTime now = LocalDateTime.now();
        String actor = firstText(actorId, "system");
        COAdminDetailResponseDto payload = new COAdminDetailResponseDto(
                requestDto.adminSeq(),
                loginId,
                name,
                existing == null ? "admin" : existing.role(),
                status,
                passwordHash,
                existing == null ? now : existing.regDtm(),
                existing == null ? actor : firstText(existing.regId()),
                now,
                actor
        );

        if (requestDto.adminSeq() == null) {
            adminMapper.insertAdmin(payload);
            COAdminSessionDto saved = adminMapper.selectAdminByLoginIdAnyStatus(new COAdminMapperSearchRequestDto(loginId));
            return saved == null ? null : saved.adminSeq();
        } else {
            adminMapper.updateAdmin(payload);
            return payload.adminSeq();
        }
    }

    /**
     * 관리자 계정을 삭제한다.
     */
    @Transactional
    public void deleteAdmin(Long adminSeq) {
        COAdminDetailResponseDto existing = adminMapper.selectAdminBySeq(adminSeq);
        if (existing == null) {
            throw new IllegalArgumentException("관리자를 찾을 수 없습니다.");
        }
        if ("super".equalsIgnoreCase(existing.role())) {
            throw new IllegalArgumentException("최고관리자는 삭제할 수 없습니다.");
        }
        adminMapper.deleteAdmin(adminSeq);
    }

    private COAdminManagementSearchRequestDto normalize(COAdminManagementSearchRequestDto searchRequestDto) {
        LocalDate dateFrom = searchRequestDto.dateFrom() == null ? LocalDate.now().minusDays(60) : searchRequestDto.dateFrom();
        LocalDate dateTo = searchRequestDto.dateTo() == null ? LocalDate.now().plusDays(1) : searchRequestDto.dateTo();
        String keywordType = KEYWORD_TYPES.contains(searchRequestDto.keywordType()) ? searchRequestDto.keywordType() : "all";
        String keyword = searchRequestDto.keyword() == null ? "" : searchRequestDto.keyword().trim();
        return new COAdminManagementSearchRequestDto(dateFrom, dateTo, keywordType, keyword);
    }

    private String firstText(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstText(String value, String defaultValue) {
        String normalized = firstText(value);
        return normalized.isBlank() ? defaultValue : normalized;
    }
}
