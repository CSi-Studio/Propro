package net.csibio.propro.algorithm.parser;

import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.bean.peptide.Annotation;
import net.csibio.propro.domain.db.FragmentInfo;
import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.db.TaskDO;
import net.csibio.propro.service.TaskService;
import net.csibio.propro.utils.FileUtil;
import net.csibio.propro.utils.PeptideUtil;
import net.csibio.propro.algorithm.decoy.generator.ShuffleGenerator;
import org.apache.commons.lang3.StringUtils;
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

import static net.csibio.propro.utils.PeptideUtil.parseModification;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-07 11:07
 */
@Component("tsvParser")
public class LibraryTsvParser extends BaseLibraryParser {

    @Autowired
    TaskService taskService;
    @Autowired
    ShuffleGenerator shuffleGenerator;

    public final Logger logger = LoggerFactory.getLogger(LibraryTsvParser.class);

    private static String PrecursorMz = "precursormz";
    private static String ProductMz = "productmz";
    private static String NormalizedRetentionTime = "tr_recalibrated";
    private static String TransitionName = "transition_name";
    private static String TransitionGroupId = "transition_group_id";
    private static String UniprotId = "uniprotid";
    private static String IsDecoy = "decoy";
    private static String ProductIonIntensity = "libraryintensity";
    private static String PeptideSequence = "peptidesequence";
    private static String ProteinName = "proteinname";
    private static String Annotation = "annotation";
    private static String FullUniModPeptideName = "fullunimodpeptidename";
    private static String PrecursorCharge = "precursorcharge";
    private static String Detecting = "detecting_transition";
    private static String Identifying = "identifying_transition";
    private static String Quantifying = "quantifying_transition";

