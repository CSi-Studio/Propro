package net.csibio.propro.algorithm.merger;

import net.csibio.propro.domain.db.AnalyseDataDO;
import net.csibio.propro.domain.query.AnalyseDataQuery;
import net.csibio.propro.service.AnalyseDataService;
import net.csibio.propro.service.AnalyseOverviewService;
import net.csibio.propro.service.ExperimentService;
import net.csibio.propro.utils.ArrayUtil;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 将多个实验的实验结果按照算法进行合并处理
 * Created by Nico Wang
 * Time: 2019-03-25 16:08
 */

@Component
public class ProjectMerger {

    public final Logger logger = LoggerFactory.getLogger(ProjectMerger.class);

    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    ExperimentService experimentService;

    public HashMap<String, Integer> parameterEstimation(List<String> analyseOverviewIdList, double peakGroupFdr) {

        // 1) get peptide matrix
        HashMap<String, HashMap<String, AnalyseDataDO>> peptideMatrix = getMatrix(analyseOverviewIdList, peakGroupFdr);

        // 2) get peptide fdr by peptideAllRun fdr
        double decoyFrac = getDecoyFrac(peptideMatrix);
        double peptideFdrCalculated = findPeptideFdr(peptideMatrix, decoyFrac, 0);
        if (peptideFdrCalculated > peakGroupFdr) {
            peptideMatrix = getMatrix(analyseOverviewIdList, peptideFdrCalculated);
        }

        double alignedFdr;
        if (peptideFdrCalculated < peakGroupFdr) {
            alignedFdr = peakGroupFdr;
        } else {
            alignedFdr = 2 * peptideFdrCalculated;
        }

        // 3) peptide select and count
        HashMap<String, Integer> pepRefMap = getSelectedPeptideRef(peptideMatrix, 0d, alignedFdr);
        return pepRefMap;
    }

    private HashMap<String, HashMap<String, AnalyseDataDO>> getMatrix(List<String> analyseOverviewIdList, double fdrLimit) {
        HashMap<String, HashMap<String, AnalyseDataDO>> peptideMatrix = new HashMap<>();
        AnalyseDataQuery query = new AnalyseDataQuery();
        query.setQValueEnd(fdrLimit);
        for (String overviewId : analyseOverviewIdList) {
            query.setOverviewId(overviewId);
            List<AnalyseDataDO> analyseDataDOList = analyseDataService.getAll(query);
            for (AnalyseDataDO analyseDataDO : analyseDataDOList) {
                if (analyseDataDO.getIsDecoy()) {
                    if (peptideMatrix.containsKey("DECOY_" + analyseDataDO.getPeptideRef())) {
                        peptideMatrix.get("DECOY_" + analyseDataDO.getPeptideRef()).put(overviewId, analyseDataDO);
                    } else {
                        HashMap<String, AnalyseDataDO> runMap = new HashMap<>();
                        runMap.put(overviewId, analyseDataDO);
                        peptideMatrix.put("DECOY_" + analyseDataDO.getPeptideRef(), runMap);
                    }
                } else {
                    if (peptideMatrix.containsKey(analyseDataDO.getPeptideRef())) {
                        peptideMatrix.get(analyseDataDO.getPeptideRef()).put(overviewId, analyseDataDO);
                    } else {
                        HashMap<String, AnalyseDataDO> runMap = new HashMap<>();
                        runMap.put(overviewId, analyseDataDO);
                        peptideMatrix.put(analyseDataDO.getPeptideRef(), runMap);
                    }
                }
            }
        }
        return peptideMatrix;
    }

    private float getDecoyFrac(HashMap<String, HashMap<String, AnalyseDataDO>> peptideMatrix) {
        Long targetCount = 0L, decoyCount = 0L;
        for (HashMap<String, AnalyseDataDO> map : peptideMatrix.values()) {
            for (AnalyseDataDO analyseDataDO : map.values()) {
                if (analyseDataDO.getIsDecoy()) {
                    decoyCount++;
                } else {
                    targetCount++;
                }
            }
        }
        return (float) decoyCount / (targetCount + decoyCount);
    }

    /**
     * high utilization rate, Pay attention to performance
     *
     * @param peptideMatrix
     * @param fdr
     * @return
     */
    private double getPeptideLevelDecoyFrac(HashMap<String, HashMap<String, AnalyseDataDO>> peptideMatrix, double fdr) {

        Long decoyCount = 0L, targetCount = 0L;
        for (HashMap<String, AnalyseDataDO> map : peptideMatrix.values()) {
            boolean selected = false;
            boolean isDecoy = false;
            for (AnalyseDataDO analyseDataDO : map.values()) {
                if (analyseDataDO.getQValue() < fdr) {
                    selected = true;
                    isDecoy = analyseDataDO.getIsDecoy();
                    break;
                }
            }
            if (selected) {
                if (isDecoy) {
                    decoyCount++;
                } else {
                    targetCount++;
                }
            }
        }
        return (double) decoyCount / (targetCount + decoyCount);
    }

