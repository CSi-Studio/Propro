package net.csibio.propro.utils;

import net.csibio.propro.domain.bean.score.FeatureScores;
import net.csibio.propro.domain.bean.score.SimpleFeatureScores;
import net.csibio.propro.domain.db.simple.PeptideScores;
import net.csibio.propro.constants.FdrConst;
import net.csibio.propro.constants.enums.ScoreType;
import net.csibio.propro.domain.bean.learner.FinalResult;
import net.csibio.propro.domain.bean.learner.ScoreData;
import net.csibio.propro.domain.bean.learner.TrainAndTest;
import net.csibio.propro.domain.bean.learner.TrainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-13 22:08
 */
public class AirusUtil {

    public static final Logger logger = LoggerFactory.getLogger(AirusUtil.class);

    /**
     * 将GroupId简化为Integer数组
     * getGroupId({"100_run0","100_run0","DECOY_100_run0"})
     * -> {0, 0, 1}
     */
    public static Integer[] getGroupNumId(String[] groupId) {
        if (groupId[0] != null) {
            Integer[] b = new Integer[groupId.length];
            String s = groupId[0];
            int groupNumId = 0;
            for (int i = 0; i < groupId.length; i++) {
                if (!s.equals(groupId[i])) {
                    s = groupId[i];
                    groupNumId++;
                }
                b[i] = groupNumId;
            }
            return b;
        } else {
            logger.error("GetgroupNumId Error.\n");
            return null;
        }
    }

    public static Boolean[] findTopIndex(Double[] array, Integer[] groupNumId) {

        if (groupNumId.length == array.length) {
            int id = groupNumId[0];
            Boolean[] index = new Boolean[groupNumId.length];
            int tempIndex = 0;
            double b = array[0];
            for (int i = 0; i < groupNumId.length; i++) {

                if (groupNumId[i] != null && groupNumId[i] == id) {
                    if (array[i] >= b) {
                        b = array[i];
                        tempIndex = i;
                        //index[i]=1;
                    }

                } else if (array[i] != null && groupNumId[i] != null) {
                    index[tempIndex] = true;
                    b = array[i];
                    id = groupNumId[i];
                    tempIndex = i;
                }
            }
            index[tempIndex] = true;
            for (int i = 0; i < groupNumId.length; i++) {
                if (index[i] == null) {
                    index[i] = false;
                }
            }
            return index;
        } else {
            logger.error("FindTopIndex Error.");
            return null;
        }
    }

    public static Double[][] getDecoyPeaks(Double[][] array, Boolean[] isDecoy) {
        if (array.length == isDecoy.length) {
            return ArrayUtil.extract3dRow(array, isDecoy);
        } else {
            logger.error("GetDecoyPeaks Error");
            return null;
        }
    }

    public static Double[] getDecoyPeaks(Double[] array, Boolean[] isDecoy) {
        if (array.length == isDecoy.length) {
            return ArrayUtil.extract3dRow(array, isDecoy);
        } else {
            logger.error("GetDecoyPeaks Error");
            return null;
        }
    }

    public static Integer[] getDecoyPeaks(Integer[] array, Boolean[] isDecoy) {
        if (array.length == isDecoy.length) {
            return ArrayUtil.extract3dRow(array, isDecoy);
        } else {
            logger.error("GetDecoyPeaks Error");
            return null;
        }
    }

    public static Double[] getTargetPeaks(Double[] array, Boolean[] isDecoy) {
        if (array.length == isDecoy.length) {
            Boolean[] isTarget = getIsTarget(isDecoy);
            return ArrayUtil.extract3dRow(array, isTarget);
        } else {
            logger.error("GetDecoyPeaks Error");
            return null;
        }
    }

    public static Integer[] getTargetPeaks(Integer[] array, Boolean[] isDecoy) {
        if (array.length == isDecoy.length) {
            Boolean[] isTarget = getIsTarget(isDecoy);
            return ArrayUtil.extract3dRow(array, isTarget);
        } else {
            logger.error("GetDecoyPeaks Error");
            return null;
        }
    }

