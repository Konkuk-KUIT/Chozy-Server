package com.kuit.chozy.home.dto.response;

import java.util.List;

public record RecommendSearchKeywordResponse(
        List<KeywordResponse> keywords
) {}
