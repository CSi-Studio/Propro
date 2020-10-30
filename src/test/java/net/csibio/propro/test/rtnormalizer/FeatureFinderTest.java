package net.csibio.propro.test.rtnormalizer;

import net.csibio.propro.algorithm.peak.*;
import net.csibio.propro.test.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-09-04 10:47
 */
public class FeatureFinderTest extends BaseTest {

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

//    @Test
//    public void featureFinderTest() {
//        Double[] rtData0 = {1474.34, 1477.11, 1479.88, 1482.64,
//                1485.41, 1488.19, 1490.95, 1493.72, 1496.48, 1499.25, 1502.03, 1504.8,
//                1507.56, 1510.33, 1513.09, 1515.87, 1518.64, 1521.42};
//        Double[] intData0 = {3.26958, 3.74189, 3.31075, 86.1901,
//                3.47528, 387.864, 13281d, 6375.84, 39852.6, 2.66726, 612.747, 3.34313,
//                793.12, 3.29156, 4.00586, 4.1591, 3.23035, 3.90591};
//
//        Double[] rtData1 = {1473.55, 1476.31, 1479.08, 1481.84,
//                1484.61, 1487.39, 1490.15, 1492.92, 1495.69, 1498.45, 1501.23, 1504d,
//                1506.76, 1509.53, 1512.29, 1515.07, 1517.84, 1520.62};
//        Double[] intData1 = {3.44054, 2142.31, 3.58763, 3076.97,
//                6663.55, 45681d, 157694d, 122844d, 86034.7, 85391.1, 15992.8,
//                2293.94, 6934.85, 2735.18, 459.413, 3.93863, 3.36564, 3.44005};
//
//        RtIntensityPairsDouble rtIntensityPairsDouble0 = new RtIntensityPairsDouble(rtData0, intData0);
//        RtIntensityPairsDouble rtIntensityPairsDouble1 = new RtIntensityPairsDouble(rtData1, intData1);
//        List<RtIntensityPairsDouble> rtIntensityPairsDoubleList = new ArrayList<>();
//        rtIntensityPairsDoubleList.add(rtIntensityPairsDouble0);
//        rtIntensityPairsDoubleList.add(rtIntensityPairsDouble1);
//
//        List<RtIntensityPairsDouble> peakPickerList = new ArrayList<>();
//        List<IntensityRtLeftRtRightPairs> chromatogramPickerList = new ArrayList<>();
//
//        for(int i=0; i<rtIntensityPairsDoubleList.size(); i++){
//            RtIntensityPairsDouble gaussFilterResult = gaussFilter.filter(rtIntensityPairsDoubleList.get(i), new SigmaSpacing(6.25f, 0.01f));
//
//            double[] stn200 = signalToNoiseEstimator.computeSTN(gaussFilterResult, 200, 30);
//            double[] stnOrigin = signalToNoiseEstimator.computeSTN(rtIntensityPairsDoubleList.get(i), 1000, 30);
//
//            RtIntensityPairsDouble peakPickerResult = peakPicker.pickMaxPeak(gaussFilterResult, stn200);
//            IntensityRtLeftRtRightPairs chromatogramPickerResult = chromatogramPicker.pickChromatogram(rtIntensityPairsDoubleList.get(i), rtIntensityPairsDoubleList.get(i), stnOrigin, peakPickerResult);
//
//            peakPickerList.add(peakPickerResult);
//            chromatogramPickerList.add(chromatogramPickerResult);
//        }
//
//        List<List<ExperimentFeature>> experimentFeature = featureFinder.findFeatures(rtIntensityPairsDoubleList, peakPickerList, chromatogramPickerList);
//
//        System.out.println("finish");
//        //passed
//    }
}
