package com.easymeeting.controller;

import com.easymeeting.annotation.GlobalInterceptor;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.po.MeetingInfo;
import com.easymeeting.entity.query.MeetingInfoQuery;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.service.MeetingInfoService;
import com.easymeeting.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
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

    @RequestMapping("/getCurrentMeeting")
    @GlobalInterceptor
    public ResponseVO getCurrentMeeting() {
        return getSuccessResponseVO(null);
    }

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
                                   @NotNull Integer joinType, @Max(5) String joinPassword) {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        if (tokenUserInfoDto.getCurrentMeetingId() != null) {
            throw new BusinessException("您当前有会议正在进行中，请先结束当前会议");
        }
        MeetingInfo meetingInfo = new MeetingInfo();
        meetingInfo.setMeetingName(meetingName);
        meetingInfo.setMeetingNo(meetingNoType == 0 ? tokenUserInfoDto.getMyMeetingNo() : StringTools.getMeetingNoOrMeetingId());
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

        meetingNo=meetingNo.replace(" ", "");
        tokenUserInfoDto.setCurrentMeetingId(meetingNo);
        String meetingId = meetingInfoService.preJoinMeeting(meetingNo,tokenUserInfoDto,password);

        return getSuccessResponseVO(meetingId);
    }


    @RequestMapping("/joinMeeting")
    @GlobalInterceptor
    public ResponseVO joinMeeting(@NotNull Boolean videoOpen) {

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        if (tokenUserInfoDto.getCurrentMeetingId() != null) {
            throw new BusinessException("您当前有会议正在进行中，请先结束当前会议");
        }
        meetingInfoService.joinMeeting(tokenUserInfoDto.getCurrentMeetingId(), tokenUserInfoDto.getUserId(),
                tokenUserInfoDto.getNickName(), tokenUserInfoDto.getSex(), videoOpen);

        return getSuccessResponseVO(null);
    }
}
