// 개발 일지(worklog.md)를 파싱해 사용자 화면에 타임라인으로 제공하는 컨트롤러
package com.reven.project.client.main;

import com.reven.project.service.co.COWorklogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class COWorklogController {

    private final COWorklogService worklogService;

    public COWorklogController(COWorklogService worklogService) {
        this.worklogService = worklogService;
    }

    @GetMapping("/worklog.do")
    public String worklog(Model model) {
        model.addAttribute("months", worklogService.getWorklogMonths());
        return "client/worklog/index";
    }
}
