package com.reven.project.admin.bd;

import com.reven.project.service.bd.BDPhotoBoardService;
import com.reven.project.service.bd.dto.BDPhotoBoardSaveRequestDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class BDPhotoBoardController {

    private final BDPhotoBoardService photoBoardService;

    public BDPhotoBoardController(BDPhotoBoardService photoBoardService) {
        this.photoBoardService = photoBoardService;
    }

    /**
     * 사진 게시판 목록을 조회한다.
     */
    @GetMapping("/admin/board/photo/list.do")
    public String list(Model model) {
        var photoList = photoBoardService.findPhotoBoards();
        model.addAttribute("photoList", photoList);
        model.addAttribute("totalCount", photoList.size());
        return "admin/photo/list";
    }

    /**
     * 사진 게시판 상세를 조회한다.
     */
    @GetMapping("/admin/board/photo/detail.do")
    public String detail(@RequestParam Long photoSeq, Model model) {
        model.addAttribute("photo", photoBoardService.findPhotoBoard(photoSeq));
        model.addAttribute("photoFiles", photoBoardService.findPhotoBoardFiles(photoSeq));
        return "admin/photo/detail";
    }

    /**
     * 사진 게시판 등록/수정 화면을 표시한다.
     */
    @GetMapping("/admin/board/photo/write.do")
    public String writeForm(@RequestParam(required = false) Long photoSeq, Model model) {
        if (photoSeq != null) {
            model.addAttribute("photo", photoBoardService.findPhotoBoard(photoSeq));
            model.addAttribute("photoFiles", photoBoardService.findPhotoBoardFiles(photoSeq));
        }
        return "admin/photo/edit";
    }

    /**
     * 사진 게시판을 신규 등록한다.
     */
    @PostMapping("/admin/board/photo/insert.do")
    public String insert(
            @ModelAttribute("photo") BDPhotoBoardSaveRequestDto requestDto,
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        photoBoardService.savePhotoBoard(withActor(requestDto, principal), uploadFiles);
        redirectAttributes.addFlashAttribute("photoSavedMessage", "저장되었습니다.");
        return "redirect:/admin/board/photo/list.do";
    }

    /**
     * 사진 게시판을 수정한다.
     */
    @PostMapping("/admin/board/photo/update.do")
    public String update(
            @RequestParam Long photoSeq,
            @ModelAttribute("photo") BDPhotoBoardSaveRequestDto requestDto,
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        photoBoardService.savePhotoBoard(withActor(new BDPhotoBoardSaveRequestDto(
                photoSeq,
                requestDto.title(),
                requestDto.publishYn(),
                requestDto.actorId()
        ), principal), uploadFiles);
        redirectAttributes.addFlashAttribute("photoSavedMessage", "저장되었습니다.");
        return "redirect:/admin/board/photo/list.do";
    }

    /**
     * 사진 게시판을 삭제한다.
     */
    @PostMapping("/admin/board/photo/delete.do")
    public String delete(@RequestParam Long photoSeq, Principal principal) {
        photoBoardService.deletePhotoBoard(photoSeq, principal == null ? "system" : principal.getName());
        return "redirect:/admin/board/photo/list.do";
    }

    /**
     * 첨부 파일을 반환한다.
     */
    @GetMapping("/admin/board/photo/file.do")
    public ResponseEntity<Resource> file(@RequestParam Long photoFileSeq) throws IOException {
        return buildPhotoFileResponse(photoFileSeq, false);
    }

    /**
     * 첨부 파일 원본을 다운로드한다.
     */
    @GetMapping("/admin/board/photo/download.do")
    public ResponseEntity<Resource> download(@RequestParam Long photoFileSeq) throws IOException {
        return buildPhotoFileResponse(photoFileSeq, true);
    }

    private BDPhotoBoardSaveRequestDto withActor(BDPhotoBoardSaveRequestDto requestDto, Principal principal) {
        String actorId = principal == null ? "system" : principal.getName();
        return new BDPhotoBoardSaveRequestDto(
                requestDto.photoSeq(),
                requestDto.title(),
                requestDto.publishYn(),
                actorId
        );
    }

    private ResponseEntity<Resource> buildPhotoFileResponse(Long photoFileSeq, boolean attachment) throws IOException {
        var file = photoBoardService.findPhotoBoardFile(photoFileSeq);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Path path = photoBoardService.resolvePhotoBoardFilePath(photoFileSeq);
        if (path == null || !Files.exists(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.contentType() != null && !file.contentType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(file.contentType());
            } catch (IllegalArgumentException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        ContentDisposition.Builder dispositionBuilder = attachment
                ? ContentDisposition.attachment()
                : ContentDisposition.inline();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(path))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        dispositionBuilder
                                .filename(file.originalFileName(), java.nio.charset.StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(new FileSystemResource(path));
    }
}
