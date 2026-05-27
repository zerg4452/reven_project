package com.reven.project.service.bd.dto;

public class BDPhotoBoardFileSaveCommand {
    private Long photoFileSeq;
    private Long photoSeq;
    private String originalFileName;
    private String storedFileName;
    private String storedPath;
    private String contentType;
    private Long fileSize;
    private Integer sortOrder;
    private String actorId;

    public Long getPhotoFileSeq() {
        return photoFileSeq;
    }

    public void setPhotoFileSeq(Long photoFileSeq) {
        this.photoFileSeq = photoFileSeq;
    }

    public Long getPhotoSeq() {
        return photoSeq;
    }

    public void setPhotoSeq(Long photoSeq) {
        this.photoSeq = photoSeq;
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
