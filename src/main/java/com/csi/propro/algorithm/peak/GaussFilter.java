package com.csi.propro.algorithm.peak;

import com.csi.propro.constants.Constants;
import com.csi.propro.domain.bean.analyse.SigmaSpacing;
import com.csi.propro.utils.MathUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-07-31 16-28
 */
@Component("gaussFilter")
public class GaussFilter {

    public Double[] filter(Float[] rtArray, String cutInfo, Float[] intArray, SigmaSpacing ss) {
        Double[] rts = new Double[rtArray.length];
        Double[] ints = new Double[intArray.length];
        for (int i = 0; i < rts.length; i++) {
            rts[i] = Double.parseDouble(rtArray[i].toString());
            ints[i] = Double.parseDouble(intArray[i].toString());
        }
        HashMap<String, Double[]> intensitiesMap = new HashMap<>();
        intensitiesMap.put(cutInfo, ints);

        HashMap<String, Double[]> resultMap = filter(rts, intensitiesMap, ss);
        return resultMap.values().iterator().next();
    }

    public Float[] filterForFloat(Float[] rtArray, String cutInfo, Float[] intArray) {
        return filterForFloat(rtArray, cutInfo, intArray, SigmaSpacing.create());
    }

    public Float[] filterForFloat(Float[] rtArray, String cutInfo, Float[] intArray, SigmaSpacing ss) {
        Double[] result = filter(rtArray, cutInfo, intArray, ss);
        Float[] floatArray = new Float[result.length];
        for (int i = 0; i < floatArray.length; i++) {
            floatArray[i] = result[i].floatValue();
        }
        return floatArray;
    }

    /**
     * @param rtArray
     * @param intensitiesMap
     * @param sigmaSpacing
     * @return
     */
    public HashMap<String, Double[]> filter(Double[] rtArray, HashMap<String, Double[]> intensitiesMap, SigmaSpacing sigmaSpacing) {

        Double spacing = sigmaSpacing.getSpacingDouble();
        //coeffs: 以0为中心，sigma为标准差的正态分布参数
        double[] coeffs = sigmaSpacing.getCoeffs();
        int middle = sigmaSpacing.getRightNum();
        double middleSpacing = sigmaSpacing.getRightNumSpacing();

        int rtLength = rtArray.length;
        double startPosition, endPosition;
        double minRt = rtArray[0];
        double maxRt = rtArray[rtLength - 1];

        //begin integrate
        HashMap<String, Double[]> newIntensitiesMap = new HashMap<String, Double[]>();
        for (String cutInfo : intensitiesMap.keySet()) {
            newIntensitiesMap.put(cutInfo, new Double[rtArray.length]);
        }

        Double distanceInGaussian;
        int leftPosition;
        int rightPosition;
        double residualPercent;
        double coeffRight;
        double coeffLeft;
        double norm = 0;

        for (int i = 0; i < rtLength; i++) {
            HashMap<String, Double> vMap = new HashMap<>();
            for (String cutInfo : intensitiesMap.keySet()) {
                vMap.put(cutInfo, 0d);
            }
            norm = 0;
            //startPosition
            if ((rtArray[i] - middleSpacing) > minRt) {
                startPosition = MathUtil.keepLength(rtArray[i] - middleSpacing, Constants.PRECISION);
            } else {
                startPosition = minRt;
            }

            //endPostion
            if ((rtArray[i] + middleSpacing) < maxRt) {
                endPosition = MathUtil.keepLength(rtArray[i] + middleSpacing, Constants.PRECISION);
            } else {
                endPosition = maxRt;
            }

            //help index
            int j = i;

            // left side of i
            while (j > 0 && rtArray[j - 1] > startPosition) {
                distanceInGaussian = MathUtil.keepLength(rtArray[i] - rtArray[j], Constants.PRECISION);
                leftPosition = (int) MathUtil.keepLength(distanceInGaussian / spacing, Constants.PRECISION);
                rightPosition = leftPosition + 1;
                residualPercent = (Math.abs(leftPosition * spacing) - distanceInGaussian) / spacing;
                if (rightPosition < middle) {
                    coeffRight = (1 - residualPercent) * coeffs[leftPosition] + residualPercent * coeffs[rightPosition];
                } else {
                    coeffRight = coeffs[leftPosition];
                }

                distanceInGaussian = MathUtil.keepLength(rtArray[i] - rtArray[j - 1], Constants.PRECISION);
                leftPosition = (int) MathUtil.keepLength((distanceInGaussian / spacing), Constants.PRECISION);
                rightPosition = leftPosition + 1;
                residualPercent = (Math.abs(leftPosition * spacing - distanceInGaussian)) / spacing;
                if (rightPosition < middle) {
                    coeffLeft = (1 - residualPercent) * coeffs[leftPosition] + residualPercent * coeffs[rightPosition];
                } else {
                    coeffLeft = coeffs[leftPosition];
                }

                norm += Math.abs(rtArray[j - 1] - rtArray[j]) * (coeffRight + coeffLeft) / 2.0;

                for (String cutInfo : vMap.keySet()) {
                    double t = vMap.get(cutInfo);
                    t += Math.abs(rtArray[j - 1] - rtArray[j]) * (intensitiesMap.get(cutInfo)[j - 1] * coeffLeft + intensitiesMap.get(cutInfo)[j] * coeffRight) / 2.0;
                    vMap.put(cutInfo, t);
                }

                j--;

            }

            j = i;
            // right side of i
            while (j < rtLength - 1 && rtArray[j + 1] < endPosition) {
                distanceInGaussian = MathUtil.keepLength(rtArray[j] - rtArray[i], Constants.PRECISION);
                leftPosition = (int) MathUtil.keepLength(distanceInGaussian / spacing, Constants.PRECISION);
                rightPosition = leftPosition + 1;
                residualPercent = (Math.abs(leftPosition * spacing) - distanceInGaussian) / spacing;
                if (rightPosition < middle) {
                    coeffLeft = (1 - residualPercent) * coeffs[leftPosition] + residualPercent * coeffs[rightPosition];
                } else {
                    coeffLeft = coeffs[leftPosition];
                }

                distanceInGaussian = MathUtil.keepLength(rtArray[j + 1] - rtArray[i], Constants.PRECISION);
                leftPosition = (int) MathUtil.keepLength(distanceInGaussian / spacing, Constants.PRECISION);
                rightPosition = leftPosition + 1;

                residualPercent = (Math.abs(leftPosition * spacing - distanceInGaussian)) / spacing;
                if (rightPosition < middle) {
                    coeffRight = (1 - residualPercent) * coeffs[leftPosition] + residualPercent * coeffs[rightPosition];
                } else {
                    coeffRight = coeffs[leftPosition];
                }

                norm += Math.abs(rtArray[j + 1] - rtArray[j]) * (coeffLeft + coeffRight) / 2.0;
                for (String cutInfo : vMap.keySet()) {
                    double t = vMap.get(cutInfo);
                    t += Math.abs(rtArray[j + 1] - rtArray[j]) * (intensitiesMap.get(cutInfo)[j] * coeffLeft + intensitiesMap.get(cutInfo)[j + 1] * coeffRight) / 2.0;
                    vMap.put(cutInfo, t);
                }
                j++;

            }

            for (String cutInfo : vMap.keySet()) {
                if (vMap.get(cutInfo) > 0) {
                    newIntensitiesMap.get(cutInfo)[i] = vMap.get(cutInfo) / norm;
                } else {
                    newIntensitiesMap.get(cutInfo)[i] = 0d;
                }
            }

        }
        return newIntensitiesMap;
    }
}
