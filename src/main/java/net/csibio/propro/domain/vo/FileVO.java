package net.csibio.propro.domain.vo;

import lombok.Data;

@Data
public class FileVO {

    //文件名称
    String name;
    //文件大小,单位byte
    Long size;
    //显示大小
    String sizeStr;
    //关联的ExperimentId
    String expId;

}
