package com.csi.propro.algorithm.parser;

import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.bean.peptide.Annotation;
import com.csi.propro.domain.db.FragmentInfo;
import com.csi.propro.domain.db.LibraryDO;
import com.csi.propro.domain.db.PeptideDO;
import com.csi.propro.domain.db.TaskDO;
import com.csi.propro.service.TaskService;
import com.csi.propro.utils.PeptideUtil;
import com.csi.propro.algorithm.decoy.generator.ShuffleGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.csi.propro.utils.PeptideUtil.parseModification;

/**
 * 对于TraML文件的高速解析引擎
 */
@Component("fastTraMLParser")
public class FastTraMLParser extends BaseLibraryParser {

    @Autowired
    TaskService taskService;
    @Autowired
    ShuffleGenerator shuffleGenerator;

    private static String PeptideListBeginMarker = "<CompoundList>";
    private static String TransitionListBeginMarker = "<TransitionList>";

    private static String PeptideMarker = "<Peptide";
    private static String ProteinNameMarker = "<ProteinRef";
    private static String RetentionTimeMarker = "<RetentionTime>";

    private static String TransitionMarker = "<Transition";
    private static String TransitionEndMarker = "</Transition>";
    private static String PrecursorMarker = "<Precursor>";

    private static String CvParamMarker = "<cvParam";
    private static String ValueMarker = "value=\"";
    private static String RefMarker = "ref=\"";

    @Override
    public ResultDO parseAndInsert(InputStream in, LibraryDO library, TaskDO taskDO) {
        ResultDO tranResult = new ResultDO(true);
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

            //parse Peptides
            HashMap<String, PeptideDO> peptideMap = parsePeptide(reader, library.getId());
            if (peptideMap == null || peptideMap.isEmpty()){
                throw new Exception();
            }

            //parse Transitions
            ResultDO resultDO = parseTransitions(reader, peptideMap);
            if (resultDO.isFailed()){
                throw new Exception();
            }

            for (PeptideDO peptide: peptideMap.values()){
                shuffleGenerator.generate(peptide);
            }

            peptideService.insertAll(new ArrayList<>(peptideMap.values()), false);
            taskDO.addLog(peptideMap.size() + "条肽段数据插入成功");
            taskService.update(taskDO);
            logger.info(peptideMap.size() + "条肽段数据插入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultDO.buildError(ResultCode.FILE_FORMAT_NOT_SUPPORTED);
        }
        return tranResult;
    }

    @Override
    public ResultDO selectiveParseAndInsert(InputStream in, LibraryDO library, HashSet<String> selectedPepSet, boolean selectBySequence, TaskDO taskDO) {
        ResultDO tranResult = new ResultDO(true);
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

            //parse Peptides
            HashMap<String,PeptideDO> peptideMap = selectiveParsePeptide(reader, library.getId(), selectedPepSet, selectBySequence);
            if (peptideMap == null || peptideMap.isEmpty()){
                throw new Exception();
            }

            //parse Transitions
            ResultDO resultDO = parseTransitions(reader, peptideMap);
            if (resultDO.isFailed()){
                throw new Exception();
            }

            for (PeptideDO peptide: peptideMap.values()){
                shuffleGenerator.generate(peptide);
            }

            peptideService.insertAll(new ArrayList<>(peptideMap.values()), false);
            taskDO.addLog(peptideMap.size() + "条肽段数据插入成功");
            taskService.update(taskDO);
            logger.info(peptideMap.size() + "条肽段数据插入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ResultDO.buildError(ResultCode.FILE_FORMAT_NOT_SUPPORTED);
        }
        return tranResult;
    }

