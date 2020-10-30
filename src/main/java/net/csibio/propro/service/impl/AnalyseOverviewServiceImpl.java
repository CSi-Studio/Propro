package net.csibio.propro.service.impl;

import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.dao.AnalyseOverviewDAO;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.bean.analyse.ComparisonResult;
import net.csibio.propro.domain.db.AnalyseOverviewDO;
import net.csibio.propro.domain.db.simple.MatchedPeptide;
import net.csibio.propro.domain.query.AnalyseOverviewQuery;
import net.csibio.propro.service.AnalyseDataService;
import net.csibio.propro.service.AnalyseOverviewService;
import net.csibio.propro.service.ScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-19 16:40
 */
@Service("analyseOverviewService")
public class AnalyseOverviewServiceImpl implements AnalyseOverviewService {

    public final Logger logger = LoggerFactory.getLogger(AnalyseOverviewServiceImpl.class);

    @Autowired
    AnalyseOverviewDAO analyseOverviewDAO;
    @Autowired
    ScoreService scoreService;
    @Autowired
    AnalyseDataService analyseDataService;

    @Override
    public List<AnalyseOverviewDO> getAllByExpId(String expId) {
        return analyseOverviewDAO.getAllByExperimentId(expId);
    }

    @Override
    public AnalyseOverviewDO getFirstAnalyseOverviewByExpId(String expId) {
        return analyseOverviewDAO.getFirstByExperimentId(expId);
    }

    @Override
    public Long count(AnalyseOverviewQuery query) {
        return analyseOverviewDAO.count(query);
    }

    @Override
    public ResultDO<List<AnalyseOverviewDO>> getList(AnalyseOverviewQuery targetQuery) {
        List<AnalyseOverviewDO> dataList = analyseOverviewDAO.getList(targetQuery);
        long totalCount = analyseOverviewDAO.count(targetQuery);
        ResultDO<List<AnalyseOverviewDO>> resultDO = new ResultDO<>(true);
        resultDO.setModel(dataList);
        resultDO.setTotalNum(totalCount);
        resultDO.setPageSize(targetQuery.getPageSize());
        return resultDO;
    }

    @Override
    public ResultDO insert(AnalyseOverviewDO overviewDO) {
        try {
            overviewDO.setCreateDate(new Date());
            overviewDO.setLastModifiedDate(new Date());
            analyseOverviewDAO.insert(overviewDO);
            return ResultDO.build(overviewDO);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.INSERT_ERROR);
        }
    }

    @Override
    public ResultDO update(AnalyseOverviewDO overviewDO) {
        if (overviewDO.getId() == null || overviewDO.getId().isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }

        try {
            overviewDO.setLastModifiedDate(new Date());
            analyseOverviewDAO.update(overviewDO);
            return ResultDO.build(overviewDO);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.INSERT_ERROR);
        }
    }


    @Override
    public ResultDO delete(String id) {
        if (id == null || id.isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }
        try {
            analyseDataService.deleteAllByOverviewId(id);
            analyseOverviewDAO.delete(id);

            return new ResultDO(true);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.DELETE_ERROR);
        }
    }

    @Override
    public ResultDO deleteAllByExpId(String expId) {
        if (expId == null || expId.isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }
        try {
            List<AnalyseOverviewDO> overviewDOList = analyseOverviewDAO.getAllByExperimentId(expId);
            for (AnalyseOverviewDO overviewDO : overviewDOList) {
                analyseDataService.deleteAllByOverviewId(overviewDO.getId());
            }

            analyseOverviewDAO.deleteAllByExperimentId(expId);
            return new ResultDO(true);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.DELETE_ERROR);
        }
    }

    @Override
    public ResultDO<AnalyseOverviewDO> getById(String id) {
        try {
            AnalyseOverviewDO analyseOverviewDO = analyseOverviewDAO.getById(id);
            if (analyseOverviewDO == null) {
                return ResultDO.buildError(ResultCode.OBJECT_NOT_EXISTED);
            } else {
                ResultDO<AnalyseOverviewDO> resultDO = new ResultDO<>(true);
                resultDO.setModel(analyseOverviewDO);
                return resultDO;
            }
        } catch (Exception e) {
            return ResultDO.buildError(ResultCode.QUERY_ERROR);
        }
    }

    @Override
    public ResultDO<AnalyseOverviewDO> getFirstByExpId(String expId) {
        try {
            AnalyseOverviewDO overviewDO = analyseOverviewDAO.getFirstByExperimentId(expId);
            if (overviewDO == null) {
                return ResultDO.buildError(ResultCode.OBJECT_NOT_EXISTED);
            } else {
                ResultDO<AnalyseOverviewDO> resultDO = new ResultDO<>(true);
                resultDO.setModel(overviewDO);
                return resultDO;
            }
        } catch (Exception e) {
            return ResultDO.buildError(ResultCode.QUERY_ERROR);
        }
    }

    @Override
    public ComparisonResult comparison(List<AnalyseOverviewDO> overviews) {
        ComparisonResult resultMap = new ComparisonResult();

        HashMap<String, HashSet<MatchedPeptide>> map = new HashMap<>();
        HashMap<String, AnalyseOverviewDO> overviewMap = new HashMap<>();
        HashMap<AnalyseOverviewDO, List<Boolean>> identifiesMap = new HashMap<>();

        for (AnalyseOverviewDO overview : overviews) {

            //如果MatchedPeptideCount为空,则表明分析还没有完成,无法参与横向比对
            if (overview.getMatchedPeptideCount() != null) {
                overviewMap.put(overview.getId(), overview);
                List<MatchedPeptide> peptides = analyseDataService.getAllSuccessMatchedPeptides(overview.getId());
                map.put(overview.getId(), new HashSet<>(peptides));
                identifiesMap.put(overview, new ArrayList<Boolean>());
            }

        }
        List<MatchedPeptide> samePeptides = new ArrayList<>();
        List<MatchedPeptide> diffPeptides = new ArrayList<>();
        //所有的需要比对的肽段取并集
        HashSet<MatchedPeptide> totalPeptides = new HashSet<>();
        for (HashSet<MatchedPeptide> peptides : map.values()) {
            totalPeptides.addAll(peptides);
        }

        for (MatchedPeptide mp : totalPeptides) {
            boolean isAllContained = true;
            HashMap<String, Boolean> containedMap = new HashMap<>();
            for (String id : map.keySet()) {
                boolean isContained = map.get(id).contains(mp);
                if (!isContained) {
                    isAllContained = false;
                }
                containedMap.put(id, isContained);
            }

            if (isAllContained) {
                samePeptides.add(mp);
            } else {
                diffPeptides.add(mp);
                for (Map.Entry<String, Boolean> entry : containedMap.entrySet()) {
                    identifiesMap.get(overviewMap.get(entry.getKey())).add(entry.getValue());
                }
            }
        }

        resultMap.setSamePeptides(samePeptides);
        resultMap.setDiffPeptides(diffPeptides);
        resultMap.setIdentifiesMap(identifiesMap);

        return resultMap;
    }
}
