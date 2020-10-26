package com.csi.propro.controller;

import com.csi.propro.domain.db.LibraryDO;
import com.csi.propro.domain.db.UserDO;
import com.csi.propro.exception.UserNotLoginException;
import com.csi.propro.service.ExperimentService;
import com.csi.propro.service.LibraryService;
import com.csi.propro.service.TaskService;
import com.csi.propro.async.task.ExperimentTask;
import com.csi.propro.domain.query.PageQuery;
import com.csi.propro.async.task.LibraryTask;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
public class BaseController {

    public final String redirectToLoginPage = "redirect:/login/login";

    @Autowired
    LibraryService libraryService;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    TaskService taskService;
    @Autowired
    LibraryTask libraryTask;
    @Autowired
    ExperimentTask experimentTask;

    public final Logger logger = LoggerFactory.getLogger(getClass());
    public static String ERROR_MSG = "error_msg";
    public static String SUCCESS_MSG = "success_msg";

    //0:标准库,1:irt校准库
    public List<LibraryDO> getLibraryList(Integer type, boolean includePublic) {
        String username = null;
        //如果是管理员的话不要设置指定的用户名
        if(!isAdmin()){
            username = getCurrentUsername();
        }
        if(!includePublic){
            return libraryService.getSimpleAll(username, type, null);
        }else{
            List<LibraryDO> libraries = libraryService.getSimpleAll(username, type, false);
            List<LibraryDO> publicLibraries = libraryService.getAllPublic(type);
            libraries.addAll(publicLibraries);
            return libraries;
        }
    }

    public void buildPageQuery(PageQuery query, Integer currentPage, Integer pageSize) {
        if (currentPage != null) {
            query.setPageNo(currentPage);
        }
        if (pageSize != null) {
            query.setPageSize(pageSize);
        }
    }

    public UserDO getCurrentUser() {
        Object object = SecurityUtils.getSubject().getPrincipal();
        if (object != null) {
            return (UserDO) object;
        }

        return null;
    }

    public String getCurrentUsername() {
        UserDO user = getCurrentUser();
        if (user != null && user.getUsername() != null && !user.getUsername().isEmpty()) {
            return user.getUsername();
        } else {
            throw new UserNotLoginException();
        }
    }

    public boolean isAdmin() {
        UserDO user = getCurrentUser();
        if (user != null && user.getRoles() != null && user.getRoles().contains("admin")) {
            return true;
        } else {
            return false;
        }
    }
}
