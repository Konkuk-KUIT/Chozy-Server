package com.kuit.chozy.me.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentSearchItemResponse {
    private Long searchedId;
    private String query;
    private LocalDateTime searchedAt;
}
