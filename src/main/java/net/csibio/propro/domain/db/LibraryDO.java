package net.csibio.propro.domain.db;

import net.csibio.propro.constants.Constants;
import net.csibio.propro.domain.BaseDO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-04 21:16
 */
@Data
@Document(collection = "library")
public class LibraryDO extends BaseDO {

    public static Integer TYPE_STANDARD = 0;
    public static Integer TYPE_IRT = 1;

    private static final long serialVersionUID = -3258829839160856625L;

    @Id
    String id;

    //是否设置为公共项目
    @Indexed
    boolean doPublic = false;

    @Indexed
    String creator;

    //项目标签
    List<String> labels = new ArrayList<>();

    @Indexed(unique = true)
    String name;

    /**
     * 0:标准库,1:iRT校准库
     * @see Constants
     * Constants.LIBRARY_TYPE_STANDARD,
     * Constants.LIBRARY_TYPE_IRT
     */
    Integer type;

    String description;

    /**
     * 伪肽段的生成算法
     */
    String generator;

    //蛋白总数目
    Long proteinCount;
    Long uniqueProteinCount;

    //肽段总数目
    Long totalCount;
    Long totalUniqueCount;

    int fastaDeWeightPepCount;
    int libraryDeWeightPepCount;
    int fastaDeWeightProtCount;
    int libraryDeWeightProtCount;

    Date createDate;

    Date lastModifiedDate;



}
