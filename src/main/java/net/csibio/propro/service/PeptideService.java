package net.csibio.propro.service;

import net.csibio.aird.bean.WindowRange;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.db.simple.Protein;
import net.csibio.propro.domain.db.simple.SimplePeptide;
import net.csibio.propro.domain.params.CoordinateBuildingParams;
import net.csibio.propro.domain.query.PeptideQuery;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-06 19:56
 */
public interface PeptideService {

    List<PeptideDO> getAllByLibraryId(String libraryId);

    PeptideDO getByLibraryIdAndPeptideRef(String libraryId, String peptideRef);

    SimplePeptide getTargetPeptideByDataRef(String libraryId, String peptideRef);

    List<PeptideDO> getAllByLibraryIdAndProteinName(String libraryId, String proteinName);

    List<SimplePeptide> getSimpleAll(PeptideQuery query);

    Long count(PeptideQuery query);

    ResultDO<List<PeptideDO>> getList(PeptideQuery peptideQuery);

    List<PeptideDO> getAll(PeptideQuery peptideQuery);

    ResultDO insert(PeptideDO peptideDO);

    ResultDO update(PeptideDO peptideDO);

    ResultDO insertAll(List<PeptideDO> peptides, boolean isDeleteOld);

    ResultDO updateDecoyInfos(List<PeptideDO> peptides);

    ResultDO deleteAllByLibraryId(String libraryId);

    ResultDO deleteAllDecoyByLibraryId(String libraryId);

    ResultDO<PeptideDO> getById(String id);

    /**
     * 获取某一个标准库中所有的Transition的RT的取值范围
     *
     * @param libraryId
     * @return
     */
    Double[] getRTRange(String libraryId);

    ResultDO<List<Protein>> getProteinList(PeptideQuery query);

    /**
     * 计算不同蛋白质的数目
     *
     * @param libraryId
     * @return
     */
    Long countByProteinName(String libraryId);
    Long countByUniqueProteinName(String libraryId);

    /**
     * 从标准库中构建符合条件的目标肽段
     *
     * @param library
     * @param mzRange
     * @param params 创建坐标需要的参数
     * @return
     */
    List<SimplePeptide> buildCoordinates(LibraryDO library, WindowRange mzRange, CoordinateBuildingParams params);

    /**
     * 根据PeptideRef生成一个全新的PeptideDO
     * 全新的PeptideDO包含所有的a,b,c,x,y,z碎片以及对应的mz.
     * 注意:生成的靶向肽段是没有预测rt和预测intensity的
     * @param peptideRef
     * @return
     */
    PeptideDO buildWithPeptideRef(String peptideRef);

    /**
     * 根据PeptideRef生成一个全新的PeptideDO
     * 可以指定生成的a,b,c,x,y,z碎片类型 ionTypes
     * 可以指定生成的碎片的带电量种类 chargeTypes
     * 注意:生成的靶向肽段是没有预测rt和预测intensity的
     * @param peptideRef
     * @return
     */
    PeptideDO buildWithPeptideRef(String peptideRef, int minLength, List<String> ionTypes, List<Integer> chargeTypes);
}

