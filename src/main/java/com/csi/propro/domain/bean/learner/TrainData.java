package com.csi.propro.domain.bean.learner;

import com.csi.propro.domain.db.simple.PeptideScores;
import lombok.Data;

import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-18 23:16
 */
@Data
public class TrainData {

    List<PeptideScores> targets;
    List<PeptideScores> decoys;

    public TrainData() {}

    public TrainData(List<PeptideScores> targets, List<PeptideScores> decoys) {
        this.targets = targets;
        this.decoys = decoys;
    }
}
