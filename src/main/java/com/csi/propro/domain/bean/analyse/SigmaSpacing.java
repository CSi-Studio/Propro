package com.csi.propro.domain.bean.analyse;

public class SigmaSpacing {

    //一般默认为 30/8
    Float sigma;

    //一般默认为0.01
    Float spacing;

    double[] coeffs;

    Integer rightNum;

    Double rightNumSpacing;

    public static SigmaSpacing create() {
        SigmaSpacing sigmaSpacing = new SigmaSpacing();
        sigmaSpacing.setSigma(6.25f);
        sigmaSpacing.setSpacing(0.01f);
        return sigmaSpacing;
    }

    public SigmaSpacing() {}

    public SigmaSpacing(Float sigma, Float spacing){
        this.sigma = sigma;
        this.spacing = spacing;
    }

    public Float getSigma() {
        if (sigma == null) {
            sigma = 6.25f;
        }
        return sigma;
    }

    public Double getRightNumSpacing(){
        if(rightNumSpacing == null){
            rightNumSpacing = getRightNum()*getSpacingDouble();
        }
        return rightNumSpacing;
    }

    public Double getSigmaDouble() {
        if (sigma == null) {
            sigma = 6.25f;
        }
        return Double.parseDouble(sigma.toString());
    }

    public void setSigma(Float sigma) {
        this.sigma = sigma;
    }

    public Float getSpacing() {
        if (spacing == null) {
            spacing = 0.01f;
        }
        return spacing;
    }

    public Double getSpacingDouble() {
        if (spacing == null) {
            spacing = 0.01f;
        }
        return Double.parseDouble(spacing.toString());
    }

    public void setSpacing(Float spacing) {
        this.spacing = spacing;
    }

    public double[] getCoeffs() {
        if (coeffs == null) {
            rightNum = getRightNum(getSigmaDouble(), getSpacingDouble());
            coeffs = getCoeffs(getSigma(), getSpacingDouble(), rightNum);
        }
        return coeffs;
    }

    public int getRightNum() {
        if (rightNum == null) {
            rightNum = getRightNum(getSigmaDouble(), getSpacingDouble());
        }
        return rightNum;
    }

    private int getRightNum(double sigma, double spacing) {
        return (int) Math.ceil(4 * sigma / spacing) + 1;
    }

    private double[] getCoeffs(double sigma, double spacing, int coeffSize) {
        if(coeffs == null){
            coeffs = new double[coeffSize];
            for (int i = 0; i < coeffSize; i++) {
                coeffs[i] = (1.0 / (sigma * Math.sqrt(2.0 * Math.PI)) * Math.exp(-((i * spacing) * (i * spacing)) / (2 * sigma * sigma)));
            }
        }

        return coeffs;
    }

}
