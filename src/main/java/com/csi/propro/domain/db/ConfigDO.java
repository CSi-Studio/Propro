package com.csi.propro.domain.db;

import com.csi.propro.domain.BaseDO;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-14 10:04
 */
@Data
public class ConfigDO extends BaseDO {

    String id;

    Date createDate;

    Date lastModifiedDate;

    List<String> repoUrls = new ArrayList<>();

}
