package com.csi.propro.service;

import com.csi.propro.domain.db.ProjectDO;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.query.ProjectQuery;

import java.util.List;

public interface ProjectService {

    ResultDO<List<ProjectDO>> getList(ProjectQuery query);

    List<ProjectDO> getAll(ProjectQuery query);

    ResultDO insert(ProjectDO project);

    ResultDO update(ProjectDO projectDO);

    ResultDO delete(String id);

    ProjectDO getById(String id);

    ProjectDO getByName(String name);

    long count(ProjectQuery query);
}
