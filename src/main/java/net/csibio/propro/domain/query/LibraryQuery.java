package net.csibio.propro.domain.query;

import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.Date;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-04 21:16
 */
@Data
public class LibraryQuery extends PageQuery {

    private static final long serialVersionUID = -3258829839160856625L;

    String id;

    String name;

    String creator;
    
    //0:标准库,1:iRT校准库
    Integer type;

    Date createDate;

    Date lastModifiedDate;

    public LibraryQuery(){}

    public LibraryQuery(Integer type){
        this.type = type;
    }

    public LibraryQuery(int pageNo, int pageSize, Sort.Direction direction, String sortColumn){
        super(pageNo, pageSize, direction, sortColumn);
    }
}
