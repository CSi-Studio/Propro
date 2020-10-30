package net.csibio.propro.test.rtnormalizer;

import net.csibio.propro.domain.bean.score.IonPeak;
import net.csibio.propro.domain.bean.analyse.RtIntensityPairsDouble;
import net.csibio.propro.domain.bean.analyse.SigmaSpacing;
import net.csibio.propro.algorithm.peak.ChromatogramPicker;
import net.csibio.propro.algorithm.peak.GaussFilter;
import net.csibio.propro.algorithm.peak.PeakPicker;
import net.csibio.propro.algorithm.peak.SignalToNoiseEstimator;
import net.csibio.propro.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-09-02 05:00
 */
public class ChromatogramPickerTest extends BaseTest {

    @Autowired
    GaussFilter gaussFilter;
    @Autowired
    PeakPicker peakPicker;
    @Autowired
    ChromatogramPicker chromatogramPicker;
    @Autowired
    SignalToNoiseEstimator signalToNoiseEstimator;

    @Test
    public void chromatogramTest(){
        Double[] rtData0 = {1474.34d, 1477.11d,  1479.88d, 1482.64d, 1485.41d, 1488.19d, 1490.95d, 1493.72d, 1496.48d, 1499.25d, 1502.03d, 1504.8d, 1507.56d, 1510.33d, 1513.09d, 1515.87d, 1518.64d, 1521.42d};
        Double[] intData0 = {3.26958d, 3.74189d, 3.31075d, 86.1901d, 3.47528d, 387.864d, 13281d  , 6375.84d, 39852.6d, 2.66726d, 612.747d, 3.34313d, 793.12d, 3.29156d, 4.00586d, 4.1591d, 3.23035d, 3.90591d};

        Double[] rtData1 = {1473.55, 1476.31,  1479.08, 1481.84, 1484.61, 1487.39, 1490.15, 1492.92, 1495.69, 1498.45, 1501.23, 1504d   , 1506.76, 1509.53, 1512.29, 1515.07, 1517.84, 1520.62};
        Double[] intData1 = {3.44054, 2142.31, 3.58763, 3076.97, 6663.55, 45681d ,  157694d , 122844d , 86034.7, 85391.1, 15992.8, 2293.94, 6934.85, 2735.18, 459.413, 3.93863, 3.36564, 3.44005};

        RtIntensityPairsDouble rtIntensityPairsDouble = new RtIntensityPairsDouble(rtData1, intData1);

        HashMap<String, Double[]> map = new HashMap<>();
        map.put("temp", intData1);
        HashMap<String, Double[]> gaussFilterResult = gaussFilter.filter(rtData1,map, new SigmaSpacing(6.25f, 0.01f));

        double[] stn200 = signalToNoiseEstimator.computeSTN(rtData1, gaussFilterResult.get("temp"), 200, 30);
        double[] stnOrigin = signalToNoiseEstimator.computeSTN(rtData1, intData1, 1000, 30);

        RtIntensityPairsDouble peakPickerResult = peakPicker.pickMaxPeak(rtData1, gaussFilterResult.get("temp"), stn200);

        List<IonPeak> chromatogramPickerResult = chromatogramPicker.pickChromatogram(rtData1, intData1, gaussFilterResult.get("temp"), stnOrigin, peakPickerResult);

        System.out.printf("Chromatogram Picker Test finished.");

    }



}
