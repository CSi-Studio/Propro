package net.csibio.propro.service.impl;

import com.alibaba.fastjson.JSON;
import net.csibio.aird.bean.MzIntensityPairs;
import net.csibio.propro.algorithm.feature.*;
import net.csibio.propro.algorithm.fitter.LinearFitter;
import net.csibio.propro.algorithm.peak.*;
import net.csibio.propro.constants.enums.ScoreType;
import net.csibio.propro.dao.ConfigDAO;
import net.csibio.propro.domain.bean.analyse.SigmaSpacing;
import net.csibio.propro.domain.bean.score.FeatureScores;
import net.csibio.propro.domain.bean.score.PeakGroup;
import net.csibio.propro.domain.bean.score.PeptideFeature;
import net.csibio.propro.domain.db.AnalyseDataDO;
import net.csibio.propro.domain.db.simple.SimplePeptide;
import net.csibio.propro.domain.params.WorkflowParams;
import net.csibio.propro.service.*;
import net.csibio.propro.utils.FeatureUtil;
import net.csibio.propro.utils.MathUtil;
import net.csibio.propro.utils.SortUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-15 10:05
 */
@Service("scoresService")
public class ScoreServiceImpl implements ScoreService {

    public final Logger logger = LoggerFactory.getLogger(ScoreServiceImpl.class);

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
    RtNormalizerScorer rtNormalizerScorer;
    @Autowired
    TaskService taskService;
    @Autowired
    FeatureExtractor featureExtractor;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    ChromatographicScorer chromatographicScorer;
    @Autowired
    DIAScorer diaScorer;
    @Autowired
    ElutionScorer elutionScorer;
    @Autowired
    LibraryScorer libraryScorer;
    @Autowired
    SwathLDAScorer swathLDAScorer;
    @Autowired
    ConfigDAO configDAO;
    @Autowired
    LinearFitter linearFitter;
    @Autowired
    SwathIndexService swathIndexService;

    @Override
    public void scoreForOne(AnalyseDataDO dataDO, SimplePeptide peptide, TreeMap<Float, MzIntensityPairs> rtMap, WorkflowParams input) {

        if (dataDO.getIntensityMap() == null || dataDO.getIntensityMap().size() <= peptide.getFragmentMap().size() / 2) {
            dataDO.setIdentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_NO_FIT);
            return;
        }
        dataDO.setIsUnique(peptide.getIsUnique());