    public static Double[][] getTopTargetPeaks(Double[][] array, Boolean[] isDecoy, Boolean[] index) {
        Boolean[] isTopTarget = getIsTopTarget(isDecoy, index);
        if (isTopTarget != null && array.length == isTopTarget.length) {
            return getDecoyPeaks(array, isTopTarget);
        } else {
            logger.error("GetTopTargetPeaks Error");
            return null;
        }
    }

    public static Double[] getTopTargetPeaks(Double[] array, Boolean[] isDecoy, Boolean[] index) {
        Boolean[] isTopTarget = getIsTopTarget(isDecoy, index);
        if (isTopTarget != null && array.length == isTopTarget.length) {
            return getDecoyPeaks(array, isTopTarget);
        } else {
            logger.error("GetTopTargetPeaks Error");
            return null;
        }
    }

    public static Double[][] getTopDecoyPeaks(Double[][] array, Boolean[] isDecoy, Boolean[] index) {
        Boolean[] isTopDecoy = getIsTopDecoy(isDecoy, index);
        if (isTopDecoy != null && array.length == isTopDecoy.length) {
            return getDecoyPeaks(array, isTopDecoy);
        } else {
            logger.error("GetTopDecoyPeaks Error");
            return null;
        }
    }

    public static Double[] getTopDecoyPeaks(Double[] array, Boolean[] isDecoy, Boolean[] index) {
        Boolean[] isTopDecoy = getIsTopDecoy(isDecoy, index);
        if (isTopDecoy != null && array.length == isTopDecoy.length) {
            return getDecoyPeaks(array, isTopDecoy);
        } else {
            logger.error("GetTopDecoyPeaks Error");
            return null;
        }
    }

    public static HashMap<String, Integer> buildDistributionMap() {
        HashMap<String, Integer> distributionMap = new HashMap<>();
        distributionMap.put(FdrConst.GROUP_0_0001, 0);
        distributionMap.put(FdrConst.GROUP_0001_0002, 0);
        distributionMap.put(FdrConst.GROUP_0002_0003, 0);
        distributionMap.put(FdrConst.GROUP_0003_0004, 0);
        distributionMap.put(FdrConst.GROUP_0004_0005, 0);
        distributionMap.put(FdrConst.GROUP_0005_0006, 0);
        distributionMap.put(FdrConst.GROUP_0006_0007, 0);
        distributionMap.put(FdrConst.GROUP_0007_0008, 0);
        distributionMap.put(FdrConst.GROUP_0008_0009, 0);
        distributionMap.put(FdrConst.GROUP_0009_001, 0);
        distributionMap.put(FdrConst.GROUP_001_002, 0);
        distributionMap.put(FdrConst.GROUP_002_003, 0);
        distributionMap.put(FdrConst.GROUP_003_004, 0);
        distributionMap.put(FdrConst.GROUP_004_005, 0);
        distributionMap.put(FdrConst.GROUP_005_006, 0);
        distributionMap.put(FdrConst.GROUP_006_007, 0);
        distributionMap.put(FdrConst.GROUP_007_008, 0);
        distributionMap.put(FdrConst.GROUP_008_009, 0);
        distributionMap.put(FdrConst.GROUP_009_01, 0);
        distributionMap.put(FdrConst.GROUP_01_02, 0);
        distributionMap.put(FdrConst.GROUP_02_03, 0);
        distributionMap.put(FdrConst.GROUP_03_04, 0);
        distributionMap.put(FdrConst.GROUP_04_05, 0);
        distributionMap.put(FdrConst.GROUP_05_06, 0);
        distributionMap.put(FdrConst.GROUP_06_07, 0);
        distributionMap.put(FdrConst.GROUP_07_08, 0);
        distributionMap.put(FdrConst.GROUP_08_09, 0);
        distributionMap.put(FdrConst.GROUP_09_10, 0);
        distributionMap.put(FdrConst.GROUP_10_N, 0);
        return distributionMap;
    }

