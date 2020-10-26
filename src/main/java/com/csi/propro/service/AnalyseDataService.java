package com.csi.propro.service;

import com.csi.propro.domain.bean.score.SimpleFeatureScores;
import com.csi.propro.domain.db.AnalyseDataDO;
import com.csi.propro.domain.db.simple.MatchedPeptide;
import com.csi.propro.domain.db.simple.PeptideIntensity;
import com.csi.propro.domain.db.simple.PeptideScores;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.bean.analyse.AnalyseDataRT;
import com.csi.propro.domain.query.AnalyseDataQuery;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-19 16:02
 */
public interface AnalyseDataService {

    List<AnalyseDataDO> getAllByOverviewId(String overviewId);

    List<PeptideIntensity> getPeptideIntensityByOverviewId(String overviewId);

    List<PeptideScores> getSimpleScoresByOverviewId(String overviewId);

    List<MatchedPeptide> getAllSuccessMatchedPeptides(String overviewId);

    AnalyseDataDO getByOverviewIdAndPeptideRefAndIsDecoy(String overviewId, String peptideRef, Boolean isDecoy);

    Long count(AnalyseDataQuery query);

    ResultDO<List<AnalyseDataDO>> getList(AnalyseDataQuery query);

    List<AnalyseDataDO> getAll(AnalyseDataQuery query);

    ResultDO insert(AnalyseDataDO dataDO);

    ResultDO insertAll(List<AnalyseDataDO> convList, boolean isDeleteOld);

    ResultDO update(AnalyseDataDO dataDO);

    ResultDO delete(String id);

    ResultDO deleteAllByOverviewId(String overviewId);

    ResultDO<AnalyseDataDO> getById(String id);

    List<AnalyseDataRT> getRtList(AnalyseDataQuery query);

    void updateMulti(String overviewId, List<SimpleFeatureScores> simpleFeatureScoresList);

    void removeUselessData(String overviewId, List<SimpleFeatureScores> simpleFeatureScoresList, Double fdr);

    int countIdentifiedProteins(String overviewId);
}
