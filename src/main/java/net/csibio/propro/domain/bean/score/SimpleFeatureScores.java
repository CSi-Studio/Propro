package net.csibio.propro.domain.bean.score;

import lombok.Data;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-05 22:42
 */
@Data
public class SimpleFeatureScores extends BaseScores{

    String peptideRef;

    Boolean isDecoy;

    //本峰值对应的最佳RT时间
    Double rt;

    Double intensitySum;

    //本峰对应的最终综合打分
    Double mainScore;

    Double pValue;

    Double qValue;

    Double fdr;

    String fragIntFeature;

    public SimpleFeatureScores(){}

    public SimpleFeatureScores(int scoreTypesSize){
        this.scores = new Double[scoreTypesSize];
    }

    public SimpleFeatureScores(String peptideRef, Boolean isDecoy){
        this.peptideRef = peptideRef;
        this.isDecoy = isDecoy;
    }
}
