package com.kuit.chozy.community.dto.response;

import com.kuit.chozy.community.domain.CommentStatus;
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
    private Long parentCommentId;
    private int depth;
    private boolean isMine;
    private FeedUserResponse user;
    private String content;
    private CommentReplyToResponse replyTo;
    private List<CommentMentionResponse> mentions;
    private CommentCountsResponse counts;
    private CommentMyStateResponse myState;
    private CommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentItemResponse> replies;
    private Boolean hasMoreReplies;
    private String nextRepliesCursor;
}
