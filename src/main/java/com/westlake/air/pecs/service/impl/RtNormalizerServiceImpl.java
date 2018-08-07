package com.westlake.air.pecs.service.impl;

import com.westlake.air.pecs.constants.ResultCode;
import com.westlake.air.pecs.domain.ResultDO;
import com.westlake.air.pecs.domain.bean.RtIntensityPairs;
import com.westlake.air.pecs.domain.db.AnalyseDataDO;
import com.westlake.air.pecs.domain.db.AnalyseOverviewDO;
import com.westlake.air.pecs.domain.db.simple.TransitionGroup;
import com.westlake.air.pecs.domain.query.AnalyseDataQuery;
import com.westlake.air.pecs.rtnormalizer.GaussFilter;
import com.westlake.air.pecs.service.AnalyseDataService;
import com.westlake.air.pecs.service.AnalyseOverviewService;
import com.westlake.air.pecs.service.RTNormalizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("rtNormalizerService")
public class RtNormalizerServiceImpl implements RTNormalizerService {

    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    GaussFilter gaussFilter;

    @Override
    public ResultDO compute(String overviewId, float sigma, float spacing) {
        ResultDO<AnalyseOverviewDO> overviewDOResult = analyseOverviewService.getById(overviewId);
        if(overviewDOResult.isFailed()){
            return ResultDO.buildError(ResultCode.ANALYSE_OVERVIEW_NOT_EXISTED);
        }

        AnalyseDataQuery query = new AnalyseDataQuery();
        query.setLibraryId(overviewDOResult.getModel().getVLibraryId());
        query.setOverviewId(overviewId);
        ResultDO<List<TransitionGroup>> groupsResult = analyseDataService.getTransitionGroup(query,true);
        if(groupsResult.isFailed()){
            ResultDO resultDO = new ResultDO(false);
            resultDO.setErrorResult(groupsResult.getMsgCode(), groupsResult.getMsgInfo());
            return resultDO;
        }

        for(TransitionGroup group : groupsResult.getModel()){
            if(group.getDataList() == null || group.getDataList().size() == 0){
                continue;
            }

            for(AnalyseDataDO dataDO : group.getDataList()){
                RtIntensityPairs pairs = new RtIntensityPairs(dataDO.getRtArray(), dataDO.getIntensityArray());
                pairs = gaussFilter.filter(pairs, sigma, spacing);
            }
        }
        return null;
    }
}