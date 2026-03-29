package com.easymeeting.mappers;

import com.easymeeting.entity.po.UserContact;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserContactMapper {

    Integer insert(@Param("bean") UserContact userContact);

    Integer insertOrUpdate(@Param("bean") UserContact userContact);

    UserContact selectByUserIdAndContactId(@Param("userId") String userId, @Param("contactId") String contactId);

    List<UserContact> selectByUserId(@Param("userId") String userId);
}
