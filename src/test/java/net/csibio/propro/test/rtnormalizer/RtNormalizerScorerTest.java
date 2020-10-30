package net.csibio.propro.test.rtnormalizer;

import net.csibio.propro.domain.bean.score.ScoreRtPair;
import net.csibio.propro.algorithm.feature.RtNormalizerScorer;
import net.csibio.propro.test.BaseTest;
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