        //获取标准库中对应的PeptideRef组
        //重要步骤,"或许是目前整个工程最重要的核心算法--选峰算法."--陆妙善
        PeptideFeature peptideFeature = featureExtractor.getExperimentFeature(dataDO, peptide.buildIntensityMap(), input.getSigmaSpacing());
        if (!peptideFeature.isFeatureFound()) {
            dataDO.setIdentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_UNKNOWN);
            logger.info("肽段没有被选中的特征：PeptideRef: " + dataDO.getPeptideRef());
            return;
        }
        List<FeatureScores> featureScoresList = new ArrayList<>();
        List<PeakGroup> peakGroupFeatureList = peptideFeature.getPeakGroupList();
        HashMap<String, Double> normedLibIntMap = peptideFeature.getNormedLibIntMap();
        HashMap<String, Float> productMzMap = new HashMap<>();
        HashMap<String, Integer> productChargeMap = new HashMap<>();

        for (String cutInfo : dataDO.getMzMap().keySet()) {
            try {
                if (cutInfo.contains("^")) {
                    String temp = cutInfo;
                    if (cutInfo.contains("[")) {
                        temp = cutInfo.substring(0, cutInfo.indexOf("["));
                    }
                    if (temp.contains("i")) {
                        temp = temp.replace("i", "");
                    }
                    productChargeMap.put(cutInfo, Integer.parseInt(temp.split("\\^")[1]));
                } else {
                    productChargeMap.put(cutInfo, 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("cutInfo:" + cutInfo + ";data:" + JSON.toJSONString(dataDO));
            }

            float mz = dataDO.getMzMap().get(cutInfo);
            productMzMap.put(cutInfo, mz);
        }

        HashMap<Integer, String> unimodHashMap = peptide.getUnimodMap();
        String sequence = peptide.getSequence();

        for (PeakGroup peakGroupFeature : peakGroupFeatureList) {
            FeatureScores featureScores = new FeatureScores(input.getScoreTypes().size());
            chromatographicScorer.calculateChromatographicScores(peakGroupFeature, normedLibIntMap, featureScores, input.getScoreTypes());
            Double shapeScore = featureScores.get(ScoreType.XcorrShape, input.getScoreTypes());
            Double shapeScoreWeighted = featureScores.get(ScoreType.XcorrShapeWeighted, input.getScoreTypes());
            if (!dataDO.getIsDecoy()
                    && ((shapeScoreWeighted != null && shapeScoreWeighted < input.getXcorrShapeWeightThreshold())
                    || (shapeScore != null && shapeScore < input.getXcorrShapeThreshold()))) {
                continue;
            }
            //根据RT时间和前体MZ获取最近的一个原始谱图
            if (input.isUsedDIAScores()) {
                MzIntensityPairs mzIntensityPairs = swathIndexService.getNearestSpectrumByRt(rtMap, peakGroupFeature.getApexRt());
                if (mzIntensityPairs != null) {
                    float[] spectrumMzArray = mzIntensityPairs.getMzArray();
                    float[] spectrumIntArray = mzIntensityPairs.getIntensityArray();
                    if (input.getScoreTypes().contains(ScoreType.IsotopeCorrelationScore.getTypeName()) || input.getScoreTypes().contains(ScoreType.IsotopeOverlapScore.getTypeName())) {
                        diaScorer.calculateDiaIsotopeScores(peakGroupFeature, productMzMap, spectrumMzArray, spectrumIntArray, productChargeMap, featureScores, input.getScoreTypes());
                    }
                    if (input.getScoreTypes().contains(ScoreType.BseriesScore.getTypeName()) || input.getScoreTypes().contains(ScoreType.YseriesScore.getTypeName())) {
                        diaScorer.calculateBYIonScore(spectrumMzArray, spectrumIntArray, unimodHashMap, sequence, 1, featureScores, input.getScoreTypes());
                    }
                    diaScorer.calculateDiaMassDiffScore(productMzMap, spectrumMzArray, spectrumIntArray, normedLibIntMap, featureScores, input.getScoreTypes());

                }
            }
            if (input.getScoreTypes().contains(ScoreType.LogSnScore.getTypeName())) {
                chromatographicScorer.calculateLogSnScore(peakGroupFeature, featureScores, input.getScoreTypes());
            }

            if (input.getScoreTypes().contains(ScoreType.IntensityScore.getTypeName())) {
                libraryScorer.calculateIntensityScore(peakGroupFeature, featureScores, input.getScoreTypes());
            }

            libraryScorer.calculateLibraryScores(peakGroupFeature, normedLibIntMap, featureScores, input.getScoreTypes());
            if (input.getScoreTypes().contains(ScoreType.NormRtScore.getTypeName())) {
                libraryScorer.calculateNormRtScore(peakGroupFeature, input.getSlopeIntercept(), dataDO.getRt(), featureScores, input.getScoreTypes());
            }
            swathLDAScorer.calculateSwathLdaPrescore(featureScores, input.getScoreTypes());
            featureScores.setRt(peakGroupFeature.getApexRt());
            featureScores.setRtRangeFeature(FeatureUtil.toString(peakGroupFeature.getBestLeftRt(), peakGroupFeature.getBestRightRt()));
            featureScores.setIntensitySum(peakGroupFeature.getPeakGroupInt());
            featureScores.setFragIntFeature(FeatureUtil.toString(peakGroupFeature.getIonIntensity()));
            featureScoresList.add(featureScores);
        }

        if (featureScoresList.size() == 0) {
            dataDO.setIdentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_NO_FIT);
            return;
        }

        dataDO.setFeatureScoresList(featureScoresList);
    }

    @Override
    public void strictScoreForOne(AnalyseDataDO dataDO, SimplePeptide peptide, TreeMap<Float, MzIntensityPairs> rtMap, float shapeScoreThreshold) {
        if (dataDO.getIntensityMap() == null || dataDO.getIntensityMap().size() < peptide.getFragmentMap().size()) {
            dataDO.setIdentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_NO_FIT);
            return;
        }

        SigmaSpacing ss = SigmaSpacing.create();
        PeptideFeature peptideFeature = featureExtractor.getExperimentFeature(dataDO, peptide.buildIntensityMap(), ss);
        if (!peptideFeature.isFeatureFound()) {
            return;
        }
        List<FeatureScores> featureScoresList = new ArrayList<>();
        List<PeakGroup> peakGroupFeatureList = peptideFeature.getPeakGroupList();
        HashMap<String, Double> normedLibIntMap = peptideFeature.getNormedLibIntMap();
        for (PeakGroup peakGroupFeature : peakGroupFeatureList) {
            FeatureScores featureScores = new FeatureScores(2);
            List<String> scoreTypes = new ArrayList<>();
            scoreTypes.add(ScoreType.XcorrShape.getTypeName());
            scoreTypes.add(ScoreType.XcorrShapeWeighted.getTypeName());
            chromatographicScorer.calculateChromatographicScores(peakGroupFeature, normedLibIntMap, featureScores, scoreTypes);
            if (featureScores.get(ScoreType.XcorrShapeWeighted.getTypeName(), scoreTypes) < shapeScoreThreshold
                    || featureScores.get(ScoreType.XcorrShape.getTypeName(), scoreTypes) < shapeScoreThreshold) {
                continue;
            }
            featureScores.setRt(peakGroupFeature.getApexRt());
            featureScoresList.add(featureScores);
        }

        if (featureScoresList.size() == 0) {
            return;
        }

        dataDO.setFeatureScoresList(featureScoresList);
    }

    /**
     * 先进行线性拟合，每次从pairs中选取一个residual最大的点丢弃，获得pairsCorrected
     *
     * @param pairs       RTPairs left:TheoryRt right:ExpRt
     * @param minRsq      goal of iteration
     * @param minCoverage limit of picking
     * @return pairsCorrected
     */
    private List<Pair<Double, Double>> removeOutlierIterative(List<Pair<Double, Double>> pairs, double minRsq, double minCoverage, double delta) {

        int pairsSize = pairs.size();
        if (pairsSize < 3) {
            return null;
        }

        //获取斜率和截距
        double rsq = 0;
        double[] coEff = new double[2];

        WeightedObservedPoints obs = new WeightedObservedPoints();
        while (pairs.size() >= pairsSize * minCoverage && rsq < minRsq) {
            obs.clear();
            for (Pair<Double, Double> rtPair : pairs) {
                obs.add(rtPair.getRight(), rtPair.getLeft());
            }
            PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
            coEff = fitter.fit(obs.toList());

            rsq = MathUtil.getRsq(pairs);
            if (rsq < minRsq) {
                // calculate residual and get max index
                double res, max = 0;
                int maxIndex = 0;
                for (int i = 0; i < pairs.size(); i++) {
                    res = (Math.abs(pairs.get(i).getLeft() - (coEff[0] + coEff[1] * pairs.get(i).getRight())));
                    if (res > max) {
                        max = res;
                        maxIndex = i;
                    }
                }
                //remove outlier of pairs iteratively
                pairs.remove(maxIndex);
            }
        }
        if (rsq < minRsq) {
            logger.error("RTNormalizer: unable to perform outlier detection.");
            return null;
        } else {
            return pairs;
        }
    }

    /**
     * 判断是否对RT空间实现了正常密度的覆盖
     *
     * @param rtRange           getRTRange
     * @param pairsCorrected    remove outlier之后的pairs
     * @param rtBins            需要分成的bin的数量
     * @param minPeptidesPerBin 每个bin最小分到的数量
     * @param minBinsFilled     需要满足↑条件的bin的数量
     * @return boolean 是否覆盖
     */
    private boolean computeBinnedCoverage(double[] rtRange, List<Pair<Double, Double>> pairsCorrected, int rtBins, int minPeptidesPerBin, int minBinsFilled) {
        int[] binCounter = new int[rtBins];
        double rtDistance = rtRange[1] - rtRange[0];

        //获得theorRt部分的分布
        for (Pair<Double, Double> pair : pairsCorrected) {
            double percent = (pair.getLeft() - rtRange[0]) / rtDistance;
            int bin = (int) (percent * rtBins);
            if (bin >= rtBins) {
                bin = rtBins - 1;
            }
            binCounter[bin]++;
        }

        //判断分布是否覆盖
        int binFilled = 0;
        for (int binCount : binCounter) {
            if (binCount >= minPeptidesPerBin) {
                binFilled++;
            }
        }
        return binFilled >= minBinsFilled;
    }

    private List<FeatureScores> getFilteredScore(List<FeatureScores> featureScoresList, int topN, String scoreName, List<String> scoreTypes) {
        if (featureScoresList.size() < topN) {
            return featureScoresList;
        }
        List<FeatureScores> filteredScoreList = SortUtil.sortBySelectedScore(featureScoresList, scoreName, true, scoreTypes);
        return filteredScoreList.subList(0, topN);
    }
}