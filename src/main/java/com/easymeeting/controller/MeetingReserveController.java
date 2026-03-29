package com.easymeeting.controller;

import com.easymeeting.annotation.GlobalInterceptor;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.po.MeetingInfo;
import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.service.MeetingInfoService;
import com.easymeeting.utils.DataUtils;
import com.easymeeting.utils.StringTools;
import com.easymeeting.enums.DateTimePatternEnum;
import com.easymeeting.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RestController
@RequestMapping({ "/meetingReserve", "/userContact" })
@Validated
@Slf4j
public class MeetingReserveController extends ABaseController {

    @Resource
    private MeetingInfoService meetingInfoService;

    @RequestMapping("/reserveMeeting")
    @GlobalInterceptor
    public ResponseVO reserveMeeting(@NotNull Integer meetingNoType, @NotEmpty @Size(max = 100) String meetingName,
            @NotNull Integer joinType, @Size(max = 5) String joinPassword,
            @NotEmpty String startTime) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        if (!StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())) {
            throw new BusinessException("您当前有会议正在进行中，请先结束当前会议");
        }
        MeetingInfo meetingInfo = new MeetingInfo();
        meetingInfo.setMeetingName(meetingName);
        meetingInfo.setMeetingNo(
                meetingNoType == 0 ? tokenUserInfoDto.getMyMeetingNo() : StringTools.getMeetingNoOrMeetingId());
        meetingInfo.setJoinType(joinType);
        meetingInfo.setJoinPassword(joinPassword);
        meetingInfo.setCreateUserId(tokenUserInfoDto.getUserId());
        meetingInfo.setStartTime(DataUtils.parse(startTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
        if (meetingInfo.getStartTime() == null) {
            throw new BusinessException("预约时间格式错误，请使用 yyyy-MM-dd HH:mm:ss");
        }
        meetingInfoService.reserveMeeting(meetingInfo, tokenUserInfoDto.getNickName());
        return getSuccessResponseVO(meetingInfo.getMeetingId());
    }

    @RequestMapping("/loadTodayMeeting")
    @GlobalInterceptor
    public ResponseVO loadTodayMeeting() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        return getSuccessResponseVO(meetingInfoService.loadTodayMeeting(tokenUserInfoDto.getUserId()));
    }

    @RequestMapping("/cancelReserveMeeting")
    @GlobalInterceptor
    public ResponseVO cancelReserveMeeting(@NotEmpty String meetingId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        meetingInfoService.cancelReserveMeeting(meetingId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
