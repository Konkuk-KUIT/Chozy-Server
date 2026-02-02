package com.kuit.chozy.post.dto;

import java.util.List;

public class PostCreateRequest {

    private String content;
    private String hashTags;
    private List<ImageMeta> img;

    public PostCreateRequest() {
    }

    public String getContent() {
        return content;
    }

    public String getHashTags() {
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