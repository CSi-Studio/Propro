package net.csibio.propro.controller;

import com.alibaba.fastjson.JSON;
import net.csibio.aird.bean.WindowRange;
import net.csibio.propro.algorithm.formula.FragmentFactory;
import net.csibio.propro.algorithm.parser.LibraryTsvParser;
import net.csibio.propro.algorithm.playground.PairComparator;
import net.csibio.propro.constants.Constants;
import net.csibio.propro.constants.enums.ScoreType;
import net.csibio.propro.domain.bean.score.FeatureScores;
import net.csibio.propro.domain.db.*;
import net.csibio.propro.service.AnalyseDataService;
import net.csibio.propro.service.ExperimentService;
import net.csibio.propro.service.LibraryService;
import net.csibio.propro.service.PeptideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileInputStream;
import java.util.*;

/**
 * Created by Nico Wang
 * Time: 2019-07-08 16:42
 */
@Controller
@RequestMapping("patch")
public class PatchController extends BaseController{

    @Autowired
    LibraryService libraryService;
    @Autowired
    PeptideService peptideService;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    LibraryTsvParser tsvParser;
    @Autowired
    FragmentFactory fragmentFactory;
    @Autowired
    PairComparator pairComparator;

    @RequestMapping("/addToLib")
    @ResponseBody
    String addToLibrary() {
        String fromLibId = "5d22f3bce0073c12f081d5bca";
        String toLibId = "5d2220d9e0073c44c41b10cda";
        String[] selectedPeps = new String[]{"INNEEIVSDPIYIEVQGLPHFTK_3", "IQIEAIPLALQGR_3", "DYAVSTVPVADGLHLK_2", "GLTEVTQSLK_2", "DMYSFLEDMGLK_3", "SPEQPPEGSSTPAEPEPSGGGPSAEAAPDTTADPAIAASDPATK_4",
                "ADVTPADFSEWSK_2", "GVFTKPIDSSSQPQQQFPK_2", "GREEINFVEIK_2", "MFEIVFEDPKIPGEK_2", "NYTQNIDTLEQVAGIQR_2", "DGLDAASYYAPVR_2", "LTLLHYDPVVK_2", "LAAAGPPGPFR_2", "TPVITGAPYEYR_2",
                "QEIIDWPGTEGR_2", "TLGDQLSLLLGAR_3", "TPEDAQAVINAYTEINK_2", "VEQVLSLEPQHELK_2", "GTFIIDPAAVIR_2", "AADFIDQALAQK_3", "TPVISGGPYEYR_2", "YILAGVENSK_2", "THLMSESEWR_3", "LKPQYLEELPGQLK_2",
                "LFLQFGAQGSPFLK_2", "GAGSSEPVTGLDAK_2", "VADPDHDHTGFLTEYVATR_3", "VEATFGVDESNAK_2", "DIILTVK_2", "HLYFWGYSEAAK_2", "GTFIIDPGGVIR_2", "LGGNEQVTR_2"};

        LibraryDO toLibraryDO = libraryService.getById(toLibId);

        List<String> remainPep = new ArrayList<>();
        for (String pep : selectedPeps) {
            PeptideDO peptideDO = peptideService.getByLibraryIdAndPeptideRef(fromLibId, pep);
            if (peptideDO != null) {
                peptideDO.setLibraryId(toLibId);
                peptideDO.setId(null);
                peptideService.insert(peptideDO);
            } else {
                remainPep.add(pep);
            }
        }

        libraryService.countAndUpdateForLibrary(toLibraryDO);
        logger.info("Still remain: " + JSON.toJSON(remainPep));
        return null;
    }


