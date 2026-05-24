package com.reven.project.admin.co;

import com.reven.project.service.co.COAdminHomeService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class COAdminHomeController {

    private final COAdminHomeService adminHomeService;

    public COAdminHomeController(COAdminHomeService adminHomeService) {
        this.adminHomeService = adminHomeService;
    }

    /**
     * 로그인 후 처음 진입하는 관리자 홈 화면을 표시한다.
     */
    @GetMapping("/admin/home.do")
    public String adminHome(Model model) {
        model.addAttribute("home", adminHomeService.adminHome());
        model.addAttribute("activeSurveyCount", 0);
        model.addAttribute("todaySubmissionCount", 0);
        model.addAttribute("submissionCount", 0);
        model.addAttribute("recentSubmissions", List.of());
        return "admin/home/index";
    }
}
