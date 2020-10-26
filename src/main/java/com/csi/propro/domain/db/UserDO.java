package com.csi.propro.domain.db;

import com.csi.propro.domain.BaseDO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "user")
public class UserDO extends BaseDO {

    @Id
    String id;

    /**
     * 使用email作为唯一识别账号
     */
    @Indexed(unique = true)
    String username;

    String email;

    String nick;

    String password;

    String telephone;

    String organization;

    String salt;

    //分配的空间大小,单位GB
    Float allocatedSpace = 500f;

    //已使用空间大小,单位GB
    Float usedSpace;

    Date created;   // 创建时间
    Date updated;   // 修改时间
    Set<String> roles = new HashSet<>();    //用户所有角色值，用于shiro做角色权限的判断
    Set<String> perms = new HashSet<>();    //用户所有权限值，用于shiro做资源权限的判断
}
