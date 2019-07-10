package com.westlake.air.propro.dao;

import com.westlake.air.propro.domain.bean.analyse.AnalyseDataRT;
import com.westlake.air.propro.domain.bean.score.SimpleFeatureScores;
import com.westlake.air.propro.domain.db.AnalyseDataDO;
import com.westlake.air.propro.domain.db.simple.MatchedPeptide;
import com.westlake.air.propro.domain.db.simple.SimpleScores;
import com.westlake.air.propro.domain.query.AnalyseDataQuery;
import com.westlake.air.propro.utils.AnalyseUtil;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-07 20:50
 */
@Service
public class AnalyseDataDAO extends BaseDAO<AnalyseDataDO, AnalyseDataQuery>{

    public static String CollectionName = "analyseData";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return AnalyseDataDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(AnalyseDataQuery analyseDataQuery) {
        Query query = new Query();
        if (analyseDataQuery.getId() != null) {
            query.addCriteria(where("id").is(analyseDataQuery.getId()));
        }
        if (analyseDataQuery.getDataRef() != null){
            query.addCriteria(where("dataRef").is(analyseDataQuery.getDataRef()));
        }
        if (analyseDataQuery.getOverviewId() != null) {
            query.addCriteria(where("overviewId").is(analyseDataQuery.getOverviewId()));
        }
        if (analyseDataQuery.getPeptideId() != null) {
            query.addCriteria(where("peptideId").is(analyseDataQuery.getPeptideId()));
        }
        if (analyseDataQuery.getPeptideRef() != null) {
            query.addCriteria(where("peptideRef").regex(analyseDataQuery.getPeptideRef(), "i"));
        }
        if (analyseDataQuery.getProteinName() != null) {
            query.addCriteria(where("proteinName").is(analyseDataQuery.getProteinName()));
        }
        if (analyseDataQuery.getIsDecoy() != null) {
            query.addCriteria(where("isDecoy").is(analyseDataQuery.getIsDecoy()));
        }
        if (analyseDataQuery.getMzStart() != null && analyseDataQuery.getMzEnd() != null) {
            query.addCriteria(where("mz").gte(analyseDataQuery.getMzStart()).lt(analyseDataQuery.getMzEnd()));
        }
        if (analyseDataQuery.getFdrStart() != null || analyseDataQuery.getFdrEnd() != null) {
            query.addCriteria(where("fdr").gte(analyseDataQuery.getFdrStart()==null?0:analyseDataQuery.getFdrStart()).lte(analyseDataQuery.getFdrEnd()==null?1:analyseDataQuery.getFdrEnd()));
        }
        if (analyseDataQuery.getQValueStart() != null || analyseDataQuery.getQValueEnd() != null) {
            query.addCriteria(where("qValue").gte(analyseDataQuery.getQValueStart()==null?0:analyseDataQuery.getQValueStart()).lte(analyseDataQuery.getQValueEnd()==null?1:analyseDataQuery.getQValueEnd()));
        }
        if (analyseDataQuery.getIdentifiedStatus() != null) {
            query.addCriteria(where("identifiedStatus").in(analyseDataQuery.getIdentifiedStatus()));
        }
        return query;
    }

    public List<AnalyseDataDO> getAllByOverviewId(String overviewId) {
        Query query = new Query(where("overviewId").is(overviewId));
        return mongoTemplate.find(query, AnalyseDataDO.class, CollectionName);
    }

    public List<SimpleScores> getSimpleScoresByOverviewId(String overviewId){
        AnalyseDataQuery query = new AnalyseDataQuery(overviewId);
        return mongoTemplate.find(buildQueryWithoutPage(query), SimpleScores.class, CollectionName);
    }

    public List<MatchedPeptide> getAllMatchedPeptide(AnalyseDataQuery query) {
        return mongoTemplate.find(buildQueryWithoutPage(query), MatchedPeptide.class, CollectionName);
    }

    public void deleteAllByOverviewId(String overviewId) {
        Query query = new Query(where("overviewId").is(overviewId));
        mongoTemplate.remove(query, AnalyseDataDO.class, CollectionName);
    }

    public List<AnalyseDataRT> getRtList(AnalyseDataQuery query) {
        return mongoTemplate.find(buildQuery(query), AnalyseDataRT.class, CollectionName);
    }

    public void updateMulti(String overviewId, List<SimpleFeatureScores> simpleFeatureScoresList){
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, AnalyseDataDO.class);
        for (SimpleFeatureScores simpleFeatureScores : simpleFeatureScoresList) {

            Query query = new Query();
            query.addCriteria(Criteria.where("dataRef").is(AnalyseUtil.getDataRef(overviewId, simpleFeatureScores.getPeptideRef(), simpleFeatureScores.getIsDecoy())));
            Update update = new Update();
            update.set("bestRt", simpleFeatureScores.getRt());
            update.set("intensitySum", simpleFeatureScores.getIntensitySum());
            update.set("fragIntFeature", simpleFeatureScores.getFragIntFeature());
            update.set("fdr", simpleFeatureScores.getFdr());
            update.set("qValue", simpleFeatureScores.getQValue());

            if (!simpleFeatureScores.getIsDecoy()) {
                //投票策略
                if (simpleFeatureScores.getFdr() <= 0.01) {
                    update.set("identifiedStatus",AnalyseDataDO.IDENTIFIED_STATUS_SUCCESS);
                } else {
                    update.set("identifiedStatus",AnalyseDataDO.IDENTIFIED_STATUS_UNKNOWN);
                }
            }
            ops.updateOne(query, update);
        }
        ops.execute();
    }

    public void deleteMulti(String overviewId, List<SimpleFeatureScores> simpleFeatureScoresList){
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, AnalyseDataDO.class);
        for (SimpleFeatureScores simpleFeatureScores : simpleFeatureScoresList) {
            Query query = new Query();
            query.addCriteria(Criteria.where("dataRef").is(AnalyseUtil.getDataRef(overviewId, simpleFeatureScores.getPeptideRef(), simpleFeatureScores.getIsDecoy())));
            ops.remove(query);
        }
        ops.execute();
    }
}
