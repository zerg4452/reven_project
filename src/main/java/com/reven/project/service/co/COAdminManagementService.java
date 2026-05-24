package com.reven.project.service.co;

import com.reven.project.service.co.dto.COAdminListItemResponseDto;
import com.reven.project.service.co.dto.COAdminManagementPageResponseDto;
import com.reven.project.service.co.dto.COAdminManagementSearchRequestDto;
import com.reven.project.service.co.mapper.COAdminMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class COAdminManagementService {

    private static final Set<String> KEYWORD_TYPES = Set.of("all", "name", "login_id");

    private final COAdminMapper adminMapper;

    public COAdminManagementService(COAdminMapper adminMapper) {
        this.adminMapper = adminMapper;
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

    private COAdminManagementSearchRequestDto normalize(COAdminManagementSearchRequestDto searchRequestDto) {
        LocalDate dateFrom = searchRequestDto.dateFrom() == null ? LocalDate.now().minusDays(60) : searchRequestDto.dateFrom();
        LocalDate dateTo = searchRequestDto.dateTo() == null ? LocalDate.now().plusDays(1) : searchRequestDto.dateTo();
        String keywordType = KEYWORD_TYPES.contains(searchRequestDto.keywordType()) ? searchRequestDto.keywordType() : "all";
        String keyword = searchRequestDto.keyword() == null ? "" : searchRequestDto.keyword().trim();
        return new COAdminManagementSearchRequestDto(dateFrom, dateTo, keywordType, keyword);
    }
}
