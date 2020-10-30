package net.csibio.propro.service.impl;

import com.alibaba.fastjson.JSONObject;
import net.csibio.propro.algorithm.parser.AirdFileParser;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.constants.enums.TaskStatus;
import net.csibio.propro.dao.ExperimentDAO;
import net.csibio.propro.dao.ProjectDAO;
import net.csibio.propro.dao.SwathIndexDAO;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.bean.aird.AirdInfo;
import net.csibio.propro.domain.bean.experiment.ExpFileSize;
import net.csibio.propro.domain.db.ExperimentDO;
import net.csibio.propro.domain.db.ProjectDO;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.domain.db.TaskDO;
import net.csibio.propro.domain.db.simple.SimpleExperiment;
import net.csibio.propro.domain.query.ExperimentQuery;
import net.csibio.propro.domain.query.SwathIndexQuery;
import net.csibio.propro.service.*;
import net.csibio.propro.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-06 09:45
 */
@Service("experimentService")
public class ExperimentServiceImpl implements ExperimentService {

    public final Logger logger = LoggerFactory.getLogger(ExperimentServiceImpl.class);

    @Autowired
    ExperimentDAO experimentDAO;
    @Autowired
    ProjectDAO projectDAO;
    @Autowired
    SwathIndexDAO swathIndexDAO;

    @Autowired
    PeptideService peptideService;
    @Autowired
    AirdFileParser airdFileParser;
    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    TaskService taskService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    ScoreService scoreService;

    @Override
    public ResultDO<List<ExperimentDO>> getList(ExperimentQuery query) {
        List<ExperimentDO> expList = experimentDAO.getList(query);
        long totalCount = experimentDAO.count(query);
        ResultDO<List<ExperimentDO>> resultDO = new ResultDO<>(true);
        resultDO.setModel(expList);
        resultDO.setTotalNum(totalCount);
        resultDO.setPageSize(query.getPageSize());
        return resultDO;
    }

    @Override
    public long count(ExperimentQuery query) {
        return experimentDAO.count(query);
    }

    @Override
    public List<ExperimentDO> getAll(ExperimentQuery query) {
        return experimentDAO.getAll(query);
    }

    @Override
    public List<ExperimentDO> getAllByProjectId(String projectId) {
        ExperimentQuery query = new ExperimentQuery();
        query.setProjectId(projectId);
        return experimentDAO.getAll(query);
    }

    @Override
    public List<SimpleExperiment> getAllSimpleExperimentByProjectId(String projectId) {
        return experimentDAO.getSimpleExperimentByProjectId(projectId);
    }

    @Override
    public List<ExperimentDO> getAllByProjectName(String projectName) {
        ProjectDO project = projectDAO.getByName(projectName);
        if (project == null) {
            return null;
        } else {
            ExperimentQuery query = new ExperimentQuery();
            query.setProjectId(project.getId());
            return experimentDAO.getAll(query);
        }
    }

