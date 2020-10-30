package net.csibio.propro.service;

import net.csibio.propro.domain.db.UserDO;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.query.UserQuery;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserDO getByUsername(String username);

    ResultDO<List<UserDO>> getList(UserQuery query);

    UserDO getById(String userId);

    ResultDO delete(String userId);

    UserDO update(UserDO userDO);

    Set<String> getRoleByUserId(String uid);

    Set<String> getPermsByUserId(String uid);

    ResultDO<UserDO> register(UserDO userDO);
}
