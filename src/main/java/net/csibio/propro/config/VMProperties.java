package net.csibio.propro.config;

import net.csibio.propro.utils.RepositoryUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("vmProperties")
public class VMProperties {

    @Autowired
    private Environment env;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${dingtalk.robot}")
    private String dingtalkRobot;

    @Value("${repository}")
    private String repository;

    @Value("${multiple}")
    private int multiple;

    @PostConstruct
    public void init() {
        System.out.println(env.getProperty("multiple"));
        RepositoryUtil.repository = repository;
    }

    public String getDingtalkRobot() {
        if (StringUtils.isEmpty(dingtalkRobot)) {
            dingtalkRobot = "https://oapi.dingtalk.com/robot/send?access_token=f2fb029431f174e678106b30c2db5fb0e40e921999386a61031bf864f18beb77";
        }
        return dingtalkRobot;
    }

    public void setDingtalkRobot(String dingtalkRobot) {
        this.dingtalkRobot = dingtalkRobot;
    }

    public String getAdminUsername() {
        if (StringUtils.isEmpty(adminUsername)) {
            adminUsername = "Admin";
        }
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        if (StringUtils.isEmpty(adminPassword)) {
            adminPassword = "propro";
        }
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getRepository() {
        if (StringUtils.isEmpty(repository)) {
            return "/nas/data";
        }
        return repository;
    }

    public void setMultiple(int multiple) {
        this.multiple = multiple;
    }

    public int getMultiple() {
        if (multiple <= 1) {
            return 1;
        }
        return multiple;
    }

}
