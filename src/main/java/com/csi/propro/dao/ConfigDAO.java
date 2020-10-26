package com.csi.propro.dao;

import com.csi.propro.domain.db.ConfigDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-14 10:08
 */
@Service
public class ConfigDAO {

    public static String CollectionName = "config";

    public final Logger logger = LoggerFactory.getLogger(ConfigDAO.class);

    @Autowired
    MongoTemplate mongoTemplate;

    public ConfigDO getConfig() {
        ConfigDO configDO = mongoTemplate.findOne(new Query(), ConfigDO.class, CollectionName);

        if (configDO == null) {
            configDO = new ConfigDO();
            configDO.setCreateDate(new Date());
            configDO.setLastModifiedDate(new Date());
            addConfig(configDO);
        }

        return configDO;
    }

    public Boolean addConfig(ConfigDO configDO) {
        try {
            mongoTemplate.insert(configDO, CollectionName);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public Boolean updateConfig(ConfigDO configDO) {
        if (configDO.getId() == null || configDO.getId().isEmpty()) {
            logger.error("Config Id cannot been empty");
            return false;
        }
        try {
            mongoTemplate.save(configDO, CollectionName);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
