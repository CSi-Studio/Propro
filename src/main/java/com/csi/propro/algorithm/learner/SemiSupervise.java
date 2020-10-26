package com.csi.propro.algorithm.learner;

import com.csi.propro.algorithm.learner.classifier.Lda;
import com.csi.propro.algorithm.learner.classifier.Xgboost;
import com.csi.propro.constants.ExpTypeConst;
import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.constants.enums.ScoreType;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.bean.learner.ErrorStat;
import com.csi.propro.domain.bean.learner.FinalResult;
import com.csi.propro.domain.bean.learner.LearningParams;
import com.csi.propro.domain.bean.score.FeatureScores;
import com.csi.propro.domain.bean.score.SimpleFeatureScores;
import com.csi.propro.domain.db.AnalyseOverviewDO;
import com.csi.propro.domain.db.simple.PeptideScores;
import com.csi.propro.service.AnalyseDataService;
import com.csi.propro.service.AnalyseOverviewService;
import com.csi.propro.service.ScoreService;
import com.csi.propro.utils.AirusUtil;
import com.csi.propro.utils.SortUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-19 09:25
 */
@Component
public class SemiSupervise {

    public final Logger logger = LoggerFactory.getLogger(SemiSupervise.class);

    @Autowired
    Lda lda;
    @Autowired
    Xgboost xgboost;
    @Autowired
    Statistics statistics;
    @Autowired
    ScoreService scoreService;
    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;

    public FinalResult doSemiSupervise(String overviewId, LearningParams learningParams) {
        FinalResult finalResult = new FinalResult();

        //Step1. 数据预处理
        logger.info("数据预处理");
        ResultDO<AnalyseOverviewDO> overviewDOResultDO = analyseOverviewService.getById(overviewId);
        if (overviewDOResultDO.isFailed()) {
            finalResult.setErrorInfo(ResultCode.ANALYSE_OVERVIEW_NOT_EXISTED.getMessage());
            return finalResult;
        }
        AnalyseOverviewDO overviewDO = overviewDOResultDO.getModel();
        overviewDO.setClassifier(learningParams.getClassifier().name());
        if (learningParams.getScoreTypes() == null) {
            learningParams.setScoreTypes(overviewDO.getScoreTypes());
        }

        if (overviewDO.getMatchedPeptideCount() != null) {
            overviewDO.setMatchedPeptideCount(null);
        }
        analyseOverviewService.update(overviewDO);
        learningParams.setType(overviewDO.getType());

        //Step2. 从数据库读取全部打分数据
        logger.info("开始获取打分数据");
        List<PeptideScores> scores = analyseDataService.getSimpleScoresByOverviewId(overviewId);
        ResultDO resultDO = check(scores);
        if (resultDO.isFailed()) {
            finalResult.setErrorInfo(resultDO.getMsgInfo());
            return finalResult;
        }
        if (learningParams.getType().equals(ExpTypeConst.PRM)) {
            cleanScore(scores, overviewDO.getScoreTypes());
        }

        //Step3. 开始训练数据集
        HashMap<String, Double> weightsMap = new HashMap<>();
        switch (learningParams.getClassifier()) {
            case lda:
                weightsMap = lda.classifier(scores, learningParams, overviewDO.getScoreTypes());
                lda.score(scores, weightsMap, learningParams.getScoreTypes());
                finalResult.setWeightsMap(weightsMap);
                break;

            case xgboost:
                xgboost.classifier(scores, overviewDO.getScoreTypes(), learningParams);
                break;

            default:
                break;
        }

        List<SimpleFeatureScores> featureScoresList = AirusUtil.findTopFeatureScores(scores, ScoreType.WeightedTotalScore.getTypeName(), overviewDO.getScoreTypes(), false);
        int count = 0;
        if (learningParams.getType().equals(ExpTypeConst.PRM)) {
            double maxDecoy = Double.MIN_VALUE;
            for (SimpleFeatureScores simpleFeatureScores : featureScoresList) {
                if (simpleFeatureScores.getIsDecoy() && simpleFeatureScores.getMainScore() > maxDecoy) {
                    maxDecoy = simpleFeatureScores.getMainScore();
                }
            }
            for (SimpleFeatureScores simpleFeatureScores : featureScoresList) {
                if (!simpleFeatureScores.getIsDecoy() && simpleFeatureScores.getMainScore() > maxDecoy) {
                    simpleFeatureScores.setFdr(0d);
                    count++;
                } else {
                    simpleFeatureScores.setFdr(1d);
                }
            }

        } else {
            ErrorStat errorStat = statistics.errorStatistics(featureScoresList, learningParams);
            finalResult.setAllInfo(errorStat);
            count = AirusUtil.checkFdr(finalResult, learningParams.getFdr());
        }

        //Step4. 对于最终的打分结果和选峰结果保存到数据库中
        logger.info("将合并打分及定量结果反馈更新到数据库中,总计:" + featureScoresList.size() + "条数据");
        giveDecoyFdr(featureScoresList);
        targetDecoyDistribution(featureScoresList, overviewDO);
        analyseDataService.removeUselessData(overviewId, featureScoresList, learningParams.getFdr());
        long start = System.currentTimeMillis();
        analyseDataService.updateMulti(overviewDO.getId(), featureScoresList);
        logger.info("更新数据" + featureScoresList.size() + "条一共用时：" + (System.currentTimeMillis() - start)+"毫秒");

        logger.info("最终鉴定肽段数目为:" + count + ",打分反馈更新完毕");
        int matchedProteinsCount = analyseDataService.countIdentifiedProteins(overviewId);
        logger.info("最终鉴定蛋白数目(包含非Unique)为:"+matchedProteinsCount);
        finalResult.setMatchedPeptideCount(count);
        finalResult.setMatchedProteinCount(matchedProteinsCount);

        overviewDO.setWeights(weightsMap);
        overviewDO.setMatchedPeptideCount(count);
        overviewDO.setMatchedProteinCount(matchedProteinsCount);
        analyseOverviewService.update(overviewDO);

        logger.info("合并打分完成,共找到新肽段" + count + "个");
        return finalResult;
    }

    private ResultDO check(List<PeptideScores> scores) {
        boolean isAllDecoy = true;
        boolean isAllReal = true;
        for (PeptideScores score : scores) {
            if (score.getIsDecoy()) {
                isAllReal = false;
            } else {
                isAllDecoy = false;
            }
        }
        if (isAllDecoy) {
            return ResultDO.buildError(ResultCode.ALL_SCORE_DATA_ARE_DECOY);
        }
        if (isAllReal) {
            return ResultDO.buildError(ResultCode.ALL_SCORE_DATA_ARE_REAL);
        }
        return new ResultDO(true);
    }

    //给分布在target中的decoy赋以Fdr值, 最末尾部分的decoy忽略, fdr为null
    private void giveDecoyFdr(List<SimpleFeatureScores> featureScoresList) {
        List<SimpleFeatureScores> sortedAll = SortUtil.sortByMainScore(featureScoresList, false);
        SimpleFeatureScores leftFeatureScore = null;
        SimpleFeatureScores rightFeatureScore;
        List<SimpleFeatureScores> decoyPartList = new ArrayList<>();
        for (SimpleFeatureScores simpleFeatureScores : sortedAll) {
            if (simpleFeatureScores.getIsDecoy()) {
                decoyPartList.add(simpleFeatureScores);
            } else {
                rightFeatureScore = simpleFeatureScores;
                if (leftFeatureScore != null && !decoyPartList.isEmpty()) {
                    for (SimpleFeatureScores decoy : decoyPartList) {
                        if (decoy.getMainScore() - leftFeatureScore.getMainScore() < rightFeatureScore.getMainScore() - decoy.getMainScore()) {
                            decoy.setFdr(leftFeatureScore.getFdr());
                            decoy.setQValue(leftFeatureScore.getQValue());
                        } else {
                            decoy.setFdr(rightFeatureScore.getFdr());
                            decoy.setQValue(rightFeatureScore.getQValue());
                        }
                    }
                }
                leftFeatureScore = rightFeatureScore;
                decoyPartList.clear();
            }
        }
        if (leftFeatureScore != null && !decoyPartList.isEmpty()) {
            for (SimpleFeatureScores decoy : decoyPartList) {
                decoy.setFdr(leftFeatureScore.getFdr());
                decoy.setQValue(leftFeatureScore.getQValue());
            }
        }
    }

    /**
     * 对于FDR<=0.01,每0.001个间隔存储为一组
     * 对于FDR>0.01,每0.1间隔存储为一组
     *
     * @param featureScoresList
     * @param overviewDO
     */
    private void targetDecoyDistribution(List<SimpleFeatureScores> featureScoresList, AnalyseOverviewDO overviewDO) {
        HashMap<String, Integer> targetDistributions = AirusUtil.buildDistributionMap();
        HashMap<String, Integer> decoyDistributions = AirusUtil.buildDistributionMap();
        for (SimpleFeatureScores sfs : featureScoresList) {
            if (sfs.getFdr() != null) {
                if (sfs.getIsDecoy()) {
                    AirusUtil.addOneForFdrDistributionMap(sfs.getFdr(), decoyDistributions);
                } else {
                    AirusUtil.addOneForFdrDistributionMap(sfs.getFdr(), targetDistributions);
                }
            }
        }

        overviewDO.setTargetDistributions(targetDistributions);
        overviewDO.setDecoyDistributions(decoyDistributions);

    }

    private void cleanScore(List<PeptideScores> scoresList, List<String> scoreTypes) {
        for (PeptideScores peptideScores : scoresList) {
            if (peptideScores.getIsDecoy()) {
                continue;
            }
            for (FeatureScores featureScores : peptideScores.getFeatureScoresList()) {
                int count = 0;
                if (featureScores.get(ScoreType.NormRtScore, scoreTypes) != null && featureScores.get(ScoreType.NormRtScore, scoreTypes) > 8) {
                    count++;
                }
                if (featureScores.get(ScoreType.LogSnScore, scoreTypes) != null && featureScores.get(ScoreType.LogSnScore, scoreTypes) < 3) {
                    count++;
                }
                if (featureScores.get(ScoreType.IsotopeCorrelationScore, scoreTypes) != null && featureScores.get(ScoreType.IsotopeCorrelationScore, scoreTypes) < 0.8) {
                    count++;
                }
                if (featureScores.get(ScoreType.IsotopeOverlapScore, scoreTypes) != null && featureScores.get(ScoreType.IsotopeOverlapScore, scoreTypes) > 0.2) {
                    count++;
                }
                if (featureScores.get(ScoreType.MassdevScoreWeighted, scoreTypes) != null && featureScores.get(ScoreType.MassdevScoreWeighted, scoreTypes) > 15) {
                    count++;
                }
                if (featureScores.get(ScoreType.BseriesScore, scoreTypes) != null && featureScores.get(ScoreType.BseriesScore, scoreTypes) < 1) {
                    count++;
                }
                if (featureScores.get(ScoreType.YseriesScore, scoreTypes) != null && featureScores.get(ScoreType.YseriesScore, scoreTypes) < 5) {
                    count++;
                }
                if (featureScores.get(ScoreType.XcorrShapeWeighted, scoreTypes) != null && featureScores.get(ScoreType.XcorrShapeWeighted, scoreTypes) < 0.6) {
                    count++;
                }
                if (featureScores.get(ScoreType.XcorrShape, scoreTypes) != null && featureScores.get(ScoreType.XcorrShape, scoreTypes) < 0.5) {
                    count++;
                }

                if (count > 3) {
                    featureScores.setThresholdPassed(false);
                }
            }
        }
    }
}