    private double findPeptideFdr(HashMap<String, HashMap<String, AnalyseDataDO>> peptideMatrix, double decoyFrac, int recursion) {
        double startFdr = 0.0005d / Math.pow(10, recursion);
        double endFdr = 0.01d / Math.pow(10, recursion);
        double step = startFdr;
        double decoyFrac001 = getPeptideLevelDecoyFrac(peptideMatrix, startFdr + step);
        double decoyFrac01 = getPeptideLevelDecoyFrac(peptideMatrix, endFdr);
        if (recursion == 0 && Math.abs(decoyFrac01 - decoyFrac) < 1e-6) {
            return endFdr;
        }
        if (decoyFrac < decoyFrac001) {
            return findPeptideFdr(peptideMatrix, decoyFrac, recursion + 1);
        }
        if (decoyFrac > decoyFrac01) {
            startFdr = 0.005d;
            endFdr = 1d;
            step = 0.005d;
        }
        double prevFrac = 0d, tempFrac = 0d;
        double tempFdr = 0d;
        for (double fdr = startFdr; fdr <= endFdr + step; fdr += step) {
            tempFrac = getPeptideLevelDecoyFrac(peptideMatrix, fdr);
            tempFdr = fdr;
            if (tempFrac > decoyFrac) {
                break;
            }
            if (Math.abs(tempFrac - decoyFrac) < 1e-6) {
                break;
            }
            prevFrac = tempFrac;
        }
        return tempFdr - step * (tempFrac - decoyFrac) / (tempFrac - prevFrac);
    }

    private String detemineBestRun(List<String> analyseOverviewIdList, double bestRunFdr) {
        Long maxCount = -1L;
        String maxOverviewId = "";
        for (String overviewId : analyseOverviewIdList) {
            AnalyseDataQuery query = new AnalyseDataQuery();
            query.setOverviewId(overviewId);
            query.setQValueEnd(bestRunFdr);
            Long tempCount = analyseDataService.count(query);
            if (tempCount > maxCount) {
                maxCount = tempCount;
                maxOverviewId = overviewId;
            }
        }
        return maxOverviewId;
    }

    /**
     * 1. align slave runs' rt to master rt
     * 2. return median of std(rt) within all runs
     *
     * @param peptideMatrix
     * @param analyseOverviewIdList
     * @param masterOverviewId
     * @return median of std("aligned slave rt" and "original master rt") in all slave runs
     */
    private double alignAndGetStd(HashMap<String, HashMap<String, AnalyseDataDO>> peptideMatrix, List<String> analyseOverviewIdList, String masterOverviewId) {

        List<Double> stdList = new ArrayList<>();
        for (String slaveOverviewId : analyseOverviewIdList) {
            if (slaveOverviewId.equals(masterOverviewId)) {
                continue;
            }

            List<Double> masterList = new ArrayList<>();
            List<Double> slaveList = new ArrayList<>();
            for (HashMap<String, AnalyseDataDO> peptide : peptideMatrix.values()) {
                if (peptide.get(masterOverviewId) != null && peptide.get(slaveOverviewId) != null) {
                    masterList.add(peptide.get(masterOverviewId).getBestRt());
                    slaveList.add(peptide.get(slaveOverviewId).getBestRt());
                }
            }
            int[] index = ArrayUtil.indexAfterSort(masterList);
            double[] masterRtArray = new double[masterList.size()];
            double[] slaveRtArray = new double[slaveList.size()];
            for (int i = 0; i < masterRtArray.length; i++) {
                masterRtArray[i] = masterList.get(index[i]);
                slaveRtArray[i] = slaveList.get(index[i]);
            }
            LoessInterpolator loess = new LoessInterpolator(0.01, 10);
            double[] smoothedMasterRt = loess.smooth(slaveRtArray, masterRtArray);
            PolynomialSplineFunction function = new LinearInterpolator().interpolate(slaveRtArray, smoothedMasterRt);
            double squareSum = 0d;
            int count = 0;
            for (HashMap<String, AnalyseDataDO> peptide : peptideMatrix.values()) {
                if (peptide.containsKey(slaveOverviewId) && function.isValidPoint(peptide.get(slaveOverviewId).getBestRt())) {
                    double slaveAlignedRt = function.value(peptide.get(slaveOverviewId).getBestRt());
                    peptide.get(slaveOverviewId).setBestRt(slaveAlignedRt);
                    if (peptide.containsKey(masterOverviewId)) {
                        double masterRt = peptide.get(masterOverviewId).getBestRt();
                        squareSum += (masterRt - slaveAlignedRt) * (masterRt - slaveAlignedRt);
                        count++;
                    }
                }
            }

            double std = Math.sqrt(squareSum / count);
            stdList.add(std);
        }
        Collections.sort(stdList);
        return stdList.get(stdList.size() / 2);
    }

    private double getRtDiffCutoff(double medianStd, int times) {
        return times * medianStd;
    }

    /**
     * TODO isotope_grouping  "SWATHScoringReader.OpenSWATH_SWATHScoringReader(SWATHScoringReader)"
     *
     * @param peptideMatrix
     * @param rtDiffCutoff
     * @param alignedFdr
     * @return
     */
    private HashMap<String, Integer> getSelectedPeptideRef(HashMap<String, HashMap<String, AnalyseDataDO>> peptideMatrix, double rtDiffCutoff, double alignedFdr) {
        HashMap<String, Integer> pepRefMap = new HashMap<>();
        for (String peptideRef : peptideMatrix.keySet()) {
            double bestFdr = alignedFdr;
//            double bestRt = 0d;
//            int count = 0;
            for (AnalyseDataDO peptideRun : peptideMatrix.get(peptideRef).values()) {
                if (peptideRun.getQValue() < bestFdr) {
//                    bestFdr = peptideRun.getQValue();
//                    bestRt = peptideRun.getBestRt();
                    if (pepRefMap.containsKey(peptideRef)) {
                        pepRefMap.put(peptideRef, pepRefMap.get(peptideRef) + 1);
                    } else {
                        pepRefMap.put(peptideRef, 1);
                    }
//                    count ++;
                }
            }
//            if (count == 0){
//                continue;
//            }
//            pepRefMap.put(peptideRef, count);
        }
        return pepRefMap;
    }
}
