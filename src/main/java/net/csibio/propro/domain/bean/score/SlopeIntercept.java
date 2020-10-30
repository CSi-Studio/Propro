package net.csibio.propro.domain.bean.score;

import lombok.Data;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-08 00:06
 */
@Data
public class SlopeIntercept {

    Double slope;

    Double intercept;

    public SlopeIntercept() {
    }

    public SlopeIntercept(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public static SlopeIntercept create() {
        SlopeIntercept si = new SlopeIntercept();
        si.setIntercept(0d);
        si.setSlope(0d);
        return si;
    }

    @Override
    public String toString() {
        return "slope:" + slope + ";intercept:" + intercept;
    }

    public String printFormula() {
        return "y=" + slope + "x" + (intercept > 0 ? ("+" + intercept) : intercept);
    }
}
