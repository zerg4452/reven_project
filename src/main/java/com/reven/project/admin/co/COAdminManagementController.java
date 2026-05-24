package com.reven.project.admin.co;

import com.reven.project.service.co.dto.COAccessLogSearchRequestDto;
import com.reven.project.service.co.COAccessLogService;
import com.reven.project.service.co.COAdminHomeService;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class COAdminManagementController {

    private final COAdminHomeService adminHomeService;
    private final COAccessLogService accessLogService;

    public COAdminManagementController(COAdminHomeService adminHomeService, COAccessLogService accessLogService) {
        this.adminHomeService = adminHomeService;
        this.accessLogService = accessLogService;
    }

    /**
     * 관리자 관리 영역의 기본 목록 화면을 표시한다.
     */
    @GetMapping("/admin/management")
    public String managementHome(Model model) {
        model.addAttribute("management", adminHomeService.managementHome());
        model.addAttribute("admins", java.util.List.of());
        model.addAttribute("totalCount", 0);
        return "admin/management/list";
    }

    /**
     * 관리자 접속 이력을 검색 조건과 함께 조회한다.
     */
    @GetMapping("/admin/management/access-logs")
    public String accessLogs(@ModelAttribute("search") COAccessLogSearchRequestDto search, Model model) {
        COAccessLogSearchRequestDto normalized = normalizeSearch(search);
        model.addAttribute("search", normalized);
        model.addAttribute("logs", accessLogService.searchAccessLogs(normalized));
        return "admin/management/access-logs";
    }

    /**
     * 접속 이력 검색 조건의 날짜/페이징 기본값을 보정한다.
     */
    private COAccessLogSearchRequestDto normalizeSearch(COAccessLogSearchRequestDto search) {
        LocalDate endDate = search.endDate() == null ? LocalDate.now().plusDays(1) : search.endDate();
        LocalDate startDate = search.startDate() == null ? LocalDate.now().minusDays(60) : search.startDate();
        int limit = search.limit() <= 0 ? 20 : search.limit();
        int offset = Math.max(0, search.offset());
        return new COAccessLogSearchRequestDto(startDate, endDate, search.keyword(), offset, limit);
    }
}
