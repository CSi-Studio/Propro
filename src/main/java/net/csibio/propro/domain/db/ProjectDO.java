package net.csibio.propro.domain.db;

import net.csibio.propro.domain.BaseDO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "project")
public class ProjectDO extends BaseDO {

    @Id
    String id;

    //项目名称,唯一值
    @Indexed(unique = true)
    String name;

    //DIA_SWATH, PRM
    String type;

    //项目负责人名称
    String creator;

    //默认的标准库
    String libraryId;

    //默认的标准库名称
    String libraryName;

    //默认的irt库Id
    String iRtLibraryId;

    //默认的irt库名称
    String iRtLibraryName;

    //被授权的合作者
    List<String> collaborators;

    //项目标签
    List<String> labels = new ArrayList<>();

    //项目描述
    String description;

    //单位Byte
    Long totalSize;

    //实验的创建日期
    Date createDate;

    //最后修改日期
    Date lastModifiedDate;
}
