package net.csibio.propro.constants.enums;

import net.csibio.propro.constants.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ScoreType {

    MainScore("MainScore", "main_VarXxSwathPrelimScore",
            "Swath主打分",
            true, true),

    BseriesScore("BseriesScore", "var_BseriesScore",
            "peptideRt对应的spectrumArray中，检测到的b离子的数量",
            true, true),

    ElutionModelFitScore("ElutionModelFitScore", "var_ElutionModelFitScore",
            "",
            null, false),
    IntensityScore("IntensityScore", "var_IntensityScore",
            "同一个peptideRef下, 所有HullPoints的intensity之和 除以 所有intensity之和",
            true, true),
    IsotopeCorrelationScore("IsotopeCorrelationScore", "var_IsotopeCorrelationScore",
            "",
            true, true),
    IsotopeOverlapScore("IsotopeOverlapScore", "var_IsotopeOverlapScore",
            "feature intensity加权的可能（带电量1-4）无法区分同位素峰值的平均发生次数之和",
            true, true),
    LibraryCorr("LibraryCorr", "var_LibraryCorr",
            "对experiment和library intensity算Pearson相关系",
            true, true),
    LibraryRsmd("LibraryRsmd", "var_LibraryRsmd",
            "对experiment intensity 算占比差距平均值",
            false, true),
    LogSnScore("LogSnScore", "var_LogSnScore",
            "log(距离ApexRt最近点的stn值之和)",
            true, true),
    MassdevScore("MassdevScore", "var_MassdevScore",
            "按spectrum intensity加权的mz与product mz的偏差ppm百分比之和",
            false, true),
    MassdevScoreWeighted("MassdevScoreWeighted", "var_MassdevScoreWeighted",
            "按spectrum intensity加权的mz与product mz的偏差ppm百分比按libraryIntensity加权之和",
            false, true),
    NormRtScore("NormRtScore", "var_NormRtScore",
            "normalizedExperimentalRt与groupRt之差",
            false, true),
    XcorrCoelution("XcorrCoelution", "var_XcorrCoelution",
            "互相关偏移的mean + std",
            false, true),
    XcorrCoelutionWeighted("XcorrCoelutionWeighted", "var_XcorrCoelutionWeighted",
            "带权重的相关偏移sum",
            false, true),
    XcorrShape("XcorrShape", "var_XcorrShape",
            "互相关序列最大值的平均值",
            true, true),
    XcorrShapeWeighted("XcorrShapeWeighted", "var_XcorrShapeWeighted",
            "带权重的互相关序列最大值的平均值",
            true, true),
    LibraryDotprod("LibraryDotprod", "var_LibraryDotprod",
            "",
            true, true),
    LibraryManhattan("LibraryManhattan", "var_LibraryManhattan",
            "",
            false, true),
    LibrarySangle("LibrarySangle", "var_LibrarySangle",
            "",
            false, true),
    LibraryRootmeansquare("LibraryRootmeansquare", "var_LibraryRootmeansquare",
            "",
            false, true),
    ManhattScore("ManhattScore", "var_ManhattScore",
            "",
            null, false),
    YseriesScore("YseriesScore", "var_YseriesScore",
            "peptideRt对应的spectrumArray中，检测到的y离子的数量",
            true, true),
    WeightedTotalScore("WeightedTotalScore", "",
            "根据权重算出的加权总分-加权总分的平均分",
            null, false),
    IntensityTotalScore("IntensityTotalScore", "",
            "针对特殊需要的只做Intensity分类得到的总分-Intensity总分",
            null, false),
    ;

    String typeName;

    String pyProphetName;

    String description;

    Boolean biggerIsBetter;

    boolean isUsed;

    ScoreType(String typeName, String pyProphetName, String description, Boolean biggerIsBetter, Boolean isUsed) {
        this.typeName = typeName;
        this.pyProphetName = pyProphetName;
        this.isUsed = isUsed;
        this.biggerIsBetter = biggerIsBetter;
        this.description = description;
    }

    public static Boolean getBiggerIsBetter(String typeName) {
        for (ScoreType type : values()) {
            if (type.getTypeName().equals(typeName)) {
                return type.getBiggerIsBetter();
            }
        }
        return null;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getPyProphetName() {
        return pyProphetName;
    }

    public boolean getIsUsed() {
        return isUsed;
    }

    public Boolean getBiggerIsBetter() {
        return biggerIsBetter;
    }

    public String getDescription() {
        return description;
    }

    public static List<ScoreType> getUsedTypes() {
        List<ScoreType> types = new ArrayList<>();
        for (ScoreType type : values()) {
            if (type.isUsed) {
                types.add(type);
            }
        }
        return types;
    }

    public static List<ScoreType> getShownTypes() {

        List<ScoreType> types = Arrays.asList(values());
        types.remove(ScoreType.WeightedTotalScore);
        types.remove(ScoreType.MainScore);
//        if(type.equals("1")){
//            types.remove(ScoreType.BseriesScore);
//            types.remove(ScoreType.YseriesScore);
//            types.remove(ScoreType.);
//        }
        return types;
    }

    public static List<ScoreType> getUnusedTypes() {
        List<ScoreType> types = new ArrayList<>();
        for (ScoreType type : values()) {
            if (!type.isUsed) {
                types.add(type);
            }
        }
        return types;
    }

    public static List<String> getAllTypesName(){
        List<String> scoreNameList = new ArrayList<>();
        for (ScoreType type: values()){
            scoreNameList.add(type.getTypeName());
        }
        return scoreNameList;
    }

    public static String getPyProphetScoresColumns(String spliter) {
        StringBuilder columns = new StringBuilder();
        List<ScoreType> scoreTypes = ScoreType.getUsedTypes();
        for (int i = 0; i < scoreTypes.size(); i++) {
            if (i != scoreTypes.size() - 1) {
                columns.append(scoreTypes.get(i).getPyProphetName()).append(spliter);
            } else {
                columns.append(scoreTypes.get(i).getPyProphetName()).append(Constants.CHANGE_LINE);
            }
        }
        return columns.toString();
    }
}
