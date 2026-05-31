package com.reven.project.client.bd;

import com.reven.project.service.bd.BDAiNewsService;
import com.reven.project.service.bd.dto.BDAiNewsDetailResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BDAiNewsPublicController {
    private final BDAiNewsService aiNewsService;

    public BDAiNewsPublicController(BDAiNewsService aiNewsService) {
        this.aiNewsService = aiNewsService;
    }

    /**
     * 게시 상태 AI News를 FAQ형 아코디언 목록으로 조회한다.
     */
    @GetMapping("/board/ai-news/list.do")
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("newsList", aiNewsService.searchPublishedAiNews(keyword));
        model.addAttribute("keyword", keyword);
        return "client/news/ai-list";
    }

    /**
     * 게시 상태 AI News 단건을 상세 화면으로 표시한다.
     */
    @GetMapping("/board/ai-news/detail.do")
    public String detail(
            @RequestParam Long newsSeq,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        BDAiNewsDetailResponseDto news = aiNewsService.findPublishedAiNewsDetail(newsSeq);
        if (news != null) {
            BDBoardViewCountSupport.countOnce(request, response, "bd_viewed_news", newsSeq,
                    () -> aiNewsService.increaseViewCount(newsSeq));
        }
        model.addAttribute("news", news);
        return "client/news/ai-detail";
    }
}
