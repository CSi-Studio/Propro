package com.csi.propro.algorithm.parser;

import com.csi.propro.algorithm.formula.FragmentFactory;
import com.csi.propro.constants.Constants;
import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.constants.enums.Unimod;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.bean.peptide.Annotation;
import com.csi.propro.domain.bean.score.BYSeries;
import com.csi.propro.domain.db.FragmentInfo;
import com.csi.propro.domain.db.LibraryDO;
import com.csi.propro.domain.db.PeptideDO;
import com.csi.propro.domain.db.TaskDO;
import com.csi.propro.service.LibraryService;
import com.csi.propro.service.TaskService;
import com.csi.propro.utils.PeptideUtil;
import com.csi.propro.algorithm.decoy.generator.ShuffleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.csi.propro.utils.PeptideUtil.parseModification;

/**
 * Created by Nico Wang
 * Time: 2019-04-23 14:52
 */
@Component("msmsParser")
public class MsmsParser extends BaseLibraryParser {

    @Autowired
    TaskService taskService;
    @Autowired
    LibraryService libraryService;
    @Autowired
    FragmentFactory fragmentFactory;
    @Autowired
    ShuffleGenerator shuffleGenerator;

    public final Logger logger = LoggerFactory.getLogger(MsmsParser.class);

    @Override
    public ResultDO parseAndInsert(InputStream in, LibraryDO library, TaskDO taskDO) {

        try {
            //开始插入前先清空原有的数据库数据
            ResultDO resultDOTmp = peptideService.deleteAllByLibraryId(library.getId());
            if (resultDOTmp.isFailed()) {
                logger.error(resultDOTmp.getMsgInfo());
                return ResultDO.buildError(ResultCode.DELETE_ERROR);
            }
            taskDO.addLog("Delete the old data already, starting parsing file, 删除旧数据完毕,开始文件解析");
            taskService.update(taskDO);

            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
            if (line == null) {
                return ResultDO.buildError(ResultCode.LINE_IS_EMPTY);
            }
            HashMap<String, Integer> columnMap = parseColumns(line);

            //以sequence为单位进行批处理
            String lastSequence = "";
            List<String[]> sequenceInExps = new ArrayList<>();
            HashMap<String, PeptideDO> libPepMap = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                String[] row = line.split("\t");
                String sequence = row[columnMap.get("sequence")];
                if (sequence.equals(lastSequence)) {
                    sequenceInExps.add(row);
                } else {
                    HashMap<String, PeptideDO> peptideDOMap = parseSequence(sequenceInExps, columnMap, library);
                    libPepMap.putAll(peptideDOMap);
                    //deal with same sequence
                    sequenceInExps.clear();
                    sequenceInExps.add(row);
                    lastSequence = sequence;
                }
            }
            for (PeptideDO peptide: libPepMap.values()){
                shuffleGenerator.generate(peptide);
            }
            peptideService.insertAll(new ArrayList<>(libPepMap.values()), false);
            taskDO.addLog(libPepMap.size() + "条肽段数据插入成功");
            taskService.update(taskDO);
            logger.info(libPepMap.size() + "条肽段数据插入成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResultDO(true);
    }

    @Override
    public ResultDO selectiveParseAndInsert(InputStream in, LibraryDO library, HashSet<String> selectedPepSet, boolean selectBySequence, TaskDO taskDO) {

        ResultDO<List<PeptideDO>> tranResult = new ResultDO<>(true);
        try {
            //开始插入前先清空原有的数据库数据
            ResultDO resultDOTmp = peptideService.deleteAllByLibraryId(library.getId());
            if (resultDOTmp.isFailed()) {
                logger.error(resultDOTmp.getMsgInfo());
                return ResultDO.buildError(ResultCode.DELETE_ERROR);
            }
            taskDO.addLog("删除旧数据完毕,开始文件解析");
            taskService.update(taskDO);

            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
            if (line == null) {
                return ResultDO.buildError(ResultCode.LINE_IS_EMPTY);
            }
            HashMap<String, Integer> columnMap = parseColumns(line);

            boolean withCharge = new ArrayList<>(selectedPepSet).get(0).contains("_");
            HashSet<String> selectedSeqSet = new HashSet<>();
            for (String pep: selectedPepSet) {
                if (withCharge){
                    selectedSeqSet.add(PeptideUtil.removeUnimod(pep.split("_")[0]));
                }else {
                    selectedSeqSet.add(PeptideUtil.removeUnimod(pep));
                }
            }
            String lastSequence = "";
            List<String[]> selectedRowList = new ArrayList<>();
            List<PeptideDO> selectedPepList = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] row = line.split("\t");
                String sequence = row[columnMap.get("sequence")];
                if (sequence.equals(lastSequence)) {
                    //not empty means choosed
                    if (!selectedRowList.isEmpty()) {
                        selectedRowList.add(row);
                    }
                } else {
                    HashMap<String, PeptideDO> peptideDOMap = parseSequence(selectedRowList, columnMap, library);
                    if (selectBySequence){
                        selectedPepList = new ArrayList<>(peptideDOMap.values());
                    }else {
                        for (String pepRef: peptideDOMap.keySet()){
                            if (withCharge && selectedPepSet.contains(pepRef)){
                                selectedPepList.add(peptideDOMap.get(pepRef));
                                selectedPepSet.remove(pepRef);
                            }
                            if (!withCharge && selectedPepSet.contains(peptideDOMap.get(pepRef).getFullName())){
                                selectedPepList.add(peptideDOMap.get(pepRef));
                                selectedPepSet.remove(peptideDOMap.get(pepRef).getFullName());
                            }
                        }
                    }
                    //deal with same sequence
                    selectedRowList.clear();
                    if (selectedSeqSet.contains(sequence)) {
                        selectedRowList.add(row);
                    }
                    lastSequence = sequence;
                }
            }

            for (PeptideDO peptide: selectedPepList){
                shuffleGenerator.generate(peptide);
            }

            peptideService.insertAll(selectedPepList, false);
            taskDO.addLog(selectedPepList.size() + "条肽段数据插入成功");
            taskService.update(taskDO);
            logger.info(selectedPepList.size() + "条肽段数据插入成功");
            logger.info("在选中的" + selectedSeqSet.size() + "条肽段中, 有" + selectedPepSet.size() + "条没有在库中找到");
            logger.info(selectedPepSet.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tranResult;
    }

    private HashMap<String, PeptideDO> parseSequence(List<String[]> sequenceInExps, HashMap<String, Integer> columnMap, LibraryDO library){
        HashMap<String, Float> scoreMap = new HashMap<>();
        HashMap<String,String[]> peptideRefMap = new HashMap<>();
        for (String[] row: sequenceInExps){
            String modifiedSequence = row[columnMap.get("modifiedsequence")].replace("_","");
            if (modifiedSequence.contains("(")){
                modifiedSequence = replaceModification(modifiedSequence);
            }

            String charge = row[columnMap.get("charge")];
            String peptideRef = modifiedSequence + "_" + charge;
            Float pepScore = Float.parseFloat(row[columnMap.get("pep")]);
            if (scoreMap.get(peptideRef) == null || scoreMap.get(peptideRef) < pepScore){
                scoreMap.put(peptideRef, pepScore);
                peptideRefMap.put(peptideRef, row);
            }
        }
        HashMap<String, PeptideDO> peptideDOMap = new HashMap<>();
        for (String peptideRef: peptideRefMap.keySet()){
            PeptideDO peptideDO = new PeptideDO();
            String[] row = peptideRefMap.get(peptideRef);
            String[] ionArray = row[columnMap.get("matches")].split(";");
            String[] massArray = row[columnMap.get("masses")].split(";");
            setFragmentInfo(peptideDO, ionArray, row[columnMap.get("intensities")].split(";"), massArray);
            peptideDO.setMz(Double.parseDouble(row[columnMap.get("m/z")]));
            String protName = row[columnMap.get("proteins")];
            peptideDO.setProteinName(protName.startsWith("sp|iRT") ? "iRT" : protName);

            peptideDO.setLibraryId(library.getId());
            peptideDO.setSequence(row[columnMap.get("sequence")]);

            peptideDO.setCharge(Integer.parseInt(row[columnMap.get("charge")]));
            peptideDO.setRt(Double.parseDouble(row[columnMap.get("retentiontime")]));
            peptideDO.setFullName(peptideRef.split("_")[0]);
            PeptideUtil.parseModification(peptideDO);
            verifyUnimod(ionArray, massArray, peptideDO.getUnimodMap(), peptideDO.getSequence(), Double.parseDouble(row[columnMap.get("mass")]));
            peptideDO.setFullName(getPeptideFullName(peptideDO.getSequence(), peptideDO.getUnimodMap()));
            peptideDO.setPeptideRef(peptideDO.getFullName() + "_" + peptideDO.getCharge());
            peptideDOMap.put(peptideDO.getPeptideRef(), peptideDO);
        }
        return peptideDOMap;
    }

    private String replaceModification(String modifiedSequence){
        String replacedSequence = null;
        for (Unimod unimod: Unimod.values()){
            replacedSequence = modifiedSequence.replace(unimod.getMsmsAbbr(), unimod.getSerialName());
        }
        return replacedSequence;
    }

    private void setFragmentInfo(PeptideDO peptideDO, String[] ionArray, String[] intensityArray, String[] massArray){
        //
        peptideDO.setFragmentMap(new HashMap<>());
        for (int i=0; i<ionArray.length; i++){
            String cutInfo = ionArray[i];
            if (!cutInfo.startsWith("b") && !cutInfo.startsWith("y")){
                continue;
            }
            if (cutInfo.contains("-")){
                continue;
            }
            Double intensity = Double.parseDouble(intensityArray[i]);
            FragmentInfo fragmentInfo = new FragmentInfo(cutInfo, Double.parseDouble(massArray[i]), intensity, 1);
            ResultDO<Annotation> resultDO = parseAnnotation(cutInfo);
            fragmentInfo.setAnnotations(cutInfo);
            fragmentInfo.setAnnotation(resultDO.getModel());
            if (fragmentInfo.getAnnotation().getCharge() != 1){
                fragmentInfo.setCharge(fragmentInfo.getAnnotation().getCharge());
            }
            peptideDO.getFragmentMap().put(cutInfo, fragmentInfo);
        }
    }

    private String getPeptideFullName(String sequence, HashMap<Integer, String> unimodMap){
        int length = sequence.length();
        int offset = 0;
        if (unimodMap == null){
            return sequence;
        }
        for (int i=0; i<length; i++){
            if (unimodMap.get(i) != null){
                sequence = sequence.substring(0, i + offset + 1) + "(UniMod:" + unimodMap.get(i) + ")" + sequence.substring(i + offset + 1);
                offset = unimodMap.get(i).length() + 9;
            }
        }
        return sequence;
    }

    public boolean verifyUnimod(String[] ionArray, String[] massArray, HashMap<Integer, String> unimodMap, String sequence, Double mass){
        //check total mass
        double massDiff = mass - fragmentFactory.getTheoryMass(unimodMap, sequence);
        if (massDiff < Constants.ELEMENT_TOLERANCE){
            return true;
        }

        BYSeries bySeries = fragmentFactory.getBYSeries(unimodMap, sequence, 1);
        List<Double> bSeries = bySeries.getBSeries();
        List<Double> ySeries = bySeries.getYSeries();
        String[] bModInfoArray = new String[sequence.length()];
        String[] yModInfoArray = new String[sequence.length()];
        Double bCompensateMz = 0d, yCompensateMz = 0d;
        int lastBPosition = 0, lastYPosition = 0;
        int bMax = 0, yMax = 0;
        //get b,y map; default y2,y3,b2,b3
        for (int i = 0; i < ionArray.length; i++){
            String cutInfo = ionArray[i];
            if (cutInfo.contains("-") || cutInfo.contains("+")){
                continue;
            }
            double fragmentMz = Double.parseDouble(massArray[i]);
            int position = Integer.parseInt(ionArray[i].substring(1));
            if (cutInfo.startsWith("b")){
                if (position > bMax){
                    bMax = position;
                }
                double theoMz = bSeries.get(position - 1) + bCompensateMz;
                double mzDiff = fragmentMz - theoMz;
                //TODO: @Nico infer all kinds of unimods
                if (mzDiff > Constants.ELEMENT_TOLERANCE){
                    String roundModMz = Long.toString(Math.round(mzDiff));
                    if (position - lastBPosition == 1){
                        bModInfoArray[position - 1] = "1;" + roundModMz;
                    }else {
                        for (int pos = lastBPosition; pos < position; pos ++){
                            bModInfoArray[pos] = "2;" + lastBPosition + ";" + roundModMz;
                        }
                    }
                }
                lastBPosition = position;
                bCompensateMz += mzDiff;
                continue;
            }
            if (cutInfo.startsWith("y")){
                if (position > yMax){
                    yMax = position;
                }
                double theoMz = ySeries.get(position - 1) + yCompensateMz;
                double mzDiff = fragmentMz - theoMz;
                if (mzDiff > Constants.ELEMENT_TOLERANCE){
                    String roundModMz = Long.toString(Math.round(mzDiff));
                    if (position - lastYPosition == 1){
                        yModInfoArray[sequence.length() - position] = "1;" + roundModMz;
                    }else {
                        for (int pos = lastYPosition; pos < position; pos ++){
                            yModInfoArray[sequence.length() - lastYPosition - 1] = "2;" + lastYPosition + ";" + roundModMz;
                        }
                    }
                }
                lastYPosition = position;
                yCompensateMz += mzDiff;
            }
        }
        //deal with unknown part
        int unknownBMz = (int) Math.round(massDiff - bCompensateMz);
        int unknownYMz = (int) Math.round(massDiff - yCompensateMz);
        if (bMax < sequence.length() && unknownBMz > 0){
            for (int i = bMax; i < sequence.length(); i++){
                bModInfoArray[i] = "2;" + bMax + ";" + unknownBMz;
            }
        }
        if (yMax < sequence.length() && unknownYMz > 0){
            for (int i = yMax; i < sequence.length(); i++){
                yModInfoArray[sequence.length() - i - 1] = "2;" + yMax + ";" + unknownYMz;
            }
        }

        HashMap<String, Integer> positionMzDiffMap = new HashMap<>();
        boolean isSuccess = true;
        for (int i = 0; i < sequence.length(); i++){
            if (bModInfoArray[i] == null || yModInfoArray[i] == null){
                continue;
            }
            if (bModInfoArray[i].startsWith("1") && yModInfoArray[i].startsWith("1")){
                int roundModMz = Integer.parseInt(bModInfoArray[i].split(";")[1]);
                boolean success = certainIntepreter(i, roundModMz, unimodMap);
                if (!success){
                    logger.info(sequence);
                    isSuccess = false;
                }
                continue;
            }
            if (bModInfoArray[i].startsWith("1") && yModInfoArray[i].startsWith("2")){
                boolean success = semicertainIntepreter(bModInfoArray, yModInfoArray, unimodMap, i);
                if (!success){
                    logger.info(sequence);
                    isSuccess = false;
                }
                continue;
            }
            if (bModInfoArray[i].startsWith("2") && yModInfoArray[i].startsWith("1")){
                boolean success = semicertainIntepreter(yModInfoArray, bModInfoArray, unimodMap, i);
                if (!success){
                    logger.info(sequence);
                    isSuccess = false;
                }
                continue;
            }
            if (bModInfoArray[i].startsWith("2") && yModInfoArray[i].startsWith("2")){
                int bRoundModMz = Integer.parseInt(bModInfoArray[i].split(";")[2]);
                int yRoundModMz = Integer.parseInt(yModInfoArray[i].split(";")[2]);
                if (bRoundModMz == yRoundModMz){
                    int groupIter = i + 1;
                    String bInfo = bModInfoArray[i].split(";")[1];
                    String yInfo = yModInfoArray[i].split(";")[1];
                    while (groupIter < bModInfoArray.length
                            && bModInfoArray[groupIter] != null && bModInfoArray[groupIter].split(";")[1].equals(bInfo)
                            && yModInfoArray[groupIter] != null && yModInfoArray[groupIter].split(";")[1].equals(yInfo)){
                        groupIter ++;
                    }
                    positionMzDiffMap.put(i + ";" + (groupIter-1), bRoundModMz);
                    i = groupIter - 1;
                } else if(bRoundModMz > yRoundModMz){
                    i = findModPosition(bModInfoArray, yModInfoArray, bRoundModMz, yRoundModMz, positionMzDiffMap, i);
                } else {
                    i = findModPosition(yModInfoArray, bModInfoArray, yRoundModMz, bRoundModMz, positionMzDiffMap, i);
                }
            }
        }
        boolean success = analysePosMzDiffMap(positionMzDiffMap, sequence, unimodMap);
        if (!success){
            isSuccess = false;
        }
        return isSuccess;
    }

    private boolean certainIntepreter(int index, int roundModMz, HashMap<Integer, String> unimodMap){
        if (roundModMz == 57){
            unimodMap.put(index, "4");
            return true;
        }else {
            logger.info("Modification is not UniMod:4");
            return false;
        }
    }

    private boolean semicertainIntepreter(String[] certainList, String[] uncertainList, HashMap<Integer, String> unimodMap, int i){
        int cRoundModMz = Integer.parseInt(certainList[i].split(";")[1]);
        int uncRoundModMz = Integer.parseInt(uncertainList[i].split(";")[2]);
        int newRoundModMz = 0;
        if (cRoundModMz != uncRoundModMz){
            newRoundModMz = uncRoundModMz - cRoundModMz;
        }
        String groupIdentifier = uncertainList[i].split(";")[1];
        int groupIter = i + 1;
        while (groupIter < certainList.length && uncertainList[groupIter] != null &&  uncertainList[groupIter].startsWith("2")){
            String[] modInfo = uncertainList[groupIter].split(";");
            if (!modInfo[1].equals(groupIdentifier)){
                break;
            }
            if (newRoundModMz == 0){
                uncertainList[groupIter] = null;
            }else {
                uncertainList[groupIter] = "2;" + groupIdentifier + ";" + newRoundModMz;
            }
            groupIter ++;
        }
        return certainIntepreter(i, cRoundModMz, unimodMap);
    }

    private boolean uncertainIntepreter(int start, int end, int roundMzDiff, String sequence, HashMap<Integer, String> unimodMap){
        //count C
        int count = 0;
        char[] charList = sequence.toCharArray();
        for (int i = start; i <= end; i++){
            if (charList[i] == 'C'){
                count ++;
            }
        }
        if (roundMzDiff/57 == count){
            for (int i = start; i <= end; i++){
                if (charList[i] == 'C'){
                    unimodMap.put(i, "4");
                }
            }
            return true;
        }else {
            logger.info("Multi Modification is not UniMod:4 " + sequence);
            return false;
        }
    }

    /**
     *
     * @param bigInfoArray      String[] bigInfoArray = new String[]{"2;1;114","2;1;114","2;1;114","2;1;114"};
     * @param smallInfoArray    String[] smallInfoArray = new String[]{"2;1;57","2;1;57",null,null};
     * @param bigRoundMz        114
     * @param smallRoundMz      57
     * @param positionMzDiffMap
     * @param i                 begin index of info arrays
     * @return                  new start position for info arrays
     */
    private int findModPosition(String[] bigInfoArray, String[] smallInfoArray, int bigRoundMz, int smallRoundMz, HashMap<String, Integer> positionMzDiffMap, int i){
        int groupIter = i;
        int rightBoundary = i;
        String bigInfo = bigInfoArray[i].split(";")[1];
        String smallInfo = smallInfoArray[i].split(";")[1];
        while (groupIter < bigInfoArray.length && bigInfoArray[groupIter] != null && bigInfoArray[groupIter].split(";")[1].equals(bigInfo)){
            bigInfoArray[groupIter] = "2;" + bigInfo + ";" + (bigRoundMz - smallRoundMz);
            if (smallInfoArray[groupIter] != null && smallInfoArray[groupIter].split(";")[1].equals(smallInfo)){
                rightBoundary = groupIter;
            }
            groupIter ++;
        }
        positionMzDiffMap.put(i + ";" + rightBoundary, smallRoundMz);
        return rightBoundary;
    }

    private boolean analysePosMzDiffMap(HashMap<String, Integer> positionMzDiffMap, String sequence, HashMap<Integer, String> unimodMap){
        boolean isSuccess = true;
        for (String key: positionMzDiffMap.keySet()){
            String[] startEnd = key.split(";");
            int startPosition = Integer.parseInt(startEnd[0]);
            int endPosition = Integer.parseInt(startEnd[1]);
            boolean success = analysePosMzDiff(startPosition, endPosition, positionMzDiffMap.get(key), sequence, unimodMap);
            if (!success){
                logger.info(sequence);
                isSuccess = false;
            }
        }
        return isSuccess;
    }

    private boolean analysePosMzDiff(int start, int end, int roundMzDiff, String sequence, HashMap<Integer, String> unimodMap){
        if (start == end){
            return certainIntepreter(start, roundMzDiff, unimodMap);
        }else {
            return uncertainIntepreter(start, end, roundMzDiff, sequence, unimodMap);
        }
    }

}
