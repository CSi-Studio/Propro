package net.csibio.propro.utils;

import net.csibio.propro.domain.bean.score.IntegrateWindowMzIntensity;
import net.csibio.propro.domain.bean.score.SlopeIntercept;
import net.csibio.propro.constants.enums.ScoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-15 19:38
 */
public class ScoreUtil {

    public static final Logger logger = LoggerFactory.getLogger(ScoreUtil.class);

    /**
     * invert y = kx + b to x = 1/k y + -b/k;
     *
     * @param slopeIntercept k & b input
     * @return 1/k -b/k
     */
    public static SlopeIntercept trafoInverter(SlopeIntercept slopeIntercept) {
        double slope = slopeIntercept.getSlope();
        double intercept = slopeIntercept.getIntercept();
        SlopeIntercept slopeInterceptInvert = new SlopeIntercept();

        if (slope == 0d) {
            slope = 0.000001d;
        }
        slopeInterceptInvert.setSlope(1 / slope);
        slopeInterceptInvert.setIntercept(-intercept / slope);

        return slopeInterceptInvert;
    }

    /**
     * apply kx + b
     *
     * @param slopeIntercept k & b (may be inverted)
     * @param value          x
     * @return y
     */
    public static double trafoApplier(SlopeIntercept slopeIntercept, Double value) {
        if (slopeIntercept.getSlope() == 0) {
            return value;
        } else {
            return value * slopeIntercept.getSlope() + slopeIntercept.getIntercept();
        }
    }

    /**
     * 1) get sum of list
     * 2) divide elements in list by sum
     *
     * @param intensityList input intensity list
     * @return output normalized intensity list
     */
    public static double[] normalizeSumDouble(List<Double> intensityList) {
        double[] normalizedIntensity = new double[intensityList.size()];
        double sum = 0d;
        for (Double intensity : intensityList) {
            sum += intensity;
        }
        for (int i = 0; i < intensityList.size(); i++) {
            normalizedIntensity[i] = (intensityList.get(i) / sum);
        }
        return normalizedIntensity;
    }
    public static List<Double> normalizeSumDouble(List<Double> intensityList, double sum) {
        List<Double> normalizedIntensity = new ArrayList<>();
        for (int i = 0; i < intensityList.size(); i++) {
            normalizedIntensity.add(intensityList.get(i) / sum);
        }
        return normalizedIntensity;
    }
    /**
     * 1) get left and right index corresponding to spectrum
     * 2) get interval intensity sum to intensity
     * 3) get interval average mz by intensity(as weight)
     *
     * @param spectrumMzArray  spectrum
     * @param spectrumIntArray spectrum
     * @param left             left mz
     * @param right            right mz
     * @return float mz,intensity boolean signalFound
     */
    public static IntegrateWindowMzIntensity integrateWindow(Float[] spectrumMzArray, Float[] spectrumIntArray, float left, float right) {
        IntegrateWindowMzIntensity mzIntensity = new IntegrateWindowMzIntensity();

        double mz = 0d, intensity = 0d;
        int leftIndex = ConvolutionUtil.findIndex(spectrumMzArray, left, true);
        int rightIndex = ConvolutionUtil.findIndex(spectrumMzArray, right, false);

        if(leftIndex == -1 || rightIndex == -1){
            return new IntegrateWindowMzIntensity(false);
        }
        for (int index = leftIndex; index <= rightIndex; index++) {
            intensity += spectrumIntArray[index];
            mz += spectrumMzArray[index] * spectrumIntArray[index];
        }
        if (intensity > 0f) {
            mz /= intensity;
            mzIntensity.setSignalFound(true);
        } else {
            mz = -1;
            intensity = 0;
            mzIntensity.setSignalFound(false);
        }
        mzIntensity.setMz(mz);
        mzIntensity.setIntensity(intensity);

        return mzIntensity;
    }

    public static List<String> getScoreTypes(HttpServletRequest request){
        List<String> scoreTypes = new ArrayList<>();
        scoreTypes.add(ScoreType.MainScore.getTypeName());
        scoreTypes.add(ScoreType.WeightedTotalScore.getTypeName());
        for (ScoreType type : ScoreType.values()) {
            String typeParam = request.getParameter(type.getTypeName());
            if (typeParam != null && typeParam.equals("on")) {
                scoreTypes.add(type.getTypeName());
            }
        }
        return scoreTypes;
    }

    //test failed
    public static void weightsMapFilter(HashMap<String,Double> weightsMap){
        for (String scoreTypeName: weightsMap.keySet()){
            Boolean biggerIsBetter = ScoreType.getBiggerIsBetter(scoreTypeName);
            if (biggerIsBetter == null || (biggerIsBetter != weightsMap.get(scoreTypeName) > 0)){
                weightsMap.put(scoreTypeName, 0d);
            }
        }
    }
}
