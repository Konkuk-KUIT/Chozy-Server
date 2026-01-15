package com.kuit.chozy.userrelation.dto.response;

import java.util.List;

public class BlockedUserListResponse {

    private final List<BlockedUserItem> items;

    public BlockedUserListResponse(List<BlockedUserItem> items) {
        this.items = items;
    }

    public List<BlockedUserItem> getItems() {
        return items;
    }
}