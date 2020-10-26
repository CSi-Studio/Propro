package com.csi.propro.algorithm.peak;

import com.csi.propro.constants.Constants;
import com.csi.propro.domain.bean.analyse.RtIntensityPairsDouble;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-07-31 19-16
 */
@Component("peakPicker")
public class PeakPicker {

    /**
     * 1）选取最高峰
     * 2）对最高峰进行样条插值
     * 3）根据插值判断maxPeak的rt位置
     * 4）用插值与rt计算intensity
     *
     * @param intensityArray smoothed rtIntensityPairs
     * @param signalToNoise  window width = 200
     * @return maxPeaks
     */
    public RtIntensityPairsDouble pickMaxPeak(Double[] rtArray, Double[] intensityArray, double[] signalToNoise) {
        if (rtArray.length < 5) {
            return null;
        }
        List<Double> maxPeakRtList = new ArrayList<>();
        List<Double> maxPeakIntList = new ArrayList<>();
        double centralPeakRt, leftNeighborRt, rightNeighborRt;
        double centralPeakInt, leftBoundaryInt, rightBoundaryInt;
        double stnLeft, stnMiddle, stnRight;
        int leftBoundary, rightBoundary;
        int missing;
        double maxPeakRt;
        double maxPeakInt;
        double leftHand, rightHand;
        double mid;
        double midDerivVal;


        for (int i = 2; i < rtArray.length - 2; i++) {
            leftNeighborRt = rtArray[i - 1];
            centralPeakRt = rtArray[i];
            rightNeighborRt = rtArray[i + 1];

            leftBoundaryInt = intensityArray[i - 1];
            centralPeakInt = intensityArray[i];
            rightBoundaryInt = intensityArray[i + 1];

            if (rightBoundaryInt < 0.000001) continue;
            if (leftBoundaryInt < 0.000001) continue;

            double leftToCentral, centralToRight, minSpacing = 0d;
            if (Constants.CHECK_SPACINGS) {
                leftToCentral = centralPeakRt - leftNeighborRt;
                centralToRight = rightNeighborRt - centralPeakRt;
                //选出间距较小的那个
                minSpacing = (leftToCentral > centralToRight ? centralToRight : leftToCentral);
            }

            stnLeft = signalToNoise[i - 1];
            stnMiddle = signalToNoise[i];
            stnRight = signalToNoise[i + 1];

            //如果中间的比两边的都大
            if (centralPeakInt > leftBoundaryInt &&
                    centralPeakInt > rightBoundaryInt &&
                    stnLeft >= Constants.SIGNAL_TO_NOISE_LIMIT &&
                    stnMiddle >= Constants.SIGNAL_TO_NOISE_LIMIT &&
                    stnRight >= Constants.SIGNAL_TO_NOISE_LIMIT) {
                // 搜索左边的边界
                missing = 0;
                leftBoundary = i - 1;
                for (int left = 2; left < i + 1; left++) {

                    stnLeft = signalToNoise[i - left];

                    if (intensityArray[i - left] < leftBoundaryInt &&
                            (!Constants.CHECK_SPACINGS || (rtArray[leftBoundary] - rtArray[i - left] < Constants.SPACING_DIFFERENCE_GAP * minSpacing))) {
                        if (stnLeft >= Constants.SIGNAL_TO_NOISE_LIMIT &&
                                (!Constants.CHECK_SPACINGS || rtArray[leftBoundary] - rtArray[i - left] < Constants.SPACING_DIFFERENCE * minSpacing)) {
                            leftBoundaryInt = intensityArray[i - left];
                            leftBoundary = i - left;
                        } else {
                            missing++;
                            if (missing <= Constants.MISSING_LIMIT) {
                                leftBoundaryInt = intensityArray[i - left];
                                leftBoundary = i - left;
                            } else {
                                leftBoundary = i - left + 1;
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                    //zeroLeft
                    if (intensityArray[i - left] == 0) {
                        break;
                    }
                }

                // 搜索右边的边界
                missing = 0;
                rightBoundary = i + 1;
                for (int right = 2; right < rtArray.length - i; right++) {

                    stnRight = signalToNoise[i + right];


                    if (intensityArray[i + right] < rightBoundaryInt &&
                            (!Constants.CHECK_SPACINGS || (rtArray[i + right] - rtArray[rightBoundary] < Constants.SPACING_DIFFERENCE_GAP * minSpacing))) {
                        if (stnRight >= Constants.SIGNAL_TO_NOISE_LIMIT &&
                                (!Constants.CHECK_SPACINGS || rtArray[i + right] - rtArray[rightBoundary] < Constants.SPACING_DIFFERENCE * minSpacing)) {
                            rightBoundaryInt = intensityArray[i + right];
                            rightBoundary = i + right;
                        } else {
                            missing++;
                            if (missing <= Constants.MISSING_LIMIT) {
                                rightBoundaryInt = intensityArray[i + right];
                                rightBoundary = i + right;
                            } else {
                                rightBoundary = i + right - 1;
                                break;
                            }
                        }
                    } else {
                        break;
                    }

                    //zeroLeft
                    if (intensityArray[i + right] == 0) {
                        break;
                    }
                }

                PeakSpline peakSpline = new PeakSpline();
                peakSpline.init(rtArray, intensityArray, leftBoundary, rightBoundary);
                leftHand = leftNeighborRt;
                rightHand = rightNeighborRt;

                while (rightHand - leftHand > Constants.THRESHOLD) {
                    mid = (leftHand + rightHand) / 2.0d;
                    midDerivVal = peakSpline.derivatives(mid);
                    if (Math.abs(midDerivVal) < 0.001) {
                        break;
                    }
                    if (midDerivVal < 0.0d) {
                        rightHand = mid;
                    } else {
                        leftHand = mid;
                    }
                }

                maxPeakRt = (leftHand + rightHand) / 2.0d;
                maxPeakInt = peakSpline.eval(maxPeakRt);
                maxPeakRtList.add(maxPeakRt);
                maxPeakIntList.add(maxPeakInt);
                i = rightBoundary;
            }
        }
        Double[] rt = maxPeakRtList.toArray(new Double[0]);
        Double[] intensity = maxPeakIntList.toArray(new Double[0]);
        return new RtIntensityPairsDouble(rt, intensity);
    }

}
