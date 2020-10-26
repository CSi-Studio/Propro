package com.csi.propro.domain.bean.score;

import lombok.Data;

/**
 * Time: 2018-08-05 22:42
 * @author Nico Wang Ruimin
 */
@Data
public class FeatureScores extends BaseScores{

    /**
     * return scores.library_corr                     * -0.34664267 + 2
     * scores.library_norm_manhattan           *  2.98700722 + 2
     * scores.norm_rt_score                    *  7.05496384 + //0
     * scores.xcorr_coelution_score            *  0.09445371 + 1
     * scores.xcorr_shape_score                * -5.71823862 + 1
     * scores.log_sn_score                     * -0.72989582 + 1
     * scores.elution_model_fit_score          *  1.88443209; //0
     * <p>
     * <p>
     * <p>
     * scores.xcorr_coelution_score 互相关偏移的mean + std
     * scores.weighted_coelution_score 带权重的相关偏移sum
     * scores.xcorr_shape_score 互相关序列最大值的平均值
     * scores.weighted_xcorr_shape 带权重的互相关序列最大值的平均值
     * scores.log_sn_score log(距离ApexRt最近点的stn值之和)
     * <p>
     * scores.var_intensity_score 同一个peptideRef下, 所有HullPoints的intensity之和 除以 所有intensity之和
     * <p>
     * scores.library_corr //对experiment和library intensity算Pearson相关系数
     * scores.library_norm_manhattan //对experiment intensity 算平均占比差距
     * <p>
     * scores.massdev_score 按spectrum intensity加权的mz与product mz的偏差ppm百分比之和
     * scores.weighted_massdev_score 按spectrum intensity加权的mz与product mz的偏差ppm百分比按libraryIntensity加权之和
     */

    /**
     * 检测出的该峰的峰顶的rt时间
     */
    Double rt;

    //rt begin;rt end
    String rtRangeFeature;

    //定量值
    Double intensitySum;

    //HashMap --> String
    String fragIntFeature;

    Boolean thresholdPassed;

    public FeatureScores(){}

    public FeatureScores(int scoreTypesSize){
        this.scores = new Double[scoreTypesSize];
    }

}
