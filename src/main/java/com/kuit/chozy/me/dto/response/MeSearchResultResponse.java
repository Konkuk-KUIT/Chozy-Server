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
public class MeSearchResultResponse {
    private String query;
    private List<FeedItemResponse> feeds;
}
