package com.csi.propro.domain.params;

import com.csi.propro.domain.bean.score.SlopeIntercept;
import com.csi.propro.domain.db.ExperimentDO;
import com.csi.propro.domain.db.LibraryDO;
import com.csi.propro.domain.db.TaskDO;
import com.csi.propro.constants.enums.ScoreType;
import com.csi.propro.domain.bean.analyse.SigmaSpacing;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-29 13:55
 */
@Data
public class WorkflowParams {

    ExperimentDO experimentDO;

    String overviewId;

    LibraryDO iRtLibrary;

    LibraryDO library;

    ExtractParams extractParams;

    /**
     * iRT求得的斜率和截距
     */
    SlopeIntercept slopeIntercept;

    SigmaSpacing sigmaSpacing = SigmaSpacing.create();

    /**
     * 流程的创建人
     */
    String ownerName;

    /**
     * 用于打分的子分数模板快照,会和AnalyseDataDO中的每一个FeatureScore中的scores对象做一一映射
     */
    List<String> scoreTypes = new ArrayList<>();

    /**
     * 是否使用DIA打分,如果使用DIA打分的话,需要提前读取Aird文件中的谱图信息以提升系统运算速度
     */
    boolean usedDIAScores = true;

    boolean uniqueOnly = false;

    /**
     * shape的筛选阈值,一般建议在0.6左右
     */
    Float xcorrShapeThreshold;
    /**
     * shape的筛选阈值,一般建议在0.8左右
     */
    Float xcorrShapeWeightThreshold;

    /**
     * 筛选的FDR值,默认值为0.01
     */
    Double fdr;

    //上下文备忘录
    String note;

    TaskDO taskDO;

    //用于PRM, <precursor mz, [rt start, rt end]>
    HashMap<Float, Float[]> rtRangeMap;

    HashMap<String, Object> resultMap = new HashMap<>();

    public WorkflowParams(){
        scoreTypes = ScoreType.getAllTypesName();
    }

}
