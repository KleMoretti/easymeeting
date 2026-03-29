package com.easymeeting.enums;

public enum MessageTypesEnum {

    INIT(0, "连接WS获取信息"),
    ADD_MEETING_ROOM(1, "加入房间"),
    PEER(2, "发送peer"),
    EXIT_MEETING_ROOM(3, "退出房间"),
    FINIS_MEETING(4, "结束会议"),
    CHAT_TEXT_MESSAGE(5, "文本消息"),
    CHAT_MEDIA_MESSAGE(6, "媒体消息"),
    CHAT_MEDIA_MESSAGE_UPDATE(7, "媒体消息更新"),
    USER_CONTACT_APPLY(8, "好友申请消息"),
    INVITE_MEMBER_MEETING(9, "邀请入会"),
    FORCE_OFFLINE(10, "强制下线"),
    MEETING_USER_VDEO_CHANGE(11, "用戶视频改变"),
    MEETING_USER_AUDIO_CHANGE(12, "用户麦克风改变"),
    MEETING_USER_MEDIA_CHANGE(13, "用户音视频状态改变"),
    ;

    private Integer type;
    private String desc;

    public static MessageTypesEnum getByStatus(Integer status) {
        for (MessageTypesEnum item : values()) {
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

    MessageTypesEnum(Integer status, String desc) {
        this.type = status;
        this.desc = desc;
    }
}