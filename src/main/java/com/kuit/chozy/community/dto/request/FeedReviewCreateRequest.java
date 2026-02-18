package com.kuit.chozy.community.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedReviewCreateRequest {

    private String content;
    private String vendor;
    private Float rating;
    private String productUrl;
    private List<String> hashTags;
    private List<ImageMeta> img;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageMeta {
        private String fileName;
        private String contentType;
    }
}