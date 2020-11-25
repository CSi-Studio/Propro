package net.csibio.propro.domain.query;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class ProjectQuery extends PageQuery {

    String id;

    String name;

    //项目负责人名称
    String creator;

    Boolean doPublic;

    public ProjectQuery(){}

    public ProjectQuery(String creator){
        this.creator = creator;
    }

    public ProjectQuery(int pageNo, int pageSize, Sort.Direction direction, String sortColumn){
        super(pageNo, pageSize, direction, sortColumn);
    }
}
