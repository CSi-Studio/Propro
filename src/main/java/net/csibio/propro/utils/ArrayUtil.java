package net.csibio.propro.utils;

import net.csibio.propro.domain.bean.score.SlopeIntercept;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.db.simple.SimplePeptide;
import net.csibio.propro.domain.bean.learner.IndexValue;
import net.csibio.propro.domain.bean.learner.TrainAndTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-12 21:44
 */

public class ArrayUtil {

    public static final Logger logger = LoggerFactory.getLogger(ArrayUtil.class);

    /**
     * Return reverse of given array[].
     */
    public static double[] reverse(double[] array) {
        int length = array.length;
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            result[i] = array[length - 1 - i];
        }
        return result;
    }

    public static int[] reverse(int[] array) {
        int length = array.length;
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[i] = array[length - 1 - i];
        }
        return result;
    }

    /**
     * Concatenate arrayA[] and arrayB[][] y dimension.
     */
    public static Double[][] concat3d(Double[][] arrayA, Double[][] arrayB) {
        if (arrayA[0].length == arrayB[0].length) {
            Double[][] c = new Double[arrayA.length + arrayB.length][arrayA[0].length];
            for (int i = 0; i < arrayA.length; i++) {
                System.arraycopy(arrayA[i], 0, c[i], 0, arrayA[0].length);
            }
            for (int i = 0; i < arrayB.length; i++) {
                System.arraycopy(arrayB[i], 0, c[i + arrayA.length], 0, arrayB[0].length);
            }
            return c;
        } else {
            logger.error("Concat3d Error");
            return null;
        }
    }

    public static double[][] concat3d(double[][] arrayA, double[][] arrayB) {
        if (arrayA[0].length == arrayB[0].length) {
            double[][] c = new double[arrayA.length + arrayB.length][arrayA[0].length];
            for (int i = 0; i < arrayA.length; i++) {
                System.arraycopy(arrayA[i], 0, c[i], 0, arrayA[0].length);
            }
            for (int i = 0; i < arrayB.length; i++) {
                System.arraycopy(arrayB[i], 0, c[i + arrayA.length], 0, arrayB[0].length);
            }
            return c;
        } else {
            logger.error("Concat3d Error");
            return null;
        }
    }

    public static Double[] extractRow(Double[] array, Integer[] row) {
        Double[] result = new Double[row.length];
        for (int i = 0; i < row.length; i++) {
            if (row[i] > -1 && row[i] < array.length) {
                result[i] = array[row[i]];
            } else {
                logger.error("ExtractRow Error");
                return null;
            }
        }
        return result;
    }

    public static double[] extractRow(double[] array, Integer[] row) {
        double[] result = new double[row.length];
        for (int i = 0; i < row.length; i++) {
            if (row[i] > -1 && row[i] < array.length) {
                result[i] = array[row[i]];
            } else {
                logger.error("ExtractRow Error");
                return null;
            }
        }
        return result;
    }

    public static String[] extractRow(String[] array, Integer[] row) {
        String[] result = new String[row.length];
        for (int i = 0; i < row.length; i++) {
            if (row[i] > -1 && row[i] < array.length) {
                result[i] = array[row[i]];
            } else {
                logger.error("ExtractRow Error.");
                return null;
            }
        }
        return result;
    }

    public static Double[][] extract3dColumn(Double[][] array, Integer begin, Integer end) {
        if (begin <= end && end < array[0].length) {
            Double[][] b = new Double[array.length][end - begin + 1];
            for (int i = 0; i < array.length; i++) {
                System.arraycopy(array[i], begin, b[i], 0, end - begin + 1);
            }
            return b;
        } else {
            logger.error("Extract3dColumn Error");
            return null;
        }
    }

    public static Double[][] extract3dColumn(Double[][] array, Integer begin) {
        return extract3dColumn(array, begin, array[0].length - 1);
    }

    public static Double[][] extract3dRow(Double[][] array, Boolean[] isDecoy) {
        if (array.length == isDecoy.length) {
            int sum = 0;
            for (Boolean i : isDecoy) {
                if (i) {
                    sum++;
                }
            }
            Double[][] extractedRow = new Double[sum][array[0].length];
            int j = 0;
            for (int i = 0; i < array.length; i++) {
                if (isDecoy[i]) {
                    for (int k = 0; k < array[0].length; k++) {
                        extractedRow[j][k] = array[i][k];
                    }
                    j++;
                }
            }
            return extractedRow;
        } else {
            logger.error("Extract3dRow Error");
            return null;
        }

    }

    public static Integer[] concat2d(Integer[] arrayA, Integer[] arrayB) {
        Integer[] arrayC = new Integer[arrayA.length + arrayB.length];
        System.arraycopy(arrayA, 0, arrayC, 0, arrayA.length);
        System.arraycopy(arrayB, 0, arrayC, arrayA.length, arrayB.length);
        return arrayC;
    }

    public static Double[] extractColumn(Double[][] array, int column) {
        Double[] result = new Double[array.length];
        if (column > -1 && column < array[0].length) {
            for (int i = 0; i < array.length; i++) {
                result[i] = array[i][column];
            }
            return result;
        } else {
            logger.error("ExtractColumn Error.");
            return null;
        }
    }

    /**
     * @param array
     * @param groupNumId
     * @param isDecoy
     * @param indexSet   去重集合
     * @return
     */
    public static TrainAndTest extract3dRow(Double[][] array, Integer[] groupNumId, Boolean[] isDecoy, HashSet<Integer> indexSet) {
        TrainAndTest trainAndTest = new TrainAndTest();

        if (array.length == groupNumId.length) {
            int k = -1, l = -1;
            int symbol;
            Double[][] trainRow = new Double[array.length][array[0].length];
            Double[][] testRow = new Double[array.length][array[0].length];
            Integer[] trainIdRow = new Integer[array.length];
            Integer[] testIdRow = new Integer[array.length];
            Boolean[] trainIsDecoy = new Boolean[array.length];
            Boolean[] testIsDecoy = new Boolean[array.length];

            for (int i = 0; i < groupNumId.length; i++) {
                symbol = k;
                if (indexSet.contains(groupNumId[i])) {
                    k++;
                    trainIdRow[k] = groupNumId[i];
                    trainRow[k] = array[i];
                    trainIsDecoy[k] = isDecoy[i];
                }
                if (k == symbol) {
                    l++;
                    testIdRow[l] = groupNumId[i];
                    testRow[l] = array[i];
                    testIsDecoy[l] = isDecoy[i];
                }
            }
            Double[][] extractedTrainRow = new Double[k + 1][array[0].length];
            Double[][] extractedTestRow = new Double[l + 1][array[0].length];
            Integer[] extractedTrainIdRow = new Integer[k + 1];
            Integer[] extractedTestIdRow = new Integer[l + 1];
            Boolean[] extractedTrainIsDecoyRow = new Boolean[k + 1];
            Boolean[] extractedTestIsDecoyRow = new Boolean[l + 1];
            for (int i = 0; i <= k; i++) {
                extractedTrainRow[i] = trainRow[i];
                extractedTrainIdRow[i] = trainIdRow[i];
                extractedTrainIsDecoyRow[i] = trainIsDecoy[i];

            }
            for (int i = 0; i <= l; i++) {
                extractedTestRow[i] = testRow[i];
                extractedTestIdRow[i] = testIdRow[i];
                extractedTestIsDecoyRow[i] = testIsDecoy[i];
            }
            trainAndTest.setTrainData(extractedTrainRow);
            trainAndTest.setTrainId(extractedTrainIdRow);
            trainAndTest.setTrainIsDecoy(extractedTrainIsDecoyRow);
            trainAndTest.setTestData(extractedTestRow);
            trainAndTest.setTestId(extractedTestIdRow);
            trainAndTest.setTestIsDecoy(extractedTestIsDecoyRow);

            return trainAndTest;
        } else {
            logger.error("Extract3dRow Error: array.length must be equal with id.length");
            return null;
        }

    }

    public static Double[] extract3dRow(Double[] array, Boolean[] isDecoy) {
        if (array.length == isDecoy.length) {
            int sum = 0;
            for (boolean i : isDecoy) {
                if (i) {
                    sum++;
                }
            }
            Double[] extractedRow = new Double[sum];
            int j = 0;
            for (int i = 0; i < array.length; i++) {
                if (isDecoy[i]) {
                    extractedRow[j] = array[i];
                    j++;
                }
            }
            return extractedRow;
        } else {
            logger.error("Extract3dRow Error");
            return null;
        }

    }

    public static Integer[] extract3dRow(Integer[] array, Boolean[] isDecoy) {
        if (array.length == isDecoy.length) {
            int sum = 0;
            for (boolean i : isDecoy) {
                if (i) {
                    sum++;
                }
            }
            Integer[] extractedRow = new Integer[sum];
            int j = 0;
            for (int i = 0; i < array.length; i++) {
                if (isDecoy[i]) {
                    extractedRow[j] = array[i];
                    j++;
                }
            }
            return extractedRow;
        } else {
            logger.error("Extract3dRow Error");
            return null;
        }
    }

    public static Integer[] getPartOfArray(Integer[] array, int cutoff) {
        Integer[] result = new Integer[cutoff];
        System.arraycopy(array, 0, result, 0, cutoff);
        return result;
    }

    /**
     * Get ascend sort index of array[].
     */
    public static Integer[] indexAfterSort(Double[] array) {
        List<IndexValue<Double>> indexValues = new IndexValue<Double>().buildList(array);
        Collections.sort(indexValues);

        int n = array.length;
        Integer[] result = new Integer[n];

        for (int i = 0; i < n; i++) {
            result[i] = indexValues.get(i).getIndex();
        }
        return result;
    }

    /**
     * Get ascend sort index of list.
     */
    public static int[] indexAfterSort(List<Double> array) {
        List<IndexValue<Double>> indexValues = new IndexValue<Double>().buildList(array);
        Collections.sort(indexValues);

        int n = array.size();
        int[] result = new int[n];

        for (int i = 0; i < n; i++) {
            result[i] = indexValues.get(i).getIndex();
        }
        return result;
    }

    /**
     * Get ascend sort index of array[].
     */
    public static Integer[] indexAfterSort(Integer[] array) {
        List<IndexValue<Integer>> indexValues = new IndexValue<Integer>().buildList(array);
        Collections.sort(indexValues);

        int n = array.length;
        Integer[] result = new Integer[n];

        for (int i = 0; i < n; i++) {
            result[i] = indexValues.get(i).getIndex();
        }
        return result;
    }

    public static Integer[] indexAfterSort(Float[] array) {
        List<IndexValue<Float>> indexValues = new IndexValue<Float>().buildList(array);
        Collections.sort(indexValues);

        int n = array.length;
        Integer[] result = new Integer[n];

        for (int i = 0; i < n; i++) {
            result[i] = indexValues.get(i).getIndex();
        }
        return result;
    }

    public static Integer[] indexAfterSort(String[] array) {
        List<IndexValue<String>> indexValues = new IndexValue<String>().buildList(array);
        Collections.sort(indexValues);

        int n = array.length;
        Integer[] result = new Integer[n];

        for (int i = 0; i < n; i++) {
            result[i] = indexValues.get(i).getIndex();
        }
        return result;
    }

    /**
     * Get descend sort index of array[].
     */
    public static Integer[] indexBeforeReversedSort(Double[] array) {
        List<IndexValue<Double>> indexValues = new IndexValue<Double>().buildList(array);
        Collections.sort(indexValues);
        Collections.reverse(indexValues);
        int n = array.length;
        Integer[] result = new Integer[n];

        for (int i = 0; i < n; i++) {
            result[i] = indexValues.get(i).getIndex();
        }
        return result;
    }

    /**
     * Get unique array index of array[].
     */
    public static Integer[] sortedUniqueIndex(Integer[] array) {
        int j = 1;
        int value = array[0];
        List<Integer> index = new ArrayList<Integer>();
        index.add(0);
        for (int i = 0; i < array.length; i++) {
            if (array[i] != value) {
                j++;
                value = array[i];
                index.add(i);
            }
        }
        Integer[] result = new Integer[j];
        for (int i = 0; i < j; i++) {
            result[i] = index.get(i);
        }
        return result;
    }

    /**
     * rank([1,2,2,3,3,3,4])
     * -> [1, 3(并列第3), 3(并列第3), 6(并列第6), 6(并列第6), 6(并列第6), 7]
     * 统计数组中每一个数字的排名,数字相同的按照最低并列排名算
     */
    public static double[] rank(Double[] array) {
        int n = array.length;
        double[] result = new double[n];
        int[] countSort = ArrayUtil.reverse(countSort(array));
        int count = 0;
        int index = array.length;
        for (int j : countSort) {
            for (int i = 0; i < j; i++) {
                result[count + i] = index - count;
            }
            count = count + j;
        }
        return result;
    }

    /**
     * rank([1,2,2,3,3,3,4])
     * -> [7, 5.5, 5.5, 3, 3, 3, 1]
     * 计算一个数组中每一个数字的从大到小的排名的平均数,然后取倒置
     * 例如上例中的数字3,从大到小排名第2,3,4位,因此平均排名是3,数字2从大到小的排名是5,6,所以平均排名是5.5
     */
    public static double[] averageRankReverse(Double[] array) {
        int n = array.length;
        double[] result = new double[n];
        int[] countSortReversed = ArrayUtil.reverse(countSort(array));
        int count = 0;
        int index = array.length;
        for (int j : countSortReversed) {
            for (int i = 0; i < j; i++) {
                result[index - j + i] = count + (j + 1) / (double) 2;
            }
            index = index - j;
            count = count + j;
        }
        return result;
    }

    /**
     * count number of times corresponding to unique sorted array.
     * 将数组转置,然后计算每一个数字出现的次数
     */
    public static int[] countSort(Double[] array) {
        Double[] aSort = array.clone();
        Arrays.sort(aSort);
        int j = 0, k = 0;
        int[] result = new int[numOfUnique(aSort)];
        double value = aSort[0];
        for (int i = 0; i < aSort.length; i++) {
            if (aSort[i] == value) {
                j++;
            } else {
                result[k] = j;
                k++;
                j = 1;
                value = aSort[i];
            }

        }
        result[k] = j;
        return result;
    }


    /**
     * Exchange position of element i,j in array[].
     */
    private static void exch(Comparable[] array, int i, int j) {
        Comparable t = array[i];
        array[i] = array[j];
        array[j] = t;
    }

    /**
     * Count number of different values in a **sorted** array.
     */
    private static int numOfUnique(Double[] array) {
        int j = 1;
        double value = array[0];
        for (double i : array) {
            if (i != value) {
                j++;
                value = i;
            }
        }
        return j;
    }

    /**
     * Find order of array.
     * 0: unsorted
     * 1: ascending
     * -1: descending
     */
    public static int findSortOrder(Double[] array) {
        int i = 0;
        int n = array.length;
        if (n <= 1) {
            return 0;
        }
        while (i < n - 1 && array[i] == array[i + 1]) {
            i++;
        }
        if (i == n - 1) {
            return 1;
        }
        if (array[i] < array[i + 1]) {
            for (; i < n - 1; i++) {
                if (array[i] > array[i + 1]) {
                    return 0;
                }
            }
            return 1;
        } else {
            for (; i < n - 1; i++) {
                if (array[i] < array[i + 1]) {
                    return 0;
                }
            }
            return -1;
        }
    }

    public static void checkNull(Double[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j].equals(Double.NaN)) {
                    logger.info("发现空值:i/j:" + i + "/" + j);
                }
            }
        }
    }

    public static List<SimplePeptide> toTargetPeptideList(List<PeptideDO> peptides, SlopeIntercept slopeIntercept, Float rtExtractionWindows){
        List<SimplePeptide> tps = new ArrayList<>();
        for(PeptideDO peptide : peptides){
            SimplePeptide tp = peptide.toTargetPeptide();
            if (rtExtractionWindows != -1) {
                float iRt = (tp.getRt() - slopeIntercept.getIntercept().floatValue()) / slopeIntercept.getSlope().floatValue();
                tp.setRtStart(iRt - rtExtractionWindows / 2.0f);
                tp.setRtEnd(iRt + rtExtractionWindows / 2.0f);
            } else {
                tp.setRtStart(-1);
                tp.setRtEnd(99999);
            }
            tps.add(tp);
        }
        return tps;
    }

    public static float[] toPrimitive(Set<Float> floatSet){
        if (floatSet.size() == 0){
            return new float[0];
        }
        float[] fArray = new float[floatSet.size()];
        int i=0;
        Iterator<Float> iterator = floatSet.iterator();
        while(iterator.hasNext()){
            fArray[i] = iterator.next();
            i++;
        }

        return fArray;
    }

}
