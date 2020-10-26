package com.csi.propro.domain.db;

import com.alibaba.fastjson.annotation.JSONField;
import com.csi.propro.domain.bean.score.FeatureScores;
import com.csi.propro.domain.BaseDO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-19 15:48
 */
@Data
@Document(collection = "analyseData")
public class AnalyseDataDO extends BaseDO {

    //鉴定成功
    public static Integer IDENTIFIED_STATUS_SUCCESS = 0;
    //不满足基础条件
    public static Integer IDENTIFIED_STATUS_NO_FIT = 1;
    //鉴定未成功
    public static Integer IDENTIFIED_STATUS_UNKNOWN = 2;
    //尚未鉴定
    public static Integer IDENTIFIED_STATUS_NOT_START = 3;

    @Id
    @JSONField(serialize=false)
    String id;

    @Indexed
    @JSONField(serialize=false)
    String overviewId;

    @Indexed
    String peptideRef;

    //是否是伪肽段
    @Indexed
    Boolean isDecoy = false;

    @Indexed
    String dataRef;

    //打分相关的字段
    @Indexed
    @JSONField(serialize=false)
    int identifiedStatus = IDENTIFIED_STATUS_NOT_START;

    //最终给出的FDR打分
    @Indexed
    @JSONField(serialize=false)
    Double fdr;

    //最终给出的qValue
    @Indexed
    @JSONField(serialize=false)
    Double qValue;

    String proteinName;

    Boolean isUnique;

    //该肽段片段的理论rt值,从标准库中冗余所得
    Float rt;

    //该肽段的前体mz,从标准库中冗余所得
    Float mz;

    //对应的标准库的peptideId
    @JSONField(serialize=false)
    String peptideId;

    //key为cutInfo, value为对应的mz
    HashMap<String, Float> mzMap = new HashMap<>();

    @JSONField(serialize=false)
    List<FeatureScores> featureScoresList;

    //最终选出的最佳峰
    @JSONField(serialize=false)
    Double bestRt;

    @JSONField(serialize=false)
    Double intensitySum;

    //最终的定量值
    @JSONField(serialize=false)
    String fragIntFeature;

    //*******************非数据库字段*******************************
    //排序后的rt,仅在解压缩的时候使用,不存入数据库
    @JSONField(serialize=false)
    @Transient
    Float[] rtArray;

    //key为cutInfo, value为对应的intensity值,仅在解压缩的时候使用,不存入数据库
    @JSONField(serialize=false)
    @Transient
    HashMap<String, Float[]> intensityMap = new HashMap<>();
}
