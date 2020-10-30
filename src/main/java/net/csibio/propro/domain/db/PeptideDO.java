package net.csibio.propro.domain.db;

import net.csibio.propro.domain.db.simple.SimplePeptide;
import net.csibio.propro.domain.BaseDO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-06 19:16
 */
@Data
@Document(collection = "peptide")
public class PeptideDO extends BaseDO {

    @Id
    String id;

    /**
     * 对应的标准库ID
     */
    @Indexed
    String libraryId;

    /**
     * 肽段的唯一识别符,格式为 : fullName_precursorCharge,如果是伪肽段,则本字段为对应的真肽段的PeptideRef
     */
    @Indexed
    String peptideRef;

    /**
     * 肽段的荷质比MZ
     */
    @Indexed
    Double mz;

    /**
     * 对应蛋白质名称
     */
    String proteinName;

    /**
     * 该肽段是否是该蛋白的不重复肽段
     */
    Boolean isUnique = true;

    /**
     * 肽段的归一化RT
     */
    Double rt;

    /**
     * 针对PRM提出，开始采集肽段的真实时间，单位：秒
     */
    Double prmRtStart;

    /**
     * 针对PRM提出，peptidelist中预计肽段出现的时间，单位：秒
     */
    Double prmRt;

    /**
     * 去除了修饰基团的肽段序列
     */
    String sequence;

    /**
     * 完整版肽段名称(含修饰基团)
     */
    String fullName;

    /**
     * 肽段带电量
     */
    Integer charge;

    /**
     * 如果是伪肽段,则本字段代表的是伪肽段中unimod的位置
     * key为unimod在肽段中的位置,位置从0开始计数,value为unimod的Id(参见unimod.obo文件)
     */
    HashMap<Integer, String> unimodMap;

    /**
     * 对应的肽段碎片的信息
     * key为cutinfo
     */
    HashMap<String, FragmentInfo> fragmentMap = new HashMap<>();

    /**
     * 伪肽段的信息
     */
    String decoySequence;
    HashMap<Integer, String> decoyUnimodMap;
    HashMap<String, FragmentInfo> decoyFragmentMap = new HashMap<>();

    /**
     * 扩展字段
     */
    String features;

    public void putFragment(String cutInfo, FragmentInfo fi){
        fragmentMap.put(cutInfo, fi);
    }

    public void putDecoyFragment(String cutInfo, FragmentInfo fi){
        decoyFragmentMap.put(cutInfo, fi);
    }

    public SimplePeptide toTargetPeptide(){
        SimplePeptide tp = new SimplePeptide();
        tp.setPeptideRef(getPeptideRef());
        tp.setRt(getRt().floatValue());
        tp.setFragmentMap(getFragmentMap());
        tp.setMz(getMz().floatValue());
        tp.setProteinName(getProteinName());
        tp.setUnimodMap(getUnimodMap());
        tp.setDecoySequence(getSequence());
        tp.setDecoyUnimodMap(getDecoyUnimodMap());
        tp.setDecoyFragmentMap(getDecoyFragmentMap());
        return tp;
    }
}
