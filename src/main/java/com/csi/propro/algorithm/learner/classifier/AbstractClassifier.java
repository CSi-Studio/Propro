package com.csi.propro.algorithm.learner.classifier;

import com.csi.propro.constants.enums.ScoreType;
import com.csi.propro.domain.bean.learner.LearningParams;
import com.csi.propro.domain.bean.learner.TrainData;
import com.csi.propro.domain.bean.learner.TrainPeaks;
import com.csi.propro.domain.bean.score.FeatureScores;
import com.csi.propro.domain.bean.score.SimpleFeatureScores;
import com.csi.propro.domain.db.simple.PeptideScores;
import com.csi.propro.utils.AirusUtil;
import com.csi.propro.algorithm.learner.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分类器,目前有LDA和XGBoost两种
 * @author lumiaoshan
 */
public abstract class AbstractClassifier {

    public final Logger logger = LoggerFactory.getLogger(AbstractClassifier.class);

    @Autowired
    public Statistics statistics;

    /**
     * Get clfScore with given confidence(params).
     * 根据weightsMap计算子分数的(加权总分-平均加权总分)
     */
    public void score(TrainData data, HashMap<String, Double> weightsMap, List<String> scoreTypes) {
        score(data.getTargets(), weightsMap, scoreTypes);
        score(data.getDecoys(), weightsMap, scoreTypes);
    }

    public void score(List<PeptideScores> scores, HashMap<String, Double> weightsMap, List<String> scoreTypes) {
        Set<Map.Entry<String, Double>> entries = weightsMap.entrySet();
        for (PeptideScores score : scores) {
            if(score.getFeatureScoresList() == null){
                continue;
            }
            for (FeatureScores featureScores : score.getFeatureScoresList()) {
                double addedScore = 0;
                for (Map.Entry<String, Double> entry : entries) {
                    addedScore += featureScores.get(entry.getKey(), scoreTypes) * entry.getValue();
                }
                featureScores.put(ScoreType.WeightedTotalScore.getTypeName(), addedScore, scoreTypes);
            }
        }
    }

    public TrainPeaks selectTrainPeaks(TrainData trainData, String usedScoreType, LearningParams learningParams, Double cutoff) {

        List<SimpleFeatureScores> topTargetPeaks = AirusUtil.findTopFeatureScores(trainData.getTargets(), usedScoreType, learningParams.getScoreTypes(), true);
        List<SimpleFeatureScores> topDecoyPeaks = AirusUtil.findTopFeatureScores(trainData.getDecoys(), usedScoreType, learningParams.getScoreTypes(), false);

        Double cutoffNew;
        if (topTargetPeaks.size() < 100) {
            Double decoyMax = Double.MIN_VALUE, targetMax = Double.MIN_VALUE;
            for (SimpleFeatureScores scores : topDecoyPeaks) {
                if (scores.getMainScore() > decoyMax) {
                    decoyMax = scores.getMainScore();
                }
            }
            for (SimpleFeatureScores scores : topTargetPeaks) {
                if (scores.getMainScore() > targetMax) {
                    targetMax = scores.getMainScore();
                }
            }
            cutoffNew = (decoyMax + targetMax) / 2;
        } else {
            // find cutoff fdr from scores and only use best target peaks:
            cutoffNew = statistics.findCutoff(topTargetPeaks, topDecoyPeaks, learningParams, cutoff);
        }
        List<SimpleFeatureScores> bestTargetPeaks = AirusUtil.peaksFilter(topTargetPeaks, cutoffNew);

        TrainPeaks trainPeaks = new TrainPeaks();
        trainPeaks.setBestTargets(bestTargetPeaks);
        trainPeaks.setTopDecoys(topDecoyPeaks);
        logger.info(topTargetPeaks.size() + " " + topDecoyPeaks.size() + " " + cutoffNew + " " + bestTargetPeaks.size());
        return trainPeaks;
    }
}
