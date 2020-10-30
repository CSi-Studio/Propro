package net.csibio.propro.domain.params;

import net.csibio.propro.domain.bean.score.SlopeIntercept;
import lombok.Data;

@Data
public class CoordinateBuildingParams {

    //斜率截距
    SlopeIntercept slopeIntercept;

    //RT卷积窗口
    float rtExtractionWindows;

    //仅用于PRM实验类型时使用
    Float[] rtRange;

    //实验类型
    String type;

    //是否只获取unique的肽段,默认为false
    boolean uniqueCheck = false;

    //是否包含伪肽段,null代表全部都获取
    Boolean noDecoy;

    //需要构建的肽段列表限制数目,如果为null则表明不限制
    Integer limit;

}
