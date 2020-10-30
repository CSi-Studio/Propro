package net.csibio.propro.algorithm.peak;

import net.csibio.propro.domain.bean.analyse.RtIntensityPairsDouble;
import net.csibio.propro.utils.MathUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-01 15：27
 */
public class PeakSpline {
    private double[] a, b, c, d, x;
    private List<BigDecimal> xBD;
    private List<BigDecimal> aBD;
    private List<BigDecimal> bBD;
    private List<BigDecimal> cBD;
    private List<BigDecimal> dBD;

    // TODO: 暂时只有1阶导数
    public double derivatives(double value){
        int i = MathUtil.bisection(x, value).getHigh();
        if(x[i] > value || x[x.length-1] == value){
            --i;
        }
        double xx = value - x[i];
        return (b[i] + 2 * c[i] * xx + 3 * d[i] * xx * xx);
    }

    public double eval(double value){
        int i = MathUtil.bisection(x, value).getHigh();
        if(x[i] > value || x[x.length-1] == value){
            --i;
        }
        double xx = value - x[i];
        return(((d[i] * xx + c[i]) * xx + b[i]) * xx + a[i]);
    }

    public void init(RtIntensityPairsDouble rtIntensityPairs, int leftBoundary, int rightBoundary){
        int maxIndex = rightBoundary - leftBoundary;

        x = new double[maxIndex + 1];
        a = new double[maxIndex + 1];
        b = new double[maxIndex];
        d = new double[maxIndex];
        c = new double[maxIndex + 1]; //c[maxIndex] = 0;
        double[] h = new double[maxIndex];
        double[] mu = new double[maxIndex];
        double[] z = new double[maxIndex];

        double l;
        for(int i = 0; i<= maxIndex; i++){
            x[i] = rtIntensityPairs.getRtArray()[leftBoundary + i];
            a[i] = rtIntensityPairs.getIntensityArray()[leftBoundary + i];
        }

        // do the 0'th element manually
        h[0] = x[1] - x[0];

        for(int i=1; i<maxIndex; i++){
            h[i] = x[i+1] - x[i];
            l = 2 * (x[i+1] - x[i-1]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l;
            z[i] = (3 *(a[i + 1] * h[i - 1] - a[i] * (x[i + 1] - x[i - 1]) + a[i - 1] * h[i]) / (h[i - 1] * h[i]) - h[i - 1] * z[i - 1])/l;
        }

        for(int j = maxIndex - 1; j>=0;j--){
            c[j] = z[j] - mu[j] * c[j + 1];
            b[j] = (a[j + 1] - a[j]) / h[j] - h[j] * (c[j + 1] + 2 * c[j]) / 3;
            d[j] = (c[j + 1] - c[j]) / (3 * h[j]);
        }
    }
    public void init(Double[] rt, Double[] intensity, int leftBoundary, int rightBoundary){
        int maxIndex = rightBoundary - leftBoundary;
        x = new double[maxIndex + 1];
        a = new double[maxIndex + 1];
        b = new double[maxIndex];
        d = new double[maxIndex];
        c = new double[maxIndex + 1]; //c[maxIndex] = 0;
        double[] h = new double[maxIndex];
        double[] mu = new double[maxIndex];
        double[] z = new double[maxIndex];

        double l;
        for(int i = 0; i<= maxIndex; i++){
            x[i] = rt[leftBoundary + i];
            a[i] = intensity[leftBoundary + i];
        }

        // do the 0'th element manually
        h[0] = x[1] - x[0];

        for(int i=1; i<maxIndex; i++){
            h[i] = x[i+1] - x[i];
            l = 2 * (x[i+1] - x[i-1]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l;
            z[i] = (3 *(a[i + 1] * h[i - 1] - a[i] * (x[i + 1] - x[i - 1]) + a[i - 1] * h[i]) / (h[i - 1] * h[i]) - h[i - 1] * z[i - 1])/l;
        }

        for(int j = maxIndex - 1; j>=0;j--){
            c[j] = z[j] - mu[j] * c[j + 1];
            b[j] = (a[j + 1] - a[j]) / h[j] - h[j] * (c[j + 1] + 2 * c[j]) / 3;
            d[j] = (c[j + 1] - c[j]) / (3 * h[j]);
        }
    }

    public void initBD(Float[] rt, Float[] intensity, int leftBoundary, int rightBoundary){
        int maxIndex = rightBoundary - leftBoundary;
        xBD = new ArrayList<>();
        aBD = new ArrayList<>();
        for(int i=leftBoundary; i<= rightBoundary; i++){
            xBD.add(new BigDecimal(Float.toString(rt[i])));
            aBD.add(new BigDecimal(Float.toString(intensity[i])));
        }

        bBD = new ArrayList<>();
        cBD = new ArrayList<>();
        dBD = new ArrayList<>();
        List<BigDecimal> h = new ArrayList<>();
        List<BigDecimal> mu = new ArrayList<>();
        List<BigDecimal> z = new ArrayList<>();
        BigDecimal l;
        // do the 0'th element manually
        h.add(xBD.get(1).subtract(xBD.get(0)));
        mu.add(new BigDecimal("0"));
        z.add(new BigDecimal("0"));

        for(int i=1; i<maxIndex; i++){
            h.add(xBD.get(i + 1).subtract(xBD.get(i)));
            l = new BigDecimal("2").multiply(xBD.get(i + 1).subtract(xBD.get(i - 1))).subtract(h.get(i-1).multiply(mu.get(i-1)));
            mu.add(h.get(i).divide(l,8,BigDecimal.ROUND_HALF_UP));
            z.add((new BigDecimal("3").multiply(aBD.get(i+1).multiply(h.get(i-1)).subtract(aBD.get(i).multiply(xBD.get(i+1).subtract(xBD.get(i-1)))).add(aBD.get(i-1).multiply(h.get(i)))).divide(h.get(i-1).multiply(h.get(i)),8,BigDecimal.ROUND_HALF_UP).subtract(h.get(i-1).multiply(z.get(i-1)))).divide(l,8,BigDecimal.ROUND_HALF_UP));
        }

        for(int i=0; i<maxIndex; i++){
            cBD.add(new BigDecimal("0"));
            bBD.add(new BigDecimal("0"));
            dBD.add(new BigDecimal("0"));
        }
        cBD.add(new BigDecimal("0"));
        for(int j = maxIndex - 1; j>=0;j--){
            cBD.set(j,(z.get(j).subtract(mu.get(j).multiply(cBD.get(j+1)))));
            bBD.set(j,(aBD.get(j+1).subtract(aBD.get(j))).divide(h.get(j),8,BigDecimal.ROUND_HALF_UP).subtract(h.get(j).multiply(cBD.get(j+1).add(new BigDecimal("2").multiply(cBD.get(j)))).divide(new BigDecimal("3"),8,BigDecimal.ROUND_HALF_UP)));
            dBD.set(j,(cBD.get(j+1).subtract(cBD.get(j))).divide(new BigDecimal("3").multiply(h.get(j)),8,BigDecimal.ROUND_HALF_UP));
        }
    }

    public double evalBD(double value){
        int i = MathUtil.bisectionBD(xBD, value).getHigh();
        BigDecimal valueBD = new BigDecimal(Double.toString(value));
        if(xBD.get(i).compareTo(valueBD) > 0 || xBD.get(xBD.size()-1).compareTo(valueBD) == 0){
            --i;
        }
        BigDecimal xxBD = valueBD.subtract(xBD.get(i));
        BigDecimal result = ((dBD.get(i).multiply(xxBD).add(cBD.get(i))).multiply(xxBD).add(bBD.get(i))).multiply(xxBD).add(aBD.get(i));
        return result.doubleValue();
    }

    public double derivativesBD(double value){
        int i = MathUtil.bisectionBD(xBD, value).getHigh();

        BigDecimal valueBD = new BigDecimal(Double.toString(value));
        if(xBD.get(i).compareTo(valueBD) > 0 || xBD.get(xBD.size()-1).compareTo(valueBD) == 0){
            --i;
        }
        BigDecimal xxBD = valueBD.subtract(xBD.get(i));
        BigDecimal result = bBD.get(i).add(new BigDecimal("2").multiply(cBD.get(i)).multiply(xxBD)).add(new BigDecimal("3").multiply(dBD.get(i)).multiply(xxBD).multiply(xxBD));
        return result.doubleValue();
    }

}
