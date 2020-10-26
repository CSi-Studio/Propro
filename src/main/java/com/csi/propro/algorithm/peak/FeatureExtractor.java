package com.csi.propro.algorithm.peak;

import com.csi.propro.algorithm.feature.RtNormalizerScorer;
import com.csi.propro.domain.bean.analyse.PeptideSpectrum;
import com.csi.propro.domain.bean.analyse.RtIntensityPairsDouble;
import com.csi.propro.domain.bean.analyse.SigmaSpacing;
import com.csi.propro.domain.bean.score.IonPeak;
import com.csi.propro.domain.bean.score.PeakGroup;
import com.csi.propro.domain.bean.score.PeptideFeature;
import com.csi.propro.domain.db.AnalyseDataDO;
import com.csi.propro.domain.params.DeveloperParams;
import com.csi.propro.service.AnalyseDataService;
import com.csi.propro.service.AnalyseOverviewService;
import com.csi.propro.service.PeptideService;
import com.csi.propro.service.TaskService;
import com.csi.propro.utils.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-20 15:47
 */
@Component("featureExtractor")
public class FeatureExtractor {

    public final Logger logger = LoggerFactory.getLogger(FeatureExtractor.class);

    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    PeptideService peptideService;
    @Autowired
    GaussFilter gaussFilter;
    @Autowired
    PeakPicker peakPicker;
    @Autowired
    SignalToNoiseEstimator signalToNoiseEstimator;
    @Autowired
    ChromatogramPicker chromatogramPicker;
    @Autowired
    FeatureFinder featureFinder;
    @Autowired
    RtNormalizerScorer RTNormalizerScorer;
    @Autowired
    TaskService taskService;

    /**
     * @param dataDO       XIC后的数据对象
     * @param intensityMap 得到标准库中peptideRef对应的碎片和强度的键值对
     * @param sigmaSpacing
     * @return
     */
    public PeptideFeature getExperimentFeature(AnalyseDataDO dataDO, HashMap<String, Float> intensityMap, SigmaSpacing sigmaSpacing) {
        if (dataDO.getIntensityMap() == null || dataDO.getIntensityMap().size() == 0) {
            return new PeptideFeature(false);
        }

        HashMap<String, RtIntensityPairsDouble> ionPeaks = new HashMap<>();
        HashMap<String, List<IonPeak>> ionPeakParams = new HashMap<>();

        //对每一个chromatogram进行运算,dataDO中不含有ms1
        HashMap<String, double[]> noise1000Map = new HashMap<>();
        HashMap<String, Double[]> intensitiesMap = new HashMap<>();

        //将没有提取到信号的CutInfo过滤掉,同时将Float类型的参数调整为Double类型进行计算
        for (String cutInfo : intensityMap.keySet()) {
            //获取对应的XIC数据
            Float[] intensityArray = dataDO.getIntensityMap().get(cutInfo);
            //如果没有提取到信号,dataDO为null
            if (intensityArray == null) {
                continue;
            }
            Double[] intensityDoubleArray = new Double[intensityArray.length];
            for (int k = 0; k < intensityArray.length; k++) {
                intensityDoubleArray[k] = Double.parseDouble(intensityArray[k].toString());
            }
            intensitiesMap.put(cutInfo, intensityDoubleArray);
        }

        if (intensitiesMap.size() == 0) {
            return new PeptideFeature(false);
        }
        //计算GaussFilter
        Double[] rtDoubleArray = new Double[dataDO.getRtArray().length];
        for (int k = 0; k < rtDoubleArray.length; k++) {
            rtDoubleArray[k] = Double.parseDouble(dataDO.getRtArray()[k].toString());
        }
        PeptideSpectrum peptideSpectrum = new PeptideSpectrum(rtDoubleArray, intensitiesMap);


        HashMap<String, Double[]> smoothIntensitiesMap = gaussFilter.filter(rtDoubleArray, intensitiesMap, sigmaSpacing);

        //对每一个片段离子选峰
        double libIntSum = MathUtil.sum(intensityMap.values());
        HashMap<String, Double> normedLibIntMap = new HashMap<>();
        for (String cutInfo : intensitiesMap.keySet()) {
            //计算两个信噪比
            double[] noises200 = signalToNoiseEstimator.computeSTN(rtDoubleArray, smoothIntensitiesMap.get(cutInfo), 200, 30);
            double[] noisesOri1000 = signalToNoiseEstimator.computeSTN(rtDoubleArray, intensitiesMap.get(cutInfo), 1000, 30);
            //根据信噪比和峰值形状选择最高峰,用降噪200及平滑过后的图去挑选Peak峰
            RtIntensityPairsDouble maxPeakPairs = peakPicker.pickMaxPeak(rtDoubleArray, smoothIntensitiesMap.get(cutInfo), noises200);
            //根据信噪比和最高峰选择谱图
            if (maxPeakPairs == null) {
                logger.info("Error: MaxPeakPairs were null!" + rtDoubleArray.length);
                break;
            }
            List<IonPeak> ionPeakList = chromatogramPicker.pickChromatogram(rtDoubleArray, intensitiesMap.get(cutInfo), smoothIntensitiesMap.get(cutInfo), noisesOri1000, maxPeakPairs);
            ionPeaks.put(cutInfo, maxPeakPairs);
            ionPeakParams.put(cutInfo, ionPeakList);
            noise1000Map.put(cutInfo, noisesOri1000);
            normedLibIntMap.put(cutInfo, intensityMap.get(cutInfo) / libIntSum);
        }
        if (ionPeakParams.size() == 0) {
            return new PeptideFeature(false);
        }

        List<PeakGroup> peakGroupFeatureList;
        if (DeveloperParams.USE_NEW_PEAKGROUP_SELECTOR) {
            peakGroupFeatureList = featureFinder.findFeaturesNew(peptideSpectrum, ionPeaks, ionPeakParams, noise1000Map);
        }else {
            peakGroupFeatureList = featureFinder.findFeatures(peptideSpectrum, ionPeaks, ionPeakParams, noise1000Map);
        }
        PeptideFeature featureResult = new PeptideFeature(true);
        featureResult.setPeakGroupList(peakGroupFeatureList);
        featureResult.setNormedLibIntMap(normedLibIntMap);

        return featureResult;
    }
}
