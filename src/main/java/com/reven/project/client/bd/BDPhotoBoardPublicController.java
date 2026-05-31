// 사용자 포토 게시판 화면 컨트롤러
package com.reven.project.client.bd;

import com.reven.project.service.bd.BDPhotoBoardService;
import com.reven.project.service.bd.dto.BDPhotoBoardFileResponseDto;
import com.reven.project.service.bd.dto.BDPhotoBoardPublicSearchRequestDto;
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
public class BDPhotoBoardPublicController {

    private final BDPhotoBoardService photoBoardService;

    public BDPhotoBoardPublicController(BDPhotoBoardService photoBoardService) {
        this.photoBoardService = photoBoardService;
    }

    /**
     * 사용자 포토 게시판 목록을 조회한다.
     */
    @GetMapping("/board/photo/list.do")
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean imageOnly,
            @RequestParam(defaultValue = "false") boolean videoOnly,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        model.addAttribute("page", photoBoardService.searchPublicPhotoBoards(
                new BDPhotoBoardPublicSearchRequestDto(keyword, imageOnly, videoOnly, page, 9)
        ));
        return "client/photo/list";
    }

    /**
     * 사용자 포토 게시판 상세를 조회한다.
     */
    @GetMapping("/board/photo/detail.do")
    public String detail(@RequestParam(required = false) Long photoSeq, Model model) {
        var photo = photoBoardService.findPublicPhotoBoard(photoSeq);
        if (photo == null) {
            model.addAttribute("message", "비정상적인 접근입니다.");
            model.addAttribute("redirectUrl", "/board/photo/list.do");
            return "client/photo/invalid-access";
        }

        model.addAttribute("photo", photo);
        model.addAttribute("photoFiles", photoBoardService.findPublicPhotoBoardFiles(photoSeq));
        return "client/photo/detail";
    }

    /**
     * 공개 포토 게시판 첨부 파일을 반환한다.
     */
    @GetMapping("/board/photo/file.do")
    public ResponseEntity<Resource> file(@RequestParam Long photoFileSeq) throws IOException {
        BDPhotoBoardFileResponseDto file = photoBoardService.findPublicPhotoBoardFile(photoFileSeq);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Path path = photoBoardService.resolvePublicPhotoBoardFilePath(photoFileSeq);
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
