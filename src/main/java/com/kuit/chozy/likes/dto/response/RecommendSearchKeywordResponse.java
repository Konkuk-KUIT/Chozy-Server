package com.kuit.chozy.likes.dto.response;

import java.util.List;

public record RecommendSearchKeywordResponse(
        List<KeywordResponse> keywords
) {}
