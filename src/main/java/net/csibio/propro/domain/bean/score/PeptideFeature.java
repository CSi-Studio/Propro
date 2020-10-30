package net.csibio.propro.domain.bean.score;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-20 16:08
 */
@Data
public class PeptideFeature {
    boolean featureFound;

    List<PeakGroup> peakGroupList;

    HashMap<String, Double> normedLibIntMap;

    public PeptideFeature() {}

    public PeptideFeature(boolean isFeatureFound) {
        this.featureFound = isFeatureFound;
    }
}
