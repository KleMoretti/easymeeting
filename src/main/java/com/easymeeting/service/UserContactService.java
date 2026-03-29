package com.easymeeting.service;

import com.easymeeting.entity.dto.ContactInfoDto;
import com.easymeeting.entity.po.UserContactApply;

import java.util.List;

public interface UserContactService {

    Integer loadContactApplicationDealWithCount(String userId);

    void applyContact(String applyUserId, String receiveMeetingNo, String applyMessage);

    void dealContactApply(String applyId, Integer status, String receiveUserId);

    List<UserContactApply> loadApplyList(String receiveUserId, Integer status);

    List<ContactInfoDto> loadContactList(String userId);

    boolean isContact(String userId, String contactId);
}
