package net.csibio.propro.domain.db;

import net.csibio.propro.domain.bean.peptide.Annotation;
import lombok.Data;

@Data
public class FragmentInfo {

    /**
     * format : y3^2
     */
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

    public FragmentInfo(){}

    public FragmentInfo(String cutInfo, Double mz, Integer charge){
        this.cutInfo = cutInfo;
        this.mz = mz;
        this.charge = charge;
    }

    public FragmentInfo(String cutInfo, Double mz, Double intensity, Integer charge){
        this.cutInfo = cutInfo;
        this.mz = mz;
        this.intensity = intensity;
        this.charge = charge;
    }

}
