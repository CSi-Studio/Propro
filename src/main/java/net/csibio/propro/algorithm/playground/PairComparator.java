package net.csibio.propro.algorithm.playground;

import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.bean.aird.Compressor;
import net.csibio.propro.domain.db.ExperimentDO;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.service.ExperimentService;
import net.csibio.propro.service.SwathIndexService;
import net.csibio.propro.utils.ConvolutionUtil;
import net.csibio.propro.algorithm.parser.AirdFileParser;
import net.csibio.propro.domain.bean.analyse.MzIntensityPairs;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by Nico Wang
 * Time: 2019-07-31 20:08
 */
@Component("pairComparator")
public class PairComparator {

    @Autowired
    AirdFileParser airdFileParser;
    @Autowired
    SwathIndexService swathIndexService;
    @Autowired
    ExperimentService experimentService;

    public void getGraph(String expId) {
        ExperimentDO experimentDO = experimentService.getById(expId).getModel();
        ResultDO<TreeMap<Float, MzIntensityPairs>> resultDO = getRtMap(experimentDO);
        if (resultDO.isFailed()) {
            return;
        }

        TreeMap<Float, MzIntensityPairs> rtMap = resultDO.getModel();
        preProcessing(rtMap, 0.05f, 10f);
    }


    private ResultDO<TreeMap<Float, MzIntensityPairs>> getRtMap(ExperimentDO experimentDO) {
        ResultDO checkResult = ConvolutionUtil.checkExperiment(experimentDO);
        if (checkResult.isFailed()) {
            return checkResult;
        }
        try {
            ResultDO<TreeMap<Float, MzIntensityPairs>> resultDO = new ResultDO(true);
            RandomAccessFile raf = new RandomAccessFile((File) checkResult.getModel(), "r");
            //Step1.获取窗口信息.
            SwathIndexDO swathIndexDO = swathIndexService.getSwathIndex(experimentDO.getId(), 700f);
            System.out.println(swathIndexDO.getRange());
            TreeMap<Float, MzIntensityPairs> rtMap = airdFileParser.parseSwathBlockValues(raf, swathIndexDO, experimentDO.fetchCompressor(Compressor.TARGET_MZ), experimentDO.fetchCompressor(Compressor.TARGET_INTENSITY));
            resultDO.setModel(rtMap);
            return resultDO;
        } catch (Exception e) {
            return ResultDO.buildError(ResultCode.FILE_FORMAT_NOT_SUPPORTED);
        }
    }

