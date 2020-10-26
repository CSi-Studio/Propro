package com.csi.propro.domain.bean.learner;

import com.csi.propro.domain.bean.score.SimpleFeatureScores;
import lombok.Data;

import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-13 16:00
 */
@Data
public class TrainPeaks {

    List<SimpleFeatureScores> bestTargets;

    List<SimpleFeatureScores> topDecoys;
}
