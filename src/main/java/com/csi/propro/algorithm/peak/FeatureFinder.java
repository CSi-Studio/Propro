package com.csi.propro.algorithm.peak;

import com.csi.propro.constants.Constants;
import com.csi.propro.domain.bean.analyse.PeptideSpectrum;
import com.csi.propro.domain.bean.analyse.RtIntensityPairsDouble;
import com.csi.propro.domain.bean.score.IonPeak;
import com.csi.propro.domain.bean.score.PeakGroup;
import com.csi.propro.utils.ArrayUtil;
import com.csi.propro.utils.MathUtil;
import com.csi.propro.utils.PeakUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-01 22：42
 */
@Component("featureFinder")
public class FeatureFinder {

    public final Logger logger = LoggerFactory.getLogger(FeatureFinder.class);

    /**
     * 1）找出pickedChrom下的最高峰，得到对应rt和rtLeft、rtRight
     * 2）将pickedChrom最高峰intensity设为0
     * 3）将最高峰对应的chromatogram设为masterChromatogram
     * 4）对每个chromatogram映射到masterChromatogram，得到usedChromatogram
     * 5）对usedChromatogram求feature
     * 6）同时累计所有chromatogram（isDetecting）的intensity为totalXIC
     *
     * @param peptideSpectrum origin chromatogram
     * @param ionPeaks        maxPeaks picked and recalculated
     * @param ionPeakParams   left right borders
     *                        totalXic : intensity sum of all chromatogram of peptideRef(not rastered and all interval)
     *                        HullPoints : rt intensity pairs of rastered chromatogram between rtLeft, rtRight;
     *                        ExperimentFeature::intensity: intensity sum of hullPoints' intensity
     * @return list of mrmFeature (mrmFeature is list of chromatogram feature)
     */
    public List<PeakGroup> findFeatures(PeptideSpectrum peptideSpectrum, HashMap<String, RtIntensityPairsDouble> ionPeaks, HashMap<String, List<IonPeak>> ionPeakParams, HashMap<String, double[]> noise1000Map) {

        //totalXIC
        double totalXic = 0.0d;
        for (Double[] intensityTmp : peptideSpectrum.getIntensitiesMap().values()) {
            for (double intensity : intensityTmp) {
                totalXic += intensity;
            }
        }

        List<PeakGroup> peakGroupList = new ArrayList<>();


        //mrmFeature loop
        while (true) {
            PeakGroup peakGroup = new PeakGroup();
            Pair<String, Integer> maxPeakLocation = findLargestPeak(ionPeaks);
            if (maxPeakLocation.getKey().equals("null")) {
                break;
            }
            String maxCutInfo = maxPeakLocation.getKey();
            int maxIndex = maxPeakLocation.getValue();
            int leftIndex = ionPeakParams.get(maxCutInfo).get(maxIndex).getLeftRtIndex();
            int rightIndex = ionPeakParams.get(maxCutInfo).get(maxIndex).getRightRtIndex();
            double apexRt = ionPeaks.get(maxCutInfo).getRtArray()[maxIndex];
            double bestLeft = peptideSpectrum.getRtArray()[leftIndex];
            double bestRight = peptideSpectrum.getRtArray()[rightIndex];


            peakGroup.setApexRt(apexRt);
            peakGroup.setBestLeftRt(bestLeft);
            peakGroup.setBestRightRt(bestRight);

            RtIntensityPairsDouble rtInt = ionPeaks.get(maxCutInfo);
            rtInt.getIntensityArray()[maxIndex] = 0.0d;

            removeOverlappingFeatures(ionPeaks, leftIndex, rightIndex, ionPeakParams);

            Double[] rtArray = peptideSpectrum.getRtArray();

            //取得[bestLeft,bestRight]对应范围的Rt
            Double[] rasteredRt = new Double[rightIndex - leftIndex + 1];
            System.arraycopy(rtArray, leftIndex, rasteredRt, 0, rightIndex - leftIndex + 1);
            int maxSpectrumIndex = PeakUtil.findNearestIndex(rasteredRt, apexRt) + leftIndex;
            //取得[bestLeft,bestRight]对应范围的Intensity
            HashMap<String, Double[]> ionHullInt = new HashMap<>();
            HashMap<String, Double> ionIntensity = new HashMap<>();
            Double peakGroupInt = 0D;
            double signalToNoiseSum = 0d;
            for (String cutInfo : ionPeakParams.keySet()) {
                Double[] intArray = peptideSpectrum.getIntensitiesMap().get(cutInfo);
                //离子峰
                Double[] rasteredInt = new Double[rightIndex - leftIndex + 1];
                System.arraycopy(intArray, leftIndex, rasteredInt, 0, rightIndex - leftIndex + 1);
                ionHullInt.put(cutInfo, rasteredInt);
                //peakGroup强度
                Double ionIntTemp = MathUtil.sum(rasteredInt);
                peakGroupInt += ionIntTemp;
                //离子峰强度
                ionIntensity.put(cutInfo, ionIntTemp);
                //信噪比
                signalToNoiseSum += noise1000Map.get(cutInfo)[maxSpectrumIndex];
            }
//            List<Double> ionIntList = new ArrayList<>(ionIntensity.values());
            if (peakGroupInt == 0D) {
                continue;
            }
            peakGroup.setIonCount(ionPeaks.size());
            peakGroup.setIonHullRt(rasteredRt);
            peakGroup.setIonHullInt(ionHullInt);
            peakGroup.setPeakGroupInt(peakGroupInt);
            peakGroup.setTotalXic(totalXic);
            peakGroup.setIonIntensity(ionIntensity);
            peakGroup.setSignalToNoiseSum(signalToNoiseSum);
            peakGroupList.add(peakGroup);
            if (peakGroupInt > 0 && peakGroupInt / totalXic < Constants.STOP_AFTER_INTENSITY_RATIO) {
                break;
            }
        }

        return peakGroupList;
    }

