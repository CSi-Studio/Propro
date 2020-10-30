package net.csibio.propro.dao;

import net.csibio.propro.domain.query.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

public abstract class BaseDAO<T, Q extends PageQuery> {

    @Autowired
    MongoTemplate mongoTemplate;

    protected abstract String getCollectionName();

    protected abstract Class getDomainClass();

    protected abstract boolean allowSort();

    protected abstract Query buildQueryWithoutPage(Q query);

    public T getById(String id) {
        return (T) mongoTemplate.findById(id, getDomainClass(), getCollectionName());
    }

    public T getOne(Q query) {
        return (T) mongoTemplate.findOne(buildQueryWithoutPage(query), getDomainClass(), getCollectionName());
    }

    @SuppressWarnings("Carefully Using!!!")
    public List<T> getAll(Q query) {
        return mongoTemplate.find(buildQueryWithoutPage(query), getDomainClass(), getCollectionName());
    }

    public List<T> getList(Q query) {
        return mongoTemplate.find(buildQuery(query), getDomainClass(), getCollectionName());
    }

    public long count(Q query) {
        return mongoTemplate.count(buildQueryWithoutPage(query), getDomainClass(), getCollectionName());
    }

    public T insert(T t) {
        mongoTemplate.insert(t, getCollectionName());
        return t;
    }

    public List<T> insert(List<T> list) {
        mongoTemplate.insert(list, getCollectionName());
        return list;
    }

    public T update(T t) {
        mongoTemplate.save(t, getCollectionName());
        return t;
    }

    public void delete(String id) {
        Query query = new Query(where("id").is(id));
        mongoTemplate.remove(query, getDomainClass(), getCollectionName());
    }

    protected Query buildQuery(Q targetQuery) {
        Query query = buildQueryWithoutPage(targetQuery);

        query.skip((targetQuery.getPageNo() - 1) * targetQuery.getPageSize());
        query.limit(targetQuery.getPageSize());
        if (allowSort()) {
            if (targetQuery.getSortColumn() != null && targetQuery.getOrderBy() != null) {
                query.with(Sort.by(targetQuery.getOrderBy(), targetQuery.getSortColumn()));
            }
        }
        return query;
    }
}
