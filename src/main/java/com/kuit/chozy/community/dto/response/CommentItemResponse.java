package com.kuit.chozy.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentItemResponse {
    private Long commentId;
    private FeedUserResponse user;
    private String mentionName;
    private String content;
    private CommentCountsResponse counts;
    private CommentMyStateResponse myState;
    private LocalDateTime createdAt;
    private List<CommentItemResponse> commentReplies;  // 대댓글 (없으면 빈 리스트)
}
