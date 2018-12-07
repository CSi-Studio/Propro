package com.westlake.air.pecs.domain.db;

import com.westlake.air.pecs.domain.bean.transition.Annotation;
import lombok.Data;

@Data
public class FragmentInfo {

    String cutInfo;

    /**
     * 离子片段的荷质比MZ(Mono荷质比)
     */
    Double mz;

    Double intensity;

    /**
     * 离子片段的带电量
     */
    Integer charge;

    /**
     * 新字段,仅记录第一种切割类型的信息
     */
    Annotation annotation;

    /**
     * 注释,see http://tools.proteomecenter.org/wiki/index.php?title=Software:SpectraST#Annotation_syntax:
     */
    String annotations;

}