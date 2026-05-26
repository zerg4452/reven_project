package com.reven.project.admin.co;

import com.reven.project.service.co.dto.COAdminDetailResponseDto;
import com.reven.project.service.co.dto.COAccessLogSearchRequestDto;
import com.reven.project.service.co.COAccessLogService;
import com.reven.project.service.co.COAdminHomeService;
import com.reven.project.service.co.COAdminManagementService;
import com.reven.project.service.co.COAdminMenuService;
import com.reven.project.service.co.dto.COAdminManagementSearchRequestDto;
import com.reven.project.service.co.dto.COAdminMenuSaveRequestDto;
import com.reven.project.service.co.dto.COAdminWriteRequestDto;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/management")
public class COAdminManagementController {

    private final COAdminHomeService adminHomeService;
    private final COAccessLogService accessLogService;
    private final COAdminManagementService adminManagementService;
    private final COAdminMenuService adminMenuService;

    public COAdminManagementController(
            COAdminHomeService adminHomeService,
            COAccessLogService accessLogService,
            COAdminManagementService adminManagementService,
            COAdminMenuService adminMenuService
    ) {
        this.adminHomeService = adminHomeService;
        this.accessLogService = accessLogService;
        this.adminManagementService = adminManagementService;
        this.adminMenuService = adminMenuService;
    }

    /**
     * 관리자 관리 영역의 기본 목록 화면을 표시한다.
     */
    @GetMapping("/list.do")
    public String managementHome(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String keywordType,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        var searchRequest = new COAdminManagementSearchRequestDto(dateFrom, dateTo, keywordType, keyword);
        model.addAttribute("management", adminHomeService.managementHome());
        try {
            var page = adminManagementService.searchAdmins(searchRequest);
            model.addAttribute("search", page.search());
            model.addAttribute("admins", page.admins());
            model.addAttribute("totalCount", page.totalCount());
            model.addAttribute("dateFrom", page.search().dateFrom());
            model.addAttribute("dateTo", page.search().dateTo());
            model.addAttribute("keywordType", page.search().keywordType());
            model.addAttribute("keyword", page.search().keyword());
        } catch (RuntimeException e) {
            model.addAttribute("search", searchRequest);
            model.addAttribute("admins", java.util.List.of());
            model.addAttribute("totalCount", 0);
            model.addAttribute("dateFrom", searchRequest.dateFrom());
            model.addAttribute("dateTo", searchRequest.dateTo());
            model.addAttribute("keywordType", searchRequest.keywordType());
            model.addAttribute("keyword", searchRequest.keyword());
            model.addAttribute("error", "관리자 목록을 불러오는 중 오류가 발생했습니다.");
        }
        return "admin/management/list";
    }

    /**
     * 관리자 등록/수정 화면을 표시한다.
     */
    @GetMapping("/write.do")
    public String writeAdmin(@RequestParam(required = false) Long adminSeq, Model model) {
        model.addAttribute("adminSeq", adminSeq);
        COAdminDetailResponseDto admin = adminManagementService.findAdmin(adminSeq);
        if (adminSeq != null && (admin == null || "super".equalsIgnoreCase(admin.role()))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("admin", admin);
        return "admin/management/edit";
    }

    /**
     * 관리자 등록 요청을 수신한다.
     */
    @PostMapping("/insert.do")
    public String saveAdmin(
            @ModelAttribute COAdminWriteRequestDto adminForm,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminManagementService.saveAdmin(adminForm, principal == null ? "system" : principal.getName());
            redirectAttributes.addFlashAttribute("message", "관리자를 등록했습니다.");
            return "redirect:/admin/management/list.do";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/management/write.do";
        }
    }