    @RequestMapping("/top/{topn}/{id}")
    @ResponseBody
    String addNewTopLibrary(@PathVariable("topn") int top,
                            @PathVariable("id") String libraryId) {
        LibraryDO oldLibrary = libraryService.getById(libraryId);
        LibraryDO newLibrary = new LibraryDO();
        newLibrary.setName(oldLibrary.getName() + "_top" + top);
        newLibrary.setType(oldLibrary.getType());
        newLibrary.setCreator(oldLibrary.getCreator());
        libraryService.insert(newLibrary);
        newLibrary = libraryService.getByName(newLibrary.getName());
        List<PeptideDO> peptideDOList = peptideService.getAllByLibraryId(libraryId);
        for (PeptideDO peptideDO : peptideDOList) {
            HashMap<String, FragmentInfo> fragInfoMap = peptideDO.getFragmentMap();
            List<Map.Entry<String, FragmentInfo>> fragInfoList = new ArrayList<>(fragInfoMap.entrySet());
            //按照切割位置排序, 判断离子数是否充足
            fragInfoList.sort(new Comparator<Map.Entry<String, FragmentInfo>>() {
                @Override
                public int compare(Map.Entry<String, FragmentInfo> o1, Map.Entry<String, FragmentInfo> o2) {
                    return Integer.parseInt(o2.getKey().substring(1).split("\\(")[0]) -
                            Integer.parseInt(o1.getKey().substring(1).split("\\(")[0]);
                }
            });
            HashMap<String, FragmentInfo> topFragMap = new HashMap<>();
            if (Integer.parseInt(fragInfoList.get(top - 1).getKey().substring(1).split("\\(")[0]) >= 4) {
                for (int i = fragInfoList.size() - 1; i >= 0; i--) {
                    if (Integer.parseInt(fragInfoList.get(i).getKey().substring(1).split("\\(")[0]) < 4) {
                        fragInfoList.remove(i);
                    }
                }
                fragInfoList.sort(new Comparator<Map.Entry<String, FragmentInfo>>() {
                    @Override
                    public int compare(Map.Entry<String, FragmentInfo> o1, Map.Entry<String, FragmentInfo> o2) {
                        return o2.getValue().getIntensity().compareTo(o1.getValue().getIntensity());
                    }
                });
            }
            for (int i = 0; i < top; i++) {
                if (i == fragInfoList.size()) {
                    break;
                }
                if (fragInfoList.get(i).getKey().substring(1).split("\\(")[0].equals("1")){
                    break;
                }
                topFragMap.put(fragInfoList.get(i).getKey(), fragInfoList.get(i).getValue());
            }


            peptideDO.setId(null);
            peptideDO.setLibraryId(newLibrary.getId());
            peptideDO.setFragmentMap(topFragMap);
        }
        peptideService.insertAll(peptideDOList, false);
        libraryService.countAndUpdateForLibrary(newLibrary);
        return null;
    }

    @RequestMapping("pcza")
    @ResponseBody
    String dealWithPcza() {
        String msmsLibId = "5d256a84e0073c16d0405646";
        String csvBigLibId = "5d25d825e0073c66a41aa44d";
        String sampleExperimentId = "5d254a19edbdf722f53b09d1";
        String[] selectedPeps = new String[]{"AVLLGPPGAGK_2", "IQSSHNFQLESVNK_3", "GPSGLLTYTGK_2", "INLPIQTYSALNFR_3", "LSGLHGQDLFGIWSK_3",
                "GFGFVTFDDHDPVDK_3", "YIDQEELNK_2", "LTLYDIAHTPGVAADLSHIETK_4", "FQELESETLK_2", "QAEMLDDLMEK_2", "LAEQFVLLNLVYETTDK_3",
                "TFGGAPGFPLGSPLSSPVFPR_3", "LFGGNFAHQASVAR_2", "MSVQPTVSLGGFEITPPVVLR_3", "EDGLAQQQTQLNLR_2", "VMEGTVAAQDEFYR_2", "IQSSHNFQLESVNK_2",
                "GFAFVEFSHLQDATR_2", "GNAGQSNYGFANSAMER_2", "LAEQFVLLNLVYETTDK_2", "LQAYHTQTTPLIEYYR_2"
        };


        /**
         * Step1 获取示例实验文件的PRM窗口
         */
        ExperimentDO experimentDO = experimentService.getById(sampleExperimentId).getModel();
        List<WindowRange> windowRanges = experimentDO.getWindowRanges();
        List<Float> expMzList = new ArrayList<>();
        for (WindowRange windowRange : windowRanges) {
            expMzList.add(windowRange.getMz().floatValue());
        }

        /**
         * Step2 将不准确的PRM库进行过滤
         */
        List<PeptideDO> msmsList = peptideService.getAllByLibraryId(msmsLibId);
        List<PeptideDO> filteredList = new ArrayList<>();
        HashSet<String> filteredPepSet = new HashSet<>();
        for (PeptideDO peptideDO : msmsList) {
            for (int i = expMzList.size() - 1; i >= 0; i--) {
                float expMz = expMzList.get(i);
                if (peptideDO.getMz() > expMz - 0.0006 && peptideDO.getMz() < expMz + 0.0006) {
                    filteredList.add(peptideDO);
                    filteredPepSet.add(peptideDO.getPeptideRef());
                    expMzList.remove(i);
                    break;
                }
            }
        }

        /**
         * Step3 添加信任的PeptideRef
         */
        List<String> errorSelectedList = new ArrayList<>();
        for (String pep : selectedPeps) {
            PeptideDO selectedPep = peptideService.getByLibraryIdAndPeptideRef(csvBigLibId, pep);
            boolean selected = false;
            for (int i = expMzList.size() - 1; i >= 0; i--) {
                float expMz = expMzList.get(i);
                if (selectedPep.getMz() > expMz - 0.0006 && selectedPep.getMz() < expMz + 0.0006) {
                    filteredList.add(selectedPep);
                    expMzList.remove(i);
                    selected = true;
                    break;
                }
            }
            if (!selected) {
                errorSelectedList.add(pep);
            }
        }
        System.out.println("Selected Not Found: " + errorSelectedList.size());

        /**
         * Step4 将msms信息转化为csv信息
         */
        for (PeptideDO pep : filteredList) {
            PeptideDO csvPep = peptideService.getByLibraryIdAndPeptideRef(csvBigLibId, pep.getPeptideRef());
            if (csvPep == null) {
                continue;
            }
            pep.setFragmentMap(csvPep.getFragmentMap());
        }


        /**
         * LastStep 插入新库
         */
        LibraryDO libraryDO = new LibraryDO();
        libraryDO.setCreator("Guomics");
        libraryDO.setName("PCZA_Manual_Nico");
        libraryDO.setType(LibraryDO.TYPE_STANDARD);
        libraryService.insert(libraryDO);
        libraryDO = libraryService.getByName(libraryDO.getName());
        for (PeptideDO peptideDO : filteredList) {
            peptideDO.setLibraryId(libraryDO.getId());
            peptideDO.setId(null);
        }
        peptideService.insertAll(filteredList, false);
        libraryService.countAndUpdateForLibrary(libraryDO);

        return null;
    }

