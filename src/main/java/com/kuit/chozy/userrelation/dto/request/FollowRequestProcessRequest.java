package com.kuit.chozy.userrelation.dto.request;

public class FollowRequestProcessRequest {

    private ProcessStatus status;

    protected FollowRequestProcessRequest() {
    }

    public FollowRequestProcessRequest(ProcessStatus status) {
        this.status = status;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public enum ProcessStatus {
        ACCEPT,
        REJECT
    }
}
