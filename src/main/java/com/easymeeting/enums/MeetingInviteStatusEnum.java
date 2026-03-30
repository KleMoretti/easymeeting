package com.easymeeting.enums;

public enum MeetingInviteStatusEnum {

    PENDING(0, "待处理"),
    ACCEPT(1, "已接受"),
    REJECT(2, "已拒绝"),
    CANCEL(3, "已撤回");

    private Integer status;
    private String desc;

    public static MeetingInviteStatusEnum getByStatus(Integer status) {
        for (MeetingInviteStatusEnum item : values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    MeetingInviteStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
