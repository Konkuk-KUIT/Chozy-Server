package com.kuit.chozy.userrelation.dto.response;

import java.util.List;

public class FollowerListResponse {

    private final List<FollowerItemResponse> items;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;

    public FollowerListResponse(
            List<FollowerItemResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }

    public List<FollowerItemResponse> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }
}
