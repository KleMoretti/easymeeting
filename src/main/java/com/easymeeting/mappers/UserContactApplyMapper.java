package com.easymeeting.mappers;

import com.easymeeting.entity.po.UserContactApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserContactApplyMapper {

        Integer insert(@Param("bean") UserContactApply userContactApply);

        UserContactApply selectByApplyId(@Param("applyId") String applyId);

        UserContactApply selectPendingByUsers(@Param("applyUserId") String applyUserId,
                        @Param("receiveUserId") String receiveUserId);

        UserContactApply selectLatestByUsers(@Param("applyUserId") String applyUserId,
                        @Param("receiveUserId") String receiveUserId);

        Integer updateByApplyId(@Param("bean") UserContactApply userContactApply, @Param("applyId") String applyId);

        Integer selectDealWithCount(@Param("receiveUserId") String receiveUserId);

        List<UserContactApply> selectListByReceiveUserId(@Param("receiveUserId") String receiveUserId,
                        @Param("status") Integer status);
}
