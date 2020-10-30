package net.csibio.propro.service.impl;

import net.csibio.propro.domain.bean.file.TableFile;
import net.csibio.propro.domain.db.AnalyseDataDO;
import net.csibio.propro.domain.db.AnalyseOverviewDO;
import net.csibio.propro.domain.db.ExperimentDO;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.query.AnalyseDataQuery;
import net.csibio.propro.service.*;
import net.csibio.propro.utils.FileUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Nico Wang
 * Time: 2019-06-27 22:15
 */
@Service("resultCompareService")
public class ResultCompareServiceImpl implements ResultCompareService {

    public final Logger logger = LoggerFactory.getLogger(ResultCompareService.class);

    @Autowired
    ExperimentService experimentService;
    @Autowired
    PeptideService peptideService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    AnalyseDataService analyseDataService;


    @Override
    public void printProteinCoverage(String projectId, String libraryId, String filePath) {
        HashMap<String, Double> libPPMap = getLibPPMap(libraryId);
        HashMap<String, HashMap<String, Double>> filePPMap = getFilePPMap(projectId, filePath);
        HashMap<String, HashMap<String, Double>> proproPPMap = getProproPPMap(projectId);
//        HashMap<String, HashMap<String,Double>> fileCoverageMap = getProjectCoverageMap(filePPMap, libPPMap);
//        HashMap<String, HashMap<String,Double>> proproCoverageMap = getProjectCoverageMap(proproPPMap, libPPMap);
        for (String expName : filePPMap.keySet()) {
            logger.info(expName + " ----------------------");
            logger.info("File : " + getCoverage(filePPMap.get(expName), libPPMap));
            logger.info("Propro: " + getCoverage(proproPPMap.get(expName), libPPMap));
        }
    }

    @Override
    public void compareMatrixReplicate(String projectId, String filePath, String expIdA, String expIdB, int overviewIndex, boolean isUnique) {
        String expNameA = experimentService.getById(expIdA).getModel().getName();
        String expNameB = experimentService.getById(expIdB).getModel().getName();
        logger.info(expNameA + " & " + expNameB + " ----------------------------");
        HashMap<String, HashSet<String>> matPepSetMap = getMatrixFilePepMap(projectId, filePath);
        HashSet<String> aFilePepSet = matPepSetMap.get(expNameA);
        HashSet<String> bFilePepSet = matPepSetMap.get(expNameB);
        logger.info("FileA Pep: " + aFilePepSet.size());
        logger.info("FileB Pep: " + bFilePepSet.size());
        logger.info("File Pep Intersection: " + getIntersectionCount(aFilePepSet, bFilePepSet));

        HashMap<String, HashSet<String>> matProtSetMap = getMatrixFileProtMap(projectId, filePath, isUnique);
        HashSet<String> aFileProtSet = matProtSetMap.get(expNameA);
        HashSet<String> bFileProtSet = matProtSetMap.get(expNameB);
        logger.info("FileA Prot: " + aFileProtSet.size());
        logger.info("FileB Prot: " + bFileProtSet.size());
        logger.info("File Prot Intersection: " + getIntersectionCount(aFileProtSet, bFileProtSet));

        AnalyseOverviewDO aAnalyseOverviewDO = analyseOverviewService.getAllByExpId(expIdA).get(overviewIndex);
        AnalyseOverviewDO bAnalyseOverviewDO = analyseOverviewService.getAllByExpId(expIdB).get(overviewIndex);
        HashSet<String> aProproPepSet = getProproPeptideRefs(aAnalyseOverviewDO.getId());
        HashSet<String> bProproPepSet = getProproPeptideRefs(bAnalyseOverviewDO.getId());
        logger.info("ProproA Pep: " + aProproPepSet.size());
        logger.info("ProproB Pep: " + bProproPepSet.size());
        logger.info("Propro Pep Intersection: " + getIntersectionCount(aProproPepSet, bProproPepSet));

        HashSet<String> aProproProtSet = getProproProteins(aAnalyseOverviewDO.getId(), isUnique);
        HashSet<String> bProproProtSet = getProproProteins(bAnalyseOverviewDO.getId(), isUnique);
        logger.info("ProproA Prot: " + aProproProtSet.size());
        logger.info("ProproB Prot: " + bProproProtSet.size());
        logger.info("Propro Prot Intersection: " + getIntersectionCount(aProproProtSet, bProproProtSet));

        logger.info("A Pep Intersection: " + getIntersectionCount(aFilePepSet, aProproPepSet));
        logger.info("A Prot Intersection: " + getIntersectionCount(aFileProtSet, aProproProtSet));
        logger.info("B Pep Intersection: " + getIntersectionCount(bFilePepSet, bProproPepSet));
        logger.info("B Prot Intersection: " + getIntersectionCount(bFileProtSet, bProproProtSet));
    }

