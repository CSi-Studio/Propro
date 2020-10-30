package net.csibio.propro.domain.bean.learner;

import lombok.Data;

import java.util.HashMap;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-17 17:59
 */

@Data
public class LDALearnData {
    Double[] topTestTargetScores;
    Double[] topTestDecoyScores;
    Double[] weights;

    HashMap<String, Double> weightsMap;
}