    /**
     * @param fdr
     * @param map
     * @see FdrConst
     */
    public static void addOneForFdrDistributionMap(Double fdr, HashMap<String, Integer> map) {
        if (fdr >= 0 && fdr < 0.001) { map.computeIfPresent(FdrConst.GROUP_0_0001, (k, v) -> v + 1);return; }
        if (fdr >= 0.001 && fdr < 0.002) { map.computeIfPresent(FdrConst.GROUP_0001_0002, (k, v) -> v + 1);return; }
        if (fdr >= 0.002 && fdr < 0.003) { map.computeIfPresent(FdrConst.GROUP_0002_0003, (k, v) -> v + 1);return; }
        if (fdr >= 0.003 && fdr < 0.004) { map.computeIfPresent(FdrConst.GROUP_0003_0004, (k, v) -> v + 1);return; }
        if (fdr >= 0.004 && fdr < 0.005) { map.computeIfPresent(FdrConst.GROUP_0004_0005, (k, v) -> v + 1);return; }
        if (fdr >= 0.005 && fdr < 0.006) { map.computeIfPresent(FdrConst.GROUP_0005_0006, (k, v) -> v + 1);return; }
        if (fdr >= 0.006 && fdr < 0.007) { map.computeIfPresent(FdrConst.GROUP_0006_0007, (k, v) -> v + 1);return; }
        if (fdr >= 0.007 && fdr < 0.008) { map.computeIfPresent(FdrConst.GROUP_0007_0008, (k, v) -> v + 1);return; }
        if (fdr >= 0.008 && fdr < 0.009) { map.computeIfPresent(FdrConst.GROUP_0008_0009, (k, v) -> v + 1);return; }
        if (fdr >= 0.009 && fdr < 0.010) { map.computeIfPresent(FdrConst.GROUP_0009_001, (k, v) -> v + 1);return; }
        if (fdr >= 0.01 && fdr < 0.02) { map.computeIfPresent(FdrConst.GROUP_001_002, (k, v) -> v + 1);return; }
        if (fdr >= 0.02 && fdr < 0.03) { map.computeIfPresent(FdrConst.GROUP_002_003, (k, v) -> v + 1);return; }
        if (fdr >= 0.03 && fdr < 0.04) { map.computeIfPresent(FdrConst.GROUP_003_004, (k, v) -> v + 1);return; }
        if (fdr >= 0.04 && fdr < 0.05) { map.computeIfPresent(FdrConst.GROUP_004_005, (k, v) -> v + 1);return; }
        if (fdr >= 0.05 && fdr < 0.06) { map.computeIfPresent(FdrConst.GROUP_005_006, (k, v) -> v + 1);return; }
        if (fdr >= 0.06 && fdr < 0.07) { map.computeIfPresent(FdrConst.GROUP_006_007, (k, v) -> v + 1);return; }
        if (fdr >= 0.07 && fdr < 0.08) { map.computeIfPresent(FdrConst.GROUP_007_008, (k, v) -> v + 1);return; }
        if (fdr >= 0.08 && fdr < 0.09) { map.computeIfPresent(FdrConst.GROUP_008_009, (k, v) -> v + 1);return; }
        if (fdr >= 0.09 && fdr < 0.10) { map.computeIfPresent(FdrConst.GROUP_009_01, (k, v) -> v + 1);return;}
        if (fdr >= 0.1 && fdr < 0.2) { map.computeIfPresent(FdrConst.GROUP_01_02, (k, v) -> v + 1);return; }
        if (fdr >= 0.2 && fdr < 0.3) { map.computeIfPresent(FdrConst.GROUP_02_03, (k, v) -> v + 1);return; }
        if (fdr >= 0.3 && fdr < 0.4) { map.computeIfPresent(FdrConst.GROUP_03_04, (k, v) -> v + 1);return; }
        if (fdr >= 0.4 && fdr < 0.5) { map.computeIfPresent(FdrConst.GROUP_04_05, (k, v) -> v + 1);return; }
        if (fdr >= 0.5 && fdr < 0.6) { map.computeIfPresent(FdrConst.GROUP_05_06, (k, v) -> v + 1);return; }
        if (fdr >= 0.6 && fdr < 0.7) { map.computeIfPresent(FdrConst.GROUP_06_07, (k, v) -> v + 1);return; }
        if (fdr >= 0.7 && fdr < 0.8) { map.computeIfPresent(FdrConst.GROUP_07_08, (k, v) -> v + 1);return; }
        if (fdr >= 0.8 && fdr < 0.9) { map.computeIfPresent(FdrConst.GROUP_08_09, (k, v) -> v + 1);return; }
        if (fdr >= 0.9 && fdr < 1.0) { map.computeIfPresent(FdrConst.GROUP_09_10, (k, v) -> v + 1);return; }
        if (fdr >= 1.0)               { map.computeIfPresent(FdrConst.GROUP_10_N, (k, v) -> v + 1); }
    }

