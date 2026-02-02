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
    private Boolean isFollowing;  // 상세 조회 시에만 사용 (게시글 작성자 팔로우 여부)
}
