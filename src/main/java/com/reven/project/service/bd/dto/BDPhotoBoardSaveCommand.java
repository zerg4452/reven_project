package com.reven.project.service.bd.dto;

public class BDPhotoBoardSaveCommand {
    private Long photoSeq;
    private String title;
    private String publishYn;
    private String actorId;

    public Long getPhotoSeq() {
        return photoSeq;
    }

    public void setPhotoSeq(Long photoSeq) {
        this.photoSeq = photoSeq;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublishYn() {
        return publishYn;
    }

    public void setPublishYn(String publishYn) {
        this.publishYn = publishYn;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }
}
