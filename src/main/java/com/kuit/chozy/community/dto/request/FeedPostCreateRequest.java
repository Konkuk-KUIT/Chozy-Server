package com.kuit.chozy.community.dto.request;

import java.util.List;

public class FeedPostCreateRequest {

    private String content;
    private List<String> hashTags;
    private List<ImageMeta> img;

    public FeedPostCreateRequest() {
    }

    public String getContent() {
        return content;
    }

    public List<String> getHashTags() {
        return hashTags;
    }

    public List<ImageMeta> getImg() {
        return img;
    }

    public static class ImageMeta {
        private String fileName;
        private String contentType;

        public ImageMeta() {
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }
    }
}