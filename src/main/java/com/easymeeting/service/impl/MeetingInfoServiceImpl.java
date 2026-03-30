package com.easymeeting.service.impl;

import com.easymeeting.entity.dto.*;
import com.easymeeting.entity.po.MeetingMember;
import com.easymeeting.entity.po.MeetingInviteRecord;
import com.easymeeting.entity.query.MeetingMemberQuery;
import com.easymeeting.entity.query.SimplePage;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.enums.*;
import com.easymeeting.entity.po.MeetingInfo;
import com.easymeeting.entity.query.MeetingInfoQuery;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.mappers.MeetingInfoMapper;
import com.easymeeting.mappers.MeetingInviteRecordMapper;
import com.easymeeting.mappers.MeetingMemberMapper;
import com.easymeeting.redis.RedisComponent;
import com.easymeeting.service.MeetingInfoService;
import com.easymeeting.service.UserContactService;

import com.easymeeting.utils.StringTools;
import com.easymeeting.websocket.ChannelContextUtils;
import com.easymeeting.websocket.message.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: Service
 * @author: klein
 * @data: 2026/03/16
 */
@Service("meetingInfoService")
public class MeetingInfoServiceImpl implements MeetingInfoService {

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private MeetingInfoMapper<MeetingInfo, MeetingInfoQuery> meetingInfoMapper;

    @Resource
    private MeetingInviteRecordMapper meetingInviteRecordMapper;

    @Resource
    private MeetingMemberMapper<MeetingMember, MeetingMemberQuery> meetingMemberMapper;
    @Autowired
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactService userContactService;

    /**
     * 根据条件查询列表
     */
    public List<MeetingInfo> findListByParam(MeetingInfoQuery param) {
        return this.meetingInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(MeetingInfoQuery param) {
        return this.meetingInfoMapper.selectCount(param);
    }

    /**
     * 分页查询
     */
    public PageinationResultVO<MeetingInfo> findListByPage(MeetingInfoQuery param) {
        Integer count = this.findCountByParam(param);
        Integer pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<MeetingInfo> list = this.findListByParam(param);
        PageinationResultVO<MeetingInfo> result = new PageinationResultVO<>(count, page.getPageSize(), page.getPageNo(),
                page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(MeetingInfo bean) {
        return this.meetingInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<MeetingInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.meetingInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    public Integer addOrUpdateBatch(List<MeetingInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.meetingInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据MeetingId查询
     */
    public MeetingInfo getMeetingInfoByMeetingId(String meetingId) {
        return this.meetingInfoMapper.selectByMeetingId(meetingId);
    }

    /**
     * 根据MeetingId更新
     */
    public Integer updateMeetingInfoByMeetingId(MeetingInfo bean, String meetingId) {
        return this.meetingInfoMapper.updateByMeetingId(bean, meetingId);
    }

    /**
     * 根据MeetingId删除
     */
    public Integer deleteMeetingInfoByMeetingId(String meetingId) {
        return this.meetingInfoMapper.deleteByMeetingId(meetingId);
    }

    @Override
    public void quickMeeting(MeetingInfo meetingInfo, String nickName) {
        Date curDate = new Date();
        meetingInfo.setCreateTime(curDate);
        meetingInfo.setMeetingId(StringTools.getMeetingNoOrMeetingId());
        meetingInfo.setStartTime(curDate);
        meetingInfo.setStatus(MeetingStatusEnum.RUNNING.getStatus());

        this.meetingInfoMapper.insert(meetingInfo);
    }

    @Override
    public void reserveMeeting(MeetingInfo meetingInfo, String nickName) {
        if (meetingInfo.getStartTime() == null || !meetingInfo.getStartTime().after(new Date())) {
            throw new BusinessException("预约开始时间必须晚于当前时间");
        }
        meetingInfo.setMeetingId(StringTools.getMeetingNoOrMeetingId());
        meetingInfo.setCreateTime(new Date());
        meetingInfo.setStatus(MeetingStatusEnum.RESERVED.getStatus());
        this.meetingInfoMapper.insert(meetingInfo);
    }

    @Override
    public void cancelReserveMeeting(String meetingId, String userId) {
        MeetingInfo meetingInfo = this.getMeetingInfoByMeetingId(meetingId);
        if (meetingInfo == null) {
            throw new BusinessException("会议不存在");
        }
        if (!meetingInfo.getCreateUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!MeetingStatusEnum.RESERVED.getStatus().equals(meetingInfo.getStatus())) {
            throw new BusinessException("仅支持取消预约中的会议");
        }
        MeetingInfo update = new MeetingInfo();
        update.setStatus(MeetingStatusEnum.FINISHED.getStatus());
        update.setEndTime(new Date());
        meetingInfoMapper.updateByMeetingId(update, meetingId);
    }

    @Override
    public List<MeetingInfo> loadTodayMeeting(String userId) {
        MeetingInfoQuery meetingInfoQuery = new MeetingInfoQuery();
        meetingInfoQuery.setCreateUserId(userId);
        meetingInfoQuery.setOrderBy("start_time asc");

        String today = com.easymeeting.utils.DataUtils.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        meetingInfoQuery.setStartTimeStart(today);
        meetingInfoQuery.setStartTimeEnd(today);

        List<MeetingInfo> list = meetingInfoMapper.selectList(meetingInfoQuery);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(item -> !MeetingStatusEnum.FINISHED.getStatus().equals(item.getStatus()))
                .collect(Collectors.toList());
    }

    private void addMeetingMember(String meetingId, String userId, String nickName, Integer memberType) {
        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setMeetingId(meetingId);
        meetingMember.setUserId(userId);
        meetingMember.setNickName(nickName);
        meetingMember.setLastJoinTime(new Date());
        meetingMember.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        meetingMember.setMeetingStatus(MeetingStatusEnum.RUNNING.getStatus());
        meetingMember.setMemberType(memberType);
        this.meetingMemberMapper.insertOrUpdate(meetingMember);
    }

    private void add2Meeting(String meetingId, String userId, String nickName, Integer sex, Integer memberType,
            Boolean videoOpen, Boolean audioOpen) {
        MeetingMemberDto meetingMemberDto = new MeetingMemberDto();
        meetingMemberDto.setUserId(userId);
        meetingMemberDto.setNickName(nickName);
        meetingMemberDto.setJoinTime(System.currentTimeMillis());
        meetingMemberDto.setMemberType(memberType);
        meetingMemberDto.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        meetingMemberDto.setOpenVideo(videoOpen);
        meetingMemberDto.setOpenAudio(audioOpen);
        meetingMemberDto.setSex(sex);
        redisComponent.add2Meeting(meetingId, meetingMemberDto);
    }

    private void checkMeetingJoin(String meetingId, String userId) {
        MeetingMember meetingMember = meetingMemberMapper.selectByMeetingIdAndUserId(meetingId, userId);
        if (meetingMember != null && MeetingMemberStatusEnum.BLACKLIST.getStatus().equals(meetingMember.getStatus())) {
            throw new BusinessException("你已经被拉黑，无法加入会议");
        }
        MeetingMemberDto meetingMemberDto = redisComponent.getMeetingMember(meetingId, userId);
        if (meetingMemberDto != null
                && MeetingMemberStatusEnum.BLACKLIST.getStatus().equals(meetingMemberDto.getStatus())) {
            throw new BusinessException("你已经被拉黑，无法加入会议");
        }
    }

    private MeetingInfo checkAndStartMeetingIfNeed(String meetingId) {
        MeetingInfo meetingInfo = this.getMeetingInfoByMeetingId(meetingId);
        if (meetingInfo == null || MeetingStatusEnum.FINISHED.getStatus().equals(meetingInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (MeetingStatusEnum.RESERVED.getStatus().equals(meetingInfo.getStatus())) {
            if (meetingInfo.getStartTime() == null || meetingInfo.getStartTime().after(new Date())) {
                throw new BusinessException("会议未开始");
            }
            MeetingInfo updateMeetingInfo = new MeetingInfo();
            updateMeetingInfo.setStatus(MeetingStatusEnum.RUNNING.getStatus());
            meetingInfoMapper.updateByMeetingId(updateMeetingInfo, meetingId);
            meetingInfo.setStatus(MeetingStatusEnum.RUNNING.getStatus());
        }
        return meetingInfo;
    }

    private void sendGroupMessage(String meetingId, Integer messageType, String sendUserId, Object content) {
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(messageType);
        messageSendDto.setMeetingId(meetingId);
        messageSendDto.setSendUserId(sendUserId);
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.GROUP.getType());
        messageSendDto.setMessageContent(content);
        messageHandler.sendMessage(messageSendDto);
    }

    private void acceptMeetingInviteIfExists(String meetingId, String receiveUserId) {
        MeetingInviteRecord updateInvite = new MeetingInviteRecord();
        updateInvite.setStatus(MeetingInviteStatusEnum.ACCEPT.getStatus());
        updateInvite.setDealTime(new Date());
        meetingInviteRecordMapper.updateByMeetingIdAndReceiveUserIdAndStatus(updateInvite, meetingId, receiveUserId,
                MeetingInviteStatusEnum.PENDING.getStatus());
    }

    @Override
    public void joinMeeting(String meetingId, String userId, String nickName, Integer sex, Boolean videoOpen,
            Boolean audioOpen) {
        if (StringTools.isEmpty(meetingId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MeetingInfo meetingInfo = checkAndStartMeetingIfNeed(meetingId);
        this.checkMeetingJoin(meetingId, userId);

        MemberTypeEnum memberTypeEnum = meetingInfo.getCreateUserId().equals(userId) ? MemberTypeEnum.COMPERE
                : MemberTypeEnum.NORMAL;
        this.addMeetingMember(meetingId, userId, nickName, memberTypeEnum.getType());

        this.add2Meeting(meetingId, userId, nickName, sex, memberTypeEnum.getType(), videoOpen,
                audioOpen == null ? Boolean.TRUE : audioOpen);

        acceptMeetingInviteIfExists(meetingId, userId);

        channelContextUtils.addMeetingRoom(meetingId, userId);
        MeetingJoinDto meetingJoinDto = new MeetingJoinDto();
        meetingJoinDto.setNewMember(redisComponent.getMeetingMember(meetingId, userId));
        meetingJoinDto.setMeetingMemberList(redisComponent.getMeetingMemberList(meetingId));
        sendGroupMessage(meetingId, MessageTypesEnum.ADD_MEETING_ROOM.getType(), userId, meetingJoinDto);
    }

    @Override
    public String preJoinMeeting(String meetingNo, TokenUserInfoDto tokenUserInfoDto, String password) {
        String userId = tokenUserInfoDto.getUserId();
        MeetingInfoQuery meetingInfoQuery = new MeetingInfoQuery();
        meetingInfoQuery.setMeetingNo(meetingNo);
        meetingInfoQuery.setOrderBy("create_time desc");

        List<MeetingInfo> meetingInfoList = meetingInfoMapper.selectList(meetingInfoQuery);
        if (meetingInfoList == null || meetingInfoList.isEmpty()) {
            throw new BusinessException("会议不存在");
        }
        MeetingInfo meetingInfo = meetingInfoList.get(0);

        if (MeetingStatusEnum.FINISHED.getStatus().equals(meetingInfo.getStatus())) {
            throw new BusinessException("会议已结束");
        }
        if (MeetingStatusEnum.RESERVED.getStatus().equals(meetingInfo.getStatus())) {
            if (meetingInfo.getStartTime() == null || meetingInfo.getStartTime().after(new Date())) {
                throw new BusinessException("会议未开始，请在预约时间后加入");
            }
            MeetingInfo updateMeetingInfo = new MeetingInfo();
            updateMeetingInfo.setStatus(MeetingStatusEnum.RUNNING.getStatus());
            meetingInfoMapper.updateByMeetingId(updateMeetingInfo, meetingInfo.getMeetingId());
            meetingInfo.setStatus(MeetingStatusEnum.RUNNING.getStatus());
        }
        if (!StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())
                && !meetingInfo.getMeetingId().equals(tokenUserInfoDto.getCurrentMeetingId())) {
            MeetingInfo currentMeeting = this.getMeetingInfoByMeetingId(tokenUserInfoDto.getCurrentMeetingId());
            if (currentMeeting != null && !MeetingStatusEnum.FINISHED.getStatus().equals(currentMeeting.getStatus())) {
                throw new BusinessException("您当前有会议正在进行中，请先结束当前会议");
            }
        }
        checkMeetingJoin(meetingInfo.getMeetingId(), userId);

        String joinPassword = meetingInfo.getJoinPassword() == null ? "" : meetingInfo.getJoinPassword();
        if (MeetingJoinTypeEnum.PASSWORD.getType().equals(meetingInfo.getJoinType())
                && (StringTools.isEmpty(password) || !joinPassword.equals(password))) {
            throw new BusinessException("会议密码错误");
        }
        tokenUserInfoDto.setCurrentMeetingId(meetingInfo.getMeetingId());
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
        return meetingInfo.getMeetingId();
    }

    @Override
    public void exitMeetingRoom(TokenUserInfoDto tokenUserInfoDto, MeetingMemberStatusEnum statusEnum) {
        if (tokenUserInfoDto == null) {
            return;
        }
        String meetingId = tokenUserInfoDto.getCurrentMeetingId();
        if (StringTools.isEmpty(meetingId)) {
            return;
        }
        String userId = tokenUserInfoDto.getUserId();
        if (StringTools.isEmpty(userId)) {
            return;
        }
        Boolean exit = redisComponent.exitMeeting(meetingId, userId, statusEnum);

        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setStatus(statusEnum.getStatus());
        meetingMemberMapper.updateByMeetingIdAndUserId(meetingMember, meetingId, userId);

        tokenUserInfoDto.setCurrentMeetingId(null);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        if (!exit) {
            return;
        }

        List<MeetingMemberDto> meetingMemberDtoList = redisComponent.getMeetingMemberList(meetingId);
        if (meetingMemberDtoList == null) {
            meetingMemberDtoList = new ArrayList<>();
        }
        MeetingExitDto exitDto = new MeetingExitDto();
        exitDto.setMeetingMemberList(meetingMemberDtoList);
        exitDto.setExitUserId(userId);
        exitDto.setExitStatus(statusEnum.getStatus());
        sendGroupMessage(meetingId, MessageTypesEnum.EXIT_MEETING_ROOM.getType(), userId, exitDto);

        List<MeetingMemberDto> onLineMemberList = meetingMemberDtoList.stream()
                .filter(item -> MeetingMemberStatusEnum.NORMAL.getStatus().equals(item.getStatus()))
                .collect(Collectors.toList());
        if (onLineMemberList.isEmpty()) {
            finishMeeting(meetingId, null);
            return;
        }
    }

    @Override
    public void forceExitMeeting(TokenUserInfoDto tokenUserInfoDto, String userId, MeetingMemberStatusEnum statusEnum) {
        if (tokenUserInfoDto == null || StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MeetingInfo meetingInfo = this.meetingInfoMapper.selectByMeetingId(tokenUserInfoDto.getCurrentMeetingId());
        if (meetingInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!meetingInfo.getCreateUserId().equals(tokenUserInfoDto.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        TokenUserInfoDto userInfoDto = redisComponent.getTokenUserInfoDtoByUserId(userId);
        if (userInfoDto != null && meetingInfo.getMeetingId().equals(userInfoDto.getCurrentMeetingId())) {
            exitMeetingRoom(userInfoDto, statusEnum);
            return;
        }

        redisComponent.exitMeeting(meetingInfo.getMeetingId(), userId, statusEnum);
        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setStatus(statusEnum.getStatus());
        meetingMemberMapper.updateByMeetingIdAndUserId(meetingMember, meetingInfo.getMeetingId(), userId);

        MeetingExitDto exitDto = new MeetingExitDto();
        exitDto.setExitUserId(userId);
        exitDto.setExitStatus(statusEnum.getStatus());
        exitDto.setMeetingMemberList(redisComponent.getMeetingMemberList(meetingInfo.getMeetingId()));
        sendGroupMessage(meetingInfo.getMeetingId(), MessageTypesEnum.EXIT_MEETING_ROOM.getType(),
                tokenUserInfoDto.getUserId(), exitDto);
    }

    @Override
    @Transactional
    public void finishMeeting(String meetingId, String userId) {
        if (StringTools.isEmpty(meetingId)) {
            return;
        }
        MeetingInfo meetingInfo = this.getMeetingInfoByMeetingId(meetingId);
        if (meetingInfo == null) {
            return;
        }
        if (userId != null && !meetingInfo.getCreateUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MeetingInfo updateMeetingInfo = new MeetingInfo();
        updateMeetingInfo.setStatus(MeetingStatusEnum.FINISHED.getStatus());
        updateMeetingInfo.setEndTime(new Date());
        meetingInfoMapper.updateByMeetingId(updateMeetingInfo, meetingId);

        sendGroupMessage(meetingId, MessageTypesEnum.FINIS_MEETING.getType(), userId, null);

        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setMeetingStatus(MeetingStatusEnum.FINISHED.getStatus());
        MeetingMemberQuery meetingMemberQuery = new MeetingMemberQuery();

        meetingMemberQuery.setMeetingId(meetingId);
        meetingMemberMapper.updateByParam(meetingMember, meetingMemberQuery);

        List<MeetingMemberDto> meetingMemberDtoList = redisComponent.getMeetingMemberList(meetingId);
        if (meetingMemberDtoList == null) {
            meetingMemberDtoList = Collections.emptyList();
        }
        for (MeetingMemberDto meetingMemberDto : meetingMemberDtoList) {
            TokenUserInfoDto userInfoDto = redisComponent.getTokenUserInfoDtoByUserId(meetingMemberDto.getUserId());
            if (userInfoDto == null) {
                continue;
            }
            userInfoDto.setCurrentMeetingId(null);
            redisComponent.saveTokenUserInfoDto(userInfoDto);
        }
        redisComponent.removeAllMeetingMember(meetingId);
    }

    @Override
    @Transactional
    public void inviteMemberMeeting(String meetingId, String inviteUserId, String receiveUserId, String inviteMessage) {
        if (StringTools.isEmpty(meetingId) || StringTools.isEmpty(inviteUserId) || StringTools.isEmpty(receiveUserId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (inviteUserId.equals(receiveUserId)) {
            throw new BusinessException("不能邀请自己");
        }
        checkAndStartMeetingIfNeed(meetingId);
        MeetingMemberDto inviteMember = redisComponent.getMeetingMember(meetingId, inviteUserId);
        if (inviteMember == null || !MeetingMemberStatusEnum.NORMAL.getStatus().equals(inviteMember.getStatus())) {
            throw new BusinessException("邀请人不在会议中");
        }
        if (!userContactService.isContact(inviteUserId, receiveUserId)) {
            throw new BusinessException("仅可邀请联系人入会");
        }
        checkMeetingJoin(meetingId, receiveUserId);
        TokenUserInfoDto receiveToken = redisComponent.getTokenUserInfoDtoByUserId(receiveUserId);
        if (receiveToken == null) {
            throw new BusinessException("联系人当前不在线，无法实时邀请");
        }
        if (meetingId.equals(receiveToken.getCurrentMeetingId())) {
            throw new BusinessException("联系人已在会议中");
        }

        MeetingInfo meetingInfo = this.getMeetingInfoByMeetingId(meetingId);
        if (meetingInfo == null) {
            throw new BusinessException("会议不存在");
        }

        MeetingInviteRecord pendingInvite = meetingInviteRecordMapper
                .selectPendingByMeetingIdAndReceiveUserId(meetingId, receiveUserId);
        if (pendingInvite != null) {
            throw new BusinessException("该联系人已有待处理邀请");
        }

        MeetingInviteRecord meetingInviteRecord = new MeetingInviteRecord();
        meetingInviteRecord.setInviteId(StringTools.getRandomNumber(20));
        meetingInviteRecord.setMeetingId(meetingId);
        meetingInviteRecord.setMeetingNo(meetingInfo.getMeetingNo());
        meetingInviteRecord.setMeetingName(meetingInfo.getMeetingName());
        meetingInviteRecord.setInviteUserId(inviteUserId);
        meetingInviteRecord.setReceiveUserId(receiveUserId);
        meetingInviteRecord.setInviteMessage(inviteMessage);
        meetingInviteRecord.setStatus(MeetingInviteStatusEnum.PENDING.getStatus());
        meetingInviteRecord.setCreateTime(new Date());
        meetingInviteRecordMapper.insert(meetingInviteRecord);

        MeetingInviteDto inviteDto = new MeetingInviteDto();
        inviteDto.setInviteId(meetingInviteRecord.getInviteId());
        inviteDto.setMeetingId(meetingId);
        inviteDto.setMeetingNo(meetingInfo.getMeetingNo());
        inviteDto.setMeetingName(meetingInfo.getMeetingName());
        inviteDto.setInviteMessage(inviteMessage);
        inviteDto.setInviteUserId(inviteUserId);

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypesEnum.INVITE_MEMBER_MEETING.getType());
        messageSendDto.setMeetingId(meetingId);
        messageSendDto.setSendUserId(inviteUserId);
        messageSendDto.setReceiveUserId(receiveUserId);
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.USER.getType());
        messageSendDto.setMessageContent(inviteDto);
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public List<MeetingInviteRecord> loadMyPendingInviteList(String receiveUserId) {
        if (StringTools.isEmpty(receiveUserId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        List<MeetingInviteRecord> list = meetingInviteRecordMapper.selectPendingByReceiveUserId(receiveUserId);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    @Override
    @Transactional
    public void rejectInvite(String inviteId, String receiveUserId) {
        if (StringTools.isEmpty(inviteId) || StringTools.isEmpty(receiveUserId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MeetingInviteRecord meetingInviteRecord = meetingInviteRecordMapper.selectByInviteId(inviteId);
        if (meetingInviteRecord == null) {
            throw new BusinessException("邀请记录不存在");
        }
        if (!receiveUserId.equals(meetingInviteRecord.getReceiveUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!MeetingInviteStatusEnum.PENDING.getStatus().equals(meetingInviteRecord.getStatus())) {
            throw new BusinessException("该邀请已处理");
        }

        MeetingInviteRecord updateInvite = new MeetingInviteRecord();
        updateInvite.setStatus(MeetingInviteStatusEnum.REJECT.getStatus());
        updateInvite.setDealTime(new Date());
        meetingInviteRecordMapper.updateByInviteId(updateInvite, inviteId);
    }

    @Override
    @Transactional
    public void cancelInvite(String inviteId, String inviteUserId) {
        if (StringTools.isEmpty(inviteId) || StringTools.isEmpty(inviteUserId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MeetingInviteRecord meetingInviteRecord = meetingInviteRecordMapper.selectByInviteId(inviteId);
        if (meetingInviteRecord == null) {
            throw new BusinessException("邀请记录不存在");
        }
        if (!inviteUserId.equals(meetingInviteRecord.getInviteUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!MeetingInviteStatusEnum.PENDING.getStatus().equals(meetingInviteRecord.getStatus())) {
            throw new BusinessException("仅可撤回待处理邀请");
        }

        MeetingInviteRecord updateInvite = new MeetingInviteRecord();
        updateInvite.setStatus(MeetingInviteStatusEnum.CANCEL.getStatus());
        updateInvite.setDealTime(new Date());
        meetingInviteRecordMapper.updateByInviteId(updateInvite, inviteId);
    }

    @Override
    public void updateMediaStatus(TokenUserInfoDto tokenUserInfoDto, Boolean videoOpen, Boolean audioOpen) {
        if (tokenUserInfoDto == null || StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String meetingId = tokenUserInfoDto.getCurrentMeetingId();
        String userId = tokenUserInfoDto.getUserId();
        MeetingMemberDto meetingMemberDto = redisComponent.getMeetingMember(meetingId, userId);
        if (meetingMemberDto == null) {
            throw new BusinessException("当前用户不在会议中");
        }
        Boolean oldVideo = meetingMemberDto.getOpenVideo();
        Boolean oldAudio = meetingMemberDto.getOpenAudio();

        MeetingMemberDto updatedMember = redisComponent.updateMeetingMemberMediaStatus(meetingId, userId, videoOpen,
                audioOpen);
        if (updatedMember == null) {
            throw new BusinessException("更新媒体状态失败");
        }

        MeetingMediaStatusDto mediaStatusDto = new MeetingMediaStatusDto();
        mediaStatusDto.setMeetingId(meetingId);
        mediaStatusDto.setUserId(userId);
        mediaStatusDto.setOpenVideo(updatedMember.getOpenVideo());
        mediaStatusDto.setOpenAudio(updatedMember.getOpenAudio());
        mediaStatusDto.setMeetingMemberList(redisComponent.getMeetingMemberList(meetingId));

        boolean changed = false;
        if (videoOpen != null && !videoOpen.equals(oldVideo)) {
            sendGroupMessage(meetingId, MessageTypesEnum.MEETING_USER_VDEO_CHANGE.getType(), userId, mediaStatusDto);
            changed = true;
        }
        if (audioOpen != null && !audioOpen.equals(oldAudio)) {
            sendGroupMessage(meetingId, MessageTypesEnum.MEETING_USER_AUDIO_CHANGE.getType(), userId, mediaStatusDto);
            changed = true;
        }
        if (changed) {
            sendGroupMessage(meetingId, MessageTypesEnum.MEETING_USER_MEDIA_CHANGE.getType(), userId, mediaStatusDto);
        }
    }

}