    @RequestMapping("checklist")
    @ResponseBody
    String checkPrmList() {
        try {
            String filePath = "P:\\data\\PCZA_PRM_sr20190709\\20181212_PCZA_schedule.csv";
            String sampleExperimentId = "5d254a19edbdf722f53b09d1";
            ExperimentDO experimentDO = experimentService.getById(sampleExperimentId).getModel();
            List<WindowRange> windowRanges = experimentDO.getWindowRanges();
            List<Float> expMzList = new ArrayList<>();
            for (WindowRange windowRange : windowRanges) {
                expMzList.add(windowRange.getMz().floatValue());
            }
            Collections.sort(expMzList);
            FileInputStream prmFileStream = new FileInputStream(filePath);
            HashMap<String, PeptideDO> prmResult = tsvParser.getPrmPeptideRef(prmFileStream).getModel();
            int matchCount = 0;
            int expListSize = expMzList.size();
            for (PeptideDO peptideDO : prmResult.values()) {
                double mass = fragmentFactory.getTheoryMass(peptideDO.getUnimodMap(), peptideDO.getSequence());
                double mz = (mass + peptideDO.getCharge() * Constants.PROTON_MASS_U) / peptideDO.getCharge();
                for (int i = expMzList.size() - 1; i >= 0; i--) {
                    if (Math.abs(expMzList.get(i) - mz) < 0.0006) {
                        matchCount++;
                        expMzList.remove(i);
                        break;
                    }
                }
            }
            System.out.println("Match count: " + matchCount + "/" + expListSize);
            System.out.println("File count: " + prmResult.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("addirt/{id}")
    @ResponseBody
    String generateIrt(@PathVariable("id") String libraryId){
        List<PeptideDO> allPeptides = peptideService.getAllByLibraryId(libraryId);
        if (allPeptides.isEmpty()){
            return null;
        }
        LibraryDO libraryDO = libraryService.getById(libraryId);
        libraryDO.setId(null);
        libraryDO.setName(libraryDO.getName() + "_iRT");
        libraryDO.setType(LibraryDO.TYPE_IRT);
        libraryService.insert(libraryDO);
        libraryDO = libraryService.getByName(libraryDO.getName());

        List<PeptideDO> irtPeptides = new ArrayList<>();
        for (PeptideDO peptideDO: allPeptides){
            if (peptideDO.getProteinName().toLowerCase().contains("irt")){
                peptideDO.setProteinName("iRT");
                peptideDO.setId(null);
                peptideDO.setLibraryId(libraryDO.getId());
                irtPeptides.add(peptideDO);
            }
        }
        peptideService.insertAll(irtPeptides, false);
        libraryService.countAndUpdateForLibrary(libraryDO);
        return null;
    }

    @RequestMapping("/getscore/{id}")
    @ResponseBody
    String getScores(@PathVariable("id") String overviewId){
        String typeName = ScoreType.LibraryCorr.getTypeName();
        List<AnalyseDataDO> analyseDataDOList = analyseDataService.getAllByOverviewId(overviewId);
        List<Double> positiveList = new ArrayList<>();
        List<Double> targetScoreList = new ArrayList<>();
        List<Double> decoyScoreList = new ArrayList<>();
        for (AnalyseDataDO dataDO: analyseDataDOList){
            if (dataDO.getIsDecoy()){
                for (FeatureScores featureScores: dataDO.getFeatureScoresList()) {
                    decoyScoreList.add(featureScores.get(typeName, ScoreType.getAllTypesName()));
                }
            } else {
                double bestRt = dataDO.getBestRt();
                for (FeatureScores featureScores: dataDO.getFeatureScoresList()) {
                    if (featureScores.getRt().equals(bestRt) && dataDO.getIdentifiedStatus() == AnalyseDataDO.IDENTIFIED_STATUS_SUCCESS){
                        positiveList.add(featureScores.get(typeName, ScoreType.getAllTypesName()));
                    }else {
                        targetScoreList.add(featureScores.get(typeName, ScoreType.getAllTypesName()));
                    }
                }
            }
        }

        List<Double> positivePiece = positiveList.subList(0,10000);
        List<Double> targetPiece = targetScoreList.subList(0,10000);
        List<Double> decoyPiece = decoyScoreList.subList(0,10000);

        List<Double> pList = getPMoreIsBetter(positivePiece, decoyPiece);
        System.out.println(pList);
//        System.out.println("positive:");
//        System.out.println(positivePiece);
//        System.out.println("target: ");
//        System.out.println(targetPiece);
//        System.out.println("decoy: ");
//        System.out.println(decoyPiece);
        return null;
    }

    @RequestMapping("getgraph")
    @ResponseBody
    String getGraph(){
        pairComparator.getGraph("5d254a12edbdf722f53aeb78");
        return null;
    }

    private List<Double> getPLessIsBetter(List<Double> positiveList, List<Double> negativeList){
        int positiveIndex = 0, negativeIndex = 0;
        List<Double> pList = new ArrayList<>();
        Collections.sort(positiveList);
        Collections.sort(negativeList);
        double score = positiveList.get(0);
        while (positiveIndex < positiveList.size() && negativeIndex < negativeList.size()){
            if (positiveList.get(positiveIndex) <= negativeList.get(negativeIndex)){
                pList.add((double)negativeIndex/negativeList.size());
                positiveIndex ++;
            }else {
                negativeIndex ++;
            }
        }
        while (positiveIndex != positiveList.size()){
            pList.add(1d);
            positiveIndex ++;
        }
        return pList;
    }

    private List<Double> getPMoreIsBetter(List<Double> positiveList, List<Double> negativeList){
        int positiveIndex = 0, negativeIndex = 0;
        List<Double> pList = new ArrayList<>();
        Collections.sort(positiveList,new Comparator<Double>(){
            @Override
            public int compare(Double o1, Double o2) {
                return o2.compareTo(o1);
            }});
        Collections.sort(negativeList,new Comparator<Double>(){
            @Override
            public int compare(Double o1, Double o2) {
                return o2.compareTo(o1);
            }});
        double score = positiveList.get(0);
        while (positiveIndex < positiveList.size() && negativeIndex < negativeList.size()){
            if (positiveList.get(positiveIndex) >= negativeList.get(negativeIndex)){
                pList.add((double)negativeIndex/negativeList.size());
                positiveIndex ++;
            }else {
                negativeIndex ++;
            }
        }
        while (positiveIndex != positiveList.size()){
            pList.add(1d);
            positiveIndex ++;
        }
        return pList;
    }
}
