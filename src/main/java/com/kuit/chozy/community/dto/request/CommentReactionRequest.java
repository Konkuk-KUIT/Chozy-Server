package com.kuit.chozy.community.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentReactionRequest {
    private Boolean like;  // true: 좋아요, false: 싫어요
}
