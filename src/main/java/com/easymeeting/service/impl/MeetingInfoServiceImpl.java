package com.easymeeting.service.impl;

import com.easymeeting.entity.dto.*;
import com.easymeeting.entity.po.MeetingMember;
import com.easymeeting.entity.query.MeetingMemberQuery;
import com.easymeeting.entity.query.SimplePage;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.enums.*;
import com.easymeeting.entity.po.MeetingInfo;
import com.easymeeting.entity.query.MeetingInfoQuery;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.mappers.MeetingInfoMapper;
import com.easymeeting.mappers.MeetingMemberMapper;
import com.easymeeting.redis.RedisComponent;
import com.easymeeting.service.MeetingInfoService;

import com.easymeeting.utils.JsonUtils;
import com.easymeeting.utils.StringTools;
import com.easymeeting.websocket.ChannelContextUtils;
import com.easymeeting.websocket.message.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Description: Service
 * @author: klein
 * @data: 2026/03/16
 */
@Service("meetingInfoService")
public class MeetingInfoServiceImpl implements MeetingInfoService {

    @Resource
    private ChannelContextUtils channelContextUtils;

/*	@Resource
	private MessageHandler  messageHandler;*/

    @Resource
    private MeetingInfoMapper<MeetingInfo, MeetingInfoQuery> meetingInfoMapper;
    @Resource
    private MeetingMemberMapper<MeetingMember, MeetingMemberQuery> meetingMemberMapper;
    @Autowired
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

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
        PageinationResultVO<MeetingInfo> result = new PageinationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
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

    private void addMeetingMember(String meetingId, String userId, String nickName, Integer memberType) {
        MeetingMember meetingMember = new MeetingMember();
        meetingMember.setMeetingId(meetingId);
        meetingMember.setUserId(userId);
        meetingMember.setNickName(nickName);
        meetingMember.setLastJoinTime(new Date());
        meetingMember.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        meetingMember.setMemberType(MeetingStatusEnum.RUNNING.getStatus());
        this.meetingMemberMapper.insert(meetingMember);
    }

    private void add2Meeting(String meetingId, String userId, String nickName, Integer sex, Integer memberType, Boolean videoOpen) {
        MeetingMemberDto meetingMemberDto = new MeetingMemberDto();
        meetingMemberDto.setUserId(userId);
        meetingMemberDto.setNickName(nickName);
        meetingMemberDto.setJoinTime(System.currentTimeMillis());
        meetingMemberDto.setMemberType(memberType);
        meetingMemberDto.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        meetingMemberDto.setOpenVideo(videoOpen);
        meetingMemberDto.setSex(sex);
        redisComponent.add2Meeting(meetingId, meetingMemberDto);
    }

    private void checkMeetingJoin(String meetingId, String userId) {
        MeetingMemberDto meetingMemberDto = redisComponent.getMeetingMember(meetingId, userId);
        if (meetingMemberDto != null || MeetingMemberStatusEnum.BLACKLIST.getStatus().equals(meetingMemberDto.getStatus())) {
            throw new BusinessException("你已经被拉黑，无法加入会议");
        }
    }