    @Override
    public ResultDO parseAndInsert(InputStream in, LibraryDO library, TaskDO taskDO) {

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
            HashMap<String, PeptideDO> map = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                ResultDO<PeptideDO> resultDO = parseTransition(line, columnMap, library);
                if (resultDO.isFailed()) {
                    if (!resultDO.getMsgCode().equals(ResultCode.NO_DECOY.getCode())) {
                        tranResult.addErrorMsg(resultDO.getMsgInfo());
                    }
                    continue;
                }
                PeptideDO peptide = resultDO.getModel();
                addFragment(peptide, map);
            }
            List<PeptideDO> peptideDOList = new ArrayList<>(map.values());
            //在导入Peptide的同时生成伪肽段
            shuffleGenerator.generate(peptideDOList);
            peptideService.insertAll(peptideDOList, false);
            taskDO.addLog(map.size() + "条肽段数据插入成功");
            taskService.update(taskDO);
            logger.info(map.size() + "条肽段数据插入成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tranResult;
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


            HashMap<String, PeptideDO> map = new HashMap<>();

            boolean withCharge = new ArrayList<>(selectedPepSet).get(0).contains("_");
            if (selectBySequence) {
                selectedPepSet = convertPepToSeq(selectedPepSet, withCharge);
            }
            while ((line = reader.readLine()) != null) {
                if (!selectedPepSet.isEmpty() && !isSelectedLine(line, columnMap, selectedPepSet, withCharge, selectBySequence)) {
                    continue;
                }
                ResultDO<PeptideDO> resultDO = parseTransition(line, columnMap, library);
                if (resultDO.isFailed()) {
                    if (!resultDO.getMsgCode().equals(ResultCode.NO_DECOY.getCode())) {
                        tranResult.addErrorMsg(resultDO.getMsgInfo());
                    }
                    continue;
                }

                PeptideDO peptide = resultDO.getModel();
                addFragment(peptide, map);
                //在导入Peptide的同时生成伪肽段
                shuffleGenerator.generate(peptide);
            }

            //删除命中的部分, 得到未命中的Set
            int selectedCount = selectedPepSet.size();
            if (withCharge) {
                for (PeptideDO peptideDO : map.values()) {
                    selectedPepSet.remove(peptideDO.getPeptideRef());
                }
            } else {
                for (PeptideDO peptideDO : map.values()) {
                    selectedPepSet.remove(peptideDO.getFullName());
                }
            }

            peptideService.insertAll(new ArrayList<>(map.values()), false);
            taskDO.addLog(map.size() + "条肽段数据插入成功");
            taskService.update(taskDO);
            logger.info(map.size() + "条肽段数据插入成功");
            logger.info("在选中的" + selectedCount + "条肽段中, 有" + selectedPepSet.size() + "条没有在库中找到");
            logger.info(selectedPepSet.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tranResult;
    }

    /**
     * 从TSV文件中解析出每一行数据
     *
     * @param line
     * @param columnMap
     * @param library
     * @return
     */
    private ResultDO<PeptideDO> parseTransition(String line, HashMap<String, Integer> columnMap, LibraryDO library) {
        ResultDO<PeptideDO> resultDO = new ResultDO<>(true);
        String[] row = StringUtils.splitByWholeSeparator(line, "\t");
        PeptideDO peptideDO = new PeptideDO();
        boolean isDecoy = !row[columnMap.get(IsDecoy)].equals("0");
        if (isDecoy) {
            return ResultDO.buildError(ResultCode.NO_DECOY);
        }
        FragmentInfo fi = new FragmentInfo();

        peptideDO.setLibraryId(library.getId());
        peptideDO.setMz(Double.parseDouble(row[columnMap.get(PrecursorMz)]));
        fi.setMz(Double.parseDouble(row[columnMap.get(ProductMz)]));
        peptideDO.setRt(Double.parseDouble(row[columnMap.get(NormalizedRetentionTime)]));

        fi.setIntensity(Double.parseDouble(row[columnMap.get(ProductIonIntensity)]));
        peptideDO.setSequence(row[columnMap.get(PeptideSequence)]);
        peptideDO.setProteinName(row[columnMap.get(ProteinName)]);
        if (peptideDO.getProteinName().toLowerCase().contains("irt")) {
            peptideDO.setProteinName("iRT");
        }

        String annotations = row[columnMap.get(Annotation)].replaceAll("\"", "");
        fi.setAnnotations(annotations);
        String fullName = row[columnMap.get(FullUniModPeptideName)];//no target sequence
        String[] transitionGroupId = row[columnMap.get(TransitionGroupId)].split("_");
        if (fullName == null) {
            logger.info("Full Peptide Name cannot be empty");
        } else {
            if (transitionGroupId.length > 2) {
                peptideDO.setFullName(transitionGroupId[1]);
            } else {
                peptideDO.setFullName(transitionGroupId[0]);
            }
        }
        peptideDO.setSequence(PeptideUtil.removeUnimod(peptideDO.getFullName()));
        try {
            peptideDO.setCharge(Integer.parseInt(row[columnMap.get(PrecursorCharge)]));
        } catch (Exception e) {
            logger.error("Line插入错误(PrecursorCharge未知):" + line + ";");
            logger.error(e.getMessage());
        }
        peptideDO.setPeptideRef(peptideDO.getFullName() + "_" + peptideDO.getCharge());
        try {
            ResultDO<net.csibio.propro.domain.bean.peptide.Annotation> annotationResult = parseAnnotation(fi.getAnnotations());
            Annotation annotation = annotationResult.getModel();
            fi.setAnnotation(annotation);
            fi.setCharge(annotation.getCharge());
            fi.setCutInfo(annotation.toCutInfo());
            peptideDO.putFragment(fi.getCutInfo(), fi);
            resultDO.setModel(peptideDO);
        } catch (Exception e) {
            resultDO.setSuccess(false);
            logger.error("Line插入错误(Sequence未知):" + line + ";");
            resultDO.setMsgInfo("Line插入错误(Sequence未知):" + line + ";");
            logger.error(peptideDO.getLibraryId() + ":" + fi.getAnnotation(), e);
            return resultDO;
        }

        PeptideUtil.parseModification(peptideDO);

        return resultDO;
    }

    public ResultDO<HashMap<String, PeptideDO>> getPrmPeptideRef(InputStream in) {
        try {
            InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
            if (line == null) {
                return ResultDO.buildError(ResultCode.PRM_FILE_IS_EMPTY);
            }
            String[] columns = line.split(",");
            HashMap<String, Integer> columnMap = new HashMap<>();
            for (int i = 0; i < columns.length; i++) {
                columnMap.put(StringUtils.deleteWhitespace(columns[i].toLowerCase()), i);
            }

            /**
             * format check
             * peptide  charge  start[min]
             */
            if (!columnMap.containsKey("peptide")) {
                return ResultDO.buildError(ResultCode.PRM_FILE_FORMAT_NOT_SUPPORTED);
            }
            boolean withCharge = columnMap.containsKey("charge");
            HashMap<String, PeptideDO> prmPepMap = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                columns = line.split(",");
                String modSequence = columns[columnMap.get("peptide")]
                        .replace("[CAM]", "(UniMod:4)")
                        .replace(" (light)", "")
                        .replace("[+57.021464]", "(UniMod:4)")
                        .replace("[+57]", "(UniMod:4)")
                        .replace("[+15.994915]", "(UniMod:35)")
                        .replace(" ", "");

                PeptideDO peptideDO = new PeptideDO();
                if (columnMap.containsKey("rt[min]")) {
                    peptideDO.setPrmRt(Double.parseDouble(columns[columnMap.get("rt[min]")]));
                }
                peptideDO.setFullName(modSequence);
                peptideDO.setSequence(PeptideUtil.removeUnimod(modSequence));
                peptideDO.setUnimodMap(PeptideUtil.parseModification(modSequence));
                if (withCharge) {
                    peptideDO.setCharge(Integer.parseInt(columns[columnMap.get("charge")]));
                    peptideDO.setPeptideRef(modSequence + "_" + peptideDO.getCharge());
                    prmPepMap.put(peptideDO.getPeptideRef(), peptideDO);
                } else {
                    prmPepMap.put(peptideDO.getFullName(), peptideDO);
                }
            }
            return new ResultDO<HashMap<String, PeptideDO>>(true).setModel(prmPepMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultDO.buildError(ResultCode.PRM_FILE_FORMAT_NOT_SUPPORTED);
        } finally {
            FileUtil.close(in);
        }
    }

    private boolean isSelectedLine(String line, HashMap<String, Integer> columnMap, HashSet<String> peptideSet, boolean withCharge, boolean selectBySequence) {
        String[] row = line.split("\t");
        String fullName = row[columnMap.get(FullUniModPeptideName)];
        String charge = row[columnMap.get(PrecursorCharge)];
        if (selectBySequence) {
            String sequence = row[columnMap.get(PeptideSequence)];
            return peptideSet.contains(sequence);
        }
        if (withCharge) {
            return peptideSet.contains(fullName + "_" + charge);
        } else {
            return peptideSet.contains(fullName);
        }
    }

}
