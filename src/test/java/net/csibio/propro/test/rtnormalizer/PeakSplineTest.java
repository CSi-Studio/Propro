package net.csibio.propro.test.rtnormalizer;

import net.csibio.propro.domain.bean.analyse.RtIntensityPairs;
import net.csibio.propro.domain.bean.analyse.RtIntensityPairsDouble;
import net.csibio.propro.algorithm.peak.PeakSpline;
import net.csibio.propro.test.BaseTest;
import org.junit.Test;

/**
 * Created by Song Jian
 * Time: 2018-08-13 17-22
 */
public class PeakSplineTest extends BaseTest {
    @Test
    public void evalTest() {
        PeakSpline peakSpline1 = new PeakSpline();
        PeakSpline peakSpline0 = new PeakSpline();
        PeakSpline peakSplineBD = new PeakSpline();
        Float[] rt = {486.784F, 486.787F, 486.790F, 486.793F, 486.795F, 486.797F, 486.800F, 486.802F, 486.805F, 486.808F, 486.811F};
        Double[] rtDouble = {486.784d, 486.787d, 486.790d, 486.793d, 486.795d, 486.797d, 486.800d, 486.802d, 486.805d, 486.808d, 486.811d};
        Float[] intensity = {0.0f, 154683.17f, 620386.5f, 1701390.12f, 2848879.25f, 3564045.5f, 2744585.7f, 1605583.0f, 1518984.0f, 1591352.21f, 1691345.1f};
        Double[] intensityDouble = {0.0, 154683.17, 620386.5, 1701390.12, 2848879.25, 3564045.5, 2744585.7, 1605583.0, 1518984.0, 1591352.21, 1691345.1};
        RtIntensityPairsDouble input = new RtIntensityPairsDouble();
        input.setRtArray(rtDouble);
        input.setIntensityArray(intensityDouble);
        peakSpline1.init(input, 0, 10);
        peakSpline0.init(rtDouble, intensityDouble, 0 , 10);
        peakSplineBD.initBD(rt, intensity, 0, 10);
        double tmp = peakSpline1.eval(486.785d);
        double tmp0 = peakSpline0.eval(Double.parseDouble("486.785"));
        double tmpBD = peakSplineBD.evalBD(486.785d);
        assert isSimilar(tmp, 35173.1841778984f, 1.0f);
        assert isSimilar(peakSpline1.eval(486.794d), 2271426.93316241f, 1.0f);
        assert isSimilar(peakSpline1.eval(486.784d), 0.0f, 1.0f);
        assert isSimilar(peakSpline1.eval(486.790d), 620386.5f, 1.0f);
        assert isSimilar(peakSpline1.eval(486.808d), 1591352.21f, 1.0f);
        assert isSimilar(peakSpline1.eval(486.811d), 1691345.1f, 1.0f);

        PeakSpline peakSpline2 = new PeakSpline();
        Float[] x = new Float[11];
        Float[] y = new Float[11];
        Float x_min = -0.5F;
        Float x_max = 1.5F;
        for (int i = 0; i < 11; i++) {
            x[i] = (x_min + i / 10.0F * (x_max - x_min));
            y[i] = Float.valueOf((float)Math.sin((x_min + i / 10.0F * (x_max - x_min))));
        }
        RtIntensityPairs input2Float = new RtIntensityPairs(x, y);
        RtIntensityPairsDouble input2 = new RtIntensityPairsDouble(input2Float);
        peakSpline2.init(input2, 0, 10);
        for (int i = 0; i < 11; i++) {
            assert isSimilar(peakSpline2.eval(x[i]), y[i], 1.0f);
        }

        for (int i = 0; i < 16; i++) {
            float xx = x_min + i/15.0f*(x_max-x_min);
            assert isSimilar(peakSpline2.eval(xx), (float)Math.sin(xx), 1.0f);
        }
    }

    @Test
    public void derivativesTest() {
        PeakSpline peakSpline1 = new PeakSpline();
        PeakSpline peakSplineBD = new PeakSpline();
        Float[] rt = {486.784F, 486.787F, 486.790F, 486.793F, 486.795F, 486.797F, 486.800F, 486.802F, 486.805F, 486.808F, 486.811F};
        Double[] rtDouble = {486.784, 486.787, 486.790, 486.793, 486.795, 486.797, 486.800, 486.802, 486.805, 486.808, 486.811};
        Double[] intensity = {0.0, 154683.17, 620386.5, 1701390.12, 2848879.25, 3564045.5, 2744585.7, 1605583.0, 1518984.0, 1591352.21, 1691345.1};

        peakSpline1.init(rtDouble, intensity, 0, 10);
//        peakSplineBD.initBD(rtDouble, intensity, 0, 10);
        double tmp = peakSpline1.derivatives(486.785d);
        double tmp1 = peakSpline1.derivatives(486.794d);
//        double tmpBd = peakSplineBD.derivativesBD(486.785d);
        assert isSimilar(peakSpline1.derivatives(486.785d), 39270152.2996247d, 1.0f);
        assert isSimilar(peakSpline1.derivatives(486.794d), 594825947.154264d, 1.0f);

        PeakSpline peakSpline2 = new PeakSpline();
        Float[] x = new Float[11];
        Float[] y = new Float[11];
        Float x_min = -0.5F;
        Float x_max = 1.5F;
        for (int i = 0; i < 11; i++) {
            x[i] = (x_min + i / 10.0F * (x_max - x_min));
            y[i] = Float.valueOf((float)Math.sin((x_min + i / 10.0F * (x_max - x_min))));
        }
        RtIntensityPairs input2Float = new RtIntensityPairs(x, y);
        RtIntensityPairsDouble input2 = new RtIntensityPairsDouble(input2Float);
        peakSpline2.init(input2, 0, 10);
        for (int i = 2; i < 9; i++) {
            assert isSimilar(peakSpline2.derivatives(x[i]), (float)Math.cos(x[i]), 1.0f);
        }
        for (int i = 2; i < 14; i++) {
            float xx = x_min + i/15.0f*(x_max-x_min);
            assert isSimilar(peakSpline2.derivatives(xx), (float)Math.cos(xx), 1.0f);
        }

        // TODO:test boundary conditions y'' = 0
//        assert isSimilar(peakSpline2.derivatives(x[0], 2), 0.0f, 1.0f);
//        assert isSimilar(peakSpline2.derivatives(x[10], 2), 0.0f, 1.0f);
    }

    private boolean isSimilar(double a, double b, float tolerance ) {
        float TOLERANCE = tolerance;
        if (Math.abs(a-b) < TOLERANCE) {
            return true;
        } else {
            return false;
        }
    }
}
