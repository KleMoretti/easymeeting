package com.easymeeting.controller;

import com.easymeeting.annotation.GlobalInterceptor;
import com.easymeeting.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userContact")
@Validated
@Slf4j
public class UserContactController extends ABaseController{
    @RequestMapping("/loadContactApplicationDealWithCount")
    @GlobalInterceptor
    public ResponseVO loadContactApplicationDealWithCount() {
        return getSuccessResponseVO(null);
    }
}