    /**
     * 以scoreType为主分数挑选出所有主分数最高的峰
     *
     * @param scores
     * @param scoreType  需要作为主分数的分数
     * @param scoreTypes 打分开始的时候所有参与打分的子分数快照列表
     * @return
     */
    public static List<SimpleFeatureScores> findTopFeatureScores(List<PeptideScores> scores, String scoreType, List<String> scoreTypes, boolean strict) {
        List<SimpleFeatureScores> bestFeatureScoresList = new ArrayList<>();
        for (PeptideScores score : scores) {
            if (score.getFeatureScoresList() == null || score.getFeatureScoresList().size() == 0) {
                continue;
            }
            SimpleFeatureScores bestFeatureScores = new SimpleFeatureScores(score.getPeptideRef(), score.getIsDecoy());
            double maxScore = -Double.MAX_VALUE;
            FeatureScores topFeatureScore = null;
            for (FeatureScores featureScores : score.getFeatureScoresList()) {
                if (strict && featureScores.getThresholdPassed() != null && !featureScores.getThresholdPassed()) {
                    continue;
                }
                Double featureMainScore = featureScores.get(scoreType, scoreTypes);
                if (featureMainScore > maxScore) {
                    maxScore = featureMainScore;
                    topFeatureScore = featureScores;
                }
            }

            if (topFeatureScore != null) {
                bestFeatureScores.setMainScore(topFeatureScore.get(scoreType, scoreTypes));
                bestFeatureScores.setScores(topFeatureScore.getScores());
                bestFeatureScores.setRt(topFeatureScore.getRt());
                bestFeatureScores.setIntensitySum(topFeatureScore.getIntensitySum());
                bestFeatureScores.setFragIntFeature(topFeatureScore.getFragIntFeature());
                bestFeatureScoresList.add(bestFeatureScores);
            }
        }
        return bestFeatureScoresList;
    }

    public static Double[] buildMainScoreArray(List<SimpleFeatureScores> scores, Boolean needToSort) {
        Double[] result = new Double[scores.size()];
        for (int i = 0; i < scores.size(); i++) {
            result[i] = scores.get(i).getMainScore();
        }
        if (needToSort) {
            Arrays.sort(result);
        }
        return result;
    }

    public static Double[] buildPValueArray(List<SimpleFeatureScores> scores, Boolean needToSort) {
        Double[] result = new Double[scores.size()];
        for (int i = 0; i < scores.size(); i++) {
            result[i] = scores.get(i).getPValue();
        }
        if (needToSort) {
            Arrays.sort(result);
        }
        return result;
    }

    /**
     * Get feature Matrix of useMainScore or not.
     */
    public static Double[][] getFeatureMatrix(Double[][] array, Boolean useMainScore) {
        if (array != null) {
            if (useMainScore) {
                return ArrayUtil.extract3dColumn(array, 0);
            } else {
                return ArrayUtil.extract3dColumn(array, 1);
            }
        } else {
            logger.error("GetFeatureMatrix Error");
            return null;
        }
    }

    public static List<SimpleFeatureScores> peaksFilter(List<SimpleFeatureScores> trainTargets, double cutOff) {
        List<SimpleFeatureScores> peakScores = new ArrayList<>();
        for (SimpleFeatureScores i : trainTargets) {
            if (i.getMainScore() >= cutOff) {
                peakScores.add(i);
            }
        }

        return peakScores;
    }

