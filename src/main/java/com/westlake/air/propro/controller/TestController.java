package com.westlake.air.propro.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.westlake.air.propro.algorithm.feature.DIAScorer;
import com.westlake.air.propro.algorithm.fitter.LinearFitter;
import com.westlake.air.propro.algorithm.formula.FragmentFactory;
import com.westlake.air.propro.algorithm.learner.Airus;
import com.westlake.air.propro.algorithm.merger.Tric;
import com.westlake.air.propro.algorithm.parser.AirdFileParser;
import com.westlake.air.propro.algorithm.parser.MsmsParser;
import com.westlake.air.propro.constants.ScoreType;
import com.westlake.air.propro.dao.AnalyseDataDAO;
import com.westlake.air.propro.domain.bean.file.TableFile;
import com.westlake.air.propro.domain.bean.score.FeatureScores;
import com.westlake.air.propro.domain.db.*;
import com.westlake.air.propro.domain.params.LumsParams;
import com.westlake.air.propro.domain.query.AnalyseDataQuery;
import com.westlake.air.propro.service.*;
import com.westlake.air.propro.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.util.*;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-07 13:30
 */
@Controller
@RequestMapping("test")
public class TestController extends BaseController {

    @Autowired
    ExperimentService experimentService;
    @Autowired
    ProjectService projectService;
    @Autowired
    TaskService taskService;
    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    AnalyseDataDAO analyseDataDAO;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    ScoreService scoreService;
    @Autowired
    Airus airus;
    @Autowired
    PeptideService peptideService;
    @Autowired
    FragmentFactory fragmentFactory;
    @Autowired
    AirdFileParser airdFileParser;
    @Autowired
    Tric tric;
    @Autowired
    MsmsParser msmsParser;
    @Autowired
    DIAScorer diaScorer;
    @Autowired
    LinearFitter linearFitter;
    @Autowired
    ResultCompareService resultCompareService;

    public static float MZ_EXTRACT_WINDOW = 0.05f;
    public static float RT_EXTRACT_WINDOW = 1200f;
    public static float SIGMA = 6.25f;
    public static float SPACING = 0.01f;

    //计算iRT
    @RequestMapping("testLMS")
    @ResponseBody
    String testLMS(Model model, RedirectAttributes redirectAttributes) {
        String a = resultCompareService.getProproProteins("5d1d57b05bb5e9463e9dcc16", false).size() + "";
//        String b = resultCompareService.getProproProteins("5d037a57fc6f9e4100b55d81", true).size() + "";
//        String c = resultCompareService.getProproProteins("5d038111fc6f9e4100b74990", true).size() + "";
        return a;
    }

    //计算iRT
    @RequestMapping("test2")
    @ResponseBody
    String test2(Model model, RedirectAttributes redirectAttributes) {
        List<ExperimentDO> exps = experimentService.getAllByProjectName("SGS");
        exps.forEach(exp -> {
            exp.setOwnerName("lms");
            experimentService.update(exp);
        });
        return "success";
    }

    @RequestMapping("test6")
    @ResponseBody
    String test6(Model model, RedirectAttributes redirectAttributes) throws IOException {
        String jsonTest = "{\"start\": 482.14798,\"end\": 483.348,\"interval\": 1.20001221}";
        HashMap test = JSONObject.parseObject(jsonTest, HashMap.class);
        System.out.println(test.size());
        return null;
    }

