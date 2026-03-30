package com.easymeeting.mappers;

import com.easymeeting.entity.po.MeetingFileRecord;
import org.apache.ibatis.annotations.Param;

public interface MeetingFileRecordMapper {

    Integer insert(@Param("bean") MeetingFileRecord meetingFileRecord);

    MeetingFileRecord selectByFileId(@Param("fileId") String fileId);
}
