package net.csibio.propro.domain.db;

import lombok.Data;
import net.csibio.aird.bean.WindowRange;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document("swathIndex")
public class SwathIndexDO {

    @Id
    String id;

    @Indexed
    String expId;

    /**
     * 1: ms1 swath block, 2: ms2 swath block
     */
    @Indexed
    Integer level;
    /**
     * 在文件中的开始位置
     */
    @Indexed
    Long startPtr;
    /**
     * 在文件中的结束位置
     */
    @Indexed
    Long endPtr;
    /**
     * SWATH块对应的WindowRange
     */
    WindowRange range;
    /**
     * 当msLevel=1时,本字段为Swath Block中每一个MS1谱图的序号,,当msLevel=2时本字段为Swath Block中每一个MS2谱图对应的MS1谱图的序列号,MS2谱图本身不需要记录序列号
     */
    List<Integer> nums;
    /**
     * 一个Swath块中所有子谱图的rt时间列表
     */
    List<Float> rts;
    /**
     * 一个Swath块中所有子谱图的mz的压缩后的大小列表
     */
    List<Long> mzs;
    /**
     * 一个Swath块中所有子谱图的intenisty的压缩后的大小列表
     */
    List<Long> ints;
    /**
     * 用于存储KV键值对
     */
    String features;

    public void init(){
        startPtr = 0L;
        endPtr = 0L;
        nums = new ArrayList<>();
        rts = new ArrayList<>();
        mzs = new ArrayList<>();
        ints = new ArrayList<>();
    }
}