    @RequestMapping("hyeTest")
    @ResponseBody
    String hyeTest() throws IOException {
        List<String> analyseOverviewIdList = new ArrayList<>();
//        List<ExperimentDO> experimentDOList = experimentService.getAllByProjectName("HYE110_TTOF6600_64var");
//        for (ExperimentDO experimentDO : experimentDOList) {
//            analyseOverviewIdList.add(analyseOverviewService.getAllByExpId(experimentDO.getId()).get(0).getId());
//        }
//        analyseOverviewIdList.add("5c9c3280dfdfdd7078dabb1d");//006
//        analyseOverviewIdList.add("5c9c393ddfdfdd7078dd8507");//004
//        analyseOverviewIdList.add("5c9c3f0cdfdfdd7078e04f2a");//002
//        analyseOverviewIdList.add("5c9c3658dfdfdd7078dc2050");//005
//        analyseOverviewIdList.add("5c9c3c27dfdfdd7078deea5d");//003
//        analyseOverviewIdList.add("5c9c41fadfdfdd7078e1b4ad");//001
        //32var-B
//        analyseOverviewIdList.add("5ca07216dfdfdd6c5806fd83");//006
//        analyseOverviewIdList.add("5ca0781ddfdfdd6c5809b3a5");//004
//        analyseOverviewIdList.add("5ca07e2fdfdfdd6c580c6a17");//002
        //32var-A
        analyseOverviewIdList.add("5ca0751edfdfdd6c5808599b");//005
        analyseOverviewIdList.add("5ca07b2cdfdfdd6c580b0fdc");//003
        analyseOverviewIdList.add("5ca08169dfdfdd6c580dc75a");//001

        List<String> analyseOverviewIdList64VarA = new ArrayList<>();
        analyseOverviewIdList64VarA.add("5d063f62e0073c3f28b023ed");//1
        analyseOverviewIdList64VarA.add("5d06494be0073c3f28b2f2bc");//3
        analyseOverviewIdList64VarA.add("5d065335e0073c3f28b5c19e");//5
        List<String> analyseOverviewIdList64VarB = new ArrayList<>();
        analyseOverviewIdList64VarB.add("5d06445ae0073c3f28b18b4c");//2
        analyseOverviewIdList64VarB.add("5d064e3ae0073c3f28b45a28");//4
        analyseOverviewIdList64VarB.add("5d06582be0073c3f28b7290a");//6
        List<String> analyseOverviewIdList64FixA = new ArrayList<>();
        analyseOverviewIdList64FixA.add("5d0700cce0073c3f28b8921d");//1
        analyseOverviewIdList64FixA.add("5d070b34e0073c3f28bb5f5e");//3
        analyseOverviewIdList64FixA.add("5d0714e5e0073c3f28be2ca0");//5
        List<String> analyseOverviewIdList64FixB = new ArrayList<>();
        analyseOverviewIdList64FixB.add("5d070639e0073c3f28b9f8c3");//2
        analyseOverviewIdList64FixB.add("5d071007e0073c3f28bcc5ff");//4
        analyseOverviewIdList64FixB.add("5d0719cbe0073c3f28bf9323");//6

        HashMap<String, Integer> proproPepRef = tric.parameterEstimation(analyseOverviewIdList64VarA, 0.01d);


        // HYE110_TTOF6600_64var    C:/E1603141345_feature_alignment.tsv
        //       file: 38567 + 1224
        //     propro: 41419 + 5093
        //      match: 37470 + 264
        //propro only: 3949 + 4829
        //  file only: 1097 + 960
        //match file target:   [1862,3597,10959,3295,4558,13199]
        //match propro target: [825,1448,9670,3137,2590,19800]
        //match file decoy:    [105,49,60,25,11,14]
        //match propro decoy:  [140,60,32,11,10,11]
        //only file target:    [262,240,354,71,68,102]
        //only file decoy:     [474,239,123,53,49,22]
        //only propro target:  [1386,970,1127,198,92,176]
        //only propro decoy:   [3468,792,317,119,80,53]

//         HYE110_TTOF6600_32var    C:/E1603141355_feature_alignment.tsv

        // HYE110_TTOF6600_64fix    C:/E1603141357_feature_alignment.tsv
        //       file: 33700 + 843
        //     propro: 37683 + 3453
        //      match: 32252 + 164
        //propro only: 5431 + 3289
        //  file only: 1448 + 679
        //match file target:   [1835,2784,10252,2371,2379,12631]
        //match propro target: [1157,2011,8957,2683,2983,14461]
        //match file decoy:    [76,29,21,15,13,10]
        //match propro decoy:  [82,34,18,11,8,11]
        //only file target:    [398,284,450,66,68,182]
        //only file decoy:     [350,122,115,43,28,21]
        //only propro target:  [2212,1389,1313,233,133,151]
        //only propro decoy:   [2411,480,213,95,45,45]

        // HYE110_TTOF6600_32fix    C:/E1603141346_feature_alignment.tsv
        //       file: 32053 + 1184
        //     propro: 34425 + 1298
        //      match: 29952 + 81
        //propro only: 4473 + 1217
        //  file only: 2101 + 1103
        //match file target:   [1652,2859,9401,2417,2343,11280]
        //match propro target: [1517,3866,7271,2361,7493,7444]
        //match file decoy:    [36,14,17,3,6,5]
        //match propro decoy:  [16,3,18,8,9,27]
        //only file target:    [655,448,559,112,70,257]
        //only file decoy:     [514,247,190,66,50,36]
        //only propro target:  [1968,1239,890,175,126,75]
        //only propro decoy:   [333,185,185,152,162,200]
        BufferedReader reader = new BufferedReader(new FileReader("C:/E1603141345_feature_alignment.tsv"));
        reader.readLine();//第一行信息，为标题信息
        String line;
        HashMap<String, Integer> fileTargetMap = new HashMap<>();
        HashMap<String, Integer> fileDecoyMap = new HashMap<>();
        //32var 0_02 0_11 0_24 0_35 0_46 0_53
        //64var 0_05 0_14 0_22 0_31 0_43 0_56
        //64fix 0_03 0_15 0_26 0_31 0_44 0_52
        List<String> aList64Var = new ArrayList<>();
        aList64Var.add("0_3");
        aList64Var.add("0_0");
        aList64Var.add("0_4");
        List<String> aList64Fix = new ArrayList<>();
        aList64Fix.add("0_3");
        aList64Fix.add("0_0");
        aList64Fix.add("0_1");
        while ((line = reader.readLine()) != null) {
            if (!aList64Var.contains(line.split("\t")[2])) {
                continue;
            }
            String item[] = line.split("\t")[0].split("_");
            if (item[0].equals("DECOY")) {
                String peptideRef = item[0] + "_" + item[2] + "_" + item[3];
                if (fileDecoyMap.containsKey(peptideRef)) {
                    fileDecoyMap.put(peptideRef, fileDecoyMap.get(peptideRef) + 1);
                } else {
                    fileDecoyMap.put(peptideRef, 1);
                }
            } else {
                String peptideRef = item[1] + "_" + item[2];
                if (fileTargetMap.containsKey(peptideRef)) {
                    fileTargetMap.put(peptideRef, fileTargetMap.get(peptideRef) + 1);
                } else {
                    fileTargetMap.put(peptideRef, 1);
                }
            }
        }

        HashMap<String, Integer> proproMatchTarget = new HashMap<>();
        HashMap<String, Integer> proproMatchDecoy = new HashMap<>();
        HashMap<String, Integer> proproOnlyTarget = new HashMap<>();
        HashMap<String, Integer> proproOnlyDecoy = new HashMap<>();
        HashMap<String, Integer> fileMatchTarget = new HashMap<>();
        HashMap<String, Integer> fileMatchDecoy = new HashMap<>();
        HashMap<String, Integer> fileOnlyTarget = new HashMap<>();
        HashMap<String, Integer> fileOnlyDecoy = new HashMap<>();

        for (String peptideRef : proproPepRef.keySet()) {
            if (peptideRef.startsWith("DECOY_")) {
                if (fileDecoyMap.containsKey(peptideRef)) {
                    proproMatchDecoy.put(peptideRef, proproPepRef.get(peptideRef));
                    fileMatchDecoy.put(peptideRef, fileDecoyMap.get(peptideRef));
                } else {
                    proproOnlyDecoy.put(peptideRef, proproPepRef.get(peptideRef));
                }
            } else {
                if (fileTargetMap.containsKey(peptideRef)) {
                    proproMatchTarget.put(peptideRef, proproPepRef.get(peptideRef));
                    fileMatchTarget.put(peptideRef, fileTargetMap.get(peptideRef));
                } else {
                    proproOnlyTarget.put(peptideRef, proproPepRef.get(peptideRef));
                }
            }
        }
        for (String peptideRef : fileTargetMap.keySet()) {
            if (!fileMatchTarget.containsKey(peptideRef)) {
                fileOnlyTarget.put(peptideRef, fileTargetMap.get(peptideRef));
            }
        }
        for (String peptideRef : fileDecoyMap.keySet()) {
            if (!fileMatchDecoy.containsKey(peptideRef)) {
                fileOnlyDecoy.put(peptideRef, fileDecoyMap.get(peptideRef));
            }
        }

        int dimension = 3;
        System.out.println("file: " + fileTargetMap.size() + " + " + fileDecoyMap.size());
        System.out.println("propro: " + (proproMatchTarget.size() + proproOnlyTarget.size()) + " + " + (proproMatchDecoy.size() + proproOnlyDecoy.size()));
        System.out.println("match: " + proproMatchTarget.size() + " + " + proproMatchDecoy.size());
        System.out.println("propro only: " + proproOnlyTarget.size() + " + " + proproOnlyDecoy.size());
        System.out.println("file only: " + fileOnlyTarget.size() + " + " + fileOnlyDecoy.size());
        System.out.println("match file target:   " + JSON.toJSON(getHitMap(fileMatchTarget, dimension)));
        System.out.println("match propro target: " + JSON.toJSON(getHitMap(proproMatchTarget, dimension)));
        System.out.println("match file decoy:    " + JSON.toJSON(getHitMap(fileMatchDecoy, dimension)));
        System.out.println("match propro decoy:  " + JSON.toJSON(getHitMap(proproMatchDecoy, dimension)));
        System.out.println("only file target:    " + JSON.toJSON(getHitMap(fileOnlyTarget, dimension)));
        System.out.println("only file decoy:     " + JSON.toJSON(getHitMap(fileOnlyDecoy, dimension)));
        System.out.println("only propro target:  " + JSON.toJSON(getHitMap(proproOnlyTarget, dimension)));
        System.out.println("only propro decoy:   " + JSON.toJSON(getHitMap(proproOnlyDecoy, dimension)));
//        printScoreDistribution(analyseOverviewIdList, proproMatchTarget, ScoreType.XcorrShape.getTypeName(), 0);
//        printScoreDistribution(analyseOverviewIdList, proproMatchTarget, ScoreType.XcorrShape.getTypeName(), 0);
        return null;
    }

