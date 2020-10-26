package com.csi.propro.domain.bean.learner;

import com.csi.propro.domain.db.simple.PeptideScores;
import lombok.Data;

import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-18 23:16
 */
@Data
public class TrainAndTest {
    Double[][] trainData;
    Integer[] trainId;
    Boolean[] trainIsDecoy;
    Double[][] testData;
    Integer[] testId;
    Boolean[] testIsDecoy;

    List<PeptideScores> trainTargets;
    List<PeptideScores> trainDecoys;
    List<PeptideScores> testTargets;
    List<PeptideScores> testDecoys;

    public TrainAndTest(){}

    public TrainAndTest(List<PeptideScores> trainTargets, List<PeptideScores> trainDecoys, List<PeptideScores> testTargets, List<PeptideScores> testDecoys){
        this.trainTargets = trainTargets;
        this.trainDecoys = trainDecoys;
        this.testTargets = testTargets;
        this.testDecoys = testDecoys;
    }
}