    /**
     * 默认输入为Centroid谱图
     *
     * @param rtMap           ms2块
     * @param mzPrecision     mz正则化精度
     * @param rtDurationLimit 最低持续时间,去除不持久的信号
     */
    private void preProcessing(TreeMap<Float, MzIntensityPairs> rtMap, float mzPrecision, float rtDurationLimit) {
        List<Float> rtList = new ArrayList<>();
        List<Float> mzList = new ArrayList<>();
        List<Float> intList = new ArrayList<>();
        //key:mz value:List<Pair<rt,int>>
        HashMap<Float, List<Pair<Float, Float>>> mzDurationMap = new HashMap<>();
        HashSet<Float> mzSet = new HashSet<>();
        for (Map.Entry<Float, MzIntensityPairs> entry : rtMap.entrySet()) {
            if (entry.getKey() < 1320 || entry.getKey() > 1360) {
                continue;
            }
            MzIntensityPairs mzIntensityPairs = entry.getValue();
            Float[] mzs = mzIntensityPairs.getMzArray();
            Float[] ints = mzIntensityPairs.getIntensityArray();
            float totalInt = 0f;
            float tempNormedMz = (int) (mzs[0] / mzPrecision) * mzPrecision;
            for (int i = 0; i < mzs.length; i++) {
                Float normedMz = (int) (mzs[i] / mzPrecision) * mzPrecision;

                if (normedMz.equals(tempNormedMz)){
                    totalInt += ints[i];
                } else {
                    Pair<Float, Float> rtIntPair = Pair.of(entry.getKey(), ints[i]);
                    if (mzDurationMap.containsKey(normedMz)) {
                        totalInt += ints[i];
                        List<Pair<Float,Float>> rtIntList = mzDurationMap.get(normedMz);
                        Pair<Float,Float> lastPair = rtIntList.get(rtIntList.size() - 1);
                        if (lastPair.getLeft().equals(entry.getKey())){
                            lastPair = Pair.of(entry.getKey(), lastPair.getRight()+ints[i]);
                        } else {
                            mzDurationMap.get(normedMz).add(rtIntPair);
                        }
                        mzSet.remove(normedMz);
                    } else {
                        List<Pair<Float, Float>> rtIntPairList = new ArrayList<>();
                        rtIntPairList.add(rtIntPair);
                        mzDurationMap.put(normedMz, rtIntPairList);
                    }
                    totalInt = ints[i];

                }


            }
            setPeaks(mzSet, mzDurationMap, rtDurationLimit, rtList, mzList, intList);
            mzSet = new HashSet<>(mzDurationMap.keySet());
        }
        setPeaks(mzSet, mzDurationMap, rtDurationLimit, rtList, mzList, intList);
        File file = new File("C:\\result.txt");

        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(rtList.toString());
            bw.write(";\n");
            bw.write(mzList.toString());
            bw.write(";\n");
            bw.write(intList.toString());
            bw.write(";\n");
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setPeaks(HashSet<Float> mzSet, HashMap<Float, List<Pair<Float, Float>>> mzDurationMap, Float rtDurationLimit, List<Float> rtList, List<Float> mzList, List<Float> intList){

        for (Float mz : mzSet) {
            List<Pair<Float, Float>> rtIntPairList = mzDurationMap.get(mz);
            int length = rtIntPairList.size();
            Float rtStart = rtIntPairList.get(0).getLeft();
            Float rtEnd = rtIntPairList.get(length - 1).getLeft();
            Float rtDuration = rtEnd - rtStart;
            if (rtDuration < rtDurationLimit && length < 3) {
                continue;
            }
            Pair<List<Float>, List<Float>> peaks = pickPeak(rtIntPairList, rtDurationLimit);
            for (int i = 0; i < peaks.getLeft().size(); i++) {
                mzList.add(mz);
            }
            rtList.addAll(peaks.getLeft());
            intList.addAll(peaks.getRight());
            mzDurationMap.remove(mz);
        }
    }

    private Pair<List<Float>, List<Float>> pickPeak(List<Pair<Float, Float>> rtIntPairList, float rtDurationLimit) {
        List<Float> rts = new ArrayList<>();
        List<Float> ints = new ArrayList<>();
        int length = rtIntPairList.size();
        boolean goHigher = true;
        float intSum = 0f;
        float lastRtStart = rtIntPairList.get(0).getLeft();
        float tempApexRt = rtIntPairList.get(0).getLeft();
        for (int i = 1; i < length; i++) {
            if (goHigher && rtIntPairList.get(i).getRight() < rtIntPairList.get(i - 1).getRight()) {
                goHigher = false;
                tempApexRt = rtIntPairList.get(i - 1).getLeft();
            }

            if (!goHigher && rtIntPairList.get(i).getRight() > rtIntPairList.get(i - 1).getRight()) {
                goHigher = true;
                float rtEnd = rtIntPairList.get(i - 1).getLeft();
                if (rtEnd - lastRtStart > rtDurationLimit) {
                    ints.add(intSum);
                    rts.add(tempApexRt);

                }
                lastRtStart = rtEnd;
                intSum = 0f;
            }
            intSum += rtIntPairList.get(i).getRight();
        }
        if (rtIntPairList.get(length - 1).getLeft() - lastRtStart > rtDurationLimit) {
            if (goHigher) {
                rts.add(rtIntPairList.get(length - 1).getLeft());
                ints.add(intSum);
            }
            if (!goHigher) {
                rts.add(tempApexRt);
                ints.add(intSum);
            }
        }

        return Pair.of(rts, ints);
    }
}