    @Override
    public void compareMatrix(String projectId, String filePath, int overviewIndex, boolean isUnique) {
        HashMap<String, HashSet<String>> matPepSetMap = getMatrixFilePepMap(projectId, filePath);
        HashMap<String, HashSet<String>> matProtSetMap = getMatrixFileProtMap(projectId, filePath, isUnique);
        List<ExperimentDO> experimentDOList = experimentService.getAllByProjectId(projectId);
        for (ExperimentDO experimentDO : experimentDOList) {
            HashSet<String> filePepSet = matPepSetMap.get(experimentDO.getName());
            HashSet<String> fileProtSet = matProtSetMap.get(experimentDO.getName());

            AnalyseOverviewDO analyseOverviewDO = analyseOverviewService.getAllByExpId(experimentDO.getId()).get(overviewIndex);
            HashSet<String> proproPepSet = getProproPeptideRefs(analyseOverviewDO.getId());
            HashSet<String> proproProtSet = getProproProteins(analyseOverviewDO.getId(), isUnique);

            logger.info(experimentDO.getName() + " ------------------------------");
            logger.info("Propro Pep: " + proproPepSet.size());
            logger.info("File Pep: " + filePepSet.size());
            logger.info("Intersection Pep: " + getIntersectionCount(filePepSet, proproPepSet));
            logger.info("Propro Prot: " + proproProtSet.size());
            logger.info("File Prot: " + fileProtSet.size());
            logger.info("Intersection Prot: " + getIntersectionCount(fileProtSet, proproProtSet));
        }

    }

    @Override
    public void printSilacResults(String analyseOverviewId, String filePath) {
        String SILAC_UNIMOD = "(Unimod:188)";
        HashMap<String, Pair<Double, Double>> filePepFdrRtMap = getFilePepFdrRtMap(filePath);
        int fileHeavy = 0, fileLight = 0;
        HashMap<String, Double> fileLightMap = new HashMap<>();
        HashMap<String, Double> fileHeavyMap = new HashMap<>();
        for (String pepRef : filePepFdrRtMap.keySet()) {
            String[] pepInfo = pepRef.split("_");
            if (pepInfo[0].endsWith(SILAC_UNIMOD)) {
                fileHeavy++;
                fileHeavyMap.put(pepRef.replace(SILAC_UNIMOD, ""), filePepFdrRtMap.get(pepRef).getRight());
            } else {
                fileLight++;
                fileLightMap.put(pepRef, filePepFdrRtMap.get(pepRef).getRight());
            }
        }
        logger.info("File light peptide count: " + fileLight);
        logger.info("File heavy peptide count: " + fileHeavy);
        logger.info("Intersection File: " + getIntersectionCount(new HashSet<>(fileLightMap.keySet()), new HashSet<>(fileHeavyMap.keySet())));

        HashMap<String, Pair<Double, Double>> proproPepFdrRtMap = getProproPepFdrRtMap(analyseOverviewId);

        HashMap<String, Double> proproLightMap = new HashMap<>();
        HashMap<String, Double> proproHeavyMap = new HashMap<>();
        int proproHeavy = 0, proproLight = 0;
        for (String pepRef : proproPepFdrRtMap.keySet()) {
            if (pepRef.split("_")[0].endsWith(SILAC_UNIMOD)) {
                proproHeavy++;
                proproHeavyMap.put(pepRef.replace(SILAC_UNIMOD, ""), proproPepFdrRtMap.get(pepRef).getRight());
            } else {
                proproLight++;
                proproLightMap.put(pepRef, proproPepFdrRtMap.get(pepRef).getRight());
            }
        }
        logger.info("Propro light peptide count: " + proproLight);
        logger.info("Propro heavy peptide count: " + proproHeavy);
        logger.info("Intersection Propro: " + getIntersectionCount(new HashSet<>(proproLightMap.keySet()), new HashSet<>(proproHeavyMap.keySet())));
        logger.info("Intersection Light: " + getIntersectionCount(new HashSet<>(proproLightMap.keySet()), new HashSet<>(fileLightMap.keySet())));
        logger.info("Intersection Heavy: " + getIntersectionCount(new HashSet<>(proproHeavyMap.keySet()), new HashSet<>(fileHeavyMap.keySet())));
    }

