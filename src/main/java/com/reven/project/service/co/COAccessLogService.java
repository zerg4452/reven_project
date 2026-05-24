package com.reven.project.service.co;

import com.reven.project.service.co.dto.COAccessLogCreateRequestDto;
import com.reven.project.service.co.dto.COAccessLogResponseDto;
import com.reven.project.service.co.dto.COAccessLogSearchRequestDto;
import com.reven.project.service.co.mapper.COAccessLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class COAccessLogService {

    private final COAccessLogMapper accessLogMapper;

    public COAccessLogService(COAccessLogMapper accessLogMapper) {
        this.accessLogMapper = accessLogMapper;
    }

    /**
     * 인증된 관리자의 관리자 영역 접근 이력을 저장한다.
     */
    public void recordAdminAccess(Long adminSeq, String loginId, HttpServletRequest request) {
        COAccessLogCreateRequestDto requestDto = new COAccessLogCreateRequestDto(
                adminSeq,
                loginId,
                request.getRequestURI(),
                request.getMethod(),
                clientIp(request),
                request.getHeader("User-Agent"),
                LocalDateTime.now()
        );
        accessLogMapper.insertAccessLog(requestDto);
    }

    /**
     * 관리자 홈 요약용 오늘 접속 수를 조회한다.
     */
    public long countTodayAccesses() {
        LocalDate today = LocalDate.now();
        return accessLogMapper.countAccessLogs(new COAccessLogSearchRequestDto(today, today, null, 0, 1));
    }

    /**
     * 관리자 접속 이력 목록을 검색 조건으로 조회한다.
     */
    public List<COAccessLogResponseDto> searchAccessLogs(COAccessLogSearchRequestDto requestDto) {
        return accessLogMapper.selectAccessLogs(requestDto);
    }

    /**
     * 프록시 환경을 고려해 실제 클라이언트 IP 후보를 계산한다.
     */
    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
