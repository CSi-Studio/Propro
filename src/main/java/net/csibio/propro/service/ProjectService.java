package net.csibio.propro.service;

import net.csibio.propro.domain.db.ProjectDO;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.query.ProjectQuery;

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
