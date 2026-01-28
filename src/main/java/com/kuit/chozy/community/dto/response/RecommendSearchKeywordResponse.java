package com.kuit.chozy.community.dto.response;

import java.util.List;

public record RecommendSearchKeywordResponse(
        List<KeywordResponse> keywords
) {}
