package net.csibio.propro.algorithm.irt;

import net.csibio.propro.domain.bean.score.SlopeIntercept;
import net.csibio.propro.algorithm.extract.Extractor;
import net.csibio.propro.algorithm.feature.RtNormalizerScorer;
import net.csibio.propro.algorithm.fitter.LinearFitter;
import net.csibio.propro.algorithm.parser.AirdFileParser;
import net.csibio.propro.algorithm.peak.FeatureExtractor;
import net.csibio.propro.constants.Constants;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.bean.aird.Compressor;
import net.csibio.propro.domain.bean.analyse.MzIntensityPairs;
import net.csibio.propro.domain.bean.analyse.SigmaSpacing;
import net.csibio.propro.domain.bean.irt.IrtResult;
import net.csibio.propro.domain.bean.score.PeptideFeature;
import net.csibio.propro.domain.bean.score.ScoreRtPair;
import net.csibio.propro.domain.db.AnalyseDataDO;
import net.csibio.propro.domain.db.ExperimentDO;
import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.domain.db.simple.SimplePeptide;
import net.csibio.propro.domain.params.CoordinateBuildingParams;
import net.csibio.propro.domain.params.ExtractParams;
import net.csibio.propro.domain.params.IrtParams;
import net.csibio.propro.domain.query.SwathIndexQuery;
import net.csibio.propro.service.PeptideService;
import net.csibio.propro.service.ScoreService;
import net.csibio.propro.service.SwathIndexService;
import net.csibio.propro.utils.ConvolutionUtil;
import net.csibio.propro.utils.FileUtil;
import net.csibio.propro.utils.MathUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Component("irt")
public class Irt {

    public final Logger logger = LoggerFactory.getLogger(Irt.class);

    @Autowired
    PeptideService peptideService;
    @Autowired
    AirdFileParser airdFileParser;
    @Autowired
    Extractor extractor;
    @Autowired
    ScoreService scoreService;
    @Autowired
    SwathIndexService swathIndexService;
    @Autowired
    FeatureExtractor featureExtractor;
    @Autowired
    RtNormalizerScorer rtNormalizerScorer;
    @Autowired
    LinearFitter linearFitter;

    /**
     * XIC并且求出iRT
     *
     * @param experimentDO
     * @param irtParams
     * @return
     */
    public ResultDO<IrtResult> extractAndAlign(ExperimentDO experimentDO, IrtParams irtParams) {
        try {
            List<AnalyseDataDO> dataList = extract(experimentDO, irtParams);
            if (dataList == null) {
                return ResultDO.buildError(ResultCode.IRT_EXCEPTION);
            }
            ResultDO<IrtResult> resultDO = new ResultDO<>(false);
            try {
                resultDO = align(dataList, irtParams.getLibrary(), irtParams.getSigmaSpacing());
            } catch (Exception e) {
                e.printStackTrace();
                resultDO.setMsgInfo(e.getMessage());
            }
            experimentDO.setIrtResult(resultDO.getModel());
            return resultDO;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultDO.buildError(ResultCode.IRT_EXCEPTION);
        }
    }

