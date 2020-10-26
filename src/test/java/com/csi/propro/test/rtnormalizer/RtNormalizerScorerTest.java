package com.csi.propro.test.rtnormalizer;

import com.csi.propro.domain.bean.score.ScoreRtPair;
import com.csi.propro.algorithm.feature.RtNormalizerScorer;
import com.csi.propro.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author: An Shaowei
 * @Time: 2018/8/15 9:50
 */
public class RtNormalizerScorerTest extends BaseTest {
    @Autowired
    RtNormalizerScorer RTNormalizerScorer;

    @Test
    public void scoreTest(){
        List<ScoreRtPair> result;
        assert true;
    }

    @Test
    public void calculateChromatographicScoresTest(){
        assert true;
    }

    @Test
    public void calculateLibraryScoresTest(){
        assert true;
    }

    @Test
    public void calculateIntensityScoreTest(){
        assert true;
    }

    @Test
    public void calculateDiaMassDiffScoreTest(){
        assert true;
    }

}