    @Override
    public void printProtResults(String analyseOverviewId, String filePath, boolean isUnique) {
        //Step 1: load pyprophet result
        HashSet<String> fileProtSet = getFileProteins(filePath, isUnique);
        logger.info("File prot count: " + fileProtSet.size());

        //Step 2: load propro result
        HashSet<String> proproProtSet = getProproProteins(analyseOverviewId, isUnique);
        logger.info("Propro prot count: " + proproProtSet.size());

        logger.info("Intersection prot count: " + getIntersectionCount(fileProtSet, proproProtSet));
    }

    @Override
    public void printPepResults(String analyseOverviewId, String filePath) {
        //Step 1: load pyprophet result
        HashSet<String> filePepSet = getFilePeptideRefs(filePath);
        logger.info("File pep count: " + filePepSet.size());

        //Step 2: load propro result
        HashSet<String> proproPepSet = getProproPeptideRefs(analyseOverviewId);
        logger.info("Propro pep count: " + proproPepSet.size());

        logger.info("Intersection pep count: " + getIntersectionCount(filePepSet, proproPepSet));
    }

    @Override
    public void printSeqResults(String analyseOverviewId, String filePath) {
        //Step 1: load pyprophet result
        HashSet<String> fileSeqSet = getFilePeptideSeqs(filePath);
        logger.info("File seq count: " + fileSeqSet.size());

        //Step 2: load propro result
        HashSet<String> proproSeqSet = getProproPeptideSeqs(analyseOverviewId);
        logger.info("Propro seq count: " + proproSeqSet.size());

        logger.info("Intersection Prot count: " + getIntersectionCount(fileSeqSet, proproSeqSet));
    }

    @Override
    public void printProproOnlyPep(String analyseOverviewId, String filePath, int length) {
        HashSet<String> filePepSet = getFilePeptideSeqs(filePath);
        HashSet<String> proproPepSet = getProproPeptideSeqs(analyseOverviewId);
        logger.info(getLeftSetOnly(proproPepSet, filePepSet, length));
    }

    @Override
    public void printFileOnlyPep(String analyseOverviewId, String filePath, int length) {
        HashSet<String> filePepSet = getFilePeptideRefs(filePath);
        HashSet<String> proproPepSet = getProproPeptideRefs(analyseOverviewId);
        logger.info(getLeftSetOnly(filePepSet, proproPepSet, length));
    }

    private String getLeftSetOnly(HashSet<String> leftSet, HashSet<String> rightSet, int length) {
        int index = 0;
        StringBuilder leftOnly = new StringBuilder();
        for (String element : leftSet) {
            if (!rightSet.contains(element)) {
                if (length != -1 && index < length) {
                    leftOnly.append(element).append(";");
                    index++;
                } else {
                    break;
                }
            }
        }
        return leftOnly.toString();
    }

