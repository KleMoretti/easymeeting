package com.easymeeting.entity.query;

public class MeetingChatMessageQuery extends BaseQuery {
    private Long messageId;
    private String meetingId;
    private Integer messageType;
    private String messageContent;
    private String messageContentFuzzy;
    private String sendUserId;
    private String sendUserIdFuzzy;
    private String sendUserNickName;
    private String sendUserNickNameFuzzy;
    private Long sendTime;
    private Long sendTimeStart;
    private Long sendTimeEnd;
    private Integer receiveType;
    private String receiveUserId;
    private String receiveUserIdFuzzy;
    private Long fileSize;
    private Long fileSizeStart;
    private Long fileSizeEnd;
    private String fileName;
    private String fileNameFuzzy;
    private Integer fileType;
    private String fileSuffix;
    private String fileSuffixFuzzy;
    private Integer status;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageContentFuzzy() {
        return messageContentFuzzy;
    }

    public void setMessageContentFuzzy(String messageContentFuzzy) {
        this.messageContentFuzzy = messageContentFuzzy;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getSendUserIdFuzzy() {
        return sendUserIdFuzzy;
    }

    public void setSendUserIdFuzzy(String sendUserIdFuzzy) {
        this.sendUserIdFuzzy = sendUserIdFuzzy;
    }

    public String getSendUserNickName() {
        return sendUserNickName;
    }

    public void setSendUserNickName(String sendUserNickName) {
        this.sendUserNickName = sendUserNickName;
    }

    public String getSendUserNickNameFuzzy() {
        return sendUserNickNameFuzzy;
    }

    public void setSendUserNickNameFuzzy(String sendUserNickNameFuzzy) {
        this.sendUserNickNameFuzzy = sendUserNickNameFuzzy;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public Long getSendTimeStart() {
        return sendTimeStart;
    }

    public void setSendTimeStart(Long sendTimeStart) {
        this.sendTimeStart = sendTimeStart;
    }

    public Long getSendTimeEnd() {
        return sendTimeEnd;
    }

    public void setSendTimeEnd(Long sendTimeEnd) {
        this.sendTimeEnd = sendTimeEnd;
    }

    public Integer getReceiveType() {
        return receiveType;
    }

    public void setReceiveType(Integer receiveType) {
        this.receiveType = receiveType;
    }

    public String getReceiveUserId() {
        return receiveUserId;
    }

    public void setReceiveUserId(String receiveUserId) {
        this.receiveUserId = receiveUserId;
    }

    public String getReceiveUserIdFuzzy() {
        return receiveUserIdFuzzy;
    }

    public void setReceiveUserIdFuzzy(String receiveUserIdFuzzy) {
        this.receiveUserIdFuzzy = receiveUserIdFuzzy;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileSizeStart() {
        return fileSizeStart;
    }

    public void setFileSizeStart(Long fileSizeStart) {
        this.fileSizeStart = fileSizeStart;
    }

    public Long getFileSizeEnd() {
        return fileSizeEnd;
    }

    public void setFileSizeEnd(Long fileSizeEnd) {
        this.fileSizeEnd = fileSizeEnd;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameFuzzy() {
        return fileNameFuzzy;
    }

    public void setFileNameFuzzy(String fileNameFuzzy) {
        this.fileNameFuzzy = fileNameFuzzy;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getFileSuffixFuzzy() {
        return fileSuffixFuzzy;
    }

    public void setFileSuffixFuzzy(String fileSuffixFuzzy) {
        this.fileSuffixFuzzy = fileSuffixFuzzy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
