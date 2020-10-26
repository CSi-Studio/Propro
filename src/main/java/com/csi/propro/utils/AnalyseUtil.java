package com.csi.propro.utils;

import com.csi.propro.domain.db.AnalyseDataDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyseUtil {

    public static final Logger logger = LoggerFactory.getLogger(AnalyseUtil.class);

    public static void compress(AnalyseDataDO data) {
        data.setRtArray(null);
        data.setIntensityMap(null);
    }

    public static String getDataRef(String overviewId, String peptideRef, Boolean isDecoy) {
        return overviewId + "-" + peptideRef + "-" + isDecoy;
    }
//    public static void compress(AnalyseDataDO data) {
//        if (data.getRtArray() != null) {
//            data.setRtStart(data.getRtArray()[0]);
//            data.setRtEnd(data.getRtArray()[data.getRtArray().length - 1]);
////            data.setConvRtArray(CompressUtil.zlibCompress(CompressUtil.transToByte(ArrayUtils.toPrimitive(data.getRtArray()))));
//            data.setRtArray(null);
//        }
//        data.setConvIntensityMap(new HashMap<>());
//        for (String cutInfo : data.getIntensityMap().keySet()) {
//            Float[] intensities = data.getIntensityMap().get(cutInfo);
//            if (intensities != null) {
//                data.getConvIntensityMap().put(cutInfo, CompressUtil.zlibCompress(CompressUtil.transToByte(ArrayUtils.toPrimitive(intensities))));
//            } else {
//                data.getConvIntensityMap().put(cutInfo, null);
//            }
//        }
//        data.setIntensityMap(null);
//        data.setCompressed(true);
//    }
//
//
//    public static void decompress(AnalyseDataDO data, List<Float> rtList) {
//        int indexStart = rtList.indexOf(data.getRtStart());
//        int indexEnd = rtList.indexOf(data.getRtEnd());
//
//        Float[] rtArray = new Float[rtList.size()];
//        rtList.toArray(rtArray);
//        data.setRtArray(ArrayUtils.subarray(rtArray, indexStart, indexEnd+1));
//
//        for (String cutInfo : data.getConvIntensityMap().keySet()) {
//            byte[] intensities = data.getConvIntensityMap().get(cutInfo);
//            if (intensities != null) {
//                data.getIntensityMap().put(cutInfo, CompressUtil.transToFloat(CompressUtil.zlibDecompress(intensities)));
//            } else {
//                data.getIntensityMap().put(cutInfo, null);
//            }
//        }
//        data.setConvIntensityMap(null);
//        data.setCompressed(false);
//    }


}
