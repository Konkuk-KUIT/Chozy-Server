package com.kuit.chozy.postaction.dto;

public class QuoteCreateRequest {

    private Long feedId;
    private String content;
    private String hashTags;

    public QuoteCreateRequest() {
    }

    public Long getFeedId() {
        return feedId;
    }

    public String getContent() {
        return content;
    }

    public String getHashTags() {
        return hashTags;
    }
}