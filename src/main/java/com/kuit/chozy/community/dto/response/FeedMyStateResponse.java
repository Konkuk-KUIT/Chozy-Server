package com.kuit.chozy.community.dto.response;

import com.kuit.chozy.community.domain.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedMyStateResponse {
    private ReactionType reaction; // LIKE | DISLIKE | NONE
    private boolean isBookmarked;
    private boolean isReposted;
}
