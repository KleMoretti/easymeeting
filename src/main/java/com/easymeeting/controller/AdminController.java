package com.easymeeting.controller;

import com.easymeeting.annotation.GlobalInterceptor;
import com.easymeeting.entity.po.MeetingInfo;
import com.easymeeting.entity.po.UserInfo;
import com.easymeeting.entity.query.MeetingInfoQuery;
import com.easymeeting.entity.query.UserInfoQuery;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.enums.DateTimePatternEnum;
import com.easymeeting.enums.MeetingStatusEnum;
import com.easymeeting.service.MeetingInfoService;
import com.easymeeting.service.UserInfoService;
import com.easymeeting.utils.DataUtils;
import com.easymeeting.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Validated
@Slf4j
public class AdminController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private MeetingInfoService meetingInfoService;

    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUserList(Integer pageNo, @Size(max = 50) String keyword) {
        UserInfoQuery query = new UserInfoQuery();
        query.setPageNo(pageNo);
        query.setOrderBy("create_time desc");
        if (!StringTools.isEmpty(keyword)) {
            query.setEmailFuzzy(keyword);
            query.setNickNameFuzzy(keyword);
        }
        PageinationResultVO<UserInfo> resultVO = userInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/loadMeetingList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadMeetingList(Integer pageNo, @Size(max = 100) String meetingName, Integer status) {
        MeetingInfoQuery query = new MeetingInfoQuery();
        query.setPageNo(pageNo);
        query.setOrderBy("create_time desc");
        query.setStatus(status);
        query.setMeetingNameFuzzy(meetingName);
        query.setQueryMemberCount(true);
        PageinationResultVO<MeetingInfo> resultVO = meetingInfoService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/forceFinishMeeting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceFinishMeeting(@NotEmpty String meetingId) {
        meetingInfoService.finishMeeting(meetingId, null);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadDashboard")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        Integer userCount = userInfoService.findCountByParam(new UserInfoQuery());

        Integer totalMeetingCount = meetingInfoService.findCountByParam(new MeetingInfoQuery());

        MeetingInfoQuery runningMeetingQuery = new MeetingInfoQuery();
        runningMeetingQuery.setStatus(MeetingStatusEnum.RUNNING.getStatus());
        Integer runningMeetingCount = meetingInfoService.findCountByParam(runningMeetingQuery);

        MeetingInfoQuery reservedMeetingQuery = new MeetingInfoQuery();
        reservedMeetingQuery.setStatus(MeetingStatusEnum.RESERVED.getStatus());
        Integer reservedMeetingCount = meetingInfoService.findCountByParam(reservedMeetingQuery);

        String today = DataUtils.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        MeetingInfoQuery todayMeetingQuery = new MeetingInfoQuery();
        todayMeetingQuery.setStartTimeStart(today);
        todayMeetingQuery.setStartTimeEnd(today);
        Integer todayMeetingCount = meetingInfoService.findCountByParam(todayMeetingQuery);

        dashboard.put("userCount", userCount == null ? 0 : userCount);
        dashboard.put("totalMeetingCount", totalMeetingCount == null ? 0 : totalMeetingCount);
        dashboard.put("runningMeetingCount", runningMeetingCount == null ? 0 : runningMeetingCount);
        dashboard.put("reservedMeetingCount", reservedMeetingCount == null ? 0 : reservedMeetingCount);
        dashboard.put("todayMeetingCount", todayMeetingCount == null ? 0 : todayMeetingCount);

        return getSuccessResponseVO(dashboard);
    }
}
