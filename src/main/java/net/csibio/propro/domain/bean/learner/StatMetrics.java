package net.csibio.propro.domain.bean.learner;

import lombok.Data;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-13 21:55
 */
@Data
public class StatMetrics {
    /**
     * True Positive;
     */
    double[] tp;

    /**
     * False Positive;
     */
    double[] fp;

    /**
     * True Negative;
     */
    double[] tn;

    /**
     * False Negative;
     */
    double[] fn;

    /**
     * False Positive Rate;
     */
    double[] fpr;

    /**
     * False Discovery Rate;
     */
    double[] fdr;

    /**
     * False Negative Rate;
     */
    double[] fnr;

    /**
     * True Positive Rate;
     */
    double[] svalue;

}
