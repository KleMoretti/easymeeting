package com.easymeeting.enums;

public enum MessageSend2TypeEnum {
    USER(0,"个人"),
    GROUP(1,"群体");

    private Integer type;
    private String desc;

    MessageSend2TypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
