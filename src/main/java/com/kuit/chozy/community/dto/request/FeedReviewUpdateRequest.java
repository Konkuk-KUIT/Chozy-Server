package com.kuit.chozy.community.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class FeedReviewUpdateRequest {

    private String content;
    private String vendor;
    private Float rating;
    private String productUrl;
    private String hashTags;

    private List<ImageMeta> img;

    @Getter
    public static class ImageMeta {
        private String fileName;
    }
}