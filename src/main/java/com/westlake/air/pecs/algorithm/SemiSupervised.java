package com.westlake.air.pecs.algorithm;

import com.alibaba.fastjson.JSONArray;
import com.westlake.air.pecs.algorithm.learner.LDALearner;
import com.westlake.air.pecs.domain.bean.airus.*;
import com.westlake.air.pecs.domain.bean.score.FeatureScores;
import com.westlake.air.pecs.domain.bean.score.SimpleFeatureScores;
import com.westlake.air.pecs.domain.db.simple.SimpleScores;
import com.westlake.air.pecs.utils.AirusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-13 15:28
 */
@Component
public class SemiSupervised {

    public final Logger logger = LoggerFactory.getLogger(SemiSupervised.class);

    @Autowired
    Stats stats;
    @Autowired
    LDALearner ldaLearner;

    public LDALearnData learnRandomized(List<SimpleScores> scores, AirusParams airusParams) {
        LDALearnData ldaLearnData = new LDALearnData();
        try {
            //Get part of scores as train input.
            TrainData trainData = AirusUtil.split(scores, airusParams.getTrainTestRatio(), airusParams.isDebug());
            //第一次训练数据集使用MainScore进行训练
            TrainPeaks trainPeaks = selectTrainPeaks(trainData, airusParams.getMainScore(), airusParams);
            HashMap<String, Double> weightsMap = ldaLearner.learn(trainPeaks, airusParams.getMainScore());
            logger.info("Train Weight:"+ JSONArray.toJSONString(weightsMap));
            //根据weightsMap计算子分数的加权总分
            ldaLearner.score(trainData, weightsMap);
            for (int times = 0; times < airusParams.getXevalNumIter(); times++) {
                TrainPeaks trainPeaksTemp = selectTrainPeaks(trainData, FeatureScores.ScoreType.WeightedTotalScore.getTypeName(), airusParams);
                weightsMap = ldaLearner.learn(trainPeaksTemp, FeatureScores.ScoreType.WeightedTotalScore.getTypeName());
                ldaLearner.score(trainData, weightsMap);
            }
            //每一轮结束后要将这一轮打出的加权总分删除掉,以免影响下一轮打分
            trainData.removeWeightedTotalScore();
            ldaLearnData.setWeightsMap(weightsMap);
            return ldaLearnData;
        } catch (Exception e) {
            logger.error("learnRandomized Fail.\n");
            e.printStackTrace();
            return null;
        }

    }

    private TrainPeaks selectTrainPeaks(TrainData trainData, String usedScoreType, AirusParams airusParams) {

        List<SimpleFeatureScores> topTargetPeaks = AirusUtil.findTopFeatureScores(trainData.getTargets(), usedScoreType);
        List<SimpleFeatureScores> topDecoyPeaks = AirusUtil.findTopFeatureScores(trainData.getDecoys(), usedScoreType);

        // find cutoff fdr from scores and only use best target peaks:
        Double cutoff = stats.findCutoff(topTargetPeaks, topDecoyPeaks, airusParams);
        List<SimpleFeatureScores> bestTargetPeaks = AirusUtil.peaksFilter(topTargetPeaks, cutoff);

        TrainPeaks trainPeaks = new TrainPeaks();
        trainPeaks.setBestTargets(bestTargetPeaks);
        trainPeaks.setTopDecoys(topDecoyPeaks);
        return trainPeaks;
    }
}
