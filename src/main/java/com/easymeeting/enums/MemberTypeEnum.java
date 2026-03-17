package com.easymeeting.enums;

public enum MemberTypeEnum {

    NORMAL(0, "普通用户"),
    COMPERE(1, "主持人"),
    ;

    private Integer type;
    private String desc;


    public static MemberTypeEnum getByStatus(Integer status) {
        for (MemberTypeEnum item : values()) {
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

    MemberTypeEnum(Integer status, String desc) {
        this.type = status;
        this.desc = desc;
    }
}