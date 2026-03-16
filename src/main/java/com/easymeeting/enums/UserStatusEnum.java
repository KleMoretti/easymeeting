package com.easymeeting.enums;

public enum UserStatusEnum {
    DISABLE(0, "禁用"),
    ENABLE(1, "启用");

    private String desc;
    private Integer status;

    UserStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static UserStatusEnum getByStatus(Integer status) {
        for (UserStatusEnum userStatusEnum : UserStatusEnum.values()) {
            if (userStatusEnum.getStatus().equals(status)) {
                return userStatusEnum;
            }
        }
        return null;
    }

}