    public static Double[][] peaksFilter(Double[][] ttPeaks, Double[] ttScores, double cutOff) {
        int count = 0;
        for (double i : ttScores) {
            if (i >= cutOff) count++;
        }
        Double[][] targetPeaks = new Double[count][ttPeaks[0].length];
        int j = 0;
        for (int i = 0; i < ttScores.length; i++) {
            if (ttScores[i] >= cutOff) {
                targetPeaks[j] = ttPeaks[i];
                j++;
            }
        }
        return targetPeaks;
    }

    /**
     * 划分测试集与训练集,保证每一次对于同一份原始数据划分出的测试集都是同一份
     *
     * @param data
     * @param groupNumId
     * @param isDecoy
     * @param fraction   目前写死0.5
     * @param isDebug
     * @return
     */
    public static TrainAndTest split(Double[][] data, Integer[] groupNumId, Boolean[] isDecoy, double fraction, boolean isDebug) {
        Integer[] decoyIds = getDecoyPeaks(groupNumId, isDecoy);
        Integer[] targetIds = getTargetPeaks(groupNumId, isDecoy);

        if (isDebug) {
            TreeSet<Integer> decoyIdSet = new TreeSet<Integer>(Arrays.asList(decoyIds));
            TreeSet<Integer> targetIdSet = new TreeSet<Integer>(Arrays.asList(targetIds));

            decoyIds = new Integer[decoyIdSet.size()];
            decoyIdSet.toArray(decoyIds);
            targetIds = new Integer[targetIdSet.size()];
            targetIdSet.toArray(targetIds);
        } else {
            List<Integer> decoyIdShuffle = Arrays.asList(decoyIds);
            List<Integer> targetIdShuffle = Arrays.asList(targetIds);
            Collections.shuffle(decoyIdShuffle);
            Collections.shuffle(targetIdShuffle);
            decoyIdShuffle.toArray(decoyIds);
            targetIdShuffle.toArray(targetIds);
        }

        int decoyLength = (int) (decoyIds.length * fraction) + 1;
        int targetLength = (int) (targetIds.length * fraction) + 1;
        Integer[] learnIds = ArrayUtil.concat2d(ArrayUtil.getPartOfArray(decoyIds, decoyLength), ArrayUtil.getPartOfArray(targetIds, targetLength));

        HashSet<Integer> learnIdSet = new HashSet<Integer>(Arrays.asList(learnIds));
        return ArrayUtil.extract3dRow(data, groupNumId, isDecoy, learnIdSet);
    }

    /**
     * 划分测试集与训练集,保证每一次对于同一份原始数据划分出的测试集都是同一份
     *
     * @param scores
     * @param fraction 切分比例,目前写死1:1,即0.5
     * @param isDebug  是否取测试集
     * @return
     */
    public static TrainData split(List<PeptideScores> scores, double fraction, boolean isDebug, List<String> scoreTypes) {

        //每一轮开始前将上一轮的加权总分去掉
        for (PeptideScores ss : scores) {
            for (FeatureScores sft : ss.getFeatureScoresList()) {
                sft.remove(ScoreType.WeightedTotalScore.getTypeName(), scoreTypes);
            }
        }

        List<PeptideScores> targets = new ArrayList<>();
        List<PeptideScores> decoys = new ArrayList<>();
        //按照是否是伪肽段分为两个数组
        for (PeptideScores score : scores) {
            if (score.getIsDecoy()) {
                decoys.add(score);
            } else {
                targets.add(score);
            }
        }

        //是否在调试程序,调试程序时需要保证每一次的随机结果都相同,因此不做随机打乱,而是每一次都按照PeptideRef进行排序
        if (isDebug) {
            SortUtil.sortByPeptideRef(targets);
            SortUtil.sortByPeptideRef(decoys);
        } else {
            Collections.shuffle(targets);
            Collections.shuffle(decoys);
        }

        int targetLength = (int) Math.ceil(targets.size() * fraction);
        int decoyLength = (int) Math.ceil(decoys.size() * fraction);

        TrainData td = new TrainData(targets.subList(0, targetLength), decoys.subList(0, decoyLength));
        return td;
    }

