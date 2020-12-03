package net.csibio.propro.service.impl;

import net.csibio.aird.bean.WindowRange;
import net.csibio.propro.domain.bean.score.SlopeIntercept;
import net.csibio.propro.domain.db.simple.Protein;
import net.csibio.propro.algorithm.formula.FormulaCalculator;
import net.csibio.propro.algorithm.formula.FragmentFactory;
import net.csibio.propro.constants.ResidueType;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.dao.LibraryDAO;
import net.csibio.propro.dao.PeptideDAO;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.db.simple.SimplePeptide;
import net.csibio.propro.domain.params.CoordinateBuildingParams;
import net.csibio.propro.domain.query.PeptideQuery;
import net.csibio.propro.service.ExperimentService;
import net.csibio.propro.service.PeptideService;
import net.csibio.propro.service.TaskService;
import net.csibio.propro.utils.PeptideUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static net.csibio.propro.constants.ExpTypeConst.*;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-06 20:02
 */
@Service("peptideService")
public class PeptideServiceImpl implements PeptideService {

    public final Logger logger = LoggerFactory.getLogger(PeptideServiceImpl.class);

    public static Pattern pattern = Pattern.compile("/\\(.*\\)/");
    @Autowired
    PeptideDAO peptideDAO;
    @Autowired
    LibraryDAO libraryDAO;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    TaskService taskService;
    @Autowired
    FragmentFactory fragmentFactory;
    @Autowired
    FormulaCalculator formulaCalculator;

    @Override
    public List<PeptideDO> getAllByLibraryId(String libraryId) {
        return peptideDAO.getAllByLibraryId(libraryId);
    }

    @Override
    public PeptideDO getByLibraryIdAndPeptideRef(String libraryId, String peptideRef) {
        return peptideDAO.getByLibraryIdAndPeptideRef(libraryId, peptideRef);
    }

    @Override
    public SimplePeptide getTargetPeptideByDataRef(String libraryId, String peptideRef) {
        return peptideDAO.getTargetPeptideByDataRef(libraryId, peptideRef);
    }

    @Override
    public List<PeptideDO> getAllByLibraryIdAndProteinName(String libraryId, String proteinName) {
        return peptideDAO.getAllByLibraryIdAndProteinName(libraryId, proteinName);
    }

    @Override
    public List<SimplePeptide> getSimpleAll(PeptideQuery query) {
        return peptideDAO.getSPAll(query, null);
    }

    @Override
    public Long count(PeptideQuery query) {
        return peptideDAO.count(query);
    }

    @Override
    public ResultDO<List<PeptideDO>> getList(PeptideQuery query) {

        List<PeptideDO> peptideDOList = peptideDAO.getList(query);
        long totalCount = peptideDAO.count(query);
        ResultDO<List<PeptideDO>> resultDO = new ResultDO<>(true);
        resultDO.setModel(peptideDOList);
        resultDO.setTotalNum(totalCount);
        resultDO.setPageSize(query.getPageSize());
        return resultDO;
    }

    @Override
    public List<PeptideDO> getAll(PeptideQuery query) {
        return peptideDAO.getAll(query);
    }

    @Override
    public ResultDO insert(PeptideDO peptideDO) {
        try {
            peptideDAO.insert(peptideDO);
            return new ResultDO(true);
        } catch (Exception e) {
            return ResultDO.buildError(ResultCode.INSERT_ERROR);
        }
    }