    @Override
    public void joinMeeting(String meetingId, String userId, String nickName, Integer sex, Boolean videoOpen) {
        if (StringTools.isEmpty(meetingId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        MeetingInfo meetingInfo = this.getMeetingInfoByMeetingId(meetingId);
        if (meetingInfo == null || MeetingStatusEnum.FINISHED.getStatus().equals(meetingInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        this.checkMeetingJoin(meetingId, userId);

        MemberTypeEnum memberTypeEnum = meetingInfo.getCreateUserId().equals(userId) ? MemberTypeEnum.COMPERE : MemberTypeEnum.NORMAL;
        this.addMeetingMember(meetingId, userId, nickName, memberTypeEnum.getType());

        this.add2Meeting(meetingId, userId, nickName, sex, memberTypeEnum.getType(), videoOpen);

        channelContextUtils.addMeetingRoom(meetingId, userId);
        //发送ws消息
        MeetingJoinDto meetingJoinDto = new MeetingJoinDto();
        meetingJoinDto.setNewMember(redisComponent.getMeetingMember(meetingId, userId));
        meetingJoinDto.setMeetingMemberList(redisComponent.getMeetingMemberList(meetingId));

        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypesEnum.ADD_MEETING_ROOM.getType());
        messageSendDto.setMeetingId(meetingId);
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.GROUP.getType());
        messageSendDto.setMessageContent(meetingJoinDto);
        messageHandler.sendMessage(messageSendDto);
    }

    @Override
    public String preJoinMeeting(String meetingNo, TokenUserInfoDto tokenUserInfoDto, String password) {
        String userId = tokenUserInfoDto.getUserId();
        MeetingInfoQuery meetingInfoQuery = new MeetingInfoQuery();
        meetingInfoQuery.setMeetingNo(meetingNo);
        meetingInfoQuery.setStatus(MeetingStatusEnum.RUNNING.getStatus());
        meetingInfoQuery.setOrderBy("create_time desc");

        List<MeetingInfo> meetingInfoList = meetingInfoMapper.selectList(meetingInfoQuery);
        if (meetingInfoList == null || meetingInfoList.size() == 0) {
            throw new BusinessException("会议不存在");
        }
        MeetingInfo meetingInfo = meetingInfoList.get(0);

        if (MeetingStatusEnum.RUNNING.getStatus().equals(meetingInfo.getStatus())) {
            throw new BusinessException("会议已结束");
        }
        if (!StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId()) && !meetingInfo.getMeetingId().equals(tokenUserInfoDto.getCurrentMeetingId())) {
            throw new BusinessException("您当前有会议正在进行中，请先结束当前会议");
        }
        checkMeetingJoin(meetingInfo.getMeetingId(), userId);

        if (MeetingJoinTypeEnum.PASSWORD.getType().equals(meetingInfo.getJoinType()) && !meetingInfo.getJoinPassword().equals(password)) {
            throw new BusinessException("会议密码错误");
        }
        tokenUserInfoDto.setCurrentMeetingId(meetingInfo.getMeetingId());
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
        return meetingInfo.getMeetingId();
    }

    @Override
    public void exitMeetingRoom(TokenUserInfoDto tokenUserInfoDto, MeetingMemberStatusEnum statusEnum) {
        String meetingId = tokenUserInfoDto.getCurrentMeetingId();
        if (StringTools.isEmpty(meetingId)) {
            return;
        }
        String userId = tokenUserInfoDto.getUserId();
        Boolean exit = redisComponent.exitMeeting(meetingId, userId, statusEnum);
        if (!exit) {
            tokenUserInfoDto.setCurrentMeetingId(null);
            redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
            return;
        }
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypesEnum.EXIT_MEETING_ROOM.getType());
        //清空当前正在进行的会议
        tokenUserInfoDto.setCurrentMeetingId(null);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        List<MeetingMemberDto> meetingMemberDtoList = redisComponent.getMeetingMemberList(meetingId);
        MeetingExitDto exitDto = new MeetingExitDto();
        exitDto.setMeetingMemberList(meetingMemberDtoList);
        exitDto.setExitUserId(userId);
        exitDto.setExitStatus(statusEnum.getStatus());
        messageSendDto.setMessageContent(JsonUtils.convertObj2Json(exitDto));
        messageSendDto.setMeetingId(meetingId);
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.GROUP.getType());
        messageHandler.sendMessage(messageSendDto);

        List<MeetingMemberDto> onLineMemberList = (List<MeetingMemberDto>) meetingMemberDtoList.stream()
                .filter(item -> MeetingMemberStatusEnum.NORMAL.getStatus().equals(item.getStatus()));
        if (onLineMemberList.isEmpty()) {
            //TODO 退出结束会议
            return;
        }
        if (ArrayUtils.contains(new Integer[]{MeetingMemberStatusEnum.KICK_OUT.getStatus(), MeetingMemberStatusEnum.BLACKLIST.getStatus()}, statusEnum.getStatus())) {
            MeetingMember meetingMember = new MeetingMember();
            meetingMember.setStatus(statusEnum.getStatus());
            meetingMemberMapper.updateByMeetingIdAndUserId(meetingMember, meetingId, userId);
        }
    }
}
