package com.csi.propro.algorithm.parser;

import com.csi.propro.algorithm.parser.model.traml.*;
import com.csi.propro.algorithm.parser.xml.AirXStream;
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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.csi.propro.utils.PeptideUtil.parseModification;

@Component("traMLParser")
public class TraMLParser extends BaseLibraryParser {

    @Autowired
    AirXStream airXStream;
    @Autowired
    TaskService taskService;
    @Autowired
    ShuffleGenerator shuffleGenerator;

    public Class<?>[] classes = new Class[]{
            Compound.class, CompoundList.class, Configuration.class, Contact.class, Cv.class, CvParam.class,
            Evidence.class, Instrument.class, IntermediateProduct.class, Interpretation.class, Modification.class,
            Peptide.class, Precursor.class, Prediction.class, Product.class, Protein.class, ProteinRef.class,
            Publication.class, RetentionTime.class, Software.class, SourceFile.class, Target.class, TargetList.class,
            TraML.class, Transition.class, UserParam.class, ValidationStatus.class
    };

    private void prepare(){
        airXStream.processAnnotations(classes);
        airXStream.allowTypes(classes);
    }

    public TraML parse(File file) {
        prepare();
        TraML traML = new TraML();
        airXStream.fromXML(file, traML);
        return traML;
    }

    public String parse(TraML traML){
        prepare();
        return airXStream.toXML(traML);
    }

    public TraML parse(InputStream ins) {
        prepare();
        TraML traML = new TraML();
        airXStream.fromXML(ins, traML);
        return traML;
    }

    public HashMap<String, Peptide> makePeptideMap(List<Peptide> peptideList) {
        HashMap<String, Peptide> peptideMap = new HashMap<>();
        for (Peptide peptide : peptideList) {
            peptideMap.put(peptide.getId(), peptide);
        }
        return peptideMap;
    }

    public ResultDO<PeptideDO> parseTransition(Transition transition, HashMap<String, Peptide> peptideMap, LibraryDO library) {
        ResultDO<PeptideDO> resultDO = new ResultDO<>(true);
        PeptideDO peptideDO = new PeptideDO();
        peptideDO.setLibraryId(library.getId());

        FragmentInfo fi = new FragmentInfo();

        // parse transition attribution
        boolean isDecoy = transition.getPeptideRef().toLowerCase().contains("decoy");
        //不处理伪肽段信息
        if(isDecoy){
            return ResultDO.buildError(ResultCode.NO_DECOY);
        }
        // parse transition cvparams
        List<CvParam> listCvParams = transition.getCvParams();
        for (CvParam cvParam : listCvParams) {
            if (cvParam.getName().equals("product ion intensity")) {
                fi.setIntensity(Double.valueOf(cvParam.getValue()));
            } else if (cvParam.getName().equals("decoy SRM transition")) {
                return ResultDO.buildError(ResultCode.NO_DECOY);
            }
        }
        // parse transition userparam
        List<UserParam> listUserParams = transition.getUserParams();
        if(listUserParams != null){
            for (UserParam userParam : listUserParams) {
                if (userParam.getName().equals("annotation")) {
                    fi.setAnnotations(userParam.getValue());
                }
            }
        }

        // parse precursor
        listCvParams = transition.getPrecursor().getCvParams();
        if(listCvParams != null){
            for (CvParam cvParam : listCvParams) {
                if (cvParam.getName().equals("isolation window target m/z")) {
                    peptideDO.setMz(Double.valueOf(cvParam.getValue()));
                }
                //charge state以Peptide部分带电量为准
                if (cvParam.getName().equals("charge state")) {
                    peptideDO.setCharge(Integer.valueOf(cvParam.getValue()));
                }
            }
        }

        // parse product cvParams
        listCvParams = transition.getProduct().getCvParams();
        if(listCvParams != null){
            for (CvParam cvParam : listCvParams) {
                if (cvParam.getName().equals("isolation window target m/z")) {
                    fi.setMz(Double.valueOf(cvParam.getValue()));
                }
                if (cvParam.getName().equals("charge state")) {
                    fi.setCharge(Integer.valueOf(cvParam.getValue()));
                }
            }
        }

        // parse rt, sequence, full name, protein name from peptideMap
        Peptide peptide = peptideMap.get(transition.getPeptideRef());
        String rt = peptide.getRetentionTimeList().get(0).getCvParams().get(0).getValue();
        peptideDO.setRt(Double.valueOf(rt));
        peptideDO.setSequence(peptide.getSequence());
        peptideDO.setProteinName(peptide.getProteinRefList().get(0).getRef());
        peptideDO.setFullName(peptide.getUserParams().get(0).getValue());
        for(CvParam cvParam : peptide.getCvParams()){
            if(cvParam.getName().equals("charge state")){
                peptideDO.setCharge(Integer.valueOf(cvParam.getValue()));
                peptideDO.setPeptideRef(peptideDO.getFullName()+"_"+ peptideDO.getCharge());
            }
        }

        // parse annotations
        String annotations = fi.getAnnotations();
        fi.setAnnotations(annotations);
        try {
            ResultDO<Annotation> annotationResult = parseAnnotation(fi.getAnnotations());
            Annotation annotation = annotationResult.getModel();
            fi.setAnnotation(annotation);
            fi.setCutInfo(annotation.toCutInfo());
            peptideDO.putFragment(fi.getCutInfo(), fi);
            resultDO.setModel(peptideDO);
        } catch (Exception e) {
            resultDO.setSuccess(false);
            resultDO.setMsgInfo("Line插入错误(Sequence未知)");
            logger.error(peptideDO.getLibraryId() + ":" + fi.getAnnotation(), e);
            return resultDO;
        }

        PeptideUtil.parseModification(peptideDO);

        return resultDO;
    }

