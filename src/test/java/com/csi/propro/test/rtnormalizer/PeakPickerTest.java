package com.csi.propro.test.rtnormalizer;

import com.csi.propro.domain.bean.analyse.RtIntensityPairsDouble;
import com.csi.propro.algorithm.peak.PeakPicker;
import com.csi.propro.algorithm.peak.SignalToNoiseEstimator;
import com.csi.propro.test.BaseTest;
import com.csi.propro.utils.FileUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-30 15:40
 */
public class PeakPickerTest extends BaseTest {
    @Autowired
    PeakPicker peakPicker;
    @Autowired
    SignalToNoiseEstimator signalToNoiseEstimator;

    @Test
    public void peakPickerTest_1 ()throws IOException {

        File fileTest = new File(PeakPickerTest.class.getClassLoader().getResource("data/peakPickerTest.txt").getPath());
        BufferedReader readerTest = new BufferedReader(new FileReader(fileTest));
        RtIntensityPairsDouble rtIntensityPairsDoubleTest = FileUtil.txtReader(readerTest,"\t", 1,2);

        //原算法的测试用例输入精度有误，需要通过强转的方式修正
        Double[] rtArray = rtIntensityPairsDoubleTest.getRtArray();
        Double[] intArray = rtIntensityPairsDoubleTest.getIntensityArray();
        for(int i=0; i<rtArray.length; i++){
            rtArray[i] =(double) rtArray[i].floatValue();
            intArray[i] = (double) intArray[i].floatValue();
        }


        File fileResult = new File(PeakPickerTest.class.getClassLoader().getResource("data/peakPickerOutput.txt").getPath());
        BufferedReader readerResult = new BufferedReader(new FileReader(fileResult));
        RtIntensityPairsDouble rtIntensityPairsDoubleResult = FileUtil.txtReader(readerResult,"\t", 1, 2);

        double[] signalToNoise200 = signalToNoiseEstimator.computeSTN(rtIntensityPairsDoubleTest.getRtArray(), rtIntensityPairsDoubleTest.getIntensityArray(), 200, 30);

        RtIntensityPairsDouble pickResult = peakPicker.pickMaxPeak(rtIntensityPairsDoubleTest.getRtArray(), rtIntensityPairsDoubleTest.getIntensityArray(), signalToNoise200);
        for(int i=0; i<pickResult.getIntensityArray().length; i++){
//            if(!isSimilar(pickResult.getRtArray()[i], rtIntensityPairsDoubleResult.getRtArray()[i], Math.pow(10, -4))){
//                System.out.println(i + "\trt\t"+ pickResult.getRtArray()[i] +"\t" + rtIntensityPairsDoubleResult.getRtArray()[i]);
//            }
//            if(!isSimilar(pickResult.getIntensityArray()[i], rtIntensityPairsDoubleResult.getIntensityArray()[i], Math.pow(10, -4))){
//                System.out.println(i + "\trt\t"+ pickResult.getIntensityArray()[i] +"\t" + rtIntensityPairsDoubleResult.getIntensityArray()[i]);
//            }
            assert isSimilar(pickResult.getRtArray()[i], rtIntensityPairsDoubleResult.getRtArray()[i], Math.pow(10, -2));
            assert isSimilar(pickResult.getIntensityArray()[i], rtIntensityPairsDoubleResult.getIntensityArray()[i], Math.pow(10, -2));
        }

        System.out.println("pick Finish");
    }
    private boolean isSimilar(Double a, Double b, Double tolerance ) {
        if (Math.abs(a-b) < tolerance) {
            return true;
        } else {
            return false;
        }
    }

}
