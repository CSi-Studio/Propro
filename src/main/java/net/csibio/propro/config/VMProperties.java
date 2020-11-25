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

    @Value("${repository}")
    private String repository;

    @Value("${multiple}")
    private int multiple;

    @PostConstruct
    public void init() {
        System.out.println("Multiple Threads: " + env.getProperty("multiple"));
        System.out.println("Repository: " + env.getProperty("repository"));
        RepositoryUtil.repository = repository;
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
