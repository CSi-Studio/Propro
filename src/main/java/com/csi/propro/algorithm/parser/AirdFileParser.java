package com.csi.propro.algorithm.parser;

import com.csi.propro.domain.bean.aird.Compressor;
import com.csi.propro.domain.bean.analyse.MzIntensityPairs;
import com.csi.propro.domain.db.SwathIndexDO;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.io.RandomAccessFile;
import java.util.List;
import java.util.TreeMap;

@Component("airdFileParser")
public class AirdFileParser extends BaseParser {

    /**
     * the result key is rt
     *
     * @param raf
     * @return
     * @throws Exception
     */
    public TreeMap<Float, MzIntensityPairs> parseSwathBlockValues(RandomAccessFile raf, SwathIndexDO indexDO, Compressor mzCompressor, Compressor intCompressor) throws Exception {
        TreeMap<Float, MzIntensityPairs> map = new TreeMap<>();
        List<Float> rts = indexDO.getRts();

        raf.seek(indexDO.getStartPtr());
        Long delta = indexDO.getEndPtr() - indexDO.getStartPtr();
        byte[] result = new byte[delta.intValue()];

        raf.read(result);
        List<Long> mzSizes = indexDO.getMzs();
        List<Long> intensitySizes = indexDO.getInts();

        int start = 0;
        for (int i = 0; i < mzSizes.size(); i++) {
            byte[] mz = ArrayUtils.subarray(result, start, start + mzSizes.get(i).intValue());
            start = start + mzSizes.get(i).intValue();
            byte[] intensity = ArrayUtils.subarray(result, start, start + intensitySizes.get(i).intValue());
            start = start + intensitySizes.get(i).intValue();
            try {
                MzIntensityPairs pairs = new MzIntensityPairs(getMzValues(mz, mzCompressor), getIntValues(intensity, intCompressor));
                map.put(rts.get(i), pairs);
            } catch (Exception e) {
                logger.error("index size error:" + i);
            }

        }
        return map;
    }

    /**
     * 从aird文件中获取某一条记录
     * 从一个完整的Swath Block块中取出一条记录
     *
     * @param raf
     * @param indexDO
     * @param rt
     * @return
     */
    public MzIntensityPairs parseValue(RandomAccessFile raf, SwathIndexDO indexDO, float rt, Compressor mzCompressor, Compressor intCompressor) {

        List<Float> rts = indexDO.getRts();
        int index = rts.indexOf(rt);

        long start = indexDO.getStartPtr();

        for (int i = 0; i < index; i++) {
            start += indexDO.getMzs().get(i);
            start += indexDO.getInts().get(i);
        }

        try {
            raf.seek(start);
            byte[] reader = new byte[indexDO.getMzs().get(index).intValue()];
            raf.read(reader);
            Float[] mzArray = getMzValues(reader, mzCompressor);
            start += indexDO.getMzs().get(index).intValue();
            raf.seek(start);
            reader = new byte[indexDO.getInts().get(index).intValue()];
            raf.read(reader);

            Float[] intensityArray = getIntValues(reader, intCompressor);
            return new MzIntensityPairs(mzArray, intensityArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }
}
