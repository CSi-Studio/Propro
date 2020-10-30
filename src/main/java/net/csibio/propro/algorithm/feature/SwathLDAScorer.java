package net.csibio.propro.algorithm.feature;

import net.csibio.propro.constants.enums.ScoreType;
import net.csibio.propro.domain.bean.score.FeatureScores;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-19 21:05
 */
@Component("swathLDAScorer")
public class SwathLDAScorer {

    //    /**
//     * -scores.calculate_swath_lda_prescore
//     *
//     * @param scores
//     * @return
//     */
    public void calculateSwathLdaPrescore(FeatureScores scores, List<String> scoreTypes) {

        Double libraryCorr = scores.get(ScoreType.LibraryCorr.getTypeName(), scoreTypes);
        libraryCorr = (libraryCorr == null ? 0 : libraryCorr);

        Double libraryRsmd = scores.get(ScoreType.LibraryRsmd.getTypeName(), scoreTypes);
        libraryRsmd = (libraryRsmd == null ? 0 : libraryRsmd);

        Double normRtScore = scores.get(ScoreType.NormRtScore.getTypeName(), scoreTypes);
        normRtScore = (normRtScore == null ? 0 : normRtScore);

        Double isotopeCorrelationScore = scores.get(ScoreType.IsotopeCorrelationScore.getTypeName(), scoreTypes);
        isotopeCorrelationScore = (isotopeCorrelationScore == null ? 0 : isotopeCorrelationScore);

        Double isotopeOverlapScore = scores.get(ScoreType.IsotopeOverlapScore.getTypeName(), scoreTypes);
        isotopeOverlapScore = (isotopeOverlapScore == null ? 0 : isotopeOverlapScore);

        Double massdevScore = scores.get(ScoreType.MassdevScore.getTypeName(), scoreTypes);
        massdevScore = (massdevScore == null ? 0 : massdevScore);

        Double xcorrCoelution = scores.get(ScoreType.XcorrCoelution.getTypeName(), scoreTypes);
        xcorrCoelution = (xcorrCoelution == null ? 0 : xcorrCoelution);

        Double xcorrShape = scores.get(ScoreType.XcorrShape.getTypeName(), scoreTypes);
        xcorrShape = (xcorrShape == null ? 0 : xcorrShape);

        Double yseriesScore = scores.get(ScoreType.YseriesScore.getTypeName(), scoreTypes);
        yseriesScore = (yseriesScore == null ? 0 : yseriesScore);

        Double logSnScore = scores.get(ScoreType.LogSnScore.getTypeName(), scoreTypes);
        logSnScore = (logSnScore == null ? 0 : logSnScore);

        scores.put(ScoreType.MainScore.getTypeName(),
                (0.19011762) * libraryCorr +
                        (-2.47298914) * libraryRsmd +
                        (-5.63906731) * normRtScore +
                        (0.62640133) * isotopeCorrelationScore +
                        (-0.36006925) * isotopeOverlapScore +
                        (-0.08814003) * massdevScore +
                        (-0.13978311) * xcorrCoelution +
                        (1.16475032) * xcorrShape +
                        (0.19267813) * yseriesScore +
                        (0.61712054) * logSnScore, scoreTypes);
    }
}