    private void seekForBeginPosition(BufferedReader reader, String marker){
        String line;
        try {
            while ((line = reader.readLine()) != null){
                if (line.contains(marker)){
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String,PeptideDO> parsePeptide(BufferedReader reader, String libraryId){
        try {
            seekForBeginPosition(reader, PeptideListBeginMarker);
            HashMap<String,PeptideDO> peptideMap = new HashMap<>();
            String line, filePepRef = "";
            PeptideDO peptideDO = new PeptideDO();
            while ((line = reader.readLine()) != null){
                if (line.contains(TransitionListBeginMarker)){
                    break;
                }
                if (line.contains(PeptideMarker)){
                    filePepRef = line.split("\"")[1];
                    if (filePepRef.startsWith("DECOY")){
                        continue;
                    }
                    String[] pepInfo = filePepRef.split("_");
                    peptideDO.setPeptideRef(pepInfo[1] + "_" + pepInfo[2]);
                    peptideDO.setFullName(pepInfo[1]);
                    peptideDO.setSequence(PeptideUtil.removeUnimod(pepInfo[1]));
                    peptideDO.setCharge(Integer.parseInt(pepInfo[2]));
                    peptideDO.setLibraryId(libraryId);
                    PeptideUtil.parseModification(peptideDO);
                    continue;
                }
                if (peptideDO.getPeptideRef() != null && line.contains(ProteinNameMarker)){
                    String proteinName = line.split(RefMarker)[1].split("\"")[0];
                    peptideDO.setProteinName(proteinName);
                    continue;
                }
                if (peptideDO.getPeptideRef() != null && line.contains(RetentionTimeMarker)){
                    while ((line = reader.readLine()).contains(CvParamMarker)) {
                        if (line.contains(ValueMarker)){
                            String rt = line.split(ValueMarker)[1].split("\"")[0];
                            peptideDO.setRt(Double.parseDouble(rt));
                            break;
                        }
                    }
                    peptideMap.put(filePepRef, peptideDO);
                    peptideDO = new PeptideDO();
                }
            }
            return peptideMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private HashMap<String, PeptideDO> selectiveParsePeptide(BufferedReader reader, String libraryId, HashSet<String> selectedPepSet, boolean selectBySequence){
        try {
            boolean withCharge = new ArrayList<>(selectedPepSet).get(0).contains("_");
            if (selectBySequence){
                selectedPepSet = convertPepToSeq(selectedPepSet, withCharge);
            }
            seekForBeginPosition(reader, PeptideListBeginMarker);
            HashMap<String,PeptideDO> peptideMap = new HashMap<>();
            String line, filePepRef = "";
            PeptideDO peptideDO = new PeptideDO();
            while ((line = reader.readLine()) != null){
                if (line.contains(TransitionListBeginMarker)){
                    break;
                }
                if (line.contains(PeptideMarker)){
                    filePepRef = line.split("\"")[1];
                    if (filePepRef.startsWith("DECOY")){
                        continue;
                    }
                    String[] pepInfo = filePepRef.split("_");
                    String peptideRef = pepInfo[1] + "_" + pepInfo[2];
                    String fullName = pepInfo[1];
                    String sequence = PeptideUtil.removeUnimod(pepInfo[1]);
                    if (selectBySequence){
                        if (!selectedPepSet.contains(sequence)){
                            continue;
                        }
                    } else {
                        if (withCharge && !selectedPepSet.contains(peptideRef)){
                            continue;
                        }
                        if (!withCharge && !selectedPepSet.contains(fullName)){
                            continue;
                        }
                    }
                    peptideDO.setPeptideRef(peptideRef);
                    peptideDO.setFullName(fullName);
                    peptideDO.setSequence(sequence);
                    peptideDO.setCharge(Integer.parseInt(pepInfo[2]));
                    peptideDO.setLibraryId(libraryId);
                    PeptideUtil.parseModification(peptideDO);
                    continue;
                }
                if (peptideDO.getPeptideRef() != null && line.contains(ProteinNameMarker)){
                    String proteinName = line.split(RefMarker)[1].split("\"")[0];
                    peptideDO.setProteinName(proteinName);
                    continue;
                }
                if (peptideDO.getPeptideRef() != null && line.contains(RetentionTimeMarker)){
                    while ((line = reader.readLine()).contains(CvParamMarker)) {
                        if (line.contains(ValueMarker)){
                            String rt = line.split(ValueMarker)[1].split("\"")[0];
                            peptideDO.setRt(Double.parseDouble(rt));
                            break;
                        }
                    }
                    peptideMap.put(filePepRef, peptideDO);
                    peptideDO = new PeptideDO();
                }
            }
            return peptideMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ResultDO parseTransitions(BufferedReader reader, HashMap<String,PeptideDO> peptideMap){
        try {
            PeptideDO peptideDO = null;
            FragmentInfo fi = new FragmentInfo();
            String line, filePepRef;
            while ((line = reader.readLine()) != null) {
                if (peptideDO == null && line.contains(TransitionMarker)) {
                    filePepRef = line.split("\"")[3];
                    if (filePepRef.startsWith("DECOY")) {
                        continue;
                    }
                    peptideDO = peptideMap.get(filePepRef);
                }

                if (peptideDO != null && line.contains(TransitionEndMarker)) {
                    peptideDO.putFragment(fi.getCutInfo(), fi);
                    fi = new FragmentInfo();
                    peptideDO = null;
                    continue;
                }

                if (peptideDO != null && peptideDO.getMz() == null && line.contains(PrecursorMarker)) {
                    while ((line = reader.readLine()).contains(CvParamMarker)) {
                        if (line.contains(ValueMarker)) {
                            String mz = line.split(ValueMarker)[1].split("\"")[0];
                            peptideDO.setMz(Double.parseDouble(mz));
                            break;
                        }
                    }
                    continue;
                }

                if (peptideDO != null && line.contains(CvParamMarker)) {
                    if (line.contains("charge")) {
                        String charge = line.split(ValueMarker)[1].split("\"")[0];
                        fi.setCharge(Integer.parseInt(charge));
                        continue;
                    }
                    if (line.contains("m/z")) {
                        String mz = line.split(ValueMarker)[1].split("\"")[0];
                        fi.setMz(Double.parseDouble(mz));
                        continue;
                    }
                    if (line.contains("intensity")) {
                        String intensity = line.split(ValueMarker)[1].split("\"")[0];
                        fi.setIntensity(Double.parseDouble(intensity));
                    }
                    continue;
                }

                if (peptideDO != null && line.contains("annotation")) {
                    String annotations = line.split(ValueMarker)[1].split("\"")[0];
                    fi.setAnnotations(annotations);
                    ResultDO<Annotation> annotationResult = parseAnnotation(annotations);
                    Annotation annotation = annotationResult.getModel();
                    fi.setAnnotation(annotation);
                    fi.setCutInfo(annotation.toCutInfo());
                }
            }
            return new ResultDO(true);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultDO(false);
        }
    }
}
