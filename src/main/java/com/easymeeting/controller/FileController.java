package com.easymeeting.controller;

import com.easymeeting.annotation.GlobalInterceptor;
import com.easymeeting.entity.config.AppConfig;
import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.po.MeetingFileRecord;
import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.mappers.MeetingFileRecordMapper;
import com.easymeeting.service.MeetingInfoService;
import com.easymeeting.utils.DataUtils;
import com.easymeeting.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
@Validated
@Slf4j
public class FileController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private MeetingInfoService meetingInfoService;

    @Resource
    private MeetingFileRecordMapper meetingFileRecordMapper;

    @RequestMapping("/upload")
    @GlobalInterceptor
    public ResponseVO upload(@NotEmpty String meetingId, @NotNull Integer fileType, @NotNull MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件为空");
        }
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        if (tokenUserInfoDto == null || StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())
                || !meetingId.equals(tokenUserInfoDto.getCurrentMeetingId())) {
            throw new BusinessException("仅支持在当前会议上传文件");
        }
        if (meetingInfoService.getMeetingInfoByMeetingId(meetingId) == null) {
            throw new BusinessException("会议不存在");
        }

        String fileId = StringTools.getRandomNumber(20);
        String originalFileName = normalizeFileName(file.getOriginalFilename());
        String fileSuffix = getFileSuffix(originalFileName);
        String relativePath = buildRelativePath(meetingId, fileId, fileSuffix);
        File targetFile = new File(getFileRootPath(), relativePath);
        try {
            File parentFile = targetFile.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            file.transferTo(targetFile);

            MeetingFileRecord meetingFileRecord = new MeetingFileRecord();
            meetingFileRecord.setFileId(fileId);
            meetingFileRecord.setMeetingId(meetingId);
            meetingFileRecord.setUploadUserId(tokenUserInfoDto.getUserId());
            meetingFileRecord.setFileName(originalFileName);
            meetingFileRecord.setFilePath(relativePath);
            meetingFileRecord.setFileSize(file.getSize());
            meetingFileRecord.setFileType(fileType);
            meetingFileRecord.setFileSuffix(fileSuffix);
            meetingFileRecord.setCreateTime(new Date());
            meetingFileRecordMapper.insert(meetingFileRecord);

            Map<String, Object> result = new HashMap<>();
            result.put("fileId", fileId);
            result.put("meetingId", meetingId);
            result.put("fileName", originalFileName);
            result.put("fileSize", file.getSize());
            result.put("fileType", fileType);
            result.put("fileSuffix", fileSuffix);
            result.put("downloadUrl", "/file/download?fileId=" + fileId);
            return getSuccessResponseVO(result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("上传会议文件失败, meetingId={}, fileName={}", meetingId, originalFileName, e);
            throw new BusinessException("上传会议文件失败");
        }
    }

    @RequestMapping("/download")
    @GlobalInterceptor
    public void download(@NotEmpty String fileId, HttpServletResponse response) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo();
        MeetingFileRecord meetingFileRecord = meetingFileRecordMapper.selectByFileId(fileId);
        if (meetingFileRecord == null) {
            throw new BusinessException("文件记录不存在");
        }
        if (tokenUserInfoDto == null || StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())
                || !meetingFileRecord.getMeetingId().equals(tokenUserInfoDto.getCurrentMeetingId())) {
            throw new BusinessException("仅支持下载当前会议文件");
        }

        File fileRootPath = new File(getFileRootPath());
        File targetFile = new File(fileRootPath, meetingFileRecord.getFilePath());
        try {
            String rootCanonicalPath = fileRootPath.getCanonicalPath();
            String targetCanonicalPath = targetFile.getCanonicalPath();
            String expectedPrefix = rootCanonicalPath.endsWith(File.separator)
                    ? rootCanonicalPath
                    : rootCanonicalPath + File.separator;
            if (!targetCanonicalPath.startsWith(expectedPrefix)) {
                throw new BusinessException("下载路径非法");
            }
            if (!targetFile.exists() || !targetFile.isFile()) {
                throw new BusinessException("文件不存在或已删除");
            }

            String fileName = meetingFileRecord.getFileName();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
            response.setContentLengthLong(targetFile.length());
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(targetFile));
                    OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("下载会议文件失败, fileId={}", fileId, e);
            throw new BusinessException("下载会议文件失败");
        }
    }

    private String getFileRootPath() {
        String projectFolder = appConfig.getProjectFolder();
        if (StringTools.isEmpty(projectFolder)) {
            throw new BusinessException("未配置项目目录");
        }
        return projectFolder + Constants.FILE_FOLDER_FILE;
    }

    private String buildRelativePath(String meetingId, String fileId, String fileSuffix) {
        String dateFolder = DataUtils.format(new Date(), "yyyy-MM-dd");
        return meetingId + "/" + dateFolder + "/" + fileId + fileSuffix;
    }

    private String normalizeFileName(String originalFileName) {
        if (StringTools.isEmpty(originalFileName)) {
            return "unnamed";
        }
        String normalized = originalFileName.replace("\\", "/");
        int lastIndex = normalized.lastIndexOf('/');
        if (lastIndex >= 0) {
            normalized = normalized.substring(lastIndex + 1);
        }
        return normalized;
    }

    private String getFileSuffix(String fileName) {
        if (StringTools.isEmpty(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        String suffix = fileName.substring(index);
        return suffix.length() > 20 ? suffix.substring(0, 20) : suffix;
    }
}
