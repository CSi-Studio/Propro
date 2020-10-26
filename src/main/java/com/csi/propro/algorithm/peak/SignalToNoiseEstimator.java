package com.csi.propro.algorithm.peak;

import com.csi.propro.constants.Constants;
import com.csi.propro.utils.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-07-31 19-30
 */
@Component("signalToNoiseEstimator")
public class SignalToNoiseEstimator {

    public final Logger logger = LoggerFactory.getLogger(SignalToNoiseEstimator.class);

    /**
     * 计算信噪比
     * 按位取窗口，窗口由小到大排序取中位数
     *
     * @param rtIntensity
     * @param windowLength
     * @param binCount
     * @return
     */
    public double[] computeSTN(Double[] rtArray, Double[] rtIntensity, double windowLength, int binCount) {

        //final result
        double[] stnResults = new double[rtArray.length];

        //get mean and variance
        double[] meanVariance = MathUtil.getMeanVariance(rtIntensity);

        //get max intensity
        double maxIntensity = meanVariance[0] + Math.sqrt(meanVariance[1]) * Constants.AUTO_MAX_STDEV_FACTOR;

        //bin params
        double windowHalfSize = windowLength / 2.0d;
        double binSize = Math.max(1.0d, maxIntensity / binCount);
        double[] binValue = new double[binCount];
        for (int bin = 0; bin < binCount; bin++) {
            binValue[bin] = (bin + 0.5d) * binSize;
        }

        //params
        int[] histogram = new int[binCount];
        int toBin;// bin in which a datapoint would fall
        int medianBin;// index of bin where the median is located
        int elementIncCount;// additive number of elements from left to x in histogram
        int elementsInWindow = 0;// tracks elements in current window, which may vary because of unevenly spaced data
        int elementsInWindowHalf;// number of elements where we find the median
        double noise;// noise value of a data point
        int windowsOverall = rtArray.length;// determine how many elements we need to estimate (for progress estimation)
//        float sparseWindowPercent = 0, histogramOobPercent = 0;

        //Main loop
        int left = 0;
        int right = 0;
        for(int positionCenter = 0; positionCenter < windowsOverall; positionCenter ++){
//            elementsInWindow = 0;
            //get left/right borders
            while (rtArray[left] < rtArray[positionCenter] - windowHalfSize) {
                toBin = Math.min((int) (rtIntensity[left] / binSize), binCount - 1);
                histogram[toBin]--;
                elementsInWindow--;
                left++;
            }
            while (right < windowsOverall && rtArray[right] <= rtArray[positionCenter] + windowHalfSize) {
                toBin = Math.min((int) (rtIntensity[right] / binSize), binCount - 1);
                histogram[toBin]++;
                elementsInWindow++;
                right++;
            }

            //noise
            if (elementsInWindow < Constants.MIN_REQUIRED_ELEMENTS) {
                noise = Constants.NOISE_FOR_EMPTY_WINDOW;
//                sparseWindowPercent++;
            } else {
                medianBin = -1;
                elementIncCount = 0;
                elementsInWindowHalf = (elementsInWindow + 1) / 2;
                while (medianBin < binCount - 1 && elementIncCount < elementsInWindowHalf) {
                    ++medianBin;
                    elementIncCount += histogram[medianBin];
                }
//                if (medianBin == binCount - 1) {
//                    histogramOobPercent++;
//                }
                noise = Math.max(1.0d, binValue[medianBin]);
            }
            stnResults[positionCenter] = rtIntensity[positionCenter] / noise;
        }
//
//        sparseWindowPercent = sparseWindowPercent * 100 / windowsOverall;
//        histogramOobPercent = histogramOobPercent * 100 / windowsOverall;
//        if (sparseWindowPercent > 20) {
//            logger.warn("Warning in SignalToNoiseEstimator: " + sparseWindowPercent + "% of windows were sparse.\nIncreasing windowLength or decreasing minRequiredElements");
//        }
//        if (histogramOobPercent != 0) {
//            logger.warn("WARNING in SignalToNoiseEstimatorMedian: " + histogramOobPercent + "% of all Signal-to-Noise estimates are too high, because the median was found in the rightmost histogram-bin. " +
//                    "You should consider increasing 'max_intensity' (and maybe 'bin_count' with it, to keep bin width reasonable)");
//        }

        return stnResults;
    }

}
