package com.westlake.air.pecs.service;

import com.westlake.air.pecs.domain.ResultDO;
import com.westlake.air.pecs.domain.bean.score.SlopeIntercept;
import com.westlake.air.pecs.domain.db.AnalyseDataDO;
import com.westlake.air.pecs.domain.db.ExperimentDO;
import com.westlake.air.pecs.domain.db.TaskDO;

import java.util.List;

public interface ScoreService {

    /**
     * 从一个已经卷积完毕的数据集中求出iRT
     *
     * @param overviewId
     * @param iRtLibraryId
     * @param sigma        通常为30
     * @param space        通常为0.01
     * @param taskDO
     * @return
     */
    ResultDO<SlopeIntercept> computeIRt(String overviewId, String iRtLibraryId, Float sigma, Float space, TaskDO taskDO);

    /**
     * 从一个卷积结果列表中求出iRT
     *
     * @param dataList
     * @param iRtLibraryId
     * @param sigma        通常为30
     * @param space        通常为0.01
     * @return
     */
    ResultDO<SlopeIntercept> computeIRt(List<AnalyseDataDO> dataList, String iRtLibraryId, Float sigma, Float space);

    /**
     * 打分
     * @param overviewId
     * @param sigma
     * @param spacing
     * @param taskDO
     */
    void score(String overviewId, Float sigma, Float spacing, TaskDO taskDO);

}
