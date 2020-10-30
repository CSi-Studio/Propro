package net.csibio.propro.domain.db;

import net.csibio.propro.constants.enums.Classifier;
import net.csibio.propro.domain.BaseDO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-19 16:20
 */
@Data
@Document(collection = "analyseOverview")
public class AnalyseOverviewDO extends BaseDO {

    @Id
    String id;

    @Indexed
    String projectId;

    //对应实验的id
    @Indexed
    String expId;

    //分析实验的创建者
    @Indexed
    String ownerName;

    //对应实验的名称
    String expName;

    //0:SWATH_DIA 1:PRM 2:SCANNING_SWATH
    String type;

    //分析代号
    String name;

    //标签
    String label;

    //标准库ID
    String libraryId;

    //标准库名称
    String libraryName;

    //使用的斜率,一般来源于experimentDO对象,也可以在执行单独的数据提取步骤自由设定
    Double slope;

    //使用的截距,一般来源于experimentDO对象,也可以在执行单独的数据提取步骤自由设定
    Double intercept;

    //在打分时设定的shape分的阈值,如果低于这个阈值的峰组会被直接忽略掉,从而节省运算时间
    Float shapeScoreThreshold;

    //在打分时设定的shapeWeight分的阈值,如果低于这个阈值的峰组会被直接忽略掉,从而节省运算时间
    Float shapeScoreWeightThreshold;

    //rt数据提取窗口,一般设定为600或者800,一般来源于experimentDO对象,也可以在执行单独的数据提取步骤自由设定
    Float rtExtractWindow;

    //mz数据提取窗口,一般设定为0.05或者0.03,一般来源于experimentDO对象,也可以在执行单独的数据提取步骤自由设定
    Float mzExtractWindow;

    //最终使用的分类器:lda, xgboost
    String classifier = Classifier.lda.name();

    //在计算高斯平滑时使用的Sigma值,一般为6.25
    Float sigma;

    //在计算高斯平滑时使用的Sigma值,一般为0.01
    Float spacing;

    //最终的划分FDR,一般为0.01
    Double fdr = 0.01;

    //分析实验的创建时间
    Date createDate;

    //分析实验的最后一次修改时间
    Date lastModifiedDate;

    //最终计算所得的子分数的权重,LDA算法才有
    HashMap<String, Double> weights = new HashMap<>();

    HashMap<String, Integer> targetDistributions = new HashMap<>();
    HashMap<String, Integer> decoyDistributions = new HashMap<>();

    //最终计算鉴定到的肽段数目
    Integer matchedPeptideCount = 0;

    //最终提取到的肽段数目
    Integer totalPeptideCount = 0;

    //对应标准库中的肽段数目
    Integer libraryPeptideCount;

    //最终识别的蛋白质数目
    Integer matchedProteinCount = 0;

    //最终识别的肽段-蛋白质覆盖率,公式为:实际鉴定到的同一个蛋白质的肽段数目/标准库中同一个蛋白质对应的肽段数目
    Float ppRate;

    //本次分析最终的选峰数目
    Long peakCount = 0L;

    //用于打分的子分数类型快照,会和AnalyseDataDO中的每一个FeatureScore中的scores对象做一一映射
    List<String> scoreTypes;

    //备忘录
    String note;

    public float calPPRate() {
        if (matchedProteinCount != 0) {
            return (float)matchedPeptideCount / matchedProteinCount;
        } else {
            return 0;
        }
    }
}
