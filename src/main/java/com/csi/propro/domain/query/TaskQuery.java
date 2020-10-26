package com.csi.propro.domain.query;

import lombok.Data;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-15 10:06
 */
@Data
public class TaskQuery extends PageQuery{

    String id;

    String name;

    String creator;

    String expId;

    String libraryId;

    String overviewId;

    String currentStep;

    String taskTemplate;

    List<String> statusList;

    public void setStatus(String status){
        if(statusList == null){
            statusList = new ArrayList<>();
        }else{
            statusList.clear();
        }
        statusList.add(status);
    }

    public void addStatus(String status){
        if(statusList == null){
            statusList = new ArrayList<>();
        }
        statusList.add(status);
    }

    public void clearStatus(){
        if(statusList == null){
            return;
        }
        statusList.clear();
    }

    public TaskQuery(){}

    public TaskQuery(int pageNo, int pageSize, Sort.Direction direction, String sortColumn){
        super(pageNo, pageSize, direction, sortColumn);
    }
}
