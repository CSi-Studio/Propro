package net.csibio.propro.test.rtnormalizer;

import net.csibio.propro.domain.bean.analyse.RtIntensityPairsDouble;
import net.csibio.propro.domain.bean.analyse.SigmaSpacing;
import net.csibio.propro.test.BaseTest;
import net.csibio.propro.algorithm.peak.GaussFilter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

/**
 * Created by Song Jian
 * Time: 2018-08-09 14-31
 */
public class GauseFilterTest extends BaseTest {

    @Autowired
    GaussFilter gauseFilter;

    @Test
    public void filterTest_1() {
        Double[] rtArray = new Double[5];
        Double[] intensityArray = new Double[5];
        for (int i = 0; i < 5; i++) {
             intensityArray[i] = 1.0d;
             rtArray[i]= 500.0d + 0.2d * i;
        }

        RtIntensityPairsDouble input = new RtIntensityPairsDouble(rtArray, intensityArray);
        HashMap<String, Double[]> result = null;
        HashMap<String, Double[]> map = new HashMap<>();
        map.put("temp", intensityArray);
        result = gauseFilter.filter(rtArray, map, new SigmaSpacing(1.0f/8, 0.01f));
        for (int i = 0; i < 5; i++) {
            assert isSimilar(result.get("temp")[0], 1.0F, 1e-5F);
        }
    }

    @Test
    public void filterTest_2() {
        Double[] rtArray = new Double[9];
        Double[] intensityArray = new Double[9];
        for (int i = 0; i < 9; i++) {
            intensityArray[i] = 0.0d;
            rtArray[i]= 500.0D + 0.03d * i;
            if (i == 3) {
                intensityArray[i] = 1.0d;
            }
            if (i == 4) {
                intensityArray[i] = 0.8d;
            }
            if (i == 5) {
                intensityArray[i] = 1.2d;
            }
        }
        RtIntensityPairsDouble input = new RtIntensityPairsDouble(rtArray, intensityArray);
        HashMap<String, Double[]> result = null;
        HashMap<String, Double[]> map = new HashMap<>();
        map.put("temp", intensityArray);
        result = gauseFilter.filter(rtArray, map, new SigmaSpacing(0.2f/8, 0.01f));
        assert isSimilar(result.get("temp")[0], 0.000734827F, 0.01F);
        assert isSimilar(result.get("temp")[1], 0.0543746F, 1e-2F);
        assert isSimilar(result.get("temp")[2], 0.298025F, 1e-2F);
        assert isSimilar(result.get("temp")[3], 0.707691F, 1e-2F);
        assert isSimilar(result.get("temp")[4], 0.8963F, 1e-2F);
        assert isSimilar(result.get("temp")[5], 0.799397F, 1e-2F);
        assert isSimilar(result.get("temp")[6], 0.352416F, 1e-2F);
        assert isSimilar(result.get("temp")[7], 0.065132F, 1e-2F);
        assert isSimilar(result.get("temp")[8], 0.000881793F, 1e-2F);
    }

    private boolean isSimilar(Double a, Float b, Float tolerance ) {
        Float TOLERANCE = tolerance;
        if (Math.abs(a-b) < TOLERANCE) {
            return true;
        } else {
            return false;
        }
    }
}
