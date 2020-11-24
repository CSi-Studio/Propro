package net.csibio.propro.service.impl;

import net.csibio.aird.bean.MzIntensityPairs;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.dao.SwathIndexDAO;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.domain.query.SwathIndexQuery;
import net.csibio.propro.service.SwathIndexService;
import net.csibio.propro.utils.ArrayUtil;
import net.csibio.propro.utils.ConvolutionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Service("swathIndexService")
public class SwathIndexServiceImpl implements SwathIndexService {

    public final Logger logger = LoggerFactory.getLogger(SwathIndexServiceImpl.class);

    @Autowired
    SwathIndexDAO swathIndexDAO;

    @Override
    public List<SwathIndexDO> getAllByExpId(String expId) {
        return swathIndexDAO.getAllByExpId(expId);
    }

    @Override
    public List<SwathIndexDO> getAllMS2ByExpId(String expId) {
        return swathIndexDAO.getAllMS2ByExpId(expId);
    }

    @Override
    public Long count(SwathIndexQuery query) {
        return swathIndexDAO.count(query);
    }

    @Override
    public ResultDO<List<SwathIndexDO>> getList(SwathIndexQuery query) {
        List<SwathIndexDO> indexList = swathIndexDAO.getList(query);
        long totalCount = swathIndexDAO.count(query);
        ResultDO<List<SwathIndexDO>> resultDO = new ResultDO<>(true);
        resultDO.setModel(indexList);
        resultDO.setTotalNum(totalCount);
        resultDO.setPageSize(query.getPageSize());
        return resultDO;
    }

    @Override
    public MzIntensityPairs getNearestSpectrumByRt(TreeMap<Float, MzIntensityPairs> rtMap, Double rt) {
        float[] fArray = ArrayUtil.toPrimitive(rtMap.keySet());
        int rightIndex = ConvolutionUtil.findRightIndex(fArray, rt.floatValue());
        int finalIndex = rightIndex;
        if (rightIndex == -1) {
            //Max value in fArray is less than rt. The max index of fArray is the nearest index.
            finalIndex = fArray.length - 1;
        } else if (rightIndex != 0 && (fArray[rightIndex] - rt) > (fArray[rightIndex - 1] - rt)) {
            //if rightIndex == 0, finalIndex == 0
            finalIndex = rightIndex - 1;
        }

        return rtMap.get(fArray[finalIndex]);
    }

    @Override
    public List<SwathIndexDO> getAll(SwathIndexQuery query) {
        return swathIndexDAO.getAll(query);
    }

    @Override
    public SwathIndexDO getSwathIndex(String expId, Float mz) {
        SwathIndexQuery query = new SwathIndexQuery(expId, 2);
        query.setMz(mz);
        return swathIndexDAO.getOne(query);
    }

    /**
     * 本函数用于ScanningSwath的数据解析,deltaMz是ScanningSwath的窗口宽度,CollectedNumber是需要获取的相邻的Swath窗口的数目
     * 例如CollectedNumber=3,则意味着需要获取向上向下各3个窗口的数据,总计额外获取6个窗口的数据
     *
     * @param expId
     * @param mz
     * @param deltaMz
     * @param collectedNumber
     * @return
     */
    @Override
    public List<SwathIndexDO> getLinkedSwathIndex(String expId, Float mz, Double deltaMz, Integer collectedNumber) {
        List<SwathIndexDO> swathList = new ArrayList<>();
        SwathIndexDO index0 = getSwathIndex(expId, mz);
        swathList.add(index0);
        for (int i = 1; i <= collectedNumber; i++) {
            SwathIndexDO index1 = getSwathIndex(expId, (float)(mz - deltaMz * i));
            if (index1 != null) {
                swathList.add(index1);
            }
            SwathIndexDO index2 = getSwathIndex(expId, (float)(mz + deltaMz * i));
            if (index2 != null) {
                swathList.add(index2);
            }
        }

        return swathList;
    }

    @Override
    public SwathIndexDO getPrmIndex(String expId, Float mz) {
        SwathIndexQuery query = new SwathIndexQuery(expId, 2);
        query.setMz(mz);
        List<SwathIndexDO> swathIndexDOList = swathIndexDAO.getAll(query);
        double minDeltaMz = Double.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < swathIndexDOList.size(); i++) {
            double deltaMz = Math.abs(swathIndexDOList.get(i).getRange().getMz() - mz);
            if (deltaMz < minDeltaMz) {
                minDeltaMz = deltaMz;
                minIndex = i;
            }
        }
        return swathIndexDOList.get(minIndex);
    }

    @Override
    public ResultDO insert(SwathIndexDO swathIndexDO) {
        try {
            swathIndexDAO.insert(swathIndexDO);
            return new ResultDO(true);
        } catch (Exception e) {
            ResultDO resultDO = new ResultDO();
            return resultDO.setErrorResult(ResultCode.INSERT_ERROR.getMessage(), e.getMessage());
        }
    }

    @Override
    public ResultDO update(SwathIndexDO swathIndexDO) {
        try {
            swathIndexDAO.update(swathIndexDO);
            return new ResultDO(true);
        } catch (Exception e) {
            ResultDO resultDO = new ResultDO();
            return resultDO.setErrorResult(ResultCode.UPDATE_ERROR.getMessage(), e.getMessage());
        }
    }

    @Override
    public ResultDO insertAll(List<SwathIndexDO> swathIndexList, boolean isDeleteOld) {
        if (swathIndexList == null || swathIndexList.size() == 0) {
            return ResultDO.buildError(ResultCode.OBJECT_CANNOT_BE_NULL);
        }
        try {
            if (isDeleteOld) {
                swathIndexDAO.deleteAllByExpId(swathIndexList.get(0).getExpId());
            }
            swathIndexDAO.insert(swathIndexList);
            return new ResultDO(true);
        } catch (Exception e) {
            return ResultDO.buildError(ResultCode.INSERT_ERROR);
        }
    }

    @Override
    public ResultDO deleteAllByExpId(String expId) {
        try {
            swathIndexDAO.deleteAllByExpId(expId);
            return new ResultDO(true);
        } catch (Exception e) {
            return ResultDO.buildError(ResultCode.DELETE_ERROR);
        }
    }

    @Override
    public SwathIndexDO getById(String id) {
        try {
            return swathIndexDAO.getById(id);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return null;
        }
    }
}
