package net.csibio.propro.domain.query;

import lombok.Data;

@Data
public class SwathIndexQuery extends PageQuery{

    private static final long serialVersionUID = -3258829832460821645L;

    String id;

    String expId;

    Integer level;

    //前体的荷质比窗口开始位置,已经经过overlap参数调整
    Float mzStart;

    //前体的荷质比窗口结束位置,已经经过overlap参数调整
    Float mzEnd;

    //根据目标的前体MZ获取相关的窗口
    Float mz;

    public SwathIndexQuery() {
    }

    public SwathIndexQuery(String expId, Integer msLevel) {
        this.expId = expId;
        this.level = msLevel;
    }

    public SwathIndexQuery(String expId, Integer msLevel, Float precursorMzStart, Float precursorMzEnd) {
        this.expId = expId;
        this.level = msLevel;
        this.mzStart = precursorMzStart;
        this.mzEnd = precursorMzEnd;
    }

    public SwathIndexQuery(int pageNo, int pageSize) {
        super(pageNo, pageSize);
    }
}
