package com.easymeeting.controller;

import com.easymeeting.annotation.GlobalInterceptor;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.service.UserContactService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RestController
@RequestMapping("/userContact")
@Validated
@Slf4j
public class UserContactController extends ABaseController {

    @Resource
    private UserContactService userContactService;

    @RequestMapping("/loadContactApplicationDealWithCount")
    @GlobalInterceptor
    public ResponseVO loadContactApplicationDealWithCount() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        return getSuccessResponseVO(
                userContactService.loadContactApplicationDealWithCount(tokenUserInfoDto.getUserId()));
    }

    @RequestMapping("/applyContact")
    @GlobalInterceptor
    public ResponseVO applyContact(@NotEmpty String meetingNo, @Size(max = 200) String applyMessage) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        userContactService.applyContact(tokenUserInfoDto.getUserId(), meetingNo, applyMessage);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/dealContactApply")
    @GlobalInterceptor
    public ResponseVO dealContactApply(@NotEmpty String applyId, @NotNull Integer status) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        userContactService.dealContactApply(applyId, status, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadApplyList")
    @GlobalInterceptor
    public ResponseVO loadApplyList(Integer status) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        return getSuccessResponseVO(userContactService.loadApplyList(tokenUserInfoDto.getUserId(), status));
    }

    @RequestMapping("/loadContactList")
    @GlobalInterceptor
    public ResponseVO loadContactList() {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        return getSuccessResponseVO(userContactService.loadContactList(tokenUserInfoDto.getUserId()));
    }
}
