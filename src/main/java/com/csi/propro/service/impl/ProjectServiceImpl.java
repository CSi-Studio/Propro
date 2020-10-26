package com.csi.propro.service.impl;

import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.dao.ProjectDAO;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.db.ProjectDO;
import com.csi.propro.domain.query.ProjectQuery;
import com.csi.propro.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("projectService")
public class ProjectServiceImpl implements ProjectService {

    public final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Autowired
    ProjectDAO projectDAO;

    @Override
    public ResultDO<List<ProjectDO>> getList(ProjectQuery query) {
        List<ProjectDO> projects = projectDAO.getList(query);
        long totalCount = projectDAO.count(query);
        ResultDO<List<ProjectDO>> resultDO = new ResultDO<>(true);
        resultDO.setModel(projects);
        resultDO.setTotalNum(totalCount);
        resultDO.setPageSize(query.getPageSize());
        return resultDO;
    }

    @Override
    public List<ProjectDO> getAll(ProjectQuery query) {
        return projectDAO.getAll(query);
    }

    @Override
    public ResultDO insert(ProjectDO project) {
        if (project.getName() == null || project.getName().isEmpty()) {
            return ResultDO.buildError(ResultCode.PROJECT_NAME_CANNOT_BE_EMPTY);
        }
        try {
            project.setCreateDate(new Date());
            project.setLastModifiedDate(new Date());
            projectDAO.insert(project);
            return ResultDO.build(project);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.INSERT_ERROR);
        }
    }

    @Override
    public ResultDO update(ProjectDO project) {
        if (project.getId() == null || project.getId().isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }
        if (project.getName() == null || project.getName().isEmpty()) {
            return ResultDO.buildError(ResultCode.PROJECT_NAME_CANNOT_BE_EMPTY);
        }

        try {
            project.setLastModifiedDate(new Date());
            projectDAO.update(project);
            return ResultDO.build(project);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.UPDATE_ERROR);
        }
    }

    @Override
    public ResultDO delete(String id) {
        if (id == null || id.isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }
        try {
            projectDAO.delete(id);
            return new ResultDO(true);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.DELETE_ERROR);
        }
    }

    @Override
    public ProjectDO getById(String id) {

        try {
            ProjectDO project = projectDAO.getById(id);
            return project;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    @Override
    public ProjectDO getByName(String name) {

        try {
            ProjectDO project = projectDAO.getByName(name);
            return project;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return null;
        }
    }

    @Override
    public long count(ProjectQuery query) {
        return projectDAO.count(query);
    }
}
