package com.easymeeting.service.impl;

import com.easymeeting.entity.config.AppConfig;
import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.query.SimplePage;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.entity.vo.UserInfoVO;
import com.easymeeting.enums.PageSize;
import com.easymeeting.entity.po.UserInfo;
import com.easymeeting.entity.query.UserInfoQuery;
import com.easymeeting.enums.UserStatusEnum;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.mappers.UserInfoMapper;
import com.easymeeting.redis.RedisComponent;
import com.easymeeting.service.UserInfoService;
import com.easymeeting.utils.CopyTools;
import com.easymeeting.utils.StringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

/**
 * @Description: UserInfoService
 * @author: klein
 * @data: 2026/03/15
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询数量
     */
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    /**
     * 分页查询
     */
    public PageinationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        Integer count = this.findCountByParam(param);
        Integer pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(param);
        PageinationResultVO<UserInfo> result = new PageinationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }


    /**
     * 根据UserId查询
     */
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId更新
     */
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email查询
     */
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email更新
     */
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    @Override
    public void register(String email, String nickName, String password) {
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (userInfo != null) {
            throw new BusinessException("账号邮箱已经存在");
        }
        Date curDate = new Date();

        String userId = StringTools.getRandomNumber(Constants.LENGTH_12);

        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setEmail(email);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringTools.encodeByMD5(password));
        userInfo.setCreateTime(curDate);
        userInfo.setLastOffTime(curDate.getTime());
        userInfo.setMeetingNo(StringTools.getMeetingNoOrMeetingId());

        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        this.userInfoMapper.insert(userInfo);
    }


    @Override
    public UserInfoVO login(String email, String password) {
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (null == userInfo || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或密码错误");
        }

        if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        if (userInfo.getLastLoginTime()!=null&&userInfo.getLastOffTime() <= userInfo.getLastLoginTime()) {
            throw new BusinessException("账号已在其他设备登录");
        }

        TokenUserInfoDto tokenUserInfoDto = CopyTools.copy(userInfo, TokenUserInfoDto.class);

        String token = StringTools.encodeByMD5(tokenUserInfoDto.getUserId() + StringTools.getRandomString(Constants.LENGTH_20));
        tokenUserInfoDto.setToken(token);
        tokenUserInfoDto.setMyMeetingNo(userInfo.getMeetingNo());
        tokenUserInfoDto.setAdmin(appConfig.getAdminEmail().contains(email));

        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setToken(token);
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
        return userInfoVO;
    }

}
