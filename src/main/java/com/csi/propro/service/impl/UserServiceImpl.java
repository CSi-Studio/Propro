package com.csi.propro.service.impl;

import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.dao.UserDAO;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.db.UserDO;
import com.csi.propro.domain.query.UserQuery;
import com.csi.propro.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service("userService")
public class UserServiceImpl implements UserService {

    public final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserDAO userDAO;

    @Override
    public UserDO getByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }

        try {
            return userDAO.getByUsername(username);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public ResultDO<List<UserDO>> getList(UserQuery query) {
        List<UserDO> users = userDAO.getList(query);
        long totalCount = userDAO.count(query);
        ResultDO<List<UserDO>> resultDO = new ResultDO<>(true);
        resultDO.setModel(users);
        resultDO.setTotalNum(totalCount);
        resultDO.setPageSize(query.getPageSize());
        return resultDO;
    }

    @Override
    public UserDO getById(String userId) {
        if (userId == null || userId.isEmpty()) {
            return null;
        }

        try {
            return userDAO.getById(userId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    public ResultDO delete(String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }
        try {
            userDAO.delete(userId);
            return new ResultDO(true);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.DELETE_ERROR);
        }
    }

    @Override
    public UserDO update(UserDO userDO) {
        return userDAO.update(userDO);
    }

    @Override
    public Set<String> getRoleByUserId(String uid) {
        UserDO user = userDAO.getById(uid);
        if (user != null) {
            return user.getRoles();
        }
        return null;
    }

    @Override
    public Set<String> getPermsByUserId(String uid) {
        UserDO user = userDAO.getById(uid);
        if (user != null) {
            return user.getPerms();
        }
        return null;
    }

    @Override
    public ResultDO<UserDO> register(UserDO userDO) {
        UserDO existed = getByUsername(userDO.getUsername());
        if (existed != null) {
            return ResultDO.buildError(ResultCode.USER_ALREADY_EXISTED);
        }
        userDO.setCreated(new Date());
        userDO.setUpdated(new Date());
        UserDO user = userDAO.insert(userDO);
        ResultDO<UserDO> result = new ResultDO<>();
        result.setModel(user);
        result.setSuccess(true);
        return result;
    }
}