    private int[] getHitMap(HashMap<String, Integer> countMap, int dimension) {
        int[] hitMap = new int[dimension];
//        List<Integer> errorCountList = new ArrayList<>();
        for (String peptideRef : countMap.keySet()) {
            int count = countMap.get(peptideRef);
//            if (count > 6){
//                errorCountList.add(count);
//            }
            int i = 2;
            while (count > dimension) {
                count = count / i;
                i++;
            }
            hitMap[count - 1]++;
        }
//        System.out.println("error List: " + errorCountList.size() + errorCountList);
        return hitMap;
    }

    @RequestMapping("unimodTest")
    @ResponseBody
    String unimodTest() {
        String ions = "y1;y2;y3;y4;y5;y6;y7;y8;y10;y11;y12;y13;y14;y1-NH3;y10-NH3;y12-NH3;a2;b2;b3;b4;b5;b6;b4-H2O;b5-H2O;b6-H2O;b2-NH3;b3-NH3;b4-NH3;b5-NH3;b6-NH3";
        String masses = "175.118884156693;274.187250304226;345.224394442742;444.292854866413;557.375588788621;656.444166686439;784.505751506767;841.52493583811;1066.63664456707;1226.66811647301;1354.7271638755;1517.78644819324;1604.80365912874;158.092634757598;1049.61643693022;1337.69097318547;158.092634757598;186.087369617932;333.155702330992;420.186860855073;583.251564047471;711.309312644877;402.176596411266;565.241284287495;693.299642703383;169.060406174275;316.129241761169;403.159458030056;566.224076957964;694.287204682172";
        String sequence = "QGFSYQCPQGQVIVAVR";
        double mass = 1935.9625d;
        String[] ionArray = ions.split(";");
        String[] massArray = masses.split(";");
//        String[] ionArray = new String[]{"y1","y2","y3","y4","y5","y6","y7","y1-NH3","y4-NH3","a2","b2"};
//        String[] massArray = new String[]{"147.112657326583","294.180859636222","441.248324375729","569.308401130061","716.375457170649","817.422756693449","914.476593996905","130.086118895367","552.283287471936","264.083145995217","292.077582498917"};
//        String sequence = "CMPTFQFFK";
//        double mass = 1204.5409d;
        HashMap<Integer, String> unimodMap = new HashMap<>();
        boolean isSuccess = msmsParser.verifyUnimod(ionArray, massArray, unimodMap, sequence, mass);
        System.out.println(isSuccess);
        return null;
    }

