package net.csibio.propro.service;

import net.csibio.aird.bean.MzIntensityPairs;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.query.SwathIndexQuery;

import java.util.List;
import java.util.TreeMap;

public interface SwathIndexService {

    List<SwathIndexDO> getAllByExpId(String expId);

    List<SwathIndexDO> getAllMS2ByExpId(String expId);

    Long count(SwathIndexQuery query);

    ResultDO<List<SwathIndexDO>> getList(SwathIndexQuery query);

    MzIntensityPairs getNearestSpectrumByRt(TreeMap<Float, MzIntensityPairs> rtMap, Double rt);

    List<SwathIndexDO> getAll(SwathIndexQuery query);

    SwathIndexDO getSwathIndex(String expId, Float mz);

    List<SwathIndexDO> getLinkedSwathIndex(String expId, Float mz, Double deltaMz, Integer collectedNumber);

    SwathIndexDO getPrmIndex(String expId, Float mz);

    ResultDO insert(SwathIndexDO swathIndexDO);

    ResultDO update(SwathIndexDO swathIndexDO);

    ResultDO insertAll(List<SwathIndexDO> swathIndexList, boolean isDeleteOld);

    ResultDO deleteAllByExpId(String expId);

    SwathIndexDO getById(String id);
}
