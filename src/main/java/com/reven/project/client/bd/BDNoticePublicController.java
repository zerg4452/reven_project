// 사용자 공지사항 화면 컨트롤러
package com.reven.project.client.bd;

import com.reven.project.service.bd.BDNoticeService;
import com.reven.project.service.bd.support.BDFileStorageConstants;
import com.reven.project.service.bd.dto.BDNoticeDetailResponseDto;
import com.reven.project.service.bd.dto.BDNoticeFileResponseDto;
import com.reven.project.service.bd.dto.BDNoticePublicSearchRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BDNoticePublicController {

    private static final String VIEW_COOKIE_NAME = "bd_viewed_notice";

    private final BDNoticeService noticeService;

    public BDNoticePublicController(BDNoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**
     * 사용자 공지사항 목록을 조회한다.
     */
    @GetMapping("/board/notice/list.do")
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        model.addAttribute("page", noticeService.searchPublicNotices(
                new BDNoticePublicSearchRequestDto(keyword, page, 10)
        ));
        return "client/notice/list";
    }

    /**
     * 사용자 공지사항 상세를 조회한다(공개 조건 통과 시에만 조회수 증가).
     */
    @GetMapping("/board/notice/detail.do")
    public String detail(
            @RequestParam(required = false) Long noticeSeq,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        BDNoticeDetailResponseDto notice = noticeService.findPublicNotice(noticeSeq);
        if (notice == null) {
            model.addAttribute("message", "비정상적인 접근입니다.");
            model.addAttribute("redirectUrl", "/board/notice/list.do");
            return "client/notice/invalid-access";
        }

        BDBoardViewCountSupport.countOnce(request, response, VIEW_COOKIE_NAME, noticeSeq,
                () -> noticeService.increaseViewCount(noticeSeq));

        model.addAttribute("notice", notice);
        model.addAttribute("thumbnail", firstOrNull(noticeService.findPublicNoticeFilesForDetail(noticeSeq, BDFileStorageConstants.FILE_TYPE_THUMB)));
        model.addAttribute("attachments", noticeService.findPublicNoticeFilesForDetail(noticeSeq, BDFileStorageConstants.FILE_TYPE_ATTACH));
        return "client/notice/detail";
    }

    /**
     * 공개 공지사항 첨부 파일을 반환한다.
     */
    @GetMapping("/board/notice/file.do")
    public ResponseEntity<Resource> file(@RequestParam Long noticeFileSeq) throws IOException {
        BDNoticeFileResponseDto file = noticeService.findPublicNoticeFile(noticeFileSeq);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Path path = noticeService.resolvePublicNoticeFilePath(noticeFileSeq);
        if (path == null || !Files.exists(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok()
                .contentType(parseMediaType(file.contentType()))
                .contentLength(Files.size(path))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(file.originalFileName(), java.nio.charset.StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(new FileSystemResource(path));
    }

    private BDNoticeFileResponseDto firstOrNull(java.util.List<BDNoticeFileResponseDto> files) {
        return files == null || files.isEmpty() ? null : files.get(0);
    }

    private MediaType parseMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