    /**
     * 관리자 수정 요청을 수신한다.
     */
    @PostMapping("/update.do")
    public String updateAdmin(
            @ModelAttribute COAdminWriteRequestDto adminForm,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminManagementService.saveAdmin(adminForm, principal == null ? "system" : principal.getName());
            redirectAttributes.addFlashAttribute("message", "관리자를 수정했습니다.");
            return "redirect:/admin/management/list.do";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return adminForm.adminSeq() == null
                    ? "redirect:/admin/management/write.do"
                    : "redirect:/admin/management/write.do?adminSeq=" + adminForm.adminSeq();
        }
    }

    /**
     * 관리자 삭제 요청을 수신한다.
     */
    @PostMapping("/delete.do")
    public String deleteAdmin(@RequestParam Long adminSeq, RedirectAttributes redirectAttributes) {
        try {
            adminManagementService.deleteAdmin(adminSeq);
            redirectAttributes.addFlashAttribute("message", "관리자를 삭제했습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/management/list.do";
    }

    /**
     * HiddenHttpMethodFilter가 비활성인 환경에서도 삭제 버튼을 수신한다.
     */
    @PostMapping(value = "/write.do", params = "_method=delete")
    public String deleteAdminAsPost(@RequestParam Long adminSeq, RedirectAttributes redirectAttributes) {
        try {
            adminManagementService.deleteAdmin(adminSeq);
            redirectAttributes.addFlashAttribute("message", "관리자를 삭제했습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/management/list.do";
    }

    /**
     * 관리자 접속 이력을 검색 조건과 함께 조회한다.
     */
    @GetMapping("/access-logs/list.do")
    public String accessLogs(@ModelAttribute("search") COAccessLogSearchRequestDto search, Model model) {
        COAccessLogSearchRequestDto normalized = normalizeSearch(search);
        model.addAttribute("search", normalized);
        model.addAttribute("logs", accessLogService.searchAccessLogs(normalized));
        return "admin/management/access-logs";
    }

    /**
     * 관리자 메뉴 관리 화면을 표시한다.
     */
    @GetMapping({"/menus/list.do", "/menus/write.do"})
    public String menus(@RequestParam(required = false) Long adminMenuSeq, Model model) {
        addMenuModel(model, adminMenuSeq);
        return "admin/management/menus";
    }

    /**
     * 관리자 메뉴를 등록하거나 수정한다.
     */
    @PostMapping("/menus/insert.do")
    public String insertMenu(
            @ModelAttribute("menu") COAdminMenuSaveRequestDto requestDto,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        return saveMenu(requestDto, principal, redirectAttributes, model);
    }

    /**
     * 관리자 메뉴를 수정한다.
     */
    @PostMapping("/menus/update.do")
    public String updateMenu(
            @ModelAttribute("menu") COAdminMenuSaveRequestDto requestDto,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        return saveMenu(requestDto, principal, redirectAttributes, model);
    }

    /**
     * 하위 메뉴가 없는 관리자 메뉴를 삭제 처리한다.
     */
    @PostMapping("/menus/delete.do")
    public String deleteMenu(
            @RequestParam Long adminMenuSeq,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminMenuService.deleteMenu(adminMenuSeq, principal == null ? "system" : principal.getName());
            redirectAttributes.addFlashAttribute("message", "메뉴를 삭제 처리했습니다.");
            return "redirect:/admin/management/menus/list.do";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/management/menus/list.do?adminMenuSeq=" + adminMenuSeq;
        }
    }

    private String saveMenu(
            COAdminMenuSaveRequestDto requestDto,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        try {
            Long savedSeq = adminMenuService.saveMenu(withActor(requestDto, principal));
            redirectAttributes.addFlashAttribute("message", "메뉴를 저장했습니다.");
            return "redirect:/admin/management/menus/write.do?adminMenuSeq=" + savedSeq;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            addMenuModel(model, requestDto.adminMenuSeq());
            model.addAttribute("menu", requestDto);
            return "admin/management/menus";
        }
    }

    /**
     * 접속 이력 검색 조건의 날짜/페이징 기본값을 보정한다.
     */
    private COAccessLogSearchRequestDto normalizeSearch(COAccessLogSearchRequestDto search) {
        LocalDate endDate = search.endDate() == null ? LocalDate.now().plusDays(1) : search.endDate();
        LocalDate startDate = search.startDate() == null ? LocalDate.now().minusDays(60) : search.startDate();
        int limit = search.limit() == null || search.limit() <= 0 ? 20 : search.limit();
        int offset = search.offset() == null || search.offset() < 0 ? 0 : search.offset();
        return new COAccessLogSearchRequestDto(startDate, endDate, search.keyword(), offset, limit);
    }

    private void addMenuModel(Model model, Long selectedSeq) {
        model.addAttribute("selectedSeq", selectedSeq);
        model.addAttribute("menus", adminMenuService.menuTree(selectedSeq));
        model.addAttribute("menuTreeNodesJson", adminMenuService.menuTreeNodesJson(selectedSeq));
        model.addAttribute("menu", adminMenuService.menuForm(selectedSeq));
        model.addAttribute("parentOptions", adminMenuService.parentOptions(selectedSeq));
        model.addAttribute("extraHeadLinks", "<link rel=\"stylesheet\" href=\"/vendor/jstree/style.min.css\">");
    }

    private COAdminMenuSaveRequestDto withActor(COAdminMenuSaveRequestDto requestDto, Principal principal) {
        return new COAdminMenuSaveRequestDto(
                requestDto.adminMenuSeq(),
                requestDto.menuCode(),
                requestDto.parentMenuCode(),
                requestDto.menuName(),
                requestDto.menuUrl(),
                requestDto.matchUrlsText(),
                requestDto.menuType(),
                requestDto.boardKey(),
                requestDto.useYn(),
                requestDto.sortOrder(),
                principal == null ? "system" : principal.getName()
        );
    }
}
