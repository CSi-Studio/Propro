package net.csibio.propro.utils;

import net.csibio.aird.bean.MzIntensityPairs;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.ExperimentDO;
import net.csibio.propro.constants.enums.ResultCode;

import java.io.File;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-17 15:00
 */
public class ConvolutionUtil {

    /**
     * 计算 mz在[start, end]范围对应的intensity和
     *
     * @param pairs   其中mzArray已经从小到到已经排好序
     * @param mzStart
     * @param mzEnd
     * @return
     */
    public static float accumulation(MzIntensityPairs pairs, Float mzStart, Float mzEnd) {

        float[] mzArray = pairs.getMzArray();
        float[] intensityArray = pairs.getIntensityArray();

        float result = 0f;
        try {
            //Index of first mz bigger than mzStart
            int rightIndex;
            rightIndex = findRightIndex(mzArray, mzStart);

            //No element is bigger than mzStart in mzArray
            if (rightIndex == -1) {
                return 0f;
            }
            int iterIndex = rightIndex;

            //Accumulate when iterIndex in (mzStart, mzEnd). Return 0 if rightIndex's mz is bigger than mzEnd.
            while (mzArray[iterIndex] <= mzEnd) {
                result += intensityArray[iterIndex];
                iterIndex++;
            }
        } catch (Exception e) {
            return result;
        }
        return result;
    }

    /**
     * 计算 mz在[start, end]范围对应的intensity和
     *
     * @param pairs 其中mzArray已经从小到到已经排好序
     * @param mz
     * @return
     */
    public static float adaptiveAccumulation(MzIntensityPairs pairs, Float mz) {
        float[] mzArray = pairs.getMzArray();
        float[] intensityArray = pairs.getIntensityArray();
        float result = 0f;
        int nearestIndex = PeakUtil.findNearestIndex(mzArray, mz);
        int leftIndex = nearestIndex == 0 ? 0 : nearestIndex - 1;
        int rightIndex = nearestIndex == mzArray.length - 1 ? nearestIndex : nearestIndex + 1;
        float leftPace = mzArray[nearestIndex] - mzArray[leftIndex];
        float rightPace = mzArray[rightIndex] - mzArray[nearestIndex];
        float pace = Math.min(leftPace, rightPace);
        if (Math.abs(mzArray[nearestIndex] - mz) > 0.01) {
            return 0f;
        }
        //to the left
        while (leftIndex > 0 && mzArray[leftIndex] - mzArray[leftIndex - 1] < 2 * pace) {
            if (intensityArray[leftIndex] > intensityArray[leftIndex - 1]) {
                leftIndex--;
            } else {
                break;
            }
        }
        //to the right
        while (rightIndex < mzArray.length - 1 && mzArray[rightIndex + 1] - mzArray[rightIndex] < 2 * pace) {
            if (intensityArray[rightIndex] > intensityArray[rightIndex + 1]) {
                rightIndex++;
            } else {
                break;
            }
        }
        for (int index = leftIndex; index <= rightIndex; index++) {
            result += intensityArray[index];
        }
        return result;
    }

    public static ResultDO<File> checkExperiment(ExperimentDO experimentDO) {
        if (experimentDO == null) {
            return ResultDO.buildError(ResultCode.EXPERIMENT_NOT_EXISTED);
        }
        File file = new File(experimentDO.getAirdPath());
        if (!file.exists()) {
            return ResultDO.buildError(ResultCode.FILE_NOT_EXISTED);
        }

        ResultDO<File> resultDO = new ResultDO(true);
        resultDO.setModel(file);
        return resultDO;
    }

    /**
     * 找到从小到大排序的第一个大于等于目标值的索引
     * 当目标值大于等于范围中的最大值时,返回-1
     * 左闭右开区间
     *
     * @param array
     * @param target
     * @return
     */
    public static int findRightIndex(float[] array, Float target) {
        int rightIndex = array.length - 1;

        if (target <= array[0]) {
            return 0;
        }
        if (target >= array[rightIndex]) {
            return -1;
        }

        int leftIndex = 0;
        while (leftIndex + 1 < rightIndex) {
            int tmp = (leftIndex + rightIndex) / 2;
            if (target < array[tmp]) {
                rightIndex = tmp;
            } else if (target > array[tmp]) {
                leftIndex = tmp;
            } else {
                return tmp;
            }
        }

        return rightIndex;
    }

    /**
     * 找到从小到大排序的第一个大于目标值的索引
     * 当目标值小于范围中的最小值时,返回-1
     * 当目标值大于范围中的最大值时,返回-2
     *
     * @param array
     * @param target
     * @return
     */
    public static int findIndex(float[] array, float target, boolean isLeftIndex) {
        if (array == null) {
            return 0;
        }
        int leftIndex = 0, rightIndex = array.length - 1;
        if (isLeftIndex) {
            if (target < array[0]) {
                return 0;
            }
            if (target > array[rightIndex]) {
                return -1;
            }
        } else {
            if (target < array[0]) {
                return -1;
            }
            if (target > array[rightIndex]) {
                return rightIndex;
            }
        }

        while (leftIndex + 1 < rightIndex) {
            int tmp = (leftIndex + rightIndex) / 2;
            if (target < array[tmp]) {
                rightIndex = tmp;
            } else if (target > array[tmp]) {
                leftIndex = tmp;
            } else {
                return tmp;
            }
        }
        if (isLeftIndex) {
            return rightIndex;
        } else {
            return leftIndex;
        }
    }

    public static int findIndex(float[] array, float target, int leftIndex, int rightIndex, boolean isLeftIndex) {
        if (array == null) {
            return 0;
        }
        if (isLeftIndex) {
            if (target < array[0]) {
                return leftIndex;
            }
            if (target > array[rightIndex]) {
                return -1;
            }
        } else {
            if (target < array[0]) {
                return -1;
            }
            if (target > array[rightIndex]) {
                return rightIndex;
            }
        }

        while (leftIndex + 1 < rightIndex) {
            int tmp = (leftIndex + rightIndex) / 2;
            if (target < array[tmp]) {
                rightIndex = tmp;
            } else if (target > array[tmp]) {
                leftIndex = tmp;
            } else {
                return tmp;
            }
        }
        if (isLeftIndex) {
            return rightIndex;
        } else {
            return leftIndex;
        }
    }

    /**
     * 找到从小到大排序的第一个大于目标值的索引
     * 当目标值小于范围中的最小值时,返回-1
     * 当目标值大于范围中的最大值时,返回-2
     *
     * @param array
     * @param target
     * @return
     */
    public static int findIndex(Double[] array, Double target, boolean isLeftIndex) {
        if (array == null) {
            return 0;
        }
        int leftIndex = 0, rightIndex = array.length - 1;
        if (isLeftIndex) {
            if (target < array[0]) {
                return 0;
            }
            if (target > array[rightIndex]) {
                return -1;
            }
        } else {
            if (target < array[0]) {
                return -1;
            }
            if (target > array[rightIndex]) {
                return rightIndex;
            }
        }

        while (leftIndex + 1 < rightIndex) {
            int tmp = (leftIndex + rightIndex) / 2;
            if (target < array[tmp]) {
                rightIndex = tmp;
            } else if (target > array[tmp]) {
                leftIndex = tmp;
            } else {
                return tmp;
            }
        }
        if (isLeftIndex) {
            return rightIndex;
        } else {
            return leftIndex;
        }
    }

}
