package net.csibio.propro.dao;

import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.db.simple.Protein;
import net.csibio.propro.domain.db.simple.SimplePeptide;
import net.csibio.propro.domain.query.PeptideQuery;
import com.mongodb.BasicDBObject;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-07 20:50
 */
@Service
public class PeptideDAO extends BaseDAO<PeptideDO, PeptideQuery>{

    public static String CollectionName = "peptide";

    @Override
    protected String getCollectionName() {
        return CollectionName;
    }

    @Override
    protected Class getDomainClass() {
        return PeptideDO.class;
    }

    @Override
    protected boolean allowSort() {
        return true;
    }

    @Override
    protected Query buildQueryWithoutPage(PeptideQuery peptideQuery) {
        Query query = new Query();
        if (peptideQuery.getId() != null) {
            query.addCriteria(where("id").is(peptideQuery.getId()));
        }
        if (peptideQuery.getIsUnique() != null) {
            query.addCriteria(where("isUnique").is(peptideQuery.getIsUnique()));
        }
        if (peptideQuery.getFullName() != null) {
            query.addCriteria(where("fullName").regex(peptideQuery.getFullName(), "i"));
        }
        if (peptideQuery.getLibraryId() != null) {
            query.addCriteria(where("libraryId").is(peptideQuery.getLibraryId()));
        }
        if (peptideQuery.getSequence() != null) {
            query.addCriteria(where("sequence").regex(peptideQuery.getSequence(), "i"));
        }
        if (peptideQuery.getPeptideRef() != null) {
            query.addCriteria(where("peptideRef").is(peptideQuery.getPeptideRef()));
        }
        if (peptideQuery.getProteinName() != null) {
            query.addCriteria(where("proteinName").is(peptideQuery.getProteinName()));
        }
        if (peptideQuery.getMzStart() != null) {
            query.addCriteria(where("mz").gte(peptideQuery.getMzStart()).lt(peptideQuery.getMzEnd()));
        }
        return query;
    }


    public List<PeptideDO> getAllByLibraryId(String libraryId) {
        Query query = new Query(where("libraryId").is(libraryId));
        return mongoTemplate.find(query, PeptideDO.class, CollectionName);
    }

    public List<PeptideDO> getAllByLibraryIdAndProteinName(String libraryId, String proteinName) {
        Query query = new Query(where("libraryId").is(libraryId));
        query.addCriteria(where("proteinName").is(proteinName));
        return mongoTemplate.find(query, PeptideDO.class, CollectionName);
    }

    public PeptideDO getByLibraryIdAndPeptideRef(String libraryId, String peptideRef) {
        Query query = new Query(where("libraryId").is(libraryId));
        query.addCriteria(where("peptideRef").is(peptideRef));
        return mongoTemplate.findOne(query, PeptideDO.class, CollectionName);
    }

    public SimplePeptide getTargetPeptideByDataRef(String libraryId, String peptideRef) {
        Query query = new Query(where("libraryId").is(libraryId));
        query.addCriteria(where("peptideRef").is(peptideRef));
        return mongoTemplate.findOne(query, SimplePeptide.class, CollectionName);
    }

    public List<SimplePeptide> getSPAll(PeptideQuery query, Integer limit) {
        Query q = buildQueryWithoutPage(query);
        if(limit != null){
            q.limit(limit);
        }
        q.withHint("{'libraryId':1}");//使用libraryId作为第一优先索引
        return mongoTemplate.find(q, SimplePeptide.class, CollectionName);
    }

    public void deleteAllByLibraryId(String libraryId) {
        Query query = new Query(where("libraryId").is(libraryId));
        mongoTemplate.remove(query, PeptideDO.class, CollectionName);
    }

    public void deleteAllDecoyByLibraryId(String libraryId) {
        Query query = new Query(where("libraryId").is(libraryId));
        query.addCriteria(where("isDecoy").is(true));
        mongoTemplate.remove(query, PeptideDO.class, CollectionName);
    }

    public List<Protein> getProteinList(PeptideQuery query) {
        AggregationResults<Protein> a = mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        Protein.class,
                        Aggregation.match(where("libraryId").is(query.getLibraryId())),
                        Aggregation.group("proteinName").
                                first("proteinName").as("proteinName").
                                first("id").as("peptideId"),
                        Aggregation.skip((query.getPageNo() - 1) * query.getPageSize()),
                        Aggregation.limit(query.getPageSize())).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build()), CollectionName,
                Protein.class);
        return a.getMappedResults();
    }

    public void updateDecoyInfos(List<PeptideDO> peptideList){
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, PeptideDO.class);
        for (PeptideDO peptide : peptideList) {

            Query query = new Query();
            query.addCriteria(Criteria.where("id").is(peptide.getId()));
            Update update = new Update();
            update.set("decoySequence", peptide.getDecoySequence());
            update.set("decoyUnimodMap", peptide.getDecoyUnimodMap());
            update.set("decoyFragmentMap", peptide.getDecoyFragmentMap());

            ops.updateOne(query, update);
        }
        ops.execute();
    }

    public long countByProteinName(String libraryId) {
        AggregationResults<BasicDBObject> a = mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        PeptideDO.class,
                        Aggregation.match(where("libraryId").is(libraryId)),
                        Aggregation.group("proteinName").count().as("count")).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build()), CollectionName,
                BasicDBObject.class);
        return (long) a.getMappedResults().size();
    }

    public long countByUniqueProteinName(String libraryId) {
        AggregationResults<BasicDBObject> a = mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        PeptideDO.class,
                        Aggregation.match(where("libraryId").is(libraryId)),
                        Aggregation.match(where("isUnique").is(true)),
                        Aggregation.group("proteinName").count().as("count")).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build()), CollectionName,
                BasicDBObject.class);
        return (long) a.getMappedResults().size();
    }

    public long countByPeptideRef(String libraryId) {
        AggregationResults<BasicDBObject> a = mongoTemplate.aggregate(
                Aggregation.newAggregation(
                        PeptideDO.class,
                        Aggregation.match(where("libraryId").is(libraryId)),
                        Aggregation.group("peptideRef").count().as("count")).withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build()), CollectionName,
                BasicDBObject.class);

        return (long) a.getMappedResults().size();
    }
}
