package com.csi.propro.service;

import com.csi.propro.domain.db.UserDO;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.query.UserQuery;

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
