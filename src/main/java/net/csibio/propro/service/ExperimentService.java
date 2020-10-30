package net.csibio.propro.service;

import net.csibio.propro.domain.db.ExperimentDO;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.domain.db.TaskDO;
import net.csibio.propro.domain.db.simple.SimpleExperiment;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.bean.experiment.ExpFileSize;
import net.csibio.propro.domain.query.ExperimentQuery;

import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-06 09:36
 */
public interface ExperimentService {

    ResultDO<List<ExperimentDO>> getList(ExperimentQuery query);

    long count(ExperimentQuery query);

    List<ExperimentDO> getAll(ExperimentQuery query);

    List<ExperimentDO> getAllByProjectId(String projectId);

    List<SimpleExperiment> getAllSimpleExperimentByProjectId(String projectId);

    List<ExperimentDO> getAllByProjectName(String projectName);

    ResultDO insert(ExperimentDO experimentDO);

    ResultDO update(ExperimentDO experimentDO);

    ResultDO delete(String id);

    ResultDO<ExperimentDO> getById(String id);

    ResultDO<ExperimentDO> getByName(String name);

    HashMap<Float, Float[]> getPrmRtWindowMap(String expId);

    HashMap<Float, Float[]> getPrmRtWindowMap(List<SwathIndexDO> ms2SwathIndexes);

    void uploadAirdFile(ExperimentDO experimentDO, TaskDO taskDO);

    List<ExpFileSize> getAllFileSizeList(String ownerName);

    Float getSumUsedFileSpace(String ownerName);

}
