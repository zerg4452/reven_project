// 관리자 공지사항 화면 컨트롤러
package com.reven.project.admin.bd;

import com.reven.project.service.bd.BDNoticeService;
import com.reven.project.service.bd.dto.BDNoticeAdminSearchRequestDto;
import com.reven.project.service.bd.dto.BDNoticeSaveRequestDto;
import java.io.IOException;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/board/notice")
public class BDNoticeAdminController {

    private final BDNoticeService noticeService;

    public BDNoticeAdminController(BDNoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**
     * 공지사항 목록 화면을 조회한다.
     */
    @GetMapping("/list.do")
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model
    ) {
        var searchRequest = new BDNoticeAdminSearchRequestDto(dateFrom, dateTo);
        var normalizedSearch = noticeService.normalizedAdminSearch(searchRequest);
        var noticeList = noticeService.findNotices(searchRequest);
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("totalCount", noticeList.size());
        model.addAttribute("dateFrom", normalizedSearch.startDate());
        model.addAttribute("dateTo", normalizedSearch.endDate());
        return "admin/notice/list";
    }

    /**
     * 공지사항 등록/수정 화면을 표시한다.
     */
    @GetMapping("/write.do")
    public String writeForm(@RequestParam(required = false) Long noticeSeq, Model model) {
        if (noticeSeq != null) {
            var notice = noticeService.findNotice(noticeSeq);
            if (notice == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            model.addAttribute("notice", notice);
            model.addAttribute("thumbnail", noticeService.findNoticeThumbnail(noticeSeq));
            model.addAttribute("attachments", noticeService.findNoticeFiles(noticeSeq, "ATTACH"));
        }
        return "admin/notice/edit";
    }

    /**
     * 공지사항을 신규 등록한다.
     */
    @PostMapping("/insert.do")
    public String insert(
            @ModelAttribute("notice") BDNoticeSaveRequestDto requestDto,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
            @RequestParam(value = "keepAttachFileSeqs", required = false) List<Long> keepAttachFileSeqs,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            noticeService.saveNotice(withActor(requestDto, null, principal), thumbnailFile, uploadFiles, keepAttachFileSeqs);
            redirectAttributes.addFlashAttribute("noticeSavedMessage", "저장되었습니다.");
            return "redirect:/admin/board/notice/list.do";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flashValidationFailure(redirectAttributes, requestDto, exception.getMessage());
            return "redirect:/admin/board/notice/write.do";
        }
    }

    /**
     * 공지사항을 수정한다.
     */
    @PostMapping("/update.do")
    public String update(
            @RequestParam Long noticeSeq,
            @ModelAttribute("notice") BDNoticeSaveRequestDto requestDto,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> uploadFiles,
            @RequestParam(value = "keepAttachFileSeqs", required = false) List<Long> keepAttachFileSeqs,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            noticeService.saveNotice(withActor(requestDto, noticeSeq, principal), thumbnailFile, uploadFiles, keepAttachFileSeqs);
            redirectAttributes.addFlashAttribute("noticeSavedMessage", "저장되었습니다.");
            return "redirect:/admin/board/notice/list.do";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            flashValidationFailure(redirectAttributes, requestDto, exception.getMessage());
            return "redirect:/admin/board/notice/write.do?noticeSeq=" + noticeSeq;
        }
    }

    /**
     * 공지사항을 삭제한다.
     */
    @PostMapping("/delete.do")
    public String delete(
            @RequestParam Long noticeSeq,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            noticeService.deleteNotice(noticeSeq, principal == null ? "system" : principal.getName());
            return "redirect:/admin/board/notice/list.do";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/admin/board/notice/list.do";
        }
    }

    /**
     * 공지사항 첨부 파일을 반환한다.
     */
    @GetMapping("/file.do")
    public ResponseEntity<Resource> file(@RequestParam Long noticeFileSeq) throws IOException {
        return buildNoticeFileResponse(noticeFileSeq, false);
    }

    /**
     * 공지사항 첨부 파일 원본을 다운로드한다.
     */
    @GetMapping("/download.do")
    public ResponseEntity<Resource> download(@RequestParam Long noticeFileSeq) throws IOException {
        return buildNoticeFileResponse(noticeFileSeq, true);
    }

    private void flashValidationFailure(
            RedirectAttributes redirectAttributes,
            BDNoticeSaveRequestDto requestDto,
            String message
    ) {
        redirectAttributes.addFlashAttribute("error", message);
        redirectAttributes.addFlashAttribute("formTitle", requestDto.title());
        redirectAttributes.addFlashAttribute("formContent", requestDto.content());
        redirectAttributes.addFlashAttribute("formPublishYn", requestDto.publishYn());
        redirectAttributes.addFlashAttribute("formPinYn", requestDto.pinYn());
        redirectAttributes.addFlashAttribute("formPublishDtm", requestDto.publishDtm());
    }

    private BDNoticeSaveRequestDto withActor(BDNoticeSaveRequestDto requestDto, Long noticeSeq, Principal principal) {
        String actorId = principal == null ? "system" : principal.getName();
        LocalDateTime publishDtm = requestDto.publishDtm();
        return new BDNoticeSaveRequestDto(
                noticeSeq,
                requestDto.title(),
                requestDto.content(),
                publishDtm,
                requestDto.publishYn(),
                requestDto.pinYn(),
                actorId
        );
    }

    private ResponseEntity<Resource> buildNoticeFileResponse(Long noticeFileSeq, boolean attachment) throws IOException {
        var file = noticeService.findNoticeFile(noticeFileSeq);
        if (file == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Path path = noticeService.resolveNoticeFilePath(noticeFileSeq);
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
