package com.easymeeting.controller;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.vo.CheckCodeVO;
import com.easymeeting.entity.vo.ResponseVO;

import com.easymeeting.entity.po.UserInfo;
import com.easymeeting.entity.query.UserInfoQuery;
import com.easymeeting.entity.vo.UserInfoVO;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.redis.RedisComponent;
import com.easymeeting.redis.RedisUtils;
import com.easymeeting.service.UserInfoService;


import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Description: UserInfoController
 * @author: klein
 * @data: 2026/03/15
 */

@RestController
@RequestMapping("/account")
@Validated
@Slf4j
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;
    @Autowired
    private RedisUtils redisUtils;

    @RequestMapping("/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code);
        String checkCodeBase64 = captcha.toBase64();
        CheckCodeVO checkCodeVO = new CheckCodeVO();
        checkCodeVO.setCheckCode(checkCodeBase64);
        checkCodeVO.setCheckCodeKey(checkCodeKey);
        return getSuccessResponseVO(checkCodeVO);
    }

    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey, @NotEmpty @Email String email,
                               @NotEmpty @Size(max = 20) String password,
                               @NotEmpty @Size(max = 20) String nickName,
                               @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            this.userInfoService.register(email, nickName, password);
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey, @NotEmpty @Email String email,
                            @NotEmpty @Size(max = 32) String password, @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            UserInfoVO userInfoVO = this.userInfoService.login(email, password);
            return getSuccessResponseVO(userInfoVO);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }


}
