package com.easymeeting.controller;

import com.easymeeting.annotation.GlobalInterceptor;
import com.easymeeting.entity.config.AppConfig;
import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.entity.vo.UpdateVersionVO;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@RestController
@RequestMapping("/update")
@Validated
@Slf4j
public class UpdateController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @RequestMapping("/checkVersion")
    @GlobalInterceptor(checkLogin = false)
    public ResponseVO checkVersion(@Size(max = 50) String currentVersion) {
        UpdateVersionVO versionVO = new UpdateVersionVO();
        versionVO.setCurrentVersion(currentVersion);
        File latestFile = getLatestInstallPackage();
        if (latestFile == null) {
            versionVO.setNeedUpdate(false);
            return getSuccessResponseVO(versionVO);
        }

        String latestVersion = extractVersion(latestFile.getName());
        versionVO.setLatestVersion(latestVersion);
        versionVO.setFileName(latestFile.getName());
        versionVO.setDownloadUrl("/update/download?fileName="
                + URLEncoder.encode(latestFile.getName(), StandardCharsets.UTF_8.name()));

        boolean needUpdate = StringTools.isEmpty(currentVersion) || compareVersion(latestVersion, currentVersion) > 0;
        versionVO.setNeedUpdate(needUpdate);

        return getSuccessResponseVO(versionVO);
    }

    @RequestMapping("/download")
    @GlobalInterceptor(checkLogin = false)
    public void download(@NotEmpty String fileName, HttpServletResponse response) {
        if (!fileName.startsWith(Constants.APP_NAME) || !fileName.endsWith(Constants.APP_EXE_SUFFIX)) {
            throw new BusinessException("安装包名称非法");
        }
        File updateDir = new File(getUpdateFolderPath());
        File targetFile = new File(updateDir, fileName);
        try {
            String updateCanonicalPath = updateDir.getCanonicalPath();
            String targetCanonicalPath = targetFile.getCanonicalPath();
            String expectedPrefix = updateCanonicalPath.endsWith(File.separator)
                    ? updateCanonicalPath
                    : updateCanonicalPath + File.separator;
            if (!targetCanonicalPath.startsWith(expectedPrefix)) {
                throw new BusinessException("下载路径非法");
            }
            if (!targetFile.exists() || !targetFile.isFile()) {
                throw new BusinessException("安装包不存在");
            }

            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
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
            log.error("下载安装包失败, fileName={}", fileName, e);
            throw new BusinessException("下载安装包失败");
        }
    }

    private String getUpdateFolderPath() {
        String projectFolder = appConfig.getProjectFolder();
        if (StringTools.isEmpty(projectFolder)) {
            throw new BusinessException("未配置项目目录");
        }
        String updateFolder = Constants.APP_UPDATE_FOLDER;
        if (updateFolder.startsWith("/")) {
            updateFolder = updateFolder.substring(1);
        }
        return projectFolder + updateFolder;
    }

    private File getLatestInstallPackage() {
        File updateDir = new File(getUpdateFolderPath());
        if (!updateDir.exists() || !updateDir.isDirectory()) {
            return null;
        }
        File[] files = updateDir.listFiles((dir, name) -> name.startsWith(Constants.APP_NAME)
                && name.endsWith(Constants.APP_EXE_SUFFIX));
        if (files == null || files.length == 0) {
            return null;
        }
        Arrays.sort(files, (o1, o2) -> compareVersion(extractVersion(o2.getName()), extractVersion(o1.getName())));
        return files[0];
    }

    private String extractVersion(String fileName) {
        return fileName.substring(Constants.APP_NAME.length(), fileName.length() - Constants.APP_EXE_SUFFIX.length());
    }

    private int compareVersion(String latestVersion, String currentVersion) {
        String[] latestParts = latestVersion.split("\\.");
        String[] currentParts = currentVersion.split("\\.");
        int length = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int latestPart = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
            int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;
            if (latestPart > currentPart) {
                return 1;
            }
            if (latestPart < currentPart) {
                return -1;
            }
        }
        return 0;
    }

    private int parseVersionPart(String versionPart) {
        String safePart = versionPart.replaceAll("[^0-9]", "");
        if (StringTools.isEmpty(safePart)) {
            return 0;
        }
        try {
            return Integer.parseInt(safePart);
        } catch (Exception e) {
            return 0;
        }
    }
}
