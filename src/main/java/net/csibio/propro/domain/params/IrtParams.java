package net.csibio.propro.domain.params;

import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.bean.analyse.SigmaSpacing;
import lombok.Data;

@Data
public class IrtParams {

    Float mzExtractWindow;

    SigmaSpacing sigmaSpacing = SigmaSpacing.create();

    boolean useLibrary = false;

    LibraryDO library;

    //严格筛选使用的分数阈值,不低于0.95,不超过1,越高则采样命中率越低,运算速度越慢,但是结果越精准
    float shapeScoreThreshold = 0.95f;

    //从数据库中随机取出的点的数目,越少速度越快,但是容易出现没有命中的情况,当出现没有命中的情况是,最终的采样点数会少于设定的collectNumbers数目,为null的时候表示全部取出不限制数目
    Integer pickedNumbers = 500;

    //使用标准库进行查询时的采样点数目,默认为50个点位,不能为空
    int wantedNumber = 50;
}
