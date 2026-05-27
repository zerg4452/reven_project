package com.reven.project.admin.bd;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BDAdminBoardController {

    /**
     * 관리자 게시판 1-depth 허브를 표시한다.
     */
    @GetMapping({"/admin/board", "/admin/board/list.do"})
    public String boardHome() {
        return "admin/board/index";
    }
}