    public List<PeakGroup> findFeaturesNew(PeptideSpectrum peptideSpectrum, HashMap<String, RtIntensityPairsDouble> ionPeaks, HashMap<String, List<IonPeak>> ionPeakParams, HashMap<String, double[]> noise1000Map) {

        //totalXIC
        double totalXic = 0.0d;
        for (Double[] intensityTmp : peptideSpectrum.getIntensitiesMap().values()) {
            for (double intensity : intensityTmp) {
                totalXic += intensity;
            }
        }

        //new function
        List<PeakGroup> peakGroupList = new ArrayList<>();
        Double[] peakDensity = new Double[peptideSpectrum.getRtArray().length];
        List<HashMap<String, IonPeak>> ionPeakPositionList = new ArrayList<>();
        for (int i = 0; i < peptideSpectrum.getRtArray().length; i++) {
            ionPeakPositionList.add(new HashMap<>());
        }
        for (String cutInfo : ionPeakParams.keySet()) {
            for (IonPeak ionPeak : ionPeakParams.get(cutInfo)) {
                if (ionPeak.getIntensity() != 0) {
                    ionPeakPositionList.get(ionPeak.getApexRtIndex()).put(cutInfo, ionPeak);
                }
            }
        }
        for (int i = 1; i < peakDensity.length - 1; i++) {
//            peakDensity[i] = density(ionPeakPositionList, i);
            peakDensity[i] = ionPeakPositionList.get(i).size() + Constants.SIDE_PEAK_DENSITY * (ionPeakPositionList.get(i - 1).size() + ionPeakPositionList.get(i + 1).size());
        }
        peakDensity[0] = 0d;
        peakDensity[peakDensity.length - 1] = 0d;
        List<Integer> topIndex = getTopIndex(peakDensity, FastMath.round(ionPeakParams.size() * Constants.ION_PERCENT * 10) / 10d);

        for (int i = 0; i < topIndex.size(); i++) {
            int maxIndex = topIndex.get(i);
            if (maxIndex == 0) {
                continue;
            }
            //合并周围结果

            PeakGroup peakGroup = new PeakGroup();
            HashMap<String, IonPeak> concateMap = concatenate(ionPeakPositionList, maxIndex, 1);
            String maxIon = getMaxIntensityIndex(concateMap);
            int leftIndex = concateMap.get(maxIon).getLeftRtIndex();
            int rightIndex = concateMap.get(maxIon).getRightRtIndex();
            double apexRt = ionPeaks.get(maxIon).getRtArray()[concateMap.get(maxIon).getIndex()];
            double bestLeft = peptideSpectrum.getRtArray()[leftIndex];
            double bestRight = peptideSpectrum.getRtArray()[rightIndex];

            peakGroup.setApexRt(apexRt);
            peakGroup.setBestLeftRt(bestLeft);
            peakGroup.setBestRightRt(bestRight);

            for (int j = 0; j < topIndex.size(); j++) {
                if (topIndex.get(j) <= rightIndex && topIndex.get(j) >= leftIndex) {
                    topIndex.set(j, 0);
                }
            }
            Double[] rtArray = peptideSpectrum.getRtArray();

            //取得[bestLeft,bestRight]对应范围的Rt
            Double[] rasteredRt = new Double[rightIndex - leftIndex + 1];
            System.arraycopy(rtArray, leftIndex, rasteredRt, 0, rightIndex - leftIndex + 1);
            int nearestRtIndex = PeakUtil.findNearestIndex(rasteredRt, apexRt);
            int maxSpectrumIndex = nearestRtIndex + leftIndex;
            //取得[bestLeft,bestRight]对应范围的Intensity
            HashMap<String, Double[]> ionHullInt = new HashMap<>();
            HashMap<String, Double> ionIntensity = new HashMap<>();
            Double peakGroupInt = 0D;
            double signalToNoiseSum = 0d;
            for (String cutInfo : ionPeakParams.keySet()) {
                //离子峰
                Double[] intArray = peptideSpectrum.getIntensitiesMap().get(cutInfo);
                //设定局部最大值（峰型控制备用）
                double localMaxIntensity = intArray[maxSpectrumIndex];
//                if(intArray[maxSpectrumIndex] < intArray[maxSpectrumIndex-1] && intArray[maxSpectrumIndex] < intArray[maxSpectrumIndex+1]){
//                    localMaxIntensity = FastMath.max(intArray[maxSpectrumIndex-1], intArray[maxSpectrumIndex+1]);
//                }
                localMaxIntensity = FastMath.max(intArray[maxSpectrumIndex - 1], FastMath.max(intArray[maxSpectrumIndex], intArray[maxSpectrumIndex + 1]));

                //获得峰值部分的Intensity序列
//                Double[] rasteredInt = new Double[rightIndex - leftIndex + 1];
//                System.arraycopy(intArray, leftIndex, rasteredInt, 0, rightIndex - leftIndex + 1);
                Double[] rasteredInt = filteredCopy(intArray, leftIndex, rightIndex, Double.MAX_VALUE);
                ionHullInt.put(cutInfo, rasteredInt);

                //离子峰强度
                Double ionIntTemp = MathUtil.sum(rasteredInt);
//                Double ionIntTemp = getIonIntensity(rasteredInt, localMaxIntensity * 2);
//                Double ionIntTemp = (intArray[maxSpectrumIndex]+1) * Math.min(maxSpectrumIndex - leftIndex, rightIndex - maxSpectrumIndex) * Constants.SQRT_2PI / 2d;
                peakGroupInt += ionIntTemp;
                ionIntensity.put(cutInfo, ionIntTemp);

                //信噪比
                signalToNoiseSum += noise1000Map.get(cutInfo)[maxSpectrumIndex];
            }
            if (peakGroupInt == 0D) {
                continue;
            }
            peakGroup.setIonCount(ionPeaks.size());
            peakGroup.setIonHullRt(rasteredRt);
            peakGroup.setIonHullInt(ionHullInt);
            peakGroup.setPeakGroupInt(peakGroupInt);
            peakGroup.setTotalXic(totalXic);
            peakGroup.setIonIntensity(ionIntensity);
            peakGroup.setSignalToNoiseSum(signalToNoiseSum);
            peakGroupList.add(peakGroup);
        }
        return peakGroupList;
    }

