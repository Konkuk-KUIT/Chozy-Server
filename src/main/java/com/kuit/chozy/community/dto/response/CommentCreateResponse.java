package com.kuit.chozy.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateResponse {
    private Long commentId;
    private Long feedId;
    private Long parentCommentId;
    private String content;
    private LocalDateTime createdAt;
}
