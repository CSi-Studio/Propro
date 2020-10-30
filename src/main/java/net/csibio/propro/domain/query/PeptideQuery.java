package net.csibio.propro.domain.query;

import lombok.Data;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-06 19:16
 */
@Data
public class PeptideQuery extends PageQuery {

    String id;

    String libraryId;

    /**
     * 前体的荷质比MZ
     */
    Double mz;

    /**
     * 归一化RT
     */
    Double rt;

    /**
     * 是否是该蛋白对应的具有唯一性的肽段
     */
    Boolean isUnique;

    /**
     * 对应肽段序列
     */
    String sequence;

    /**
     * 对应蛋白质名称
     */
    String proteinName;

    /**
     * 肽段
     */
    String peptideRef;

    /**
     * 完整版肽段名称(含修饰基团)
     */
    String fullName;

    Double mzStart;

    Double mzEnd;

    public PeptideQuery() {
    }

    public PeptideQuery(String libraryId) {
        this.libraryId = libraryId;
    }

    public PeptideQuery(String libraryId, String peptideRef) {
        this.libraryId = libraryId;
        this.peptideRef = peptideRef;
    }

    public PeptideQuery(int pageNo, int pageSize){
        super(pageNo, pageSize);
    }
}
