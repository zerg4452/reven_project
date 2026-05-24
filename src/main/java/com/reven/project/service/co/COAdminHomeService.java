package com.reven.project.service.co;

import com.reven.project.service.co.dto.COAdminHomeResponseDto;
import com.reven.project.service.co.dto.COManagementPlaceholderResponseDto;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class COAdminHomeService {

    private final COAccessLogService accessLogService;

    public COAdminHomeService(COAccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    /**
     * 관리자 홈 화면에 표시할 요약 정보를 만든다.
     */
    public COAdminHomeResponseDto adminHome() {
        return new COAdminHomeResponseDto(
                "관리자 홈",
                "설문 운영 현황과 관리자 접속 흐름을 확인합니다.",
                LocalDate.now(),
                accessLogService.countTodayAccesses()
        );
    }

    /**
     * 관리자 관리 화면의 기본 안내 정보를 만든다.
     */
    public COManagementPlaceholderResponseDto managementHome() {
        return new COManagementPlaceholderResponseDto(
                "관리자 관리",
                "관리자 계정, 메뉴, 접속 이력을 관리합니다.",
                LocalDate.now(),
                List.of("관리자 계정", "메뉴 관리", "접속 이력")
        );
    }
}
