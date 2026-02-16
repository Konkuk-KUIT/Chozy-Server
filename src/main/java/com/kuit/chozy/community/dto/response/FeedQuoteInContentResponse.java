package com.kuit.chozy.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedQuoteInContentResponse {
    private Long feedId;
    private FeedUserResponse user;
    private String text;
    private List<String> hashTags;
}
