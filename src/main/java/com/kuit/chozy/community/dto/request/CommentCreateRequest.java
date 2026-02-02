package com.kuit.chozy.community.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    private String content;
    private Long parentCommentId;  // 없으면 null (대댓글일 때 부모 댓글 ID)
}
