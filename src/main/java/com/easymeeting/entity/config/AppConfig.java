package com.easymeeting.entity.config;

import com.easymeeting.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {
    private  static final Logger logger= LoggerFactory.getLogger(AppConfig.class);

    @Value("${ws.port:}")
    private Integer wsPort;

    //文件目录
    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.email:}")
    private String adminEmail;

    public Integer getWsPort() {
        return wsPort;
    }

    public String getProjectFolder() {
        if(!StringTools.isEmpty(projectFolder)&&!projectFolder.endsWith("/")){
            projectFolder=projectFolder+"/";
        }
        return projectFolder;
    }

    public String getAdminEmail() {
        return adminEmail;
    }
}
