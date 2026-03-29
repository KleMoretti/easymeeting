package com.easymeeting.enums;

public enum UserContactStatusEnum {
    NORMAL(1, "正常");

    private Integer status;
    private String desc;

    UserContactStatusEnum(Integer status, String desc) {
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
