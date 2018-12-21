package com.westlake.air.pecs.service.impl;

import com.alibaba.fastjson.JSON;
import com.westlake.air.pecs.constants.Constants;
import com.westlake.air.pecs.constants.ResultCode;
import com.westlake.air.pecs.constants.ScoreType;
import com.westlake.air.pecs.dao.ConfigDAO;
import com.westlake.air.pecs.domain.ResultDO;
import com.westlake.air.pecs.domain.db.simple.TargetPeptide;
import com.westlake.air.pecs.domain.params.LumsParams;
import com.westlake.air.pecs.domain.bean.analyse.MzIntensityPairs;
import com.westlake.air.pecs.domain.bean.analyse.RtIntensityPairsDouble;
import com.westlake.air.pecs.domain.bean.analyse.SigmaSpacing;
import com.westlake.air.pecs.domain.bean.analyse.WindowRang;
import com.westlake.air.pecs.domain.bean.score.*;
import com.westlake.air.pecs.domain.db.*;
import com.westlake.air.pecs.domain.query.PeptideQuery;
import com.westlake.air.pecs.feature.*;
import com.westlake.air.pecs.parser.AirdFileParser;
import com.westlake.air.pecs.rtnormalizer.ChromatogramFilter;
import com.westlake.air.pecs.rtnormalizer.RtNormalizerScorer;
import com.westlake.air.pecs.scorer.*;
import com.westlake.air.pecs.service.*;
import com.westlake.air.pecs.utils.AnalyseDataUtil;
import com.westlake.air.pecs.utils.FileUtil;
import com.westlake.air.pecs.utils.MathUtil;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.RandomAccessFile;
import java.util.*;

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
    ScanIndexService scanIndexService;
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
    ChromatogramFilter chromatogramFilter;
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
    AirdFileParser airdFileParser;

    @Override
    public ResultDO<SlopeIntercept> computeIRt(List<AnalyseDataDO> dataList, String iRtLibraryId, SigmaSpacing sigmaSpacing) {

        HashMap<String, TargetPeptide> ttMap = peptideService.getTPMap(new PeptideQuery(iRtLibraryId));

        List<List<ScoreRtPair>> scoreRtList = new ArrayList<>();
        List<Double> compoundRt = new ArrayList<>();
        ResultDO<SlopeIntercept> resultDO = new ResultDO<>();
        for (AnalyseDataDO dataDO : dataList) {
            SlopeIntercept slopeIntercept = new SlopeIntercept();
            FeatureByPep featureByPep = featureExtractor.getExperimentFeature(dataDO, ttMap.get(dataDO.getPeptideRef() + "_" + dataDO.getIsDecoy()).buildIntensityMap(), sigmaSpacing);
            if (!featureByPep.isFeatureFound()) {
                continue;
            }
            double groupRt = dataDO.getRt();
            List<ScoreRtPair> scoreRtPairs = rtNormalizerScorer.score(featureByPep.getRtIntensityPairsOriginList(), featureByPep.getExperimentFeatures(), featureByPep.getLibraryIntensityList(), featureByPep.getNoise1000List(), slopeIntercept, groupRt);
            scoreRtList.add(scoreRtPairs);
            compoundRt.add(groupRt);
        }

        List<RtPair> pairs = simpleFindBestFeature(scoreRtList, compoundRt);
        List<RtPair> pairsCorrected = removeOutlierIterative(pairs, Constants.MIN_RSQ, Constants.MIN_COVERAGE);

        if (pairsCorrected == null || pairsCorrected.size() < 2) {
            logger.error(ResultCode.NOT_ENOUGH_IRT_PEPTIDES.getMessage());
            resultDO.setErrorResult(ResultCode.NOT_ENOUGH_IRT_PEPTIDES);
            return resultDO;
        }

        SlopeIntercept slopeIntercept = fitRTPairs(pairsCorrected);
        resultDO.setSuccess(true);
        resultDO.setModel(slopeIntercept);

        return resultDO;
    }

    @Override
    public void scoreForAll(List<AnalyseDataDO> dataList, WindowRang rang, ScanIndexDO swathIndex, LumsParams input) {

        if (dataList == null || dataList.size() == 0) {
            return;
        }
        input.setOverviewId(dataList.get(0).getOverviewId());//取一个AnalyseDataDO的OverviewId

        //标准库按照PeptideRef分组
        PeptideQuery query = new PeptideQuery(input.getLibraryId());
        query.setMzStart(Double.parseDouble(rang.getMzStart().toString()));
        query.setMzEnd(Double.parseDouble(rang.getMzEnd().toString()));
        HashMap<String, TargetPeptide> ttMap = peptideService.getTPMap(query);

        int count = 0;
        //为每一组PeptideRef卷积结果打分
        ExperimentDO exp = input.getExperimentDO();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(exp.getAirdPath(), "r");

            TreeMap<Float, MzIntensityPairs> rtMap = null;
            if (input.isUsedDIAScores()) {
                rtMap = airdFileParser.parseSwathBlockValues(raf, swathIndex);
            }

            for (AnalyseDataDO dataDO : dataList) {
                AnalyseDataUtil.decompress(dataDO);
                scoreForOne(dataDO, ttMap.get(dataDO.getPeptideRef() + "_" + dataDO.getIsDecoy()), rtMap, input);
                analyseDataService.update(dataDO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(raf);
        }
    }


    @Override
    public FeatureByPep selectPeak(AnalyseDataDO dataDO, HashMap<String, Float> intensityMap, SigmaSpacing ss) {

        if (dataDO.isCompressed()) {
            logger.warn("进入本函数前的AnalyseDataDO需要提前被解压缩!!!!!");
            AnalyseDataUtil.decompress(dataDO);
        }

        if (dataDO.getIntensityMap() == null || dataDO.getIntensityMap().size() < 3) {
            logger.info("数据的离子片段少于3个,属于无效数据:PeptideRef:" + dataDO.getPeptideRef());
            return null;
        }
        List<FeatureScores> featureScoresList = new ArrayList<>();

        //重要步骤,"或许是目前整个工程最重要的核心算法--选峰算法."--陆妙善
        FeatureByPep featureByPep = featureExtractor.getExperimentFeature(dataDO, intensityMap, ss);
        if (!featureByPep.isFeatureFound()) {
            return null;
        } else {
            return featureByPep;
        }
    }

    @Override
    public void scoreForOne(AnalyseDataDO dataDO, TargetPeptide peptide, TreeMap<Float, MzIntensityPairs> rtMap, LumsParams input) {

        if (dataDO.isCompressed()) {
            logger.warn("进入本函数前的AnalyseDataDO需要提前被解压缩!!!!!");
            AnalyseDataUtil.decompress(dataDO);
        }

        if (dataDO.getIntensityMap() == null || dataDO.getIntensityMap().size() < 3) {
            logger.info("数据的离子片段少于3个,属于无效数据:PeptideRef:" + dataDO.getPeptideRef());
            dataDO.setIdentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_NO_FIT);
            return;
        }

        //获取标准库中对应的PeptideRef组
        HashMap<String, Float> intensityMap = peptide.buildIntensityMap();
        //重要步骤,"或许是目前整个工程最重要的核心算法--选峰算法."--陆妙善
        FeatureByPep featureByPep = featureExtractor.getExperimentFeature(dataDO, intensityMap, input.getSigmaSpacing());
        if (!featureByPep.isFeatureFound()) {
            dataDO.setIdentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_UNKNOWN);
            return;
        }
        List<FeatureScores> featureScoresList = new ArrayList<>();
        List<List<ExperimentFeature>> experimentFeatures = featureByPep.getExperimentFeatures();
        List<RtIntensityPairsDouble> chromatogramList = featureByPep.getRtIntensityPairsOriginList();
        List<Double> libraryIntensityList = featureByPep.getLibraryIntensityList();
        List<double[]> noise1000List = featureByPep.getNoise1000List();
        HashMap<String, Double> productMzMap = new HashMap<>();
        List<Double> productMzList = new ArrayList<>();
        List<Integer> productChargeList = new ArrayList<>();

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
                    productChargeList.add(Integer.parseInt(temp.split("\\^")[1]));
                } else {
                    productChargeList.add(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("cutInfo:" + cutInfo + ";data:" + JSON.toJSONString(dataDO));
            }

            double mz = Double.parseDouble(Float.toString(dataDO.getMzMap().get(cutInfo)));
            productMzMap.put(cutInfo, mz);
            productMzList.add(mz);
        }

        MathUtil.normalizeSum(intensityMap);
        HashMap<Integer, String> unimodHashMap = peptide.getUnimodMap();
        String sequence = peptide.getSequence();
        for (List<ExperimentFeature> experimentFeatureList : experimentFeatures) {

            FeatureScores featureScores = new FeatureScores();
            chromatographicScorer.calculateChromatographicScores(experimentFeatureList, libraryIntensityList, featureScores, input.getScoreTypes());
            if (input.getScoreTypes().contains(ScoreType.LogSnScore.getTypeName())) {
                chromatographicScorer.calculateLogSnScore(chromatogramList, experimentFeatureList, noise1000List, featureScores);
            }

            //根据RT时间和前体MZ获取最近的一个原始谱图
            if (input.isUsedDIAScores()) {
                MzIntensityPairs mzIntensityPairs = scanIndexService.getNearestSpectrumByRt(rtMap, experimentFeatureList.get(0).getRt());
                if (mzIntensityPairs != null) {
                    Float[] spectrumMzArray = mzIntensityPairs.getMzArray();
                    Float[] spectrumIntArray = mzIntensityPairs.getIntensityArray();
                    diaScorer.calculateBYIonScore(spectrumMzArray, spectrumIntArray, unimodHashMap, sequence, 1, featureScores);
                    diaScorer.calculateDiaMassDiffScore(productMzMap, spectrumMzArray, spectrumIntArray, intensityMap, featureScores);
                    diaScorer.calculateDiaIsotopeScores(experimentFeatureList, productMzList, spectrumMzArray, spectrumIntArray, productChargeList, featureScores);
                }
            }

            if (input.getScoreTypes().contains(ScoreType.ElutionModelFitScore.getTypeName())) {
                elutionScorer.calculateElutionModelScore(experimentFeatureList, featureScores);
            }

            if (input.getScoreTypes().contains(ScoreType.IntensityScore.getTypeName())) {
                libraryScorer.calculateIntensityScore(experimentFeatureList, featureScores);
            }

            libraryScorer.calculateLibraryScores(experimentFeatureList, libraryIntensityList, featureScores, input.getScoreTypes());
            if (input.getScoreTypes().contains(ScoreType.NormRtScore.getTypeName())) {
                libraryScorer.calculateNormRtScore(experimentFeatureList, input.getSlopeIntercept(), dataDO.getRt(), featureScores);
            }
            swathLDAScorer.calculateSwathLdaPrescore(featureScores);
            featureScores.setRt(experimentFeatureList.get(0).getRt());
            featureScores.setIntensitySum(experimentFeatureList.get(0).getIntensitySum());
            featureScoresList.add(featureScores);
        }

        if (featureScoresList.size() == 0) {
            dataDO.setIdentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_UNKNOWN);
            return;
        }

        dataDO.setFeatureScoresList(featureScoresList);
    }

    /**
     * get rt pairs for every peptideRef
     *
     * @param scoresList peptideRef list of List<ScoreRtPair>
     * @param rt         get from groupsResult.getModel()
     * @return rt pairs
     */
    private List<RtPair> simpleFindBestFeature(List<List<ScoreRtPair>> scoresList, List<Double> rt) {

        List<RtPair> pairs = new ArrayList<>();

        for (int i = 0; i < scoresList.size(); i++) {
            List<ScoreRtPair> scores = scoresList.get(i);
            double max = Double.MIN_VALUE;
            RtPair rtPair = new RtPair();
            //find max scoreForAll's rt
            for (int j = 0; j < scores.size(); j++) {
                if (scores.get(j).getScore() > max) {
                    max = scores.get(j).getScore();
                    rtPair.setExpRt(scores.get(j).getRt());
                }
            }
            if (Constants.ESTIMATE_BEST_PEPTIDES && max < Constants.OVERALL_QUALITY_CUTOFF) {
                continue;
            }
            rtPair.setTheoRt(rt.get(i));
            pairs.add(rtPair);
        }
        return pairs;
    }

    /**
     * 先进行线性拟合，每次从pairs中选取一个residual最大的点丢弃，获得pairsCorrected
     *
     * @param pairs       RTPairs
     * @param minRsq      goal of iteration
     * @param minCoverage limit of picking
     * @return pairsCorrected
     */
    private List<RtPair> removeOutlierIterative(List<RtPair> pairs, double minRsq, double minCoverage) {

        int pairsSize = pairs.size();
        if (pairsSize < 3) {
            return null;
        }

        //获取斜率和截距
        double rsq = 0;
        double[] coEff;

        WeightedObservedPoints obs = new WeightedObservedPoints();
        while (pairs.size() >= pairsSize * minCoverage && rsq < minRsq) {
            obs.clear();
            for (RtPair rtPair : pairs) {
                obs.add(rtPair.getExpRt(), rtPair.getTheoRt());
            }
            PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
            coEff = fitter.fit(obs.toList());

            rsq = MathUtil.getRsq(pairs);
            if (rsq < minRsq) {
                // calculate residual and get max index
                double res, max = 0;
                int maxIndex = 0;
                for (int i = 0; i < pairs.size(); i++) {
                    res = (Math.abs(pairs.get(i).getTheoRt() - (coEff[0] + coEff[1] * pairs.get(i).getExpRt())));
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
            System.out.println("RTNormalizer: unable to perform outlier detection.");
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
    private boolean computeBinnedCoverage(double[] rtRange, List<RtPair> pairsCorrected, int rtBins, int minPeptidesPerBin, int minBinsFilled) {
        int[] binCounter = new int[rtBins];
        double rtDistance = rtRange[1] - rtRange[0];

        //获得theorRt部分的分布
        for (RtPair pair : pairsCorrected) {
            double percent = (pair.getTheoRt() - rtRange[0]) / rtDistance;
            int bin = (int) (percent * rtBins);
            if (bin >= rtBins) {
                bin = rtBins - 1;
            }
            binCounter[bin]++;
        }

        //判断分布是否覆盖
        int binFilled = 0;
        for (int binCount : binCounter) {
            if (binCount >= minPeptidesPerBin) binFilled++;
        }
        return binFilled >= minBinsFilled;
    }

    /**
     * 最小二乘法线性拟合RTPairs
     *
     * @param rtPairs <exp_rt, theor_rt>
     * @return 斜率和截距
     */
    private SlopeIntercept fitRTPairs(List<RtPair> rtPairs) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (RtPair rtPair : rtPairs) {
            obs.add(rtPair.getExpRt(), rtPair.getTheoRt());
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(1);
        double[] coeff = fitter.fit(obs.toList());
        SlopeIntercept slopeIntercept = new SlopeIntercept();
        slopeIntercept.setSlope(coeff[1]);
        slopeIntercept.setIntercept(coeff[0]);
        return slopeIntercept;
    }
}