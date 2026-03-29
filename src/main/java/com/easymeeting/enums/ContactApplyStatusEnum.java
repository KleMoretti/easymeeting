package com.easymeeting.enums;

public enum ContactApplyStatusEnum {
    PENDING(0, "待处理"),
    ACCEPT(1, "已同意"),
    REJECT(2, "已拒绝");

    private Integer status;
    private String desc;

    ContactApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
