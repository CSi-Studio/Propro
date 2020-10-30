package net.csibio.propro.domain.query;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-19 15:55
 */
@Data
public class AnalyseDataQuery extends PageQuery{

    private static final long serialVersionUID = -3258829839166834225L;

    String id;

    String overviewId;

    String peptideId;

    String libraryId;

    String dataRef;

    String peptideRef;

    String proteinName;

    Boolean isDecoy;

    Float mzStart;

    Float mzEnd;

    List<Integer> identifiedStatus;

    Double fdrStart;

    Double fdrEnd;

    Double qValueStart;

    Double qValueEnd;

    public AnalyseDataQuery(){}

    public AnalyseDataQuery(String overviewId){
        this.overviewId = overviewId;
    }

    public AnalyseDataQuery(int pageNo,int pageSize){
        super(pageNo, pageSize);
    }

    public void addIndentifiedStatus(Integer status){
        if(identifiedStatus == null){
            identifiedStatus = new ArrayList<>();
        }
        identifiedStatus.add(status);
    }
}
