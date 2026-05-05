package com.easymeeting.enums;

public enum MeetingEventStatusEnum {
    PUBLISHED(0),
    CONSUMED(1),
    FAILED(2),
    DEAD_LETTER(3);

    private final Integer status;

    MeetingEventStatusEnum(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
