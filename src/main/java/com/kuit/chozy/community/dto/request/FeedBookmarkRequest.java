package com.kuit.chozy.community.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FeedBookmarkRequest {
    private Boolean bookmark;  // true: 북마크, false: 취소
}