    private HashSet<String> getFileProteins(String filePath, boolean isUnique) {
        HashSet<String> uniqueProt = new HashSet<>();
        try {
            TableFile ppFile = FileUtil.readTableFile(filePath);
                HashMap<String, Integer> columnMap = ppFile.getColumnMap();
                List<String[]> fileData = ppFile.getFileData();
                for (String[] lineSplit : fileData) {
                    if (columnMap.containsKey("decoy") && lineSplit[columnMap.get("decoy")].equals("1")) {
                        continue;
                    }
                    if (columnMap.containsKey("m_score") && Double.parseDouble(lineSplit[columnMap.get("m_score")]) > 0.01d) {
                        continue;
                    }
                    String proteinName = lineSplit[columnMap.get("proteinname")];
                if (isUnique && !proteinName.startsWith("1/")) {
                    continue;
                }
                uniqueProt.add(proteinName);
            }
            return uniqueProt;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    private HashSet<String> getFilePeptideRefs(String filePath) {
        HashSet<String> uniquePep = new HashSet<>();
        try {
            TableFile ppFile = FileUtil.readTableFile(filePath);
            HashMap<String, Integer> columnMap = ppFile.getColumnMap();
            List<String[]> fileData = ppFile.getFileData();
            for (String[] lineSplit : fileData) {
                if (columnMap.containsKey("decoy") && lineSplit[columnMap.get("decoy")].equals("1")) {
                    continue;
                }
                if (columnMap.containsKey("m_score") && Double.parseDouble(lineSplit[columnMap.get("m_score")]) > 0.01d) {
                    continue;
                }
                String[] peptideGroupInfo = lineSplit[columnMap.get("transition_group_id")].split("_");
                uniquePep.add(peptideGroupInfo[1] + "_" + peptideGroupInfo[2]);
            }
            return uniquePep;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    private HashMap<String, Pair<Double, Double>> getFilePepFdrRtMap(String filePath) {
        HashMap<String, Pair<Double, Double>> bestPepRtMap = new HashMap<>();
        try {
            TableFile ppFile = FileUtil.readTableFile(filePath);
            HashMap<String, Integer> columnMap = ppFile.getColumnMap();
            List<String[]> fileData = ppFile.getFileData();
            for (String[] lineSplit : fileData) {
                if (columnMap.containsKey("decoy") && lineSplit[columnMap.get("decoy")].equals("1")) {
                    continue;
                }
                if (columnMap.containsKey("m_score") && Double.parseDouble(lineSplit[columnMap.get("m_score")]) > 0.01d) {
                    continue;
                }
                String[] peptideGroupInfo = lineSplit[columnMap.get("transition_group_id")].split("_");
                String pepRef = peptideGroupInfo[1] + "_" + peptideGroupInfo[2];
                Double newMScore = Double.parseDouble(lineSplit[columnMap.get("m_score")]);
                if (bestPepRtMap.containsKey(pepRef)) {
                    Double mScore = bestPepRtMap.get(pepRef).getLeft();
                    if (newMScore < mScore) {
                        bestPepRtMap.put(pepRef, Pair.of(newMScore, Double.parseDouble(lineSplit[columnMap.get("rt")])));
                    }
                } else {
                    bestPepRtMap.put(pepRef, Pair.of(newMScore, Double.parseDouble(lineSplit[columnMap.get("rt")])));
                }
            }
            return bestPepRtMap;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private HashMap<String, Pair<Double, Double>> getProproPepFdrRtMap(String overviewId) {
        List<AnalyseDataDO> analyseDataDOList = getProproDataDO(overviewId);
        HashMap<String, Pair<Double, Double>> pepFdrRtMap = new HashMap<>();
        for (AnalyseDataDO dataDO : analyseDataDOList) {
            pepFdrRtMap.put(dataDO.getPeptideRef(), Pair.of(dataDO.getFdr(), dataDO.getBestRt()));
        }
        return pepFdrRtMap;
    }

    private HashSet<String> getFilePeptideSeqs(String filePath) {
        HashSet<String> uniqueSeq = new HashSet<>();
        try {
            TableFile ppFile = FileUtil.readTableFile(filePath);
            HashMap<String, Integer> columnMap = ppFile.getColumnMap();
            List<String[]> fileData = ppFile.getFileData();
            for (String[] lineSplit : fileData) {
                if (columnMap.containsKey("decoy") && lineSplit[columnMap.get("decoy")].equals("1")) {
                    continue;
                }
                if (columnMap.containsKey("m_score") && Double.parseDouble(lineSplit[columnMap.get("m_score")]) > 0.01d) {
                    continue;
                }
                uniqueSeq.add(lineSplit[columnMap.get("sequence")]);
            }
            return uniqueSeq;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    @Override
    public HashSet<String> getProproProteins(String overviewId, boolean isUnique) {
        List<AnalyseDataDO> analyseDataDOList = getProproDataDO(overviewId);

        HashSet<String> uniqueProt = new HashSet<>();
        for (AnalyseDataDO dataDO : analyseDataDOList) {
            if (isUnique && (!dataDO.getIsUnique() || !dataDO.getProteinName().startsWith("1/"))) {
                continue;
            } else {
                uniqueProt.add(dataDO.getProteinName());
            }
        }
        return uniqueProt;
    }

    @Override
    public HashSet<String> getProproPeptideRefs(String overviewId) {
        List<AnalyseDataDO> analyseDataDOList = getProproDataDO(overviewId);
        HashSet<String> uniquePep = new HashSet<>();
        for (AnalyseDataDO dataDO : analyseDataDOList) {
            uniquePep.add(dataDO.getPeptideRef());
        }
        return uniquePep;
    }

    private HashSet<String> getProproPeptideSeqs(String overviewId) {
        List<AnalyseDataDO> analyseDataDOList = getProproDataDO(overviewId);
        HashSet<String> uniqueSeq = new HashSet<>();
        for (AnalyseDataDO dataDO : analyseDataDOList) {
            uniqueSeq.add(peptideService.getById(dataDO.getPeptideId()).getModel().getSequence());
        }
        return uniqueSeq;
    }

    @Override
    public HashMap<String, HashSet<String>> getMatrixFilePepMap(String projectId, String filePath) {
        List<ExperimentDO> experimentDOList = experimentService.getAllByProjectId(projectId);
        HashMap<String, HashSet<String>> matPepSetMap = new HashMap<>();
        for (ExperimentDO experimentDO : experimentDOList) {
            matPepSetMap.put(experimentDO.getName(), new HashSet<>());
        }
        try {
            TableFile ppFile = FileUtil.readTableFile(filePath);
            HashMap<String, Integer> columnMap = ppFile.getColumnMap();
            List<String[]> fileData = ppFile.getFileData();
            for (String[] line : fileData) {
                for (String expName : matPepSetMap.keySet()) {
                    Integer index = columnMap.get(expName.toLowerCase() + "");
                    if (index != null && index < line.length && !line[index].isEmpty() && !line[index].equals("NA")) {
                        String[] pepInfo = line[0].split("_");
                        matPepSetMap.get(expName).add(pepInfo[1] + "_" + pepInfo[2]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return matPepSetMap;
    }

    private HashMap<String, HashSet<String>> getMatrixFileProtMap(String projectId, String filePath, boolean isUnique) {
        List<ExperimentDO> experimentDOList = experimentService.getAllByProjectId(projectId);
        HashMap<String, HashSet<String>> matProtSetMap = new HashMap<>();
        for (ExperimentDO experimentDO : experimentDOList) {
            matProtSetMap.put(experimentDO.getName(), new HashSet<>());
        }
        try {
            TableFile ppFile = FileUtil.readTableFile(filePath);
            HashMap<String, Integer> columnMap = ppFile.getColumnMap();
            List<String[]> fileData = ppFile.getFileData();
            for (String[] line : fileData) {
                if (isUnique && !line[1].startsWith("1/")) {
                    continue;
                }
                for (String expName : matProtSetMap.keySet()) {
                    Integer index = columnMap.get(expName.toLowerCase() + "");
                    if (index != null && index < line.length && !line[index].isEmpty() && !line[index].equals("NA")) {
                        matProtSetMap.get(expName).add(line[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return matProtSetMap;
    }

    private int getIntersectionCount(HashSet<String> set1, HashSet<String> set2) {
        int count = 0;
        for (String word : set1) {
            if (set2.contains(word)) {
                count++;
            }
        }
        return count;
    }

    private HashMap<String, Double> getLibPPMap(String libraryId) {
        HashMap<String, Double> libCountMap = new HashMap<>();
        List<PeptideDO> peptideDOList = peptideService.getAllByLibraryId(libraryId);
        for (PeptideDO peptideDO : peptideDOList) {
            Double count = libCountMap.get(peptideDO.getProteinName());
            if (count == null) {
                libCountMap.put(peptideDO.getProteinName(), 1d);
            } else {
                libCountMap.put(peptideDO.getProteinName(), count + 1d);
            }
        }
        return libCountMap;
    }

    private HashMap<String, HashMap<String, Double>> getFilePPMap(String projectId, String filePath) {
        HashMap<String, HashMap<String, Double>> fileCountMap = new HashMap<>();
        List<ExperimentDO> experimentDOList = experimentService.getAllByProjectId(projectId);
        try {
            TableFile ppFile = FileUtil.readTableFile(filePath);
            HashMap<String, Integer> columnMap = ppFile.getColumnMap();
            List<String[]> fileData = ppFile.getFileData();
            for (ExperimentDO experimentDO : experimentDOList) {
                Integer index = columnMap.get(experimentDO.getName().toLowerCase() + "_with_dscore_filtered");
                HashMap<String, Double> expCountMap = new HashMap<>();
                for (String[] line : fileData) {
                    if (index != null && index < line.length && !line[index].isEmpty()) {
                        Double count = expCountMap.get(line[1]);
                        if (count == null) {
                            expCountMap.put(line[1], 1d);
                        } else {
                            expCountMap.put(line[1], count + 1d);
                        }
                    }
                }
                fileCountMap.put(experimentDO.getName(), expCountMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileCountMap;
    }

    private HashMap<String, HashMap<String, Double>> getProproPPMap(String projectId) {
        HashMap<String, HashMap<String, Double>> proproCountMap = new HashMap<>();
        List<ExperimentDO> experimentList = experimentService.getAllByProjectId(projectId);
        for (ExperimentDO experimentDO : experimentList) {
            HashMap<String, Double> expCountMap = new HashMap<>();
            AnalyseOverviewDO analyseOverviewDO = analyseOverviewService.getAllByExpId(experimentDO.getId()).get(0);
            List<AnalyseDataDO> analyseDataList = getProproDataDO(analyseOverviewDO.getId());
            for (AnalyseDataDO analyseDataDO : analyseDataList) {
                expCountMap.merge(analyseDataDO.getProteinName(), 1d, Double::sum);
            }
            proproCountMap.put(experimentDO.getName(), expCountMap);
        }
        return proproCountMap;
    }

    private HashMap<String, HashMap<String, Double>> getProjectCoverageMap(HashMap<String, HashMap<String, Double>> projectPPMap, HashMap<String, Double> libPPMap) {
        HashMap<String, HashMap<String, Double>> coverageMap = new HashMap<>();
        for (String expName : projectPPMap.keySet()) {
            coverageMap.put(expName, getCoverageMap(projectPPMap.get(expName), libPPMap));
        }
        return coverageMap;
    }

    private HashMap<String, Double> getCoverageMap(HashMap<String, Double> expPPMap, HashMap<String, Double> libPPMap) {
        HashMap<String, Double> coverageMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : expPPMap.entrySet()) {
            Double libCount = libPPMap.get(entry.getKey());
            if (libCount != null) {
                coverageMap.put(entry.getKey(), entry.getValue() / libCount);
            }
        }
        return coverageMap;
    }

    private Double getCoverage(HashMap<String, Double> expPPMap, HashMap<String, Double> libPPMap) {
        int count = 0;
        double sum = 0d;
        for (Map.Entry<String, Double> entry : expPPMap.entrySet()) {
            Double libCount = libPPMap.get(entry.getKey());
            if (libCount != null) {
                sum += entry.getValue() / libCount;
                count++;
            }
        }
        return sum / count;
    }

    private List<AnalyseDataDO> getProproDataDO(String overviewId) {
        AnalyseDataQuery query = new AnalyseDataQuery();
        query.setOverviewId(overviewId);
        query.setIsDecoy(false);
        query.addIndentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_SUCCESS);
        List<AnalyseDataDO> analyseDataDOList = analyseDataService.getAll(query);
        return analyseDataDOList;
    }
}
