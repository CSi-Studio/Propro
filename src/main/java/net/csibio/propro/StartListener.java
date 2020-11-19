package net.csibio.propro;

import net.csibio.propro.domain.db.UserDO;
import net.csibio.propro.service.UserService;
import net.csibio.propro.config.VMProperties;
import net.csibio.propro.constants.Roles;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashSet;
import java.util.Set;

public class StartListener implements ApplicationListener<ContextRefreshedEvent> {

    //应用启动时根据配置文件生成一个初始的管理员用户账号,如果同名账号已经存在,则直接跳过
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        UserService userService = contextRefreshedEvent.getApplicationContext().getBean(UserService.class);
        VMProperties vmProperties = contextRefreshedEvent.getApplicationContext().getBean(VMProperties.class);
        UserDO userDO = userService.getByUsername(vmProperties.getAdminUsername());
        if(userDO == null){
            userDO = new UserDO();
            userDO.setUsername(vmProperties.getAdminUsername());
            String randomSalt = new SecureRandomNumberGenerator().nextBytes().toHex();
            String result = new Md5Hash(vmProperties.getAdminPassword(), randomSalt, 3).toString();
            userDO.setSalt(randomSalt);
            userDO.setPassword(result);
            Set<String> roles = new HashSet<>();
            roles.add(Roles.ROLE_ADMIN);
            userDO.setRoles(roles);
            userService.register(userDO);
        }
        System.out.println(vmProperties);
    }
}