    @Override
    public ResultDO insert(ExperimentDO experimentDO) {
        if (experimentDO.getName() == null || experimentDO.getName().isEmpty()) {
            return ResultDO.buildError(ResultCode.LIBRARY_NAME_CANNOT_BE_EMPTY);
        }
        try {
            experimentDO.setCreateDate(new Date());
            experimentDO.setLastModifiedDate(new Date());
            experimentDAO.insert(experimentDO);
            return ResultDO.build(experimentDO);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.INSERT_ERROR);
        }
    }

    @Override
    public ResultDO update(ExperimentDO experimentDO) {
        if (experimentDO.getId() == null || experimentDO.getId().isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }
        if (experimentDO.getName() == null || experimentDO.getName().isEmpty()) {
            return ResultDO.buildError(ResultCode.LIBRARY_NAME_CANNOT_BE_EMPTY);
        }

        try {
            experimentDO.setLastModifiedDate(new Date());
            experimentDAO.update(experimentDO);
            return ResultDO.build(experimentDO);
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
            experimentDAO.delete(id);
            return new ResultDO(true);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.DELETE_ERROR);
        }
    }

    @Override
    public ResultDO<ExperimentDO> getById(String id) {
        if (id == null || id.isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }

        try {
            ExperimentDO experimentDO = experimentDAO.getById(id);
            if (experimentDO == null) {
                return ResultDO.buildError(ResultCode.OBJECT_NOT_EXISTED);
            } else {
                return ResultDO.build(experimentDO);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.QUERY_ERROR);
        }
    }

    @Override
    public ResultDO<ExperimentDO> getByName(String name) {
        if (name == null || name.isEmpty()) {
            return ResultDO.buildError(ResultCode.LIBRARY_NAME_CANNOT_BE_EMPTY);
        }

        try {
            ExperimentDO experimentDO = experimentDAO.getByName(name);
            if (experimentDO == null) {
                return ResultDO.buildError(ResultCode.OBJECT_NOT_EXISTED);
            } else {
                return ResultDO.build(experimentDO);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.QUERY_ERROR);
        }
    }

    @Override
    public void uploadAirdFile(ExperimentDO experimentDO, TaskDO taskDO) {

        taskDO.addLog("Start Parsing Aird File:" + experimentDO.getName());
        taskService.update(taskDO);
        try {
            File indexFile = new File(experimentDO.getAirdIndexPath());
            File airdFile = new File(experimentDO.getAirdPath());
            String airdInfoJson = FileUtil.readFile(indexFile);
            AirdInfo airdInfo = null;
            try {
                airdInfo = JSONObject.parseObject(airdInfoJson, AirdInfo.class);
            } catch (Exception e) {
                taskDO.addLog("Aird Index File Format Error,Can not Convert from JSON String.");
                taskDO.finish(TaskStatus.FAILED.getName());
                taskService.update(taskDO);
                return;
            }
            experimentDO.setAirdSize(airdFile.length());
            experimentDO.setAirdIndexSize(indexFile.length());
            experimentDO.setWindowRanges(airdInfo.getRangeList());
            experimentDO.setFeatures(airdInfo.getFeatures());
            experimentDO.setInstrument(airdInfo.getInstrument());
            experimentDO.setCompressors(airdInfo.getCompressors());
            experimentDO.setParentFiles(airdInfo.getParentFiles());
            experimentDO.setSoftwares(airdInfo.getSoftwares());
            experimentDO.setVendorFileSize(airdInfo.getFileSize());

            for (SwathIndexDO swathIndex : airdInfo.getIndexList()) {
                swathIndex.setExpId(experimentDO.getId());
            }

            swathIndexDAO.insert(airdInfo.getIndexList());
            taskDO.addLog("Swath Index Store Success.索引存储成功");
            taskService.update(taskDO);

        } catch (IOException e) {
            e.printStackTrace();
            taskDO.addLog("Aird Parse Exception");
            taskDO.finish(TaskStatus.FAILED.getName());
            taskService.update(taskDO);
        }
    }

    @Override
    public List<ExpFileSize> getAllFileSizeList(String ownerName) {
        return experimentDAO.getAllFileSizeList(ownerName);
    }

    @Override
    public Float getSumUsedFileSpace(String ownerName) {
        List<ExpFileSize> fileSizeList = experimentDAO.getAllFileSizeList(ownerName);
        float fileSizeCount = 0;
        for (ExpFileSize fs : fileSizeList) {
            fileSizeCount += (fs.getAirdSize()+fs.getAirdIndexSize());
        }
        return fileSizeCount/1024/1024;
    }


    @Override
    public HashMap<Float, Float[]> getPrmRtWindowMap(String expId) {
        SwathIndexQuery query = new SwathIndexQuery();
        query.setExpId(expId);
        query.setLevel(2);
        List<SwathIndexDO> ms2SwathIndexes = swathIndexDAO.getAll(query);
        HashMap<Float, Float[]> peptideMap = new HashMap<>();
        for (SwathIndexDO swathIndex : ms2SwathIndexes) {
            float precursorMz = swathIndex.getRange().getMz();
            peptideMap.put(precursorMz, new Float[]{swathIndex.getRts().get(0), swathIndex.getRts().get(swathIndex.getRts().size() - 1)});
        }
        return peptideMap;
    }

    @Override
    public HashMap<Float, Float[]> getPrmRtWindowMap(List<SwathIndexDO> ms2SwathIndexes) {
        HashMap<Float, Float[]> peptideMap = new HashMap<>();
        for (SwathIndexDO swathIndex : ms2SwathIndexes) {
            float precursorMz = swathIndex.getRange().getMz();
            peptideMap.put(precursorMz, new Float[]{swathIndex.getRts().get(0), swathIndex.getRts().get(swathIndex.getRts().size() - 1)});
        }
        return peptideMap;
    }


}
