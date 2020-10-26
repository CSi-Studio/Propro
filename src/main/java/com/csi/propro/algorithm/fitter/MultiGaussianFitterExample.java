package com.csi.propro.algorithm.fitter;

import com.alibaba.fastjson.JSON;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nico Wang
 * Time: 2018-12-19 19:40
 */
public class MultiGaussianFitterExample {

    public static final Logger logger = LoggerFactory.getLogger(MultiGaussianFitterExample.class);

    public static void main(String args[]) {
        double[] time = new double[]{440, 442.255, 442.26, 442.265, 442.27, 442.275, 442.28, 442.285, 442.29, 442.295, 442.3, 442.306, 442.311, 442.316};
        double[] intensity = new double[]{12000, 6879.5, 20147.9, 37398.6, 73930.4, 119117, 156101.7, 131480.3, 78947.5, 27729.5, 1630.5, 2305.9, 4962.2, 3866.3};
        // Collect data.
        final WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i = 0; i < time.length; i++) {
            obs.add(time[i], intensity[i]);
        }

        final MultiGaussianFitter fitter = MultiGaussianFitter.create();

        long startTime = System.currentTimeMillis();
        final double[] coeff = fitter.fit(obs.toList());
        logger.info("time:" + (System.currentTimeMillis() - startTime));

        List<Double> mean = new ArrayList<>();
        List<Double> norm = new ArrayList<>();
        List<Double> sigma = new ArrayList<>();
        for (int i = 0; i < coeff.length / 3; i++) {
            norm.add(coeff[3 * i]);
            mean.add(coeff[3 * i + 1]);
            sigma.add(coeff[3 * i + 2]);
        }
        logger.info("norm: " + JSON.toJSON(norm));
        logger.info("mean: " + JSON.toJSON(mean));
        logger.info("sigma: " + JSON.toJSON(sigma));
        for (int i = 0; i < coeff.length / 3; i++) {
            for (WeightedObservedPoint point : obs.toList()) {
                logger.info(coeff[3 * i] * Math.exp(-Math.pow(point.getX() - coeff[3 * i + 1], 2) / (2 * coeff[3 * i + 2] * coeff[3 * i + 2])) + "");
            }
            logger.info("");
        }
        logger.info(JSON.toJSON(coeff).toString());
    }

}





