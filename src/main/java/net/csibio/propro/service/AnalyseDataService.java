package net.csibio.propro.service;

import net.csibio.propro.domain.bean.score.SimpleFeatureScores;
import net.csibio.propro.domain.db.AnalyseDataDO;
import net.csibio.propro.domain.db.simple.MatchedPeptide;
import net.csibio.propro.domain.db.simple.PeptideIntensity;
import net.csibio.propro.domain.db.simple.PeptideScores;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.bean.analyse.AnalyseDataRT;
import net.csibio.propro.domain.query.AnalyseDataQuery;

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
