package com.easymeeting.service.impl;

import com.easymeeting.entity.dto.ContactInfoDto;
import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.entity.po.UserContact;
import com.easymeeting.entity.po.UserContactApply;
import com.easymeeting.entity.po.UserInfo;
import com.easymeeting.entity.query.UserInfoQuery;
import com.easymeeting.enums.ContactApplyStatusEnum;
import com.easymeeting.enums.MessageSend2TypeEnum;
import com.easymeeting.enums.MessageTypesEnum;
import com.easymeeting.enums.ResponseCodeEnum;
import com.easymeeting.enums.UserContactStatusEnum;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.mappers.UserContactApplyMapper;
import com.easymeeting.mappers.UserContactMapper;
import com.easymeeting.mappers.UserInfoMapper;
import com.easymeeting.service.UserContactService;
import com.easymeeting.utils.StringTools;
import com.easymeeting.websocket.message.MessageHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

    private static final long APPLY_COOLDOWN_MILLIS = 60 * 1000L;

    @Resource
    private UserContactMapper userContactMapper;
    @Resource
    private UserContactApplyMapper userContactApplyMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private MessageHandler messageHandler;

    @Override
    public Integer loadContactApplicationDealWithCount(String userId) {
        return userContactApplyMapper.selectDealWithCount(userId);
    }

    @Override
    public void applyContact(String applyUserId, String receiveMeetingNo, String applyMessage) {
        if (StringTools.isEmpty(applyUserId) || StringTools.isEmpty(receiveMeetingNo)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        receiveMeetingNo = receiveMeetingNo.replace(" ", "");
        UserInfo receiveUser = userInfoMapper.selectByMeetingNo(receiveMeetingNo);
        if (receiveUser == null) {
            throw new BusinessException("目标用户不存在");
        }
        String receiveUserId = receiveUser.getUserId();
        if (applyUserId.equals(receiveUserId)) {
            throw new BusinessException("不能添加自己为联系人");
        }

        UserContact userContact = userContactMapper.selectByUserIdAndContactId(applyUserId, receiveUserId);
        if (userContact != null && UserContactStatusEnum.NORMAL.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException("对方已经是你的联系人");
        }

        UserContactApply exists = userContactApplyMapper.selectPendingByUsers(applyUserId, receiveUserId);
        if (exists != null) {
            throw new BusinessException("联系人申请已发送，请等待处理");
        }

        UserContactApply latestApply = userContactApplyMapper.selectLatestByUsers(applyUserId, receiveUserId);
        if (latestApply != null && latestApply.getCreateTime() != null
                && (System.currentTimeMillis() - latestApply.getCreateTime().getTime()) < APPLY_COOLDOWN_MILLIS) {
            throw new BusinessException("申请过于频繁，请稍后再试");
        }

        UserContactApply userContactApply = new UserContactApply();
        userContactApply.setApplyId(StringTools.getRandomNumber(20));
        userContactApply.setApplyUserId(applyUserId);
        userContactApply.setReceiveUserId(receiveUserId);
        userContactApply.setApplyMessage(StringTools.isEmpty(applyMessage) ? "请求添加你为联系人" : applyMessage);
        userContactApply.setStatus(ContactApplyStatusEnum.PENDING.getStatus());
        userContactApply.setCreateTime(new Date());
        userContactApplyMapper.insert(userContactApply);

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypesEnum.USER_CONTACT_APPLY.getType());
        messageSendDto.setSendUserId(applyUserId);
        messageSendDto.setReceiveUserId(receiveUserId);
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.USER.getType());
        messageSendDto.setMessageContent(userContactApply);
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    @Transactional
    public void dealContactApply(String applyId, Integer status, String receiveUserId) {
        if (StringTools.isEmpty(applyId) || status == null || StringTools.isEmpty(receiveUserId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!ContactApplyStatusEnum.ACCEPT.getStatus().equals(status)
                && !ContactApplyStatusEnum.REJECT.getStatus().equals(status)) {
            throw new BusinessException("处理状态非法");
        }

        UserContactApply userContactApply = userContactApplyMapper.selectByApplyId(applyId);
        if (userContactApply == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!receiveUserId.equals(userContactApply.getReceiveUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!ContactApplyStatusEnum.PENDING.getStatus().equals(userContactApply.getStatus())) {
            throw new BusinessException("该申请已处理");
        }

        UserContactApply updateApply = new UserContactApply();
        updateApply.setStatus(status);
        updateApply.setDealTime(new Date());
        userContactApplyMapper.updateByApplyId(updateApply, applyId);

        if (ContactApplyStatusEnum.ACCEPT.getStatus().equals(status)) {
            Date now = new Date();
            UserContact user2Contact = new UserContact();
            user2Contact.setUserId(userContactApply.getApplyUserId());
            user2Contact.setContactId(userContactApply.getReceiveUserId());
            user2Contact.setStatus(UserContactStatusEnum.NORMAL.getStatus());
            user2Contact.setCreateTime(now);
            user2Contact.setUpdateTime(now);
            userContactMapper.insertOrUpdate(user2Contact);

            UserContact contact2User = new UserContact();
            contact2User.setUserId(userContactApply.getReceiveUserId());
            contact2User.setContactId(userContactApply.getApplyUserId());
            contact2User.setStatus(UserContactStatusEnum.NORMAL.getStatus());
            contact2User.setCreateTime(now);
            contact2User.setUpdateTime(now);
            userContactMapper.insertOrUpdate(contact2User);
        }

        userContactApply.setStatus(status);
        userContactApply.setDealTime(updateApply.getDealTime());
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypesEnum.USER_CONTACT_APPLY.getType());
        messageSendDto.setSendUserId(receiveUserId);
        messageSendDto.setReceiveUserId(userContactApply.getApplyUserId());
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.USER.getType());
        messageSendDto.setMessageContent(userContactApply);
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public List<UserContactApply> loadApplyList(String receiveUserId, Integer status) {
        List<UserContactApply> list = userContactApplyMapper.selectListByReceiveUserId(receiveUserId, status);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    @Override
    public List<ContactInfoDto> loadContactList(String userId) {
        List<UserContact> userContacts = userContactMapper.selectByUserId(userId);
        if (userContacts == null || userContacts.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserContact> normalContacts = userContacts.stream()
                .filter(item -> UserContactStatusEnum.NORMAL.getStatus().equals(item.getStatus()))
                .collect(Collectors.toList());

        if (normalContacts.isEmpty()) {
            return Collections.emptyList();
        }

        List<ContactInfoDto> result = new ArrayList<>();
        for (UserContact userContact : normalContacts) {
            UserInfo userInfo = userInfoMapper.selectByUserId(userContact.getContactId());
            if (userInfo == null) {
                continue;
            }
            ContactInfoDto dto = new ContactInfoDto();
            dto.setUserId(userInfo.getUserId());
            dto.setNickName(userInfo.getNickName());
            dto.setSex(userInfo.getSex());
            dto.setMeetingNo(userInfo.getMeetingNo());
            result.add(dto);
        }
        return result;
    }

    @Override
    public boolean isContact(String userId, String contactId) {
        UserContact userContact = userContactMapper.selectByUserIdAndContactId(userId, contactId);
        return userContact != null && UserContactStatusEnum.NORMAL.getStatus().equals(userContact.getStatus());
    }
}
