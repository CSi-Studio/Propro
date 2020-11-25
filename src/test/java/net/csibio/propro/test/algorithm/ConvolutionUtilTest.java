package net.csibio.propro.test.algorithm;


import net.csibio.aird.bean.Compressor;
import net.csibio.aird.bean.MzIntensityPairs;
import net.csibio.aird.parser.DIAParser;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.ExperimentDO;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.service.ExperimentService;
import net.csibio.propro.service.SwathIndexService;
import net.csibio.propro.test.BaseTest;
import net.csibio.propro.utils.ConvolutionUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ConvolutionUtilTest extends BaseTest {

    @Autowired
    ExperimentService experimentService;
    @Autowired
    SwathIndexService swathIndexService;

    @Test
    public void testAcc() {
        MzIntensityPairs pairs = null;

        ResultDO<ExperimentDO> expResult = experimentService.getById("5d22e4dca1eaff5cabc0fa39");
        SwathIndexDO swathIndex = swathIndexService.getById("5d22e4dca1eaff5cabc0fa45");

        ExperimentDO experimentDO = expResult.getModel();

        Compressor mzCompressor = experimentDO.fetchCompressor(Compressor.TARGET_MZ);
        DIAParser parser = new DIAParser(experimentDO.getAirdPath(), mzCompressor, experimentDO.fetchCompressor(Compressor.TARGET_INTENSITY), mzCompressor.getPrecision());
        pairs = parser.getSpectrumByRt(swathIndex.getStartPtr(), swathIndex.getRts(), swathIndex.getMzs(), swathIndex.getInts(), 722.14197f);
        parser.close();
        long start = System.nanoTime();
        float result = ConvolutionUtil.accumulation(pairs, 173.128f, 175.108f);
        System.out.println(result + "-" + (System.nanoTime() - start));
    }

    @Test
    public void testAcc2() {
        float[] mzArray = {120f, 121f, 122f, 123f, 124f, 130f, 131f, 132f, 145f, 146f, 147f, 148f};
        float[] intArray = {1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f};
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
