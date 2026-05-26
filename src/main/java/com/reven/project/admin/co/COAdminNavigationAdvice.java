package com.reven.project.admin.co;

import com.reven.project.service.co.COAdminMenuService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.reven.project.admin")
public class COAdminNavigationAdvice {

    private final COAdminMenuService adminMenuService;

    public COAdminNavigationAdvice(COAdminMenuService adminMenuService) {
        this.adminMenuService = adminMenuService;
    }

    @ModelAttribute
    public void addAdminNavigation(Model model, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri == null || requestUri.startsWith("/admin/login") || requestUri.startsWith("/admin/logout")) {
            return;
        }
        model.addAttribute("adminNavigation", adminMenuService.adminNavigation(requestUri));
    }
}
