package com.reven.project.admin.bd;

import com.reven.project.service.bd.dto.BDAiNewsSaveRequestDto;
import com.reven.project.service.bd.dto.BDAiNewsSearchRequestDto;
import com.reven.project.service.bd.dto.BDAiNewsStatusOptionDto;
import com.reven.project.service.bd.dto.BDAiNewsCrawlResultDto;
import com.reven.project.service.bd.BDAiNewsService;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/board/ai-news")
public class BDAiNewsAdminController {

    private final BDAiNewsService aiNewsService;

    public BDAiNewsAdminController(BDAiNewsService aiNewsService) {
        this.aiNewsService = aiNewsService;
    }

    /**
     * AI News 목록 화면을 검색 조건과 함께 조회한다.
     */
    @GetMapping("/list.do")
    public String list(
            @ModelAttribute("search") BDAiNewsSearchRequestDto search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model
    ) {
        if (dateFrom != null || dateTo != null) {
            search = new BDAiNewsSearchRequestDto(
                    dateFrom,
                    dateTo,
                    search.keywordType(),
                    search.keyword(),
                    search.statuses(),
                    search.offset(),
                    search.limit()
            );
        }
        var page = aiNewsService.searchAiNews(search);
        model.addAttribute("page", page);
        model.addAttribute("newsList", page.news());
        model.addAttribute("totalCount", page.totalCount());
        model.addAttribute("dateFrom", page.search().startDate());
        model.addAttribute("dateTo", page.search().endDate());
        model.addAttribute("keyword", page.search().keyword());
        model.addAttribute("statusOptions", statusOptions());
        return "admin/news/list";
    }

    /**
     * AI News 신규 등록 또는 수정 화면을 표시한다.
     */
    @GetMapping("/write.do")
    public String writeForm(@RequestParam(required = false) Long newsSeq, Model model) {
        model.addAttribute("news", newsSeq == null ? null : aiNewsService.findAiNews(newsSeq));
        return "admin/news/edit";
    }

    /**
     * AI News 원고를 신규 등록한다.
     */
    @PostMapping("/insert.do")
    public String insert(
            @ModelAttribute("news") BDAiNewsSaveRequestDto requestDto,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        aiNewsService.saveAiNews(withActor(new BDAiNewsSaveRequestDto(
                null,
                requestDto.slug(),
                requestDto.title(),
                requestDto.category(),
                requestDto.summary(),
                requestDto.content(),
                requestDto.tagsJson(),
                requestDto.sourcesJson(),
                requestDto.publishedDate(),
                requestDto.status(),
                requestDto.actorId()
        ), principal));
        redirectAttributes.addFlashAttribute("aiNewsSavedMessage", "AI News를 등록했습니다.");
        return "redirect:/admin/board/ai-news/list.do";
    }

    /**
     * AI News 원고를 수정한다.
     */
    @PostMapping("/update.do")
    public String update(
            @RequestParam Long newsSeq,
            @ModelAttribute("news") BDAiNewsSaveRequestDto requestDto,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        aiNewsService.saveAiNews(withActor(new BDAiNewsSaveRequestDto(
                newsSeq,
                requestDto.slug(),
                requestDto.title(),
                requestDto.category(),
                requestDto.summary(),
                requestDto.content(),
                requestDto.tagsJson(),
                requestDto.sourcesJson(),
                requestDto.publishedDate(),
                requestDto.status(),
                requestDto.actorId()
        ), principal));
        redirectAttributes.addFlashAttribute("aiNewsSavedMessage", "AI News를 수정했습니다.");
        return "redirect:/admin/board/ai-news/list.do";
    }

    /**
     * AI News 상세 화면을 조회한다.
     */
    @GetMapping("/detail.do")
    public String detail(@RequestParam Long newsSeq, Model model) {
        model.addAttribute("news", aiNewsService.findAiNews(newsSeq));
        return "admin/news/detail";
    }

    /**
     * AI News를 화면에서 제외하도록 soft delete 처리한다.
     */
    @PostMapping("/delete.do")
    public String delete(@RequestParam Long newsSeq, RedirectAttributes redirectAttributes) {
        aiNewsService.deleteAiNews(newsSeq);
        redirectAttributes.addFlashAttribute("aiNewsSavedMessage", "AI News를 삭제했습니다.");
        return "redirect:/admin/board/ai-news/list.do";
    }

    /**
     * 레거시 JSON 파일을 읽어 AI News를 DB에 반영한다.
     */
    @PostMapping("/crawl.do")
    public String crawl(Principal principal, RedirectAttributes redirectAttributes) {
        String actorId = principal == null ? "system" : principal.getName();
        BDAiNewsCrawlResultDto result = aiNewsService.crawlLegacyJsonFiles(actorId);
        redirectAttributes.addFlashAttribute("message", result.message());
        return "redirect:/admin/board/ai-news/list.do";
    }

    /**
     * 등록/수정 작업자를 DTO에 보강한다.
     */
    private BDAiNewsSaveRequestDto withActor(BDAiNewsSaveRequestDto requestDto, Principal principal) {
        String actorId = principal == null ? "system" : principal.getName();
        return new BDAiNewsSaveRequestDto(
                requestDto.newsSeq(),
                requestDto.slug(),
                requestDto.title(),
                requestDto.category(),
                requestDto.summary(),
                requestDto.content(),
                requestDto.tagsJson(),
                requestDto.sourcesJson(),
                requestDto.publishedDate(),
                requestDto.status(),
                actorId
        );
    }

    /**
     * 뉴스 목록 검색에서 사용할 상태 체크박스 옵션을 반환한다.
     */
    private List<BDAiNewsStatusOptionDto> statusOptions() {
        return List.of(
                new BDAiNewsStatusOptionDto("P", "처리중"),
                new BDAiNewsStatusOptionDto("Y", "완료"),
                new BDAiNewsStatusOptionDto("E", "에러")
        );
    }
}
