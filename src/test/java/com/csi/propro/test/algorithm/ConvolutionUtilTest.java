package com.csi.propro.test.algorithm;


import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.bean.aird.Compressor;
import com.csi.propro.domain.db.ExperimentDO;
import com.csi.propro.domain.db.SwathIndexDO;
import com.csi.propro.service.ExperimentService;
import com.csi.propro.service.SwathIndexService;
import com.csi.propro.utils.ConvolutionUtil;
import com.csi.propro.algorithm.parser.AirdFileParser;
import com.csi.propro.domain.bean.analyse.MzIntensityPairs;
import com.csi.propro.test.BaseTest;
import com.csi.propro.utils.FileUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.RandomAccessFile;

public class ConvolutionUtilTest extends BaseTest {

    @Autowired
    ExperimentService experimentService;
    @Autowired
    AirdFileParser airdFileParser;
    @Autowired
    SwathIndexService swathIndexService;

    @Test
    public void testAcc(){
        MzIntensityPairs pairs = null;

        ResultDO<ExperimentDO> expResult = experimentService.getById("5d22e4dca1eaff5cabc0fa39");
        SwathIndexDO swathIndex = swathIndexService.getById("5d22e4dca1eaff5cabc0fa45");

        ExperimentDO experimentDO = expResult.getModel();

        RandomAccessFile raf = null;
        try {
            File file = new File(experimentDO.getAirdPath());
            raf = new RandomAccessFile(file, "r");
            pairs = airdFileParser.parseValue(raf, swathIndex, 722.14197f, experimentDO.fetchCompressor(Compressor.TARGET_MZ), experimentDO.fetchCompressor(Compressor.TARGET_INTENSITY));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(raf);
        }

        long start = System.nanoTime();
        float result = ConvolutionUtil.accumulation(pairs, 173.128f, 175.108f);
        System.out.println(result+"-"+(System.nanoTime() - start));
    }

    @Test
    public void testAcc2(){
        Float[] mzArray = {120f,121f,122f,123f,124f,130f,131f,132f,145f,146f,147f,148f};
        Float[] intArray = {1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f};
        float result1 = ConvolutionUtil.accumulation(new MzIntensityPairs(mzArray, intArray), 126f, 132f);
        float result2 = ConvolutionUtil.accumulation(new MzIntensityPairs(mzArray, intArray), 126f, 132f);
        float result3 = ConvolutionUtil.accumulation(new MzIntensityPairs(mzArray, intArray), 130f, 132f);
        float result4 = ConvolutionUtil.accumulation(new MzIntensityPairs(mzArray, intArray), 131f, 132f);
        float result5 = ConvolutionUtil.accumulation(new MzIntensityPairs(mzArray, intArray), 148f, 152f);
        float result6 = ConvolutionUtil.accumulation(new MzIntensityPairs(mzArray, intArray), 112f, 120f);
        assert result1 == 3f;
        assert result2 == 3f;
        assert result3 == 3f;
        assert result4 == 2f;
        assert result5 == 0f;
        assert result6 == 1f;
    }
}