    @Override
    public ResultDO parseAndInsert(InputStream in, LibraryDO library, TaskDO taskDO) {
        TraML traML = parse(in);

        HashMap<String, Peptide> peptideMap = makePeptideMap(traML.getCompoundList().getPeptideList());
        ResultDO<List<PeptideDO>> tranResult = new ResultDO<>(true);

        try {
            //开始插入前先清空原有的数据库数据
            ResultDO resultDOTmp = peptideService.deleteAllByLibraryId(library.getId());
            logger.info("删除旧数据完毕");

            if (resultDOTmp.isFailed()) {
                logger.error(resultDOTmp.getMsgInfo());
                return ResultDO.buildError(ResultCode.DELETE_ERROR);
            }

            HashMap<String, PeptideDO> map = new HashMap<>();
            for (Transition transition : traML.getTransitionList()) {
                ResultDO<PeptideDO> resultDO = parseTransition(transition, peptideMap, library);
                if (resultDO.isFailed()) {
                    if(!resultDO.getMsgCode().equals(ResultCode.NO_DECOY.getCode())){
                        tranResult.addErrorMsg(resultDO.getMsgInfo());
                    }
                    continue;
                }
                PeptideDO peptide = resultDO.getModel();
                addFragment(peptide, map);
            }

            for (PeptideDO peptide: map.values()){
                shuffleGenerator.generate(peptide);
            }

            peptideService.insertAll(new ArrayList<>(map.values()), false);
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
        TraML traML = parse(in);
        HashMap<String, Peptide> peptideMap = makePeptideMap(traML.getCompoundList().getPeptideList());
        ResultDO<List<PeptideDO>> tranResult = new ResultDO<>(true);
        int selectedCount = selectedPepSet.size();
        try {
            //开始插入前先清空原有的数据库数据
            ResultDO resultDOTmp = peptideService.deleteAllByLibraryId(library.getId());
            logger.info("删除旧数据完毕");

            if (resultDOTmp.isFailed()) {
                logger.error(resultDOTmp.getMsgInfo());
                return ResultDO.buildError(ResultCode.DELETE_ERROR);
            }

            boolean withCharge = new ArrayList<>(selectedPepSet).get(0).contains("_");
            if (selectBySequence){
                selectedPepSet = convertPepToSeq(selectedPepSet, withCharge);
            }
            HashMap<String, PeptideDO> map = new HashMap<>();
            for (Transition transition : traML.getTransitionList()) {
                if(!selectedPepSet.isEmpty() && !isSelectedPep(transition, peptideMap, selectedPepSet, withCharge, selectBySequence)){
                    continue;
                }
                ResultDO<PeptideDO> resultDO = parseTransition(transition, peptideMap, library);

                if (resultDO.isFailed()) {
                    if(!resultDO.getMsgCode().equals(ResultCode.NO_DECOY.getCode())){
                        tranResult.addErrorMsg(resultDO.getMsgInfo());
                    }
                    continue;
                }
                PeptideDO peptide = resultDO.getModel();
                addFragment(peptide, map);
                //在导入Peptide的同时生成伪肽段
                shuffleGenerator.generate(peptide);
            }
            for (PeptideDO peptideDO: map.values()){
                selectedPepSet.remove(peptideDO.getPeptideRef());
            }
            ArrayList<PeptideDO> peptides = new ArrayList<PeptideDO>(map.values());

            peptideService.insertAll(peptides, false);
            tranResult.setModel(peptides);
            taskDO.addLog(peptides.size() + "条数据插入成功");
            taskService.update(taskDO);
            logger.info(map.size() + "条肽段数据插入成功");
            logger.info("在选中的" + selectedCount + "条肽段中, 有" + selectedPepSet.size() + "条没有在库中找到");
            logger.info(selectedPepSet.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tranResult;
    }


    private boolean isSelectedPep(Transition transition, HashMap<String, Peptide> peptideMap, HashSet<String> selectedPepSet, boolean withCharge, boolean selectBySequence){
        Peptide peptide = peptideMap.get(transition.getPeptideRef());
        String fullName = peptide.getUserParams().get(0).getValue();
        if (selectBySequence){
            String sequence = PeptideUtil.removeUnimod(fullName);
            return selectedPepSet.contains(sequence);
        }
        if (withCharge){
            String charge = "";
            for(CvParam cvParam : peptide.getCvParams()){
                if(cvParam.getName().equals("charge state")){
                    charge = cvParam.getValue();
                }
            }
            return selectedPepSet.contains(fullName+"_"+charge);
        }else {
            return selectedPepSet.contains(fullName);
        }
    }


}
