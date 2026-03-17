package com.easymeeting.enums;

public enum MeetingJoinTypeEnum {

    NO_PASSWORD(0,"无需密码"),
    PASSWORD(1,"需要密码");

    private Integer type;
    private String desc;


    public static MeetingJoinTypeEnum getByStatus(Integer status) {
        for (MeetingJoinTypeEnum item : values()) {
            if (item.getType().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    MeetingJoinTypeEnum(Integer status, String desc) {
        this.type = status;
        this.desc = desc;
    }
}