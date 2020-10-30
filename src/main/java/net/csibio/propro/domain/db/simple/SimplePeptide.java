package net.csibio.propro.domain.db.simple;

import net.csibio.propro.domain.db.FragmentInfo;
import net.csibio.propro.domain.db.PeptideDO;
import lombok.Data;

import java.util.HashMap;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-17 10:16
 * 具体的注释说明请参考PeptideDO类
 */
@Data
public class SimplePeptide {

    String id;

    String proteinName;

    String peptideRef;

    /**
     * 对应肽段序列,如果是伪肽段,则为对应的伪肽段的序列(不包含UniModId)
     */
    String sequence;

    /**
     * 对应的前体荷质比
     */
    float mz;

    HashMap<String, FragmentInfo> fragmentMap;

    /**
     * 是否在蛋白中是unique类型的肽段
     */
    Boolean isUnique;

    float rt;

    /**
     * 如果是伪肽段,则本字段代表的是伪肽段中unimod的位置
     * key为unimod在肽段中的位置,位置从0开始计数,value为unimod的Id(参见unimod.obo文件)
     */
    HashMap<Integer, String> unimodMap;

    /**
     * 伪肽段的信息
     */
    String decoySequence;
    HashMap<Integer, String> decoyUnimodMap;
    HashMap<String, FragmentInfo> decoyFragmentMap;

    /**
     * 是否作为伪肽段存在,不存储到数据库中
     */
    boolean asDecoy = false;
    /**
     * rtStart是在计算时使用的,并不会存在数据库中
     */
    float rtStart;
    /**
     * rtEnd是在计算时使用的,并不会存在数据库中
     */
    float rtEnd;


    public SimplePeptide() {
    }

    public SimplePeptide(PeptideDO peptide) {
        this.id = peptide.getId();
        this.proteinName = peptide.getProteinName();
        this.peptideRef = peptide.getPeptideRef();
        this.mz = peptide.getMz().floatValue();
        this.fragmentMap = peptide.getFragmentMap();
        this.rt = peptide.getRt().floatValue();
        this.decoySequence = peptide.getDecoySequence();
        this.decoyUnimodMap = peptide.getDecoyUnimodMap();
        this.decoyFragmentMap = peptide.getDecoyFragmentMap();
    }

    public HashMap<String, FragmentInfo> getFragmentMap() {
        return asDecoy ? decoyFragmentMap : fragmentMap;
    }

    public HashMap<Integer, String> getUnimodMap() {
        return asDecoy ? decoyUnimodMap : unimodMap;
    }

    public String getSequence() {
        return asDecoy ? decoySequence : sequence;
    }

    //根据自身构建IntensityMap,key为cutInfo,value为对应的Intensity值
    public HashMap<String, Float> buildIntensityMap() {
        HashMap<String, Float> intensityMap = new HashMap<>();
        HashMap<String, FragmentInfo> tempFragmentMap = getFragmentMap();
        for (String cutInfo : tempFragmentMap.keySet()) {
            intensityMap.put(cutInfo, tempFragmentMap.get(cutInfo).getIntensity().floatValue());
        }
        return intensityMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof SimplePeptide) {
            SimplePeptide target = (SimplePeptide) obj;
            if (this.getPeptideRef().equals(target.getPeptideRef())) {
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (peptideRef).hashCode();
    }
}
