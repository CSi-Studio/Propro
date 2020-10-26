package com.csi.propro.test.rtnormalizer;

import com.csi.propro.algorithm.peak.*;
import com.csi.propro.algorithm.feature.RtNormalizerScorer;
import com.csi.propro.test.BaseTest;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-09-04 19:15
 */
public class ScoreTest extends BaseTest {
    @Autowired
    GaussFilter gaussFilter;
    @Autowired
    PeakPicker peakPicker;
    @Autowired
    ChromatogramPicker chromatogramPicker;
    @Autowired
    SignalToNoiseEstimator signalToNoiseEstimator;
    @Autowired
    FeatureFinder featureFinder;
    @Autowired
    RtNormalizerScorer rtNormalizerScorer;

    @Test
    public void test(){
        System.out.println(Math.log(2));
        System.out.println(FastMath.log(2));
        System.out.println( (int) Math.ceil(FastMath.log(2,8)));
    }
}
