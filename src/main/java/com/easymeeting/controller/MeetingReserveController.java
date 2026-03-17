package com.easymeeting.controller;

import com.easymeeting.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userContact")
@Validated
@Slf4j
public class MeetingReserveController extends ABaseController {
    @RequestMapping("/loadTodayMeeting")
    public ResponseVO loadTodayMeeting() {
        return getSuccessResponseVO(null);
    }
}
