package com.kuit.chozy.me.dto.response;

import com.kuit.chozy.community.dto.response.FeedItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeReviewsPageResponse {
    private List<FeedItemResponse> feeds;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
}
