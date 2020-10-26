package com.csi.propro.dao;

import com.csi.propro.domain.db.SwathIndexDO;
import com.csi.propro.domain.query.SwathIndexQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class SwathIndexDAO extends BaseDAO<SwathIndexDO, SwathIndexQuery>{

    public static String CollectionName = "swathIndex";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return SwathIndexDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(SwathIndexQuery swathIndexQuery) {
        Query query = new Query();
        if (swathIndexQuery.getExpId() != null) {
            query.addCriteria(where("expId").is(swathIndexQuery.getExpId()));
        }
        if (swathIndexQuery.getId() != null) {
            query.addCriteria(where("id").is(swathIndexQuery.getId()));
        }
        if (swathIndexQuery.getLevel() != null) {
            query.addCriteria(where("level").is(swathIndexQuery.getLevel()));
        }
        if (swathIndexQuery.getMzStart() != null) {
            query.addCriteria(where("range.start").is(swathIndexQuery.getMzStart()));
        }
        if (swathIndexQuery.getMzEnd() != null) {
            query.addCriteria(where("range.end").is(swathIndexQuery.getMzEnd()));
        }
        if (swathIndexQuery.getMz() != null) {
            query.addCriteria(where("range.start").lte(swathIndexQuery.getMz()));
            query.addCriteria(where("range.end").gte(swathIndexQuery.getMz()));
        }

        return query;
    }

    public List<SwathIndexDO> getAllByExpId(String expId) {
        Query query = new Query(where("expId").is(expId));
        return mongoTemplate.find(query, SwathIndexDO.class, CollectionName);
    }

    public List<SwathIndexDO> getAllMS2ByExpId(String expId) {
        Query query = new Query();
        query.addCriteria(where("expId").is(expId));
        query.addCriteria(where("level").is(2));

        return mongoTemplate.find(query, SwathIndexDO.class, CollectionName);
    }

    public void deleteAllByExpId(String expId) {
        Query query = new Query(where("expId").is(expId));
        mongoTemplate.remove(query, SwathIndexDO.class, CollectionName);
    }
}
