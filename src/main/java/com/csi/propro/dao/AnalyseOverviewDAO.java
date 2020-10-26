package com.csi.propro.dao;

import com.csi.propro.domain.db.AnalyseOverviewDO;
import com.csi.propro.domain.query.AnalyseOverviewQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-07 20:50
 */
@Service
public class AnalyseOverviewDAO extends BaseDAO<AnalyseOverviewDO, AnalyseOverviewQuery>{

    public static String CollectionName = "analyseOverview";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return AnalyseOverviewDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(AnalyseOverviewQuery targetQuery) {
        Query query = new Query();
        if (targetQuery.getId() != null) {
            query.addCriteria(where("id").is(targetQuery.getId()));
        }
        if (targetQuery.getExpId() != null) {
            query.addCriteria(where("expId").is(targetQuery.getExpId()));
        }
        if (targetQuery.getLibraryId() != null) {
            query.addCriteria(where("libraryId").is(targetQuery.getLibraryId()));
        }
        if (targetQuery.getOwnerName() != null) {
            query.addCriteria(where("ownerName").is(targetQuery.getOwnerName()));
        }

        return query;
    }

    public List<AnalyseOverviewDO> getAllByExperimentId(String expId) {
        Query query = new Query(where("expId").is(expId));
        return mongoTemplate.find(query, AnalyseOverviewDO.class, CollectionName);
    }

    public AnalyseOverviewDO getFirstByExperimentId(String expId) {
        Query query = new Query(where("expId").is(expId));
        query.limit(1);
        List<AnalyseOverviewDO> list = mongoTemplate.find(query, AnalyseOverviewDO.class, CollectionName);
        if (list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public void deleteAllByExperimentId(String expId) {
        Query query = new Query(where("expId").is(expId));
        mongoTemplate.remove(query, AnalyseOverviewDO.class, CollectionName);
    }

}
