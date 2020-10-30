package net.csibio.propro.domain.bean.analyse;

import lombok.Data;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-19 16:40
 */
@Data
public class MzResult {

    //伪肽段的序列
    String sequence;

    //原始肽段的序列
    String originSequence;

    String annotations;

    int precursorCharge =1;

    //离子碎片的序列
    String fragmentSequence;

    int charge = 1;

    double precursorMz;

    double newPrecursorMz;

    double delatPrecursorMz;

    double originMz;

    double newMz;

    double delta;

    String type;

    int location;
}