    @Override
    public ResultDO update(PeptideDO peptideDO) {
        try {
            peptideDAO.update(peptideDO);
            return ResultDO.build(peptideDO);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.UPDATE_ERROR);
        }
    }

    /**
     * 这边的代码由于时间问题写的比较简陋,先删除原有的关联数据,再插入新的关联数据,未做事务处理
     *
     * @param peptides
     * @param isDeleteOld
     * @return
     */
    @Override
    public ResultDO insertAll(List<PeptideDO> peptides, boolean isDeleteOld) {
        if (peptides == null || peptides.size() == 0) {
            return ResultDO.buildError(ResultCode.OBJECT_CANNOT_BE_NULL);
        }
        try {
            if (isDeleteOld) {
                peptideDAO.deleteAllByLibraryId(peptides.get(0).getLibraryId());
            }
            peptideDAO.insert(peptides);
            return new ResultDO(true);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResultDO.buildError(ResultCode.INSERT_ERROR);
        }
    }

    @Override
    public ResultDO updateDecoyInfos(List<PeptideDO> peptides) {
        peptideDAO.updateDecoyInfos(peptides);
        return new ResultDO(true);
    }

    @Override
    public ResultDO deleteAllByLibraryId(String libraryId) {
        try {
            peptideDAO.deleteAllByLibraryId(libraryId);
            return new ResultDO(true);
        } catch (Exception e) {
            return ResultDO.buildError(ResultCode.DELETE_ERROR);
        }
    }

    @Override
    public ResultDO deleteAllDecoyByLibraryId(String libraryId) {
        try {
            peptideDAO.deleteAllDecoyByLibraryId(libraryId);
            return new ResultDO(true);
        } catch (Exception e) {
            return ResultDO.buildError(ResultCode.DELETE_ERROR);
        }
    }

    @Override
    public ResultDO<PeptideDO> getById(String id) {
        try {
            PeptideDO peptideDO = peptideDAO.getById(id);
            if (peptideDO == null) {
                return ResultDO.buildError(ResultCode.OBJECT_NOT_EXISTED);
            } else {
                ResultDO<PeptideDO> resultDO = new ResultDO<>(true);
                resultDO.setModel(peptideDO);
                return resultDO;
            }
        } catch (Exception e) {
            return ResultDO.buildError(ResultCode.QUERY_ERROR);
        }
    }

    @Override
    public Double[] getRTRange(String libraryId) {
        Double[] range = new Double[2];
        PeptideQuery query = new PeptideQuery(libraryId);
        query.setPageSize(1);
        query.setOrderBy(Sort.Direction.ASC);
        query.setSortColumn("rt");
        List<PeptideDO> descList = peptideDAO.getList(query);
        if (descList != null && descList.size() == 1) {
            range[0] = descList.get(0).getRt();
        }
        query.setOrderBy(Sort.Direction.DESC);
        List<PeptideDO> ascList = peptideDAO.getList(query);
        if (ascList != null && ascList.size() == 1) {
            range[1] = ascList.get(0).getRt();
        }
        return range;
    }

    @Override
    public ResultDO<List<Protein>> getProteinList(PeptideQuery query) {
        LibraryDO libraryDO = libraryDAO.getById(query.getLibraryId());
        List<Protein> proteins = peptideDAO.getProteinList(query);
        ResultDO<List<Protein>> resultDO = new ResultDO<>(true);
        resultDO.setModel(proteins);
        resultDO.setTotalNum(libraryDO.getProteinCount());
        resultDO.setPageSize(query.getPageSize());
        return resultDO;
    }

    @Override
    public Long countByProteinName(String libraryId) {
        return peptideDAO.countByProteinName(libraryId);
    }

    @Override
    public Long countByUniqueProteinName(String libraryId) {
        return peptideDAO.countByUniqueProteinName(libraryId);
    }

    @Override
    public List<SimplePeptide> buildCoordinates(LibraryDO library, WindowRange mzRange, CoordinateBuildingParams params) {

        long start = System.currentTimeMillis();
        PeptideQuery query = new PeptideQuery(library.getId());
        float precursorMz = mzRange.getMz().floatValue();
        switch (params.getType()){
            case PRM:
                query.setMzStart(precursorMz - 0.0006d);
                query.setMzEnd(precursorMz + 0.0006d);
                break;
            case DIA_SWATH:
            case SCANNING_SWATH:
                query.setMzStart(mzRange.getStart());
                query.setMzEnd(mzRange.getEnd());
                break;
            default:
                break;
        }

        query.setIsUnique(params.isUniqueCheck()?true:null);

        List<SimplePeptide> targetList = peptideDAO.getSPAll(query, params.getLimit());
        if (params.getType().equals(PRM)) {
            prmFilter(targetList, params.getRtExtractionWindows(), params.getSlopeIntercept(), params.getRtRange(), precursorMz);
        }

        long dbReadTime = System.currentTimeMillis() - start;
        if (-1 != params.getRtExtractionWindows()) {
            for (SimplePeptide simplePeptide : targetList) {
                float iRt = (simplePeptide.getRt() - params.getSlopeIntercept().getIntercept().floatValue()) / params.getSlopeIntercept().getSlope().floatValue();
                simplePeptide.setRtStart(iRt - params.getRtExtractionWindows() / 2.0f);
                simplePeptide.setRtEnd(iRt + params.getRtExtractionWindows() / 2.0f);
            }
        } else {
            for (SimplePeptide simplePeptide : targetList) {
                simplePeptide.setRtStart(-1);
                simplePeptide.setRtEnd(99999);
            }
        }

        logger.info("构建提取XIC的MS2坐标,总计" + targetList.size() + "条记录,读取标准库耗时:" + dbReadTime + "毫秒");
        return targetList;
    }

    @Override
    public PeptideDO buildWithPeptideRef(String peptideRef) {
        List<Integer> chargeTypes = new ArrayList<>();
        //默认采集所有离子碎片,默认采集[1,precusor charge]区间内的整数
        if (peptideRef.contains("_")) {
            int charge = Integer.parseInt(peptideRef.split("_")[1]);
            for (int i = 1; i <= charge; i++) {
                chargeTypes.add(i);
            }
        } else {
            chargeTypes.add(1);
            chargeTypes.add(2);
        }
        return buildWithPeptideRef(peptideRef, 3, ResidueType.abcxyz, chargeTypes);
    }

    @Override
    public PeptideDO buildWithPeptideRef(String peptideRef, int minLength, List<String> ionTypes, List<Integer> chargeTypes) {
        int charge;
        String fullName;

        if (peptideRef.contains("_")) {
            String[] peptideInfos = peptideRef.split("_");
            charge = Integer.parseInt(peptideInfos[1]);
            fullName = peptideInfos[0];
        } else {
            charge = 1;
            fullName = peptideRef;
        }

        PeptideDO peptide = new PeptideDO();
        peptide.setFullName(fullName);
        peptide.setCharge(charge);
        peptide.setSequence(fullName.replaceAll("\\([^)]+\\)", ""));
        HashMap<Integer, String> unimodMap = PeptideUtil.parseModification(fullName);
        peptide.setUnimodMap(unimodMap);
        peptide.setMz(formulaCalculator.getMonoMz(peptide.getSequence(), ResidueType.Full, charge, 0, 0, false, new ArrayList<>(unimodMap.values())));
        peptide.setPeptideRef(peptideRef);
        peptide.setRt(-1d);

        peptide.setFragmentMap(fragmentFactory.buildFragmentMap(peptide, minLength, ionTypes, chargeTypes));
        return peptide;
    }

    private void prmFilter(List<SimplePeptide> targetList, float rtExtractionWindows, SlopeIntercept slopeIntercept, Float[] rtRange, float precursorMz) {
        if (!targetList.isEmpty() && targetList.size() != 2) {
            //PRM模式下, rtRange不为空;
            SimplePeptide bestTarget = null;
            float mzDistance = Float.MAX_VALUE;
            for (SimplePeptide peptide : targetList) {
                if (rtExtractionWindows != -1) {
                    float iRt = (peptide.getRt() - slopeIntercept.getIntercept().floatValue()) / slopeIntercept.getSlope().floatValue();
                    if (iRt < rtRange[0] - 30 || iRt > rtRange[1] + 30) {
                        continue;
                    }
                }
                float tempMzDistance = Math.abs(peptide.getMz() - precursorMz);
                if (tempMzDistance <= mzDistance) {
                    bestTarget = peptide;
                    mzDistance = tempMzDistance;
                }
            }
            targetList.clear();
            if (bestTarget != null) {
                targetList.add(bestTarget);
            }
            if (mzDistance >= 0.0002f && bestTarget != null) {
                logger.info("Coordinate: " + bestTarget.getPeptideRef() + " " + mzDistance);
            }
        }
    }
}
