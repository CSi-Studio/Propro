package net.csibio.propro.domain.bean.score;

import lombok.Data;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-07 23:39
 */
@Data
public class ScoreRtPair {
    double groupRt;
    double score;
    double rt;
    FeatureScores scores;
}
