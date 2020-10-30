package net.csibio.propro.dao;

import net.csibio.propro.domain.db.UserDO;
import net.csibio.propro.domain.query.UserQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class UserDAO extends BaseDAO<UserDO, UserQuery>{

    public static String CollectionName = "user";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return UserDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(UserQuery targetQuery) {
        Query query = new Query();
        if (targetQuery.getId() != null) {
            query.addCriteria(where("id").is(targetQuery.getId()));
        }
        if (targetQuery.getNick() != null) {
            query.addCriteria(where("nick").is(targetQuery.getNick()));
        }
        if (targetQuery.getUsername() != null) {
            query.addCriteria(where("username").is(targetQuery.getUsername()));
        }
        if (targetQuery.getEmail() != null) {
            query.addCriteria(where("email").is(targetQuery.getEmail()));
        }
        if (targetQuery.getTelephone() != null) {
            query.addCriteria(where("telephone").is(targetQuery.getTelephone()));
        }
        if (targetQuery.getOrganization() != null) {
            query.addCriteria(where("organization").is(targetQuery.getOrganization()));
        }

        return query;
    }

    public UserDO getByUsername(String username) {
        UserQuery query = new UserQuery();
        query.setUsername(username);
        return mongoTemplate.findOne(buildQuery(query), UserDO.class, CollectionName);
    }
}
