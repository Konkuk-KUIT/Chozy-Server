package com.kuit.chozy.postaction.dto;

public class RepostCreateRequest {

    private Long feedId;
    private String hashTags;

    public RepostCreateRequest() {
    }

    public Long getFeedId() {
        return feedId;
    }

    public String getHashTags() {
        return hashTags;
    }
}