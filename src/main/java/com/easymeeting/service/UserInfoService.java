package com.easymeeting.service;

import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.entity.po.UserInfo;
import com.easymeeting.entity.query.UserInfoQuery;
import com.easymeeting.entity.vo.UserInfoVO;

import java.util.List;

/**
 * @Description: UserInfoService
 * @author: klein
 * @data: 2026/03/15
 */
public interface UserInfoService{

	/**
	 * 根据条件查询列表
	 */
	List<UserInfo> findListByParam(UserInfoQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserInfoQuery param);

	/**
	 * 分页查询
	 */
	PageinationResultVO<UserInfo> findListByPage(UserInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(UserInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserInfo> listBean );

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<UserInfo> listBean );


	/**
	 * 根据UserId查询
	 */
	UserInfo getUserInfoByUserId(String userId);

	/**
	 * 根据UserId更新
	 */
	Integer updateUserInfoByUserId( UserInfo bean, String userId);

	/**
	 * 根据UserId删除
	 */
	Integer deleteUserInfoByUserId(String userId);

	/**
	 * 根据Email查询
	 */
	UserInfo getUserInfoByEmail(String email);

	/**
	 * 根据Email更新
	 */
	Integer updateUserInfoByEmail( UserInfo bean, String email);

	/**
	 * 根据Email删除
	 */
	Integer deleteUserInfoByEmail(String email);

	void register(String email,String nickName,String password);

	UserInfoVO login(String email, String password);


}