    public static ScoreData fakeSortTgId(ScoreData scoreData) {
        String[] groupId = scoreData.getGroupId();
        int groupIdLength = groupId.length;
        Integer[] index = ArrayUtil.indexAfterSort(groupId);

        Boolean[] isDecoy = scoreData.getIsDecoy();
        Double[][] scores = scoreData.getScoreData();
        String[] newGroupId = new String[groupIdLength];
        Boolean[] newIsDecoy = new Boolean[groupIdLength];
        Double[][] newScores = new Double[groupIdLength][scores[0].length];

        for (int i = 0; i < groupIdLength; i++) {
            int j = index[i];
            newGroupId[i] = groupId[j];
            newIsDecoy[i] = isDecoy[j];
            newScores[i] = scores[j];
        }
        Integer[] newGroupNumId = AirusUtil.getGroupNumId(newGroupId);
        scoreData.setGroupId(newGroupId);
        scoreData.setIsDecoy(newIsDecoy);
        scoreData.setScoreData(newScores);
        scoreData.setGroupNumId(newGroupNumId);

        return scoreData;
    }

    public static int checkFdr(FinalResult finalResult, Double fdrLimit) {
        return checkFdr(finalResult.getAllInfo().getStatMetrics().getFdr(), fdrLimit);
    }

    public static int checkFdr(double[] dArray, Double fdrLimit) {
        int count = 0;
        for (double d : dArray) {
            if (d <= fdrLimit) {
                count++;
            }
        }
        return count;
    }

    private static Boolean[] getIsTarget(Boolean[] isDecoy) {
        Boolean[] isTarget = new Boolean[isDecoy.length];
        for (int i = 0; i < isDecoy.length; i++) {
            isTarget[i] = !isDecoy[i];
        }
        return isTarget;
    }

    private static Boolean[] getIsTopDecoy(Boolean[] isDecoy, Boolean[] index) {
        if (isDecoy.length == index.length) {
            Boolean[] isTopDecoy = new Boolean[isDecoy.length];
            for (int i = 0; i < isDecoy.length; i++) {
                isTopDecoy[i] = isDecoy[i] && index[i];
            }
            return isTopDecoy;
        } else {
            logger.error("GetIsTopDecoy Error.Length not equals");
            return null;
        }
    }

    private static Boolean[] getIsTopTarget(Boolean[] isDecoy, Boolean[] index) {

        if (isDecoy.length == index.length) {
            Boolean[] isTopTarget = new Boolean[isDecoy.length];
            for (int i = 0; i < isDecoy.length; i++) {
                isTopTarget[i] = !isDecoy[i] && index[i];
            }
            return isTopTarget;
        } else {
            logger.error("GetIsTopTarget Error.Length not equals");
            return null;
        }
    }

    /**
     * set w as average
     *
     * @param weightsMapList the result of nevals
     */
    public static HashMap<String, Double> averagedWeights(List<HashMap<String, Double>> weightsMapList) {
        HashMap<String, Double> finalWeightsMap = new HashMap<>();
        for (HashMap<String, Double> weightsMap : weightsMapList) {
            for (String key : weightsMap.keySet()) {
                finalWeightsMap.put(key, finalWeightsMap.get(key) == null ? weightsMap.get(key) : (finalWeightsMap.get(key) + weightsMap.get(key)));
            }
        }
        for (String key : finalWeightsMap.keySet()) {
            finalWeightsMap.put(key, finalWeightsMap.get(key) / weightsMapList.size());
        }

        return finalWeightsMap;
    }

    /**
     * Count number of values bigger than threshold in array.
     */
    public static int countOverThreshold(List<SimpleFeatureScores> scores, double threshold) {
        int n = 0;
        for (SimpleFeatureScores i : scores) {
            if (i.getPValue() >= threshold) {
                n++;
            }
        }
        return n;
    }

    /**
     * 统计一个数组中的每一位数字,在该数组中小于等于自己的数还有几个
     * 例如数组3,2,1,1. 经过本函数后得到的结果是4,3,2,2
     * 入参array必须是降序排序后的数组
     */
    public static int[] countPValueNumPositives(List<SimpleFeatureScores> array) {
        int step = 0;
        int n = array.size();
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            while (step < n && array.get(i).getPValue().equals(array.get(step).getPValue())) {
                result[step] = n - i;
                step++;
            }
        }
        return result;
    }
}
