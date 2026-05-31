// 공지사항 첨부 파일 저장 명령 객체(MyBatis selectKey 입력용)
package com.reven.project.service.bd.dto;

public class BDNoticeFileSaveCommand {
    private Long noticeFileSeq;
    private Long noticeSeq;
    private String fileType;
    private String originalFileName;
    private String storedFileName;
    private String storedPath;
    private String contentType;
    private Long fileSize;
    private Integer sortOrder;
    private String actorId;

    public Long getNoticeFileSeq() {
        return noticeFileSeq;
    }

    public void setNoticeFileSeq(Long noticeFileSeq) {
        this.noticeFileSeq = noticeFileSeq;
    }

    public Long getNoticeSeq() {
        return noticeSeq;
    }

    public void setNoticeSeq(Long noticeSeq) {
        this.noticeSeq = noticeSeq;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getStoredPath() {
        return storedPath;
    }

    public void setStoredPath(String storedPath) {
        this.storedPath = storedPath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }
}
