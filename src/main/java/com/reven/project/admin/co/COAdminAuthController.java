package com.reven.project.admin.co;

import com.reven.project.service.co.dto.COAdminLoginRequestDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class COAdminAuthController {

    /**
     * 관리자 로그인 화면을 표시한다. 실제 인증 처리는 Spring Security formLogin이 담당한다.
     */
    @GetMapping("/admin/login.do")
    public String login(@ModelAttribute("loginForm") COAdminLoginRequestDto loginForm, Model model) {
        model.addAttribute("screenName", "관리자 로그인");
        model.addAttribute("purpose", "관리자 인증을 수행합니다.");
        return "admin/auth/login";
    }
}
