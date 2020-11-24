package net.csibio.propro.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.csibio.aird.bean.Compressor;
import net.csibio.aird.bean.MzIntensityPairs;
import net.csibio.aird.parser.DIAParser;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.ExperimentDO;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.service.ExperimentService;
import net.csibio.propro.service.SwathIndexService;
import net.csibio.propro.utils.CompressUtil;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.TreeMap;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-08 11:28
 */
@Controller
@RequestMapping("spectrum")
public class SpectrumController extends BaseController {

    @Autowired
    ExperimentService experimentService;
    @Autowired
    SwathIndexService swathIndexService;

    @RequestMapping(value = "/view")
    @ResponseBody
    ResultDO<JSONObject> view(Model model,
                              @RequestParam(value = "indexId", required = false) String indexId,
                              @RequestParam(value = "rt", required = false) float rt,
                              @RequestParam(value = "expId", required = false) String expId) {
        ResultDO<JSONObject> resultDO = new ResultDO<>(true);
        MzIntensityPairs pairs = null;

        ResultDO<ExperimentDO> expResult = experimentService.getById(expId);
        if (expResult.isFailed()) {
            resultDO.setErrorResult(ResultCode.EXPERIMENT_NOT_EXISTED);
            return resultDO;
        }

        SwathIndexDO swathIndex = swathIndexService.getById(indexId);
        if (swathIndex == null) {
            resultDO.setErrorResult(ResultCode.SWATH_INDEX_NOT_EXISTED);
            return resultDO;
        }

        ExperimentDO experimentDO = expResult.getModel();
        Compressor mzCompressor = experimentDO.fetchCompressor(Compressor.TARGET_MZ);
        DIAParser parser = new DIAParser(experimentDO.getAirdPath(), mzCompressor, experimentDO.fetchCompressor(Compressor.TARGET_INTENSITY), mzCompressor.getPrecision());
        pairs = parser.getSpectrumByRt(swathIndex.getStartPtr(), swathIndex.getRts(), swathIndex.getMzs(), swathIndex.getInts(), rt);

        JSONObject res = new JSONObject();
        JSONArray mzArray = new JSONArray();
        JSONArray intensityArray = new JSONArray();
        if (pairs == null) {
            return ResultDO.buildError(ResultCode.DATA_IS_EMPTY);
        }

        float[] pairMzArray = pairs.getMzArray();
        float[] pairIntensityArray = pairs.getIntensityArray();
        for (int n = 0; n < pairMzArray.length; n++) {
            mzArray.add(pairMzArray[n]);
            intensityArray.add(pairIntensityArray[n]);
        }

        res.put("mz", mzArray);
        res.put("intensity", intensityArray);
        resultDO.setModel(res);
        return resultDO;
    }

    @RequestMapping(value = "/mzxmlextractor")
    String mzxmlextractor(Model model,
                          @RequestParam(value = "isZlibCompression", required = false) boolean isZlibCompression,
                          @RequestParam(value = "values", required = false) String values,
                          @RequestParam(value = "precision", required = false, defaultValue = "32") Integer precision) {
        model.addAttribute("values", values);
        model.addAttribute("precision", precision);
        model.addAttribute("isZlibCompression", isZlibCompression);

        if (values != null && !values.isEmpty()) {
            MzIntensityPairs pairs = getPeakMap(new Base64().decode(values.trim()), precision, isZlibCompression);
            model.addAttribute("mzArray", pairs.getMzArray());
            model.addAttribute("intensityArray", pairs.getIntensityArray());
        }

        return "spectrum/mzxmlextractor";
    }

    private Float[] getValues(byte[] value, int precision, boolean isCompression, ByteOrder byteOrder) {
        double[] doubleValues;
        Float[] floatValues;
        ByteBuffer byteBuffer = null;

        if (isCompression) {
            byteBuffer = ByteBuffer.wrap(CompressUtil.zlibDecompress(value));
        }else{
            byteBuffer = ByteBuffer.wrap(value);
        }

        byteBuffer.order(byteOrder);
        if (precision == 64) {
            DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
            doubleValues = new double[doubleBuffer.capacity()];
            doubleBuffer.get(doubleValues);
            floatValues = new Float[doubleValues.length];
            for (int index = 0; index < doubleValues.length; index++) {
                floatValues[index] = (float) doubleValues[index];
            }
        } else {
            FloatBuffer floats = byteBuffer.asFloatBuffer();
            floatValues = new Float[floats.capacity()];
            for (int index = 0; index < floats.capacity(); index++) {
                floatValues[index] = floats.get(index);
            }
        }

        byteBuffer.clear();
        return floatValues;
    }

    private MzIntensityPairs getPeakMap(byte[] value, int precision, boolean isZlibCompression) {
        MzIntensityPairs pairs = new MzIntensityPairs();
        Float[] values = getValues(value, precision, isZlibCompression, ByteOrder.BIG_ENDIAN);
        TreeMap<Float, Float> map = new TreeMap<>();
        for (int peakIndex = 0; peakIndex < values.length - 1; peakIndex += 2) {
            Float mz = values[peakIndex];
            Float intensity = values[peakIndex + 1];
            map.put(mz, intensity);
        }

        float[] mzArray = new float[map.size()];
        float[] intensityArray = new float[map.size()];
        int i = 0;
        for (float key : map.keySet()) {
            mzArray[i] = key;
            intensityArray[i] = map.get(key);
            i++;
        }

        pairs.setMzArray(mzArray);
        pairs.setIntensityArray(intensityArray);
        return pairs;
    }

}
