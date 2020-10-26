package com.csi.propro.dao;

import com.csi.propro.domain.db.TaskDO;
import com.csi.propro.domain.query.TaskQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-07 20:50
 */
@Service
public class TaskDAO extends BaseDAO<TaskDO, TaskQuery>{

    public static String CollectionName = "task";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return TaskDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(TaskQuery targetQuery) {
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
        if (targetQuery.getCreator() != null) {
            query.addCriteria(where("creator").is(targetQuery.getCreator()));
        }
        if (targetQuery.getName() != null) {
            query.addCriteria(where("name").is(targetQuery.getName()));
        }
        if (targetQuery.getOverviewId() != null) {
            query.addCriteria(where("overviewId").is(targetQuery.getOverviewId()));
        }
        if (targetQuery.getTaskTemplate() != null) {
            query.addCriteria(where("taskTemplate").is(targetQuery.getTaskTemplate()));
        }
        if (targetQuery.getStatusList() != null) {
            query.addCriteria(where("status").in(targetQuery.getStatusList()));
        }

        return query;
    }

}