    /**
     * XIC iRT校准库的数据
     *
     * @param exp
     * @param irtParams
     * @return
     */
    private List<AnalyseDataDO> extract(ExperimentDO exp, IrtParams irtParams) {

        ResultDO checkResult = ConvolutionUtil.checkExperiment(exp);
        if (checkResult.isFailed()) {
            logger.error(checkResult.getMsgInfo());
            return null;
        }

        File file = (File) checkResult.getModel();
        RandomAccessFile raf = null;

        List<AnalyseDataDO> finalList = new ArrayList<>();

        SwathIndexQuery query = new SwathIndexQuery(exp.getId(), 2);
        List<SwathIndexDO> swathList = swathIndexService.getAll(query);

        LibraryDO library = irtParams.getLibrary();
        float mzExtractWindow = irtParams.getMzExtractWindow();
        int selectPoints = irtParams.getWantedNumber();
        int step;
        if (irtParams.isUseLibrary()) {
            int rangeSize = exp.getWindowRanges().size();
            selectPoints = Math.min(rangeSize, selectPoints);//获取windowRange Size大小,如果超过50的话则采用采样录取的方式
            step = rangeSize / selectPoints;
        } else {
            selectPoints = swathList.size();
            step = 1;
        }
        logger.info("Irt Selected Points Count:" + selectPoints + "; Step:" + step);
        try {
            raf = new RandomAccessFile(file, "r");
            for (int i = 0; i < selectPoints; i++) {
                logger.info("第"+(i+1)+"轮搜索开始");
                //Step1.按照步长获取SwathList的点位库
                SwathIndexDO swathIndexDO = swathList.get(i * step);

                //Step2.获取标准库的目标肽段片段的坐标
                TreeMap<Float, MzIntensityPairs> rtMap; //key为rt
                CoordinateBuildingParams params = new CoordinateBuildingParams();
                params.setSlopeIntercept(SlopeIntercept.create());
                params.setRtExtractionWindows(-1);
                params.setType(exp.getType());
                params.setNoDecoy(true);
                params.setLimit(irtParams.getPickedNumbers());
                //如果使用标准库进行卷积,为了缩短读取数据库的时间,每一轮从数据库中仅读取300个点位进行测试
                if (library.getType().equals(LibraryDO.TYPE_STANDARD)){
                    params.setLimit(irtParams.getPickedNumbers());
                }

                List<SimplePeptide> coordinates = peptideService.buildCoordinates(library, swathIndexDO.getRange(), params);
                if (coordinates.size() == 0) {
                    logger.warn("No iRT Coordinates Found,Rang:" + swathIndexDO.getRange().getStart() + ":" + swathIndexDO.getRange().getEnd());
                    continue;
                }

                //Step3.提取指定原始谱图
                try {
                    rtMap = airdFileParser.parseSwathBlockValues(raf, swathIndexDO, exp.fetchCompressor(Compressor.TARGET_MZ), exp.fetchCompressor(Compressor.TARGET_INTENSITY));
                } catch (Exception e) {
                    logger.error("PrecursorMZStart:" + swathIndexDO.getRange().getStart());
                    throw e;
                }

                //Step4.提取数据并且存储数据,如果传入的库是标准库,那么使用采样的方式进行数据提取
                if (library.getType().equals(LibraryDO.TYPE_IRT)) {
                    //如果使用的是irt校准库进行校准,那么会检索校准库中的所有数据集
                    extractor.extractForIrt(finalList, coordinates, rtMap, null, new ExtractParams(mzExtractWindow, -1f));
                } else {
                    //如果是使用标准库进行校准的,那么会按照需要选择的总点数进行抽取选择
                    ExtractParams ep = new ExtractParams(mzExtractWindow, -1f);
                    ep.setShapeScoreThreshold(irtParams.getShapeScoreThreshold());
                    extractor.extractForIrtWithLib(finalList, coordinates, rtMap, null, ep);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(raf);
        }

        return finalList;
    }

    /**
     * 从一个数据提取结果列表中求出iRT
     *
     * @param dataList
     * @param library
     * @param sigmaSpacing Sigma通常为30/8 = 6.25/Spacing通常为0.01
     * @return
     */
    private ResultDO<IrtResult> align(List<AnalyseDataDO> dataList, LibraryDO library, SigmaSpacing sigmaSpacing) throws Exception {

        List<List<ScoreRtPair>> scoreRtList = new ArrayList<>();
        List<Double> compoundRt = new ArrayList<>();
        ResultDO<IrtResult> resultDO = new ResultDO<>();
        double minGroupRt = Double.MAX_VALUE, maxGroupRt = Double.MIN_VALUE;
        for (AnalyseDataDO dataDO : dataList) {
            SimplePeptide tp = peptideService.getTargetPeptideByDataRef(library.getId(), dataDO.getPeptideRef());
            PeptideFeature peptideFeature = featureExtractor.getExperimentFeature(dataDO, tp.buildIntensityMap(), sigmaSpacing);
            if (!peptideFeature.isFeatureFound()) {
                continue;
            }
            double groupRt = dataDO.getRt();
            if (groupRt > maxGroupRt) {
                maxGroupRt = groupRt;
            }
            if (groupRt < minGroupRt) {
                minGroupRt = groupRt;
            }
            List<ScoreRtPair> scoreRtPairs = rtNormalizerScorer.score(peptideFeature.getPeakGroupList(), peptideFeature.getNormedLibIntMap(), groupRt);
            if (scoreRtPairs.size() == 0) {
                continue;
            }
            scoreRtList.add(scoreRtPairs);
            compoundRt.add(groupRt);
        }

        List<Pair<Double, Double>> pairs = simpleFindBestFeature(scoreRtList, compoundRt);
        double delta = (maxGroupRt - minGroupRt) / 30d;
        List<Pair<Double, Double>> pairsCorrected = chooseReliablePairs(pairs, delta);

        logger.info("choose finish ------------------------");
        IrtResult irtResult = new IrtResult();

        List<Double[]> selectedList = new ArrayList<>();
        List<Double[]> unselectedList = new ArrayList<>();
        for (int i = 0; i < pairs.size(); i++) {
            if (pairsCorrected.contains(pairs.get(i))) {
                selectedList.add(new Double[]{pairs.get(i).getLeft(), pairs.get(i).getRight()});
            } else {
                unselectedList.add(new Double[]{pairs.get(i).getLeft(), pairs.get(i).getRight()});
            }
        }
        irtResult.setSelectedPairs(selectedList);
        irtResult.setUnselectedPairs(unselectedList);
        SlopeIntercept slopeIntercept = linearFitter.proproFit(pairsCorrected, delta);
        irtResult.setSi(slopeIntercept);
        resultDO.setSuccess(true);
        resultDO.setModel(irtResult);

        return resultDO;
    }

    /**
     * get rt pairs for every peptideRef
     *
     * @param scoresList peptideRef list of List<ScoreRtPair>
     * @param rt         get from groupsResult.getModel()
     * @return rt pairs
     */
    private List<Pair<Double, Double>> simpleFindBestFeature(List<List<ScoreRtPair>> scoresList, List<Double> rt) {

        List<Pair<Double, Double>> pairs = new ArrayList<>();

        for (int i = 0; i < scoresList.size(); i++) {
            List<ScoreRtPair> scores = scoresList.get(i);
            double max = Double.MIN_VALUE;
            //find max scoreForAll's rt
            double expRt = 0d;
            for (int j = 0; j < scores.size(); j++) {
                if (scores.get(j).getScore() > max) {
                    max = scores.get(j).getScore();
                    expRt = scores.get(j).getRt();
                }
            }
            if (Constants.ESTIMATE_BEST_PEPTIDES && max < Constants.OVERALL_QUALITY_CUTOFF) {
                continue;
            }
            Pair<Double, Double> rtPair = Pair.of(rt.get(i), expRt);
            pairs.add(rtPair);
        }
        return pairs;
    }

    private List<Pair<Double, Double>> chooseReliablePairs(List<Pair<Double, Double>> rtPairs, double delta) throws Exception {
        List<Pair<Double, Double>> rtPairsCorrected = new ArrayList<>(rtPairs);
        preprocessRtPairs(rtPairsCorrected, 50d);
        SlopeIntercept slopeIntercept = linearFitter.huberFit(rtPairsCorrected, delta);
        while (MathUtil.getRsq(rtPairsCorrected) < 0.95 && rtPairsCorrected.size() >= 2) {
            int maxErrorIndex = findMaxErrorIndex(slopeIntercept, rtPairsCorrected);
            rtPairsCorrected.remove(maxErrorIndex);
            slopeIntercept = linearFitter.huberFit(rtPairsCorrected, delta);
        }
        return rtPairsCorrected;
    }

    private int findMaxErrorIndex(SlopeIntercept slopeIntercept, List<Pair<Double, Double>> rtPairs) {
        int maxIndex = 0;
        double maxError = 0d;
        for (int i = 0; i < rtPairs.size(); i++) {
            double tempError = Math.abs(rtPairs.get(i).getRight() * slopeIntercept.getSlope() + slopeIntercept.getIntercept() - rtPairs.get(i).getLeft());
            if (tempError > maxError) {
                maxError = tempError;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private void preprocessRtPairs(List<Pair<Double, Double>> rtPairs, double tolerance) {
        try {
            SlopeIntercept initSlopeIntercept = linearFitter.getInitSlopeIntercept(rtPairs);
            for (int i = rtPairs.size() - 1; i >= 0; i --){
                double tempError = Math.abs(rtPairs.get(i).getRight() * initSlopeIntercept.getSlope() + initSlopeIntercept.getIntercept() - rtPairs.get(i).getLeft());
                if (tempError > tolerance) {
                    rtPairs.remove(i);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
