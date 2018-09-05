package com.westlake.air.pecs.test.rtnormalizer;

import com.westlake.air.pecs.domain.bean.score.RtPair;
import com.westlake.air.pecs.test.BaseTest;
import com.westlake.air.pecs.utils.MathUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-09-05 18:41
 */
public class RsqTest extends BaseTest {

    @Test
    public void testRsq() {
        double[] rt1 = {
                990.122660955548, 2816.10547890782, 947.163908967972, 1494.42106087089,
                2408.52804116607, 3064.07976658463, 1161.83870116949, 1821.06033089161,
                2834.29082457006, 1572.47906724691, 2189.26979732752, 1787.71474971175,
                2442.26514096379, 3061.26265115797, 3066.66819218874, 2630.77600114226,
                600.903065836072
        };
        double[] rt2 = {
                6.1, 106, -5.75181, 23.6, 78.7, 154.8, 13.1, 43.1, 113.6, 36.4, 64.6, 54.3, 97.9, 127.2, 137.8, 82.7, -23.64895
        };
        List<RtPair> pairs = new ArrayList<>();
        for(int i=0; i<rt1.length; i++){
            RtPair rtPair = new RtPair();
            rtPair.setExpRt(rt1[i]);
            rtPair.setTheoRt(rt2[i]);
            pairs.add(rtPair);
        }

//        float result = MathUtil.getRsq(pairs);

        double resultNew = MathUtil.getRsq(pairs);
        System.out.println("Rsq test finished.");



    }
}