    @RequestMapping("byScore")
    @ResponseBody
    String byScoreTest() {
        try {
            String file = FileUtil.readFile("C:\\workspace\\java\\test\\byscore.txt");
            String[] mz = file.split("\n")[0].split(",");
            String[] intensity = file.split("\n")[1].split(",");
            assert mz.length == intensity.length;
            Float[] mzArray = new Float[mz.length];
            Float[] intArray = new Float[mz.length];
            for (int i = 0; i < mz.length; i++) {
                mzArray[i] = Float.parseFloat(mz[i]);
                intArray[i] = Float.parseFloat(intensity[i]);
            }
            LumsParams lp = new LumsParams();
            List<String> defaultScoreTypes = lp.getScoreTypes();
            FeatureScores featureScores = new FeatureScores(defaultScoreTypes.size());
            diaScorer.calculateBYIonScore(mzArray, intArray, new HashMap<>(), "AAMTLVQSLLNGNK", 2, featureScores, defaultScoreTypes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("compare")
    @ResponseBody
    String compareTest(){
//        String analyseOverviewId = "5d087c33e24d2e62a82055a3";
//        String filePath = "P:\\data\\HCC_sciex\\pyprophet\\D20181207yix_HCC_SW_T_46A_with_dscore_filtered.tsv";
//        HashSet<String> result = resultComparator.getFileOnlyPepRef(analyseOverviewId, filePath);
//        resultComparator.proteinResults(analyseOverviewId, filePath);
//        resultComparator.peptideRefResults(analyseOverviewId, filePath);
//        resultComparator.peptideSeqResults(analyseOverviewId, filePath);
//        resultComparator.silacResults(analyseOverviewId, filePath);
        String projectIdOld = "5d08705fe0073c9b70faff6a";
        String projectId = "5d11dd3f33251e8512a2f402";
        String libraryId = "5d0870dce0073c9b70fb008f";
        String matrixFilePath = "P:\\data\\HCC_QE3\\HCC_20190106_dia_os_peptides_matrix.tsv";
        String matrixFilePathNew = "P:\\data\\HCC_QE3\\QEpeptides_2019626.txt";
//        String projectId = "5d087107e0073c9b70fb0091";
//        String matrixFilePath = "P:\\data\\HCC_sciex\\HCC_20190114_swath_os_peptides_matrix.tsv";
//        resultCompareService.compareMatrix(projectId, matrixFilePathNew, 0, true);
        resultCompareService.printProteinCoverage(projectId, libraryId, matrixFilePathNew);
        return null;
    }

    @RequestMapping("compareRep")
    @ResponseBody
    String compareRepTest(){
//        String projectId1 = "5d08705fe0073c9b70faff6a";
//        String filePath1 = "P:\\data\\HCC_QE3\\HCC_20190106_dia_os_peptides_matrix.tsv";
//        resultComparator.compareReplicate(projectId1, filePath1, "C20181210yix_HCC_DIA_T_17A", "C20181218yix_HCC_DIA_T_17B");
//        resultComparator.compareReplicate(projectId1, filePath1, "C20181210yix_HCC_DIA_T_18A", "C20181218yix_HCC_DIA_T_18B");
//        resultComparator.compareReplicate(projectId1, filePath1, "C20181210yix_HCC_DIA_T_24A", "C20181218yix_HCC_DIA_T_24B");
//        resultComparator.compareReplicate(projectId1, filePath1, "C20181208yix_HCC_DIA_T_46A", "C20181218yix_HCC_DIA_T_46B");
//        resultComparator.compareReplicate(projectId1, filePath1, "C20181208yix_HCC_DIA_T_48A", "C20181218yix_HCC_DIA_T_48B");
        String projectId2 = "5d08705fe0073c9b70faff6a";
        String filePath2 = "P:\\data\\HCC_sciex\\HCC_20190114_swath_os_peptides_matrix.tsv";
        resultCompareService.compareMatrixReplicate(projectId2, filePath2, "D20181213yix_HCC_SW_T_17A", "D20181217yix_HCC_SW_T_17B", 0, false);
//        resultComparator.compareReplicate(projectId2, filePath2, "D20181213yix_HCC_SW_T_18A", "D20181217yix_HCC_SW_T_18B");
//        resultComparator.compareReplicate(projectId2, filePath2, "D20181213yix_HCC_SW_T_24A", "D20181217yix_HCC_SW_T_24B");
//        resultComparator.compareReplicate(projectId2, filePath2, "D20181207yix_HCC_SW_T_46A", "D20181217yix_HCC_SW_T_46B");
//        resultComparator.compareReplicate(projectId2, filePath2, "D20181207yix_HCC_SW_T_48A", "D20181217yix_HCC_SW_T_48B");
        return null;
    }

    @RequestMapping("silac")
    @ResponseBody
    String silacTest(){
        String overviewId = "5d18e4341fb7212da56b31f1";
        String filePath = "P:\\data\\SILAC_QE\\F20190530liangx_SILAC_K562_DIA_LHtitra1_1_allFrag_with_dscore_filtered.csv";
        resultCompareService.printSilacResults(overviewId, filePath);
        return null;
    }

    @RequestMapping("sequence")
    @ResponseBody
    String getSequenceNum(){
        String libraryId = "5d08739ee0073c9b70042eb5";
        List<PeptideDO> peptideDOList = peptideService.getAllByLibraryIdAndIsDecoy(libraryId, false);
        HashSet<String> sequenceSet = new HashSet<>();
        for (PeptideDO peptideDO: peptideDOList){
            sequenceSet.add(peptideDO.getSequence());
        }
        System.out.println(sequenceSet.size());
        return null;
    }

    @RequestMapping("libconfirm")
    @ResponseBody
    String libConfirmTest(){
        String filePath = "P:\\data\\HCC_sciex\\HCC_20190114_swath_os_peptides_matrix.tsv";
        String libraryId = "5d08739ee0073c9b70042eb5";
        List<PeptideDO> peptideDOList = peptideService.getAllByLibraryIdAndIsDecoy(libraryId, false);
        HashSet<String> libPepRefSet = new HashSet<>();
        for (PeptideDO peptideDO: peptideDOList){
            libPepRefSet.add(peptideDO.getPeptideRef());
        }
        HashSet<String> filePepRefSet = new HashSet<>();
        try{
            TableFile ppFile = FileUtil.readTableFile(filePath);
            List<String[]> fileData = ppFile.getFileData();
            for (String[] line: fileData){
                String[] pepInfo = line[0].split("_");
                filePepRefSet.add(pepInfo[1]+"_"+pepInfo[2]);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        for (String pepRef: filePepRefSet){
            if (!libPepRefSet.contains(pepRef)){
                System.out.println(pepRef);
            }
        }
        return null;
    }

    @RequestMapping("distribution")
    @ResponseBody
    String distributionTest(){
        String analyseOverviewId = "5cfe7adee0073c2fd07def50";
//        String scoreType = ScoreType.WeightedTotalScore.getTypeName();
        AnalyseOverviewDO overviewResult = analyseOverviewService.getById(analyseOverviewId).getModel();
        HashMap<String,Double> weightsMap = overviewResult.getWeights();
        AnalyseDataQuery query = new AnalyseDataQuery();
        query.setOverviewId(analyseOverviewId);
        query.setIsDecoy(false);
        query.addIndentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_SUCCESS);
        List<AnalyseDataDO> analyseDataDOList = analyseDataService.getAll(query);
        List<Double> heavyList = new ArrayList<>();
        List<Double> lightList = new ArrayList<>();
        HashSet<String> intensitySet = new HashSet<>();
        intensitySet.add(ScoreType.LibraryManhattan.getTypeName());
        intensitySet.add(ScoreType.LibrarySangle.getTypeName());
        intensitySet.add(ScoreType.LibraryRsmd.getTypeName());
        intensitySet.add(ScoreType.LibraryDotprod.getTypeName());
        intensitySet.add(ScoreType.LibraryRootmeansquare.getTypeName());
        intensitySet.add(ScoreType.LibraryCorr.getTypeName());
        for (AnalyseDataDO analyseDataDO: analyseDataDOList){
            if (analyseDataDO.getIsDecoy()){
                continue;
            }
            List<FeatureScores> featureScoresList = analyseDataDO.getFeatureScoresList();
            boolean isHeavy = analyseDataDO.getPeptideRef().split("_")[0].endsWith("(UniMod:188)");
            double bestRt = analyseDataDO.getBestRt();
            for (FeatureScores featureScores: featureScoresList){
                if (featureScores.getRt() == bestRt){
                    double score = 0d;
                    for (Map.Entry<String,Double> entry: weightsMap.entrySet()){
                        if (!intensitySet.contains(entry.getKey())){
                            continue;
                        }
                        //默认分数不为null
                        score += entry.getValue() * featureScores.get(entry.getKey(), overviewResult.getScoreTypes());
                    }
                    if (isHeavy){
                        heavyList.add(score);
                    }else {
                        lightList.add(score);
                    }
                }
            }
        }
        return null;
    }

    @RequestMapping("projectMainScore")
    @ResponseBody
    String projectMainScoreTest(){
        String projectId = "5d1eb9b5e0073c4720e3bfa3";
        List<ExperimentDO> experimentDOList = experimentService.getAllByProjectId(projectId);
        List<Double> scoreList = new ArrayList<>();
        for (ExperimentDO experimentDO: experimentDOList){
            AnalyseOverviewDO analyseOverviewDO = analyseOverviewService.getFirstByExpId(experimentDO.getId()).getModel();
            AnalyseDataQuery query = new AnalyseDataQuery();
            query.addIndentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_SUCCESS);
            query.setIsDecoy(false);
            query.setOverviewId(analyseOverviewDO.getId());
            List<AnalyseDataDO> dataDOList = analyseDataService.getAll(query);
            int index = 0;
            for (int i=0; i<analyseOverviewDO.getScoreTypes().size(); i++){
                if (analyseOverviewDO.getScoreTypes().get(i).equals(ScoreType.XcorrShape.getTypeName())){
                    index = i;
                    break;
                }
            }

            for (AnalyseDataDO dataDO: dataDOList){
                List<FeatureScores> featureScoresList = dataDO.getFeatureScoresList();
                Double bestRt = dataDO.getBestRt();
                for (FeatureScores featureScores: featureScoresList){
                    if (!featureScores.getRt().equals(bestRt)){
                        continue;
                    }else {
                        scoreList.add(featureScores.getScores()[index]);
                    }
                }
            }
        }
        Collections.sort(scoreList);
        double minScore = scoreList.get(0);
        double maxScore = scoreList.get(scoreList.size() - 1);
        return null;
    }

}