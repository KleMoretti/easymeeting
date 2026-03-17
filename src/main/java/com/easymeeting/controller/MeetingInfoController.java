package com.easymeeting.controller;

import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.query.MeetingInfoQuery;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.service.MeetingInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@RestController
@RequestMapping("/meeting")
@Validated
@Slf4j
public class MeetingInfoController extends ABaseController {

    @Resource
    private MeetingInfoService meetingInfoService;

    @RequestMapping("/getCurrentMeeting")
    public ResponseVO getCurrentMeeting() {
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadMeeting")
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
    public ResponseVO quickMeeting(Integer meetingNo ,String meetingName) {

        return null;
    }

}
