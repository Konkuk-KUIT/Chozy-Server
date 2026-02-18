package com.kuit.chozy.me.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentSearchesResponse {
    private List<RecentSearchItemResponse> searches;
}
