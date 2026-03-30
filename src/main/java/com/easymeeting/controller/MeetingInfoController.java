package com.easymeeting.controller;

import com.easymeeting.annotation.GlobalInterceptor;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.po.MeetingInfo;
import com.easymeeting.entity.query.MeetingInfoQuery;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.enums.MeetingMemberStatusEnum;
import com.easymeeting.enums.MeetingStatusEnum;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.service.MeetingInfoService;
import com.easymeeting.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.*;

@RestController
@RequestMapping("/meeting")
@Validated
@Slf4j
public class MeetingInfoController extends ABaseController {

    @Resource
    private MeetingInfoService meetingInfoService;

    @RequestMapping("/loadMeeting")
    @GlobalInterceptor
    public ResponseVO loadMeeting(Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        MeetingInfoQuery infoQuery = new MeetingInfoQuery();
        infoQuery.setUserId(tokenUserInfoDto.getUserId());
        infoQuery.setPageNo(pageNo);
        infoQuery.setOrderBy("create_time desc");
        infoQuery.setQueryMemberCount(true);
        PageinationResultVO resultVO = this.meetingInfoService.findListByPage(infoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/quickMeeting")
    @GlobalInterceptor
    public ResponseVO quickMeeting(@NotNull Integer meetingNoType, @NotEmpty @Size(max = 100) String meetingName,
            @NotNull Integer joinType, @Size(max = 5) String joinPassword) {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        if (tokenUserInfoDto.getCurrentMeetingId() != null) {
            throw new BusinessException("您当前有会议正在进行中，请先结束当前会议");
        }
        MeetingInfo meetingInfo = new MeetingInfo();
        meetingInfo.setMeetingName(meetingName);
        meetingInfo.setMeetingNo(
                meetingNoType == 0 ? tokenUserInfoDto.getMyMeetingNo() : StringTools.getMeetingNoOrMeetingId());
        meetingInfo.setJoinType(joinType);
        meetingInfo.setJoinPassword(joinPassword);
        meetingInfo.setCreateUserId(tokenUserInfoDto.getUserId());
        meetingInfoService.quickMeeting(meetingInfo, tokenUserInfoDto.getNickName());

        tokenUserInfoDto.setCurrentMeetingId(meetingInfo.getMeetingId());
        tokenUserInfoDto.setCurrentNickName(tokenUserInfoDto.getNickName());

        resetTokenUserInfo(tokenUserInfoDto);

        return getSuccessResponseVO(meetingInfo.getMeetingId());
    }

    @RequestMapping("/preJoinMeeting")
    @GlobalInterceptor
    public ResponseVO preJoinMeeting(@NotNull String meetingNo, @NotEmpty String nickName, String password) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();

        meetingNo = meetingNo.replace(" ", "");
        tokenUserInfoDto.setCurrentNickName(nickName);
        String meetingId = meetingInfoService.preJoinMeeting(meetingNo, tokenUserInfoDto, password);

        return getSuccessResponseVO(meetingId);
    }

    @RequestMapping("/joinMeeting")
    @GlobalInterceptor
    public ResponseVO joinMeeting(@NotNull Boolean videoOpen, Boolean audioOpen) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        if (tokenUserInfoDto.getCurrentMeetingId() == null) {
            throw new BusinessException("请先输入会议号并校验后再加入会议");
        }
        meetingInfoService.joinMeeting(tokenUserInfoDto.getCurrentMeetingId(), tokenUserInfoDto.getUserId(),
                tokenUserInfoDto.getNickName(), tokenUserInfoDto.getSex(), videoOpen, audioOpen);

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/inviteMember")
    @GlobalInterceptor
    public ResponseVO inviteMember(@NotEmpty String receiveUserId, @Size(max = 200) String inviteMessage) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        meetingInfoService.inviteMemberMeeting(tokenUserInfoDto.getCurrentMeetingId(), tokenUserInfoDto.getUserId(),
                receiveUserId, inviteMessage);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadMyPendingInviteList")
    @GlobalInterceptor
    public ResponseVO loadMyPendingInviteList() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        return getSuccessResponseVO(meetingInfoService.loadMyPendingInviteList(tokenUserInfoDto.getUserId()));
    }

    @RequestMapping("/rejectInvite")
    @GlobalInterceptor
    public ResponseVO rejectInvite(@NotEmpty String inviteId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        meetingInfoService.rejectInvite(inviteId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/cancelInvite")
    @GlobalInterceptor
    public ResponseVO cancelInvite(@NotEmpty String inviteId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        meetingInfoService.cancelInvite(inviteId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateMediaStatus")
    @GlobalInterceptor
    public ResponseVO updateMediaStatus(Boolean videoOpen, Boolean audioOpen) {
        if (videoOpen == null && audioOpen == null) {
            throw new BusinessException("至少需要更新一种媒体状态");
        }
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        meetingInfoService.updateMediaStatus(tokenUserInfoDto, videoOpen, audioOpen);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/exitMeeting")
    @GlobalInterceptor
    public ResponseVO exitMeeting() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        meetingInfoService.exitMeetingRoom(tokenUserInfoDto, MeetingMemberStatusEnum.EXIT_MEETING);

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/kickOutMeeting")
    @GlobalInterceptor
    public ResponseVO kickOutMeeting(@NotEmpty String userId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        meetingInfoService.forceExitMeeting(tokenUserInfoDto, userId, MeetingMemberStatusEnum.KICK_OUT);

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/blackMeeting")
    @GlobalInterceptor
    public ResponseVO blackMeeting(@NotEmpty String userId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        meetingInfoService.forceExitMeeting(tokenUserInfoDto, userId, MeetingMemberStatusEnum.BLACKLIST);

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getCurrentMeeting")
    @GlobalInterceptor
    public ResponseVO getCurrentMeeting(@NotEmpty String userId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        if (StringUtils.isEmpty(tokenUserInfoDto.getCurrentMeetingId())) {
            return getSuccessResponseVO(null);
        }
        MeetingInfo meetingInfo = this.meetingInfoService
                .getMeetingInfoByMeetingId(tokenUserInfoDto.getCurrentMeetingId());
        if (meetingInfo == null) {
            return getSuccessResponseVO(null);
        }
        if (MeetingStatusEnum.FINISHED.getStatus().equals(meetingInfo.getStatus())) {
            return getSuccessResponseVO(null);
        }
        return getSuccessResponseVO(meetingInfo);
    }

    @RequestMapping("/finishMeeting")
    @GlobalInterceptor
    public ResponseVO finishMeeting() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        meetingInfoService.finishMeeting(tokenUserInfoDto.getCurrentMeetingId(), tokenUserInfoDto.getUserId());

        return getSuccessResponseVO(null);
    }

}
