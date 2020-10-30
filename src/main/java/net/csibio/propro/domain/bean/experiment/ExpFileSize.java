package net.csibio.propro.domain.bean.experiment;

import lombok.Data;

@Data
public class ExpFileSize {

    //Aird文件大小,单位byte
    Long airdSize;

    //Aird索引文件的大小,单位byte
    Long airdIndexSize;

    //原始文件的大小,单位byte
    Long vendorFileSize;
}
