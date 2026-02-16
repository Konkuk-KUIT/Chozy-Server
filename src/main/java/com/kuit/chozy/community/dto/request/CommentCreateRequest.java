package com.kuit.chozy.community.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    private String content;
    private Long parentCommentId;  // 대댓글이면 부모 댓글 ID, 최상위면 null
    private String replyToUserId;  // UI "@{userId}님에게 답글" 표시 대상
    private List<MentionDto> mentions;  // 본문에서 @멘션한 사용자 목록

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentionDto {
        private String userId;
        private int startIndex;
        private int length;
    }
}
