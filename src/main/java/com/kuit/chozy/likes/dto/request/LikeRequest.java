package com.kuit.chozy.likes.dto.request;

import jakarta.validation.constraints.NotNull;

public record LikeRequest(
        @NotNull Long productId,
        @NotNull Boolean like
) {}
