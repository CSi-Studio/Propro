package com.westlake.air.pecs.utils;

import com.westlake.air.pecs.domain.bean.score.IntegrateWindowMzIntensity;
import com.westlake.air.pecs.domain.bean.score.SlopeIntercept;

import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-15 19:38
 */
public class ScoreUtil {

    public static SlopeIntercept trafoInverter(SlopeIntercept slopeIntercept){
        float slope = slopeIntercept.getSlope();
        float intercept = slopeIntercept.getIntercept();
        SlopeIntercept slopeInterceptInvert = new SlopeIntercept();

        if(slope == 0f){
            slope = 0.000001f;
        }
        slopeInterceptInvert.setSlope(1 / slope);
        slopeInterceptInvert.setIntercept(- intercept / slope);

        return slopeInterceptInvert;
    }

    public static float trafoApplier(SlopeIntercept slopeInterceptInvert, float value){
        return value * slopeInterceptInvert.getSlope() + slopeInterceptInvert.getIntercept();
    }

    public static float[] normalizeSum(List libraryIntensity){
        float[] normalizedLibraryIntensity = new float[libraryIntensity.size()];
        float sum = 0f;
        for(Object intensity: libraryIntensity){
            sum += (float)intensity;
        }

        if(sum == 0f){
            sum += 0.000001;
        }

        for(int i = 0; i<libraryIntensity.size(); i++){
            normalizedLibraryIntensity[i] = (float)libraryIntensity.get(i) / sum;
        }
        return normalizedLibraryIntensity;
    }

    public static IntegrateWindowMzIntensity integrateWindow(List<Float> spectrumMzArray, List<Float> spectrumIntArray, double left, double right){
        IntegrateWindowMzIntensity mzIntensity = new IntegrateWindowMzIntensity();

        float mz = 0f, intensity = 0f;
        int leftIndex = MathUtil.bisection(spectrumMzArray, left).getHigh();
        int rightIndex = MathUtil.bisection(spectrumMzArray, right).getLow();

        for(int index = leftIndex; index <=rightIndex; index ++){
            intensity += spectrumIntArray.get(index);
            mz += spectrumMzArray.get(index) * spectrumIntArray.get(index);
        }
        if(intensity > 0f) {
            mz /= intensity;
            mzIntensity.setSignalFound(true);
        }else {
            mz = -1;
            intensity = 0;
            mzIntensity.setSignalFound(false);
        }
        mzIntensity.setMz(mz);
        mzIntensity.setIntensity(intensity);

        return mzIntensity;
    }
}