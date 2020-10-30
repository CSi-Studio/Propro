package net.csibio.propro.service;

import net.csibio.propro.domain.db.TaskDO;
import net.csibio.propro.constants.enums.TaskTemplate;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.query.TaskQuery;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-15 10:05
 */
public interface TaskService {

    Long count(TaskQuery query);

    List<TaskDO> getAll(TaskQuery targetQuery);

    ResultDO<List<TaskDO>> getList(TaskQuery targetQuery);

    ResultDO insert(TaskDO taskDO);

    ResultDO update(TaskDO taskDO);

    ResultDO update(TaskDO taskDO, String newLog);

    ResultDO update(TaskDO taskDO,String status, String newLog);

    ResultDO finish(TaskDO taskDO, String status, String newLog);

    ResultDO delete(String id);

    ResultDO<TaskDO> getById(String id);

    ResultDO doTask(TaskTemplate taskTemplate);

}