    /**
     * 从maxPeak list中选取最大peak对应的index
     *
     * @param peaksCoord maxPeaks
     * @return list index, pairs index
     */
    private Pair<String, Integer> findLargestPeak(HashMap<String, RtIntensityPairsDouble> peaksCoord) {
        double largest = 0.0d;
        Pair<String, Integer> maxPeakLoc = Pair.of("null", -1);

        for (String cutInfo : peaksCoord.keySet()) {
            for (int i = 0; i < peaksCoord.get(cutInfo).getRtArray().length; i++) {
                if (peaksCoord.get(cutInfo).getIntensityArray()[i] > largest) {
                    largest = peaksCoord.get(cutInfo).getIntensityArray()[i];
                    maxPeakLoc = Pair.of(cutInfo, i);
                }
            }
        }
        return maxPeakLoc;
    }


    /**
     * 过滤区间内的峰值，也可以理解成：以已经选取的高峰划分peak group
     * <p>
     * 中心落在更高峰闭区间内的会被过滤掉
     * 边界落在更高峰开区间内的会被过滤掉
     *
     * @param ionPeaks
     * @param bestLeft      按从高到低顺序选择的最高峰的RT范围
     * @param bestRight     同上
     * @param ionPeakParams
     */
    private void removeOverlappingFeatures(HashMap<String, RtIntensityPairsDouble> ionPeaks, int bestLeft, int bestRight, HashMap<String, List<IonPeak>> ionPeakParams) {
        for (String cutInfo : ionPeaks.keySet()) {
            Double[] intensity = ionPeaks.get(cutInfo).getIntensityArray();
            for (int j = 0; j < intensity.length; j++) {
                if (intensity[j] <= 0d) {
                    continue;
                }
                try {
                    int mid = ionPeakParams.get(cutInfo).get(j).getApexRtIndex();
                    if (mid >= bestLeft && mid <= bestRight) {
                        intensity[j] = 0d;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                int left = ionPeakParams.get(cutInfo).get(j).getLeftRtIndex();
                int right = ionPeakParams.get(cutInfo).get(j).getRightRtIndex();
                if ((left > bestLeft && left < bestRight) || (right > bestLeft && right < bestRight)) {
                    intensity[j] = 0d;
                }
            }
        }
    }

    private List<Integer> getTopIndex(Double[] value, double minIon) {
        Integer[] indexAfterSort = ArrayUtil.indexAfterSort(value);
        List<Integer> index = new ArrayList<>();
        for (int i = 0; i < value.length; i++) {
            int indexTemp = indexAfterSort[indexAfterSort.length - 1 - i];
            if (value[indexTemp] < minIon) {
                break;
            }
            index.add(indexTemp);
        }
        return index;
    }

    private String getMaxIntensityIndex(HashMap<String, IonPeak> ionPeakList) {
        double maxIntensity = -1d;
        String maxIon = "";
        for (String cutInfo : ionPeakList.keySet()) {
            if (ionPeakList.get(cutInfo).getIntensity() > maxIntensity) {
                maxIntensity = ionPeakList.get(cutInfo).getIntensity();
                maxIon = cutInfo;
            }
        }
        return maxIon;
    }

    private HashMap<String, IonPeak> concatenate(List<HashMap<String, IonPeak>> ionPeakList, int maxIndex, int range) {
        HashMap<String, IonPeak> result = new HashMap<>();
        for (int i = maxIndex - range; i <= maxIndex + range && i < ionPeakList.size(); i++) {
            for (String cutInfo : ionPeakList.get(i).keySet()) {
                if (!result.containsKey(cutInfo)) {
                    result.put(cutInfo, ionPeakList.get(i).get(cutInfo));
                } else if (result.get(cutInfo).getIntensity() < ionPeakList.get(i).get(cutInfo).getIntensity()) {
                    result.put(cutInfo, ionPeakList.get(i).get(cutInfo));
                }
            }
        }
        return result;
    }

    private double density(List<HashMap<String, IonPeak>> ionPeakList, int maxIndex) {
        HashSet<String> set = new HashSet<>();
        for (int i = maxIndex - 1; i <= maxIndex + 1; i++) {
            set.addAll(ionPeakList.get(i).keySet());
        }
        int midSize = ionPeakList.get(maxIndex).size();
        return midSize + Constants.SIDE_PEAK_DENSITY * (midSize - set.size());
    }

    private Double[] filteredCopy(Double[] array, int leftIndex, int rightIndex, double maxValue) {
        Double[] result = new Double[rightIndex - leftIndex + 1];
        for (int i = 0; i < result.length; i++) {
            if (array[leftIndex + i] <= maxValue) {
                result[i] = array[leftIndex + i];
            } else {
                result[i] = maxValue;
            }
        }
        return result;
    }

    private Double getIonIntensity(Double[] intArray, double localMax) {
        Double result = 0d;
        for (Double intensity : intArray) {
            if (intensity > localMax) {
                result += localMax;
            } else {
                result += intensity;
            }
        }
        return result;
    }
}
