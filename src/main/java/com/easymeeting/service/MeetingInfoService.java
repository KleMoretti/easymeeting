package com.easymeeting.service;

import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.entity.po.MeetingInfo;
import com.easymeeting.entity.po.MeetingInviteRecord;
import com.easymeeting.entity.query.MeetingInfoQuery;
import com.easymeeting.enums.MeetingMemberStatusEnum;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description: Service
 * @author: klein
 * @data: 2026/03/16
 */
public interface MeetingInfoService {

    /**
     * 根据条件查询列表
     */
    List<MeetingInfo> findListByParam(MeetingInfoQuery param);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(MeetingInfoQuery param);

    /**
     * 分页查询
     */
    PageinationResultVO<MeetingInfo> findListByPage(MeetingInfoQuery param);

    /**
     * 新增
     */
    Integer add(MeetingInfo bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<MeetingInfo> listBean);

    /**
     * 批量新增或修改
     */
    Integer addOrUpdateBatch(List<MeetingInfo> listBean);

    /**
     * 根据MeetingId查询
     */
    MeetingInfo getMeetingInfoByMeetingId(String meetingId);

    /**
     * 根据MeetingId更新
     */
    Integer updateMeetingInfoByMeetingId(MeetingInfo bean, String meetingId);

    /**
     * 根据MeetingId删除
     */
    Integer deleteMeetingInfoByMeetingId(String meetingId);

    void quickMeeting(MeetingInfo meetingInfo, String nickName);

    void joinMeeting(String meetingId, String userId, String nickName, Integer sex, Boolean videoOpen,
            Boolean audioOpen);

    String preJoinMeeting(String meetingNo, TokenUserInfoDto tokenUserInfoDto, String password);

    void exitMeetingRoom(TokenUserInfoDto tokenUserInfoDto, MeetingMemberStatusEnum statusEnum);

    void forceExitMeeting(TokenUserInfoDto tokenUserInfoDto, String userId, MeetingMemberStatusEnum statusEnum);

    void finishMeeting(String meetingId, String userId);

    void reserveMeeting(MeetingInfo meetingInfo, String nickName);

    void cancelReserveMeeting(String meetingId, String userId);

    List<MeetingInfo> loadTodayMeeting(String userId);

    void inviteMemberMeeting(String meetingId, String inviteUserId, String receiveUserId, String inviteMessage);

    List<MeetingInviteRecord> loadMyPendingInviteList(String receiveUserId);

    void rejectInvite(String inviteId, String receiveUserId);

    void cancelInvite(String inviteId, String inviteUserId);

    void updateMediaStatus(TokenUserInfoDto tokenUserInfoDto, Boolean videoOpen, Boolean audioOpen);

}
