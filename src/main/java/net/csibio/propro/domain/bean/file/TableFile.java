package net.csibio.propro.domain.bean.file;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Nico Wang
 * Time: 2019-05-31 13:22
 */
@Data
public class TableFile {

    HashMap<String, Integer> columnMap;

    List<String[]> fileData;

    public TableFile(){ }

    public TableFile(HashMap<String, Integer> indexMap, List<String[]> fileData){
        this.columnMap = indexMap;
        this.fileData = fileData;
    }

}
