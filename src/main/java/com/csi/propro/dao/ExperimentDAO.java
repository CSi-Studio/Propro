package com.csi.propro.dao;

import com.csi.propro.domain.bean.experiment.ExpFileSize;
import com.csi.propro.domain.db.ExperimentDO;
import com.csi.propro.domain.db.simple.SimpleExperiment;
import com.csi.propro.domain.query.ExperimentQuery;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-4 13:16
 */
@Service
public class ExperimentDAO extends BaseDAO<ExperimentDO, ExperimentQuery>{

    public static String CollectionName = "experiment";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return ExperimentDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(ExperimentQuery experimentQuery) {
        Query query = new Query();
        if (experimentQuery.getId() != null) {
            query.addCriteria(where("id").is(experimentQuery.getId()));
        }
        if (experimentQuery.getName() != null) {
            query.addCriteria(where("name").regex(experimentQuery.getName()));
        }
        if (experimentQuery.getProjectId() != null) {
            query.addCriteria(where("projectId").is(experimentQuery.getProjectId()));
        }
        if (experimentQuery.getType() != null) {
            query.addCriteria(where("type").is(experimentQuery.getType()));
        }
        if (experimentQuery.getOwnerName() != null) {
            query.addCriteria(where("ownerName").is(experimentQuery.getOwnerName()));
        }
        return query;
    }

    public List<ExperimentDO> getSimpleAll() {
        Document queryDoc = new Document();
        Document fieldsDoc = new Document();
        fieldsDoc.put("id",true);
        fieldsDoc.put("name",true);
        fieldsDoc.put("filePath",true);

        Query query = new BasicQuery(queryDoc, fieldsDoc);
        return mongoTemplate.find(query, ExperimentDO.class, CollectionName);
    }

    public ExperimentDO getByName(String name) {
        ExperimentQuery query = new ExperimentQuery();
        query.setName(name);
        return mongoTemplate.findOne(buildQuery(query), ExperimentDO.class, CollectionName);
    }

    public List<SimpleExperiment> getSimpleExperimentByProjectId(String projectId) {
        ExperimentQuery query = new ExperimentQuery();
        query.setProjectId(projectId);
        return mongoTemplate.find(buildQueryWithoutPage(query), SimpleExperiment.class, CollectionName);
    }

    public List<ExpFileSize> getAllFileSizeList(String ownerName){
        ExperimentQuery query = new ExperimentQuery();
        query.setOwnerName(ownerName);
        return mongoTemplate.find(buildQueryWithoutPage(query), ExpFileSize.class, CollectionName);
    }

}
