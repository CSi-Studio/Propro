package com.csi.propro.domain.query;

import lombok.Data;
import org.springframework.data.domain.Sort;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-19 16:34
 */
@Data
public class AnalyseOverviewQuery extends PageQuery {

    String id;

    String expId;

    String ownerName;

    String libraryId;

    public AnalyseOverviewQuery(){}

    public AnalyseOverviewQuery(int pageNo, int pageSize, Sort.Direction direction, String sortColumn){
        super(pageNo, pageSize, direction, sortColumn);
    }
}
