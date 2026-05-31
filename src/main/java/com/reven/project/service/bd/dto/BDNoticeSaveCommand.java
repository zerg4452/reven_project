// 공지사항 저장 명령 객체(MyBatis selectKey 입력용)
package com.reven.project.service.bd.dto;

import java.time.LocalDateTime;

public class BDNoticeSaveCommand {
    private Long noticeSeq;
    private String title;
    private String content;
    private String publishYn;
    private String pinYn;
    private LocalDateTime publishDtm;
    private String actorId;

    public Long getNoticeSeq() {
        return noticeSeq;
    }

    public void setNoticeSeq(Long noticeSeq) {
        this.noticeSeq = noticeSeq;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPublishYn() {
        return publishYn;
    }

    public void setPublishYn(String publishYn) {
        this.publishYn = publishYn;
    }

    public String getPinYn() {
        return pinYn;
    }

    public void setPinYn(String pinYn) {
        this.pinYn = pinYn;
    }

    public LocalDateTime getPublishDtm() {
        return publishDtm;
    }

    public void setPublishDtm(LocalDateTime publishDtm) {
        this.publishDtm = publishDtm;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }
}
