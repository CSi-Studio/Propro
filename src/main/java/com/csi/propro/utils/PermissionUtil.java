package com.csi.propro.utils;

import com.csi.propro.domain.db.*;
import com.csi.propro.constants.Constants;
import com.csi.propro.constants.Roles;
import com.csi.propro.exception.UnauthorizedAccessException;
import org.apache.shiro.SecurityUtils;

public class PermissionUtil {

    public static void check(LibraryDO library) throws UnauthorizedAccessException {
        UserDO user = getCurrentUser();
        if(library == null || user == null){
            throw new UnauthorizedAccessException("redirect:/library/list");
        }

        if(library.isDoPublic()){
            return;
        }

        if(user.getRoles().contains(Roles.ROLE_ADMIN)){
            return;
        }

        if (!library.getCreator().equals(user.getUsername())) {
            if(library.getType().equals(Constants.LIBRARY_TYPE_IRT)){
                throw new UnauthorizedAccessException("redirect:/library/listIrt");
            }
            throw new UnauthorizedAccessException("redirect:/library/list");
        } else {
            return;
        }
    }

    public static void check(ProjectDO project) throws UnauthorizedAccessException {
        UserDO user = getCurrentUser();
        if(project == null || user == null){
            throw new UnauthorizedAccessException("redirect:/project/list");
        }

        if(project.isDoPublic()){
            return;
        }

        if(user.getRoles().contains(Roles.ROLE_ADMIN)){
            return;
        }

        if (!project.getOwnerName().equals(user.getUsername())) {
            throw new UnauthorizedAccessException("redirect:/project/list");
        } else {
            return;
        }
    }

    public static boolean check(TaskDO taskDO) throws UnauthorizedAccessException{
        UserDO user = getCurrentUser();
        if(taskDO == null || user == null){
            throw new UnauthorizedAccessException("redirect:/task/list");
        }

        if(user.getRoles().contains(Roles.ROLE_ADMIN)){
            return true;
        }

        if (!taskDO.getCreator().equals(user.getUsername())) {
            throw new UnauthorizedAccessException("redirect:/task/list");
        } else {
            return true;
        }
    }

    public static boolean check(ExperimentDO experiment) throws UnauthorizedAccessException{
        UserDO user = getCurrentUser();
        if(experiment == null || user == null){
            throw new UnauthorizedAccessException("redirect:/experiment/list");
        }

        if(user.getRoles().contains(Roles.ROLE_ADMIN)){
            return true;
        }

        if (!experiment.getOwnerName().equals(user.getUsername())) {
            throw new UnauthorizedAccessException("redirect:/experiment/list");
        } else {
            return true;
        }
    }

    public static boolean check(AnalyseOverviewDO overview) throws UnauthorizedAccessException{
        UserDO user = getCurrentUser();
        if(overview == null || user == null){
            throw new UnauthorizedAccessException("redirect:/analyse/overview/list");
        }

        if(user.getRoles().contains(Roles.ROLE_ADMIN)){
            return true;
        }

        if (!overview.getOwnerName().equals(user.getUsername())) {
            throw new UnauthorizedAccessException("redirect:/analyse/overview/list");
        } else {
            return true;
        }
    }

    public static UserDO getCurrentUser() {
        Object object = SecurityUtils.getSubject().getPrincipal();
        if (object != null) {
            return (UserDO) object;
        }

        return null;
    }
}
