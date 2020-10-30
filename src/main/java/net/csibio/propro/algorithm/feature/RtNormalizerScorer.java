package net.csibio.propro.algorithm.feature;

import net.csibio.propro.constants.enums.ScoreType;
import net.csibio.propro.domain.bean.score.FeatureScores;
import net.csibio.propro.domain.bean.score.PeakGroup;
import net.csibio.propro.domain.bean.score.ScoreRtPair;
import net.csibio.propro.domain.params.WorkflowParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-05 17:18
 */
@Component("rtNormalizerScorer")
public class RtNormalizerScorer {

    @Autowired
    ChromatographicScorer chromatographicScorer;
    @Autowired
    LibraryScorer libraryScorer;


//    private float windowLength = 1000;
//    private int binCount = 30;

//    private float rtNormalizationFactor = 1.0f;
//    private int addUpSpectra = 1;
//    private float spacingForSpectraResampling = 0.005f;
//    //scoreForAll use params


    /**
     * return scores.library_corr                     * -0.34664267 +
     * scores.library_norm_manhattan           *  2.98700722 +
     * scores.norm_rt_score                    *  7.05496384 +
     * scores.xcorr_coelution_score            *  0.09445371 +
     * scores.xcorr_shape_score                * -5.71823862 +
     * scores.log_sn_score                     * -0.72989582 +
     * scores.elution_model_fit_score          *  1.88443209;
     *
     * @param peakGroupFeatureList features extracted from chromatogramList in transitionGroup
     * @param normedLibIntMap      intensity in transitionList in transitionGroup
     * @return List of overallQuality
     */
    public List<ScoreRtPair> score(List<PeakGroup> peakGroupFeatureList, HashMap<String, Double> normedLibIntMap, double groupRt) {


        List<ScoreRtPair> finalScores = new ArrayList<>();
        List<String> defaultScoreTypes = new WorkflowParams().getScoreTypes();
        for (PeakGroup peakGroupFeature : peakGroupFeatureList) {
            if (peakGroupFeature.getBestRightRt()-peakGroupFeature.getBestLeftRt() < 15d){
                continue;
            }
            FeatureScores scores = new FeatureScores(defaultScoreTypes.size());
            chromatographicScorer.calculateChromatographicScores(peakGroupFeature, normedLibIntMap, scores, defaultScoreTypes);
            chromatographicScorer.calculateLogSnScore(peakGroupFeature, scores, defaultScoreTypes);
            libraryScorer.calculateLibraryScores(peakGroupFeature, normedLibIntMap, scores, defaultScoreTypes);

            double ldaScore = -1d * calculateLdaPrescore(scores, defaultScoreTypes);
            ScoreRtPair scoreRtPair = new ScoreRtPair();
            scoreRtPair.setGroupRt(groupRt);
            scoreRtPair.setRt(peakGroupFeature.getApexRt());
            scoreRtPair.setScore(ldaScore);
            scoreRtPair.setScores(scores);
            finalScores.add(scoreRtPair);
        }

        return finalScores;
    }

    /**
     * The scoreForAll that is really matter to final pairs selection.
     *
     * @param scores pre-calculated
     * @return final scoreForAll
     */
    private double calculateLdaPrescore(FeatureScores scores, List<String> scoreTypes) {
        return scores.get(ScoreType.LibraryCorr.getTypeName(), scoreTypes) * -0.34664267d +
                scores.get(ScoreType.LibraryRsmd.getTypeName(), scoreTypes) * 2.98700722d +
                scores.get(ScoreType.XcorrCoelution.getTypeName(), scoreTypes) * 0.09445371d +
                scores.get(ScoreType.XcorrShape.getTypeName(), scoreTypes) * -5.71823862d +
                scores.get(ScoreType.LogSnScore.getTypeName(), scoreTypes) * -0.72989582d;
    }

}
