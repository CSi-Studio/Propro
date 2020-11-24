package net.csibio.propro.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.csibio.aird.bean.WindowRange;
import net.csibio.propro.algorithm.extract.Extractor;
import net.csibio.propro.config.VMProperties;
import net.csibio.propro.constants.Constants;
import net.csibio.propro.constants.SuccessMsg;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.constants.enums.ScoreType;
import net.csibio.propro.constants.enums.TaskTemplate;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.bean.analyse.SigmaSpacing;
import net.csibio.propro.domain.bean.irt.IrtResult;
import net.csibio.propro.domain.bean.score.SlopeIntercept;
import net.csibio.propro.domain.db.*;
import net.csibio.propro.domain.params.ExtractParams;
import net.csibio.propro.domain.params.IrtParams;
import net.csibio.propro.domain.params.WorkflowParams;
import net.csibio.propro.domain.query.ExperimentQuery;
import net.csibio.propro.domain.query.SwathIndexQuery;
import net.csibio.propro.exception.UnauthorizedAccessException;
import net.csibio.propro.service.*;
import net.csibio.propro.utils.ScoreUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-04 10:00
 */
@Controller
@RequestMapping("experiment")
public class ExperimentController extends BaseController {

    @Autowired
    LibraryService libraryService;
    @Autowired
    PeptideService peptideService;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    SwathIndexService swathIndexService;
    @Autowired
    ProjectService projectService;
    @Autowired
    Extractor extractor;
    @Autowired
    VMProperties vmProperties;

    @RequestMapping(value = "/list")
    String list(Model model,
                @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize,
                @RequestParam(value = "projectName", required = false) String projectName,
                @RequestParam(value = "type", required = false) String type,
                @RequestParam(value = "expName", required = false) String expName) {

        model.addAttribute("expName", expName);
        model.addAttribute("projectName", projectName);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("type", type);
        ExperimentQuery query = new ExperimentQuery();
        if (expName != null && !expName.isEmpty()) {
            query.setName(expName);
        }
        if (projectName != null && !projectName.isEmpty()) {
            ProjectDO project = projectService.getByName(projectName);
            if (project == null) {
                return "experiment/list";
            } else {
                query.setProjectId(project.getId());
            }
            pageSize = Integer.MAX_VALUE;//如果是根据项目名称进行搜索的,直接全部展示出来
        }

        if (type != null && !type.isEmpty()) {
            query.setType(type);
        }
        query.setPageSize(pageSize);
        query.setPageNo(currentPage);
        ResultDO<List<ExperimentDO>> resultDO = experimentService.getList(query);
        HashMap<String, AnalyseOverviewDO> analyseOverviewDOMap = new HashMap<>();
        for (ExperimentDO experimentDO : resultDO.getModel()) {
            List<AnalyseOverviewDO> analyseOverviewDOList = analyseOverviewService.getAllByExpId(experimentDO.getId());
            if (analyseOverviewDOList.isEmpty()) {
                continue;
            }
            analyseOverviewDOMap.put(experimentDO.getId(), analyseOverviewDOList.get(0));
        }

        model.addAttribute("experiments", resultDO.getModel());
        model.addAttribute("analyseOverviewDOMap", analyseOverviewDOMap);
        model.addAttribute("totalPage", resultDO.getTotalPage());
        model.addAttribute("currentPage", currentPage);
        return "experiment/list";
    }

    @RequestMapping(value = "/listByExpId")
    String listByExpId(Model model,
                       @RequestParam(value = "expId", required = true) String expId) {

        ResultDO<ExperimentDO> expResult = experimentService.getById(expId);
        if (expResult.isFailed()) {
            return "redirect:/experiment/list";
        }

        ExperimentDO exp = expResult.getModel();
        return "redirect:/experiment/list?projectName=" + exp.getProjectName();
    }

    @RequestMapping(value = "/create")
    String create(Model model) {
        return "experiment/create";
    }

    @RequestMapping(value = "/batchcreate")
    String batchCreate(Model model,
                       @RequestParam(value = "projectName", required = false) String projectName,
                       RedirectAttributes redirectAttributes) {

        ProjectDO project = projectService.getByName(projectName);
        if (project == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.PROJECT_NOT_EXISTED.getMessage());
            return "redirect:/project/list";
        }
        model.addAttribute("project", project);
        return "experiment/batchcreate";
    }

    @RequestMapping(value = "/edit/{id}")
    String edit(Model model, @PathVariable("id") String id, RedirectAttributes redirectAttributes) {

        ResultDO<ExperimentDO> resultDO = experimentService.getById(id);
        if (resultDO.isFailed()) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, resultDO.getMsgInfo());
            return "redirect:/experiment/list";
        } else {
            model.addAttribute("experiment", resultDO.getModel());
            return "experiment/edit";
        }
    }

    @RequestMapping(value = "/detail/{id}")
    String detail(Model model, @PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        ResultDO<ExperimentDO> resultDO = experimentService.getById(id);
        if (resultDO.isSuccess()) {
            ExperimentDO exp = resultDO.getModel();
            model.addAttribute("experiment", exp);
            return "experiment/detail";
        } else {
            redirectAttributes.addFlashAttribute(ERROR_MSG, resultDO.getMsgInfo());
            return "redirect:/experiment/list";
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    String update(Model model,
                  @RequestParam(value = "id", required = true) String id,
                  @RequestParam(value = "name") String name,
                  @RequestParam(value = "type") String type,
                  @RequestParam(value = "iRtLibraryId") String iRtLibraryId,
                  @RequestParam(value = "slope") Double slope,
                  @RequestParam(value = "intercept") Double intercept,
                  @RequestParam(value = "description") String description,
                  @RequestParam(value = "projectName") String projectName,
                  RedirectAttributes redirectAttributes) {

        ResultDO<ExperimentDO> resultDO = experimentService.getById(id);
        if (resultDO.isFailed()) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, resultDO.getMsgInfo());
            return "redirect:/experiment/list";
        }
        ExperimentDO experimentDO = resultDO.getModel();

        ProjectDO project = projectService.getByName(projectName);
        if (project == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, resultDO.getMsgInfo());
            return "redirect:/experiment/list";
        }

        experimentDO.setName(name);
        experimentDO.setType(type);
        experimentDO.setProjectName(projectName);
        experimentDO.setProjectId(project.getId());
        experimentDO.setDescription(description);
        experimentDO.setIRtLibraryId(iRtLibraryId);
        IrtResult irtResult = experimentDO.getIrtResult();
        irtResult.setSi(new SlopeIntercept(slope, intercept));
        experimentDO.setIrtResult(irtResult);
        ResultDO result = experimentService.update(experimentDO);
        if (result.isFailed()) {
            model.addAttribute(ERROR_MSG, result.getMsgInfo());
            return "redirect:/experiment/list";
        }
        return "redirect:/experiment/list";

    }

    @RequestMapping(value = "/delete/{id}")
    String delete(Model model, @PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        ResultDO<ExperimentDO> exp = experimentService.getById(id);
        experimentService.delete(id);
        swathIndexService.deleteAllByExpId(id);
        analyseOverviewService.deleteAllByExpId(id);

        redirectAttributes.addFlashAttribute("projectName", exp.getModel().getProjectName());
        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.DELETE_SUCCESS);
        return "redirect:/experiment/list";
    }

    @RequestMapping(value = "/deleteAll/{id}")
    String deleteAll(Model model, @PathVariable("id") String id,
                     RedirectAttributes redirectAttributes) {
        ResultDO<ExperimentDO> exp = experimentService.getById(id);
        analyseOverviewService.deleteAllByExpId(id);
        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.DELETE_SUCCESS);
        return "redirect:/experiment/list";
    }

    @RequestMapping(value = "/extractor")
    String extractor(Model model,
                     @RequestParam(value = "id", required = true) String id,
                     RedirectAttributes redirectAttributes) {

        ResultDO<ExperimentDO> resultDO = experimentService.getById(id);
        if (resultDO.isFailed()) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.OBJECT_NOT_EXISTED);
            return "redirect:/experiment/list";
        }

        ProjectDO project = projectService.getById(resultDO.getModel().getProjectId());
        if (project != null) {
            model.addAttribute("libraryId", project.getLibraryId());
            model.addAttribute("iRtLibraryId", project.getIRtLibraryId());
        }

        model.addAttribute("iRtLibraries", getLibraryList(1));
        model.addAttribute("libraries", getLibraryList(0));
        model.addAttribute("experiment", resultDO.getModel());
        model.addAttribute("scoreTypes", ScoreType.getShownTypes());

        return "experiment/extractor";
    }

    @RequestMapping(value = "/doextract")
    String doExtract(Model model,
                     @RequestParam(value = "id", required = true) String id,
                     @RequestParam(value = "libraryId", required = true) String libraryId,
                     @RequestParam(value = "rtExtractWindow", required = true, defaultValue = Constants.DEFAULT_RT_EXTRACTION_WINDOW_STR) Float rtExtractWindow,
                     @RequestParam(value = "mzExtractWindow", required = true, defaultValue = Constants.DEFAULT_MZ_EXTRACTION_WINDOW_STR) Float mzExtractWindow,
                     @RequestParam(value = "slope", required = false) Double slope,
                     @RequestParam(value = "intercept", required = false) Double intercept,
                     @RequestParam(value = "note", required = false) String note,
                     //打分相关的入参
                     @RequestParam(value = "sigma", required = false, defaultValue = Constants.DEFAULT_SIGMA_STR) Float sigma,
                     @RequestParam(value = "spacing", required = false, defaultValue = Constants.DEFAULT_SPACING_STR) Float spacing,
                     @RequestParam(value = "fdr", required = false, defaultValue = Constants.DEFAULT_FDR_STR) Double fdr,
                     @RequestParam(value = "shapeScoreThreshold", required = false, defaultValue = Constants.DEFAULT_SHAPE_SCORE_THRESHOLD_STR) Float shapeScoreThreshold,
                     @RequestParam(value = "shapeWeightScoreThreshold", required = false, defaultValue = Constants.DEFAULT_SHAPE_WEIGHT_SCORE_THRESHOLD_STR) Float shapeWeightScoreThreshold,
                     @RequestParam(value = "uniqueOnly", required = false, defaultValue = "false") Boolean uniqueOnly,
                     HttpServletRequest request,
                     RedirectAttributes redirectAttributes) {

        ResultDO<ExperimentDO> resultDO = experimentService.getById(id);
        if (resultDO.isFailed()) {
            return "redirect:/extractor?id=" + id;
        }

        LibraryDO library = libraryService.getById(libraryId);
        if (library == null) {
            return "redirect:/extractor?id=" + id;
        }

        List<String> scoreTypes = ScoreUtil.getScoreTypes(request);

        TaskDO taskDO = new TaskDO(TaskTemplate.EXTRACT_PEAKPICK_SCORE, resultDO.getModel().getName() + ":" + library.getName() + "(" + libraryId + ")");
        taskService.insert(taskDO);
        SlopeIntercept si = SlopeIntercept.create();
        if (slope != null && intercept != null) {
            si.setSlope(slope);
            si.setIntercept(intercept);
        }
        SigmaSpacing ss = new SigmaSpacing(sigma, spacing);

        WorkflowParams input = new WorkflowParams();
        input.setExperimentDO(resultDO.getModel());
        input.setLibrary(library);
        input.setSlopeIntercept(si);
        input.setNote(note);
        input.setFdr(fdr);
        input.setExtractParams(new ExtractParams(mzExtractWindow, rtExtractWindow));
        input.setUniqueOnly(uniqueOnly);
        input.setScoreTypes(scoreTypes);
        input.setSigmaSpacing(ss);
        input.setXcorrShapeThreshold(shapeScoreThreshold);
        input.setXcorrShapeWeightThreshold(shapeWeightScoreThreshold);

        experimentTask.extract(taskDO, input);

        return "redirect:/task/detail/" + taskDO.getId();
    }

    @RequestMapping(value = "/irt")
    String irt(Model model,
               @RequestParam(value = "id", required = true) String id,
               RedirectAttributes redirectAttributes) {

        ResultDO<ExperimentDO> resultDO = experimentService.getById(id);
        if (resultDO.isFailed()) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.OBJECT_NOT_EXISTED);
            return "redirect:/experiment/list";
        }

        ProjectDO project = projectService.getById(resultDO.getModel().getProjectId());
        if (project != null) {
            model.addAttribute("iRtLibraryId", project.getIRtLibraryId());
            model.addAttribute("libraryId", project.getLibraryId());
        }

        model.addAttribute("wantedNumbers", 50);
        model.addAttribute("pickedNumbers", 500);
        model.addAttribute("shapeScoreThreshold", 0.95f);
        model.addAttribute("irtLibraries", getLibraryList(1));
        model.addAttribute("libraries", getLibraryList(0));
        model.addAttribute("experiment", resultDO.getModel());
        return "experiment/irt";
    }

    @RequestMapping(value = "/doirt")
    String doIrt(Model model,
                 @RequestParam(value = "id", required = true) String id,
                 @RequestParam(value = "iRtLibraryId", required = false) String iRtLibraryId,
                 @RequestParam(value = "useLibrary", required = true, defaultValue = "false") boolean useLibrary,
                 @RequestParam(value = "wantedNumbers", required = false) Integer wantedNumbers,
                 @RequestParam(value = "pickedNumbers", required = false) Integer pickedNumbers,
                 @RequestParam(value = "shapeScoreThreshold", required = false) Float shapeScoreThreshold,
                 @RequestParam(value = "libraryId", required = false) String libraryId,
                 @RequestParam(value = "sigma", required = true, defaultValue = Constants.DEFAULT_SIGMA_STR) Float sigma,
                 @RequestParam(value = "spacing", required = true, defaultValue = Constants.DEFAULT_SPACING_STR) Float spacing,
                 @RequestParam(value = "mzExtractWindow", required = true, defaultValue = Constants.DEFAULT_MZ_EXTRACTION_WINDOW_STR) Float mzExtractWindow,
                 RedirectAttributes redirectAttributes) {

        ResultDO<ExperimentDO> resultDO = experimentService.getById(id);
        if (resultDO.isFailed()) {
            return "redirect:/irt/" + id;
        }
        TaskDO taskDO = new TaskDO(TaskTemplate.IRT, resultDO.getModel().getName() + ":" + (useLibrary ? libraryId : iRtLibraryId) + "-Num:1");
        taskService.insert(taskDO);

        SigmaSpacing sigmaSpacing = new SigmaSpacing(sigma, spacing);
        List<ExperimentDO> exps = new ArrayList<>();
        exps.add(resultDO.getModel());

        LibraryDO lib = libraryService.getById((useLibrary ? libraryId : iRtLibraryId));

        IrtParams irtParams = new IrtParams();
        irtParams.setMzExtractWindow(mzExtractWindow);
        irtParams.setSigmaSpacing(sigmaSpacing);
        irtParams.setUseLibrary(useLibrary);
        irtParams.setLibrary(lib);
        irtParams.setShapeScoreThreshold(shapeScoreThreshold);
        irtParams.setPickedNumbers(pickedNumbers);
        irtParams.setWantedNumber(wantedNumbers);

        experimentTask.irt(taskDO, exps, irtParams);

        return "redirect:/task/detail/" + taskDO.getId();
    }

    @RequestMapping(value = "/irtresult")
    @ResponseBody
    ResultDO<JSONObject> irtResult(Model model,
                                   @RequestParam(value = "expId", required = false) String expId) {
        ResultDO<JSONObject> resultDO = new ResultDO<>(true);

        ResultDO<ExperimentDO> expResult = experimentService.getById(expId);
        if (expResult.isFailed()) {
            resultDO.setErrorResult(ResultCode.EXPERIMENT_NOT_EXISTED);
            return resultDO;
        }
        ExperimentDO experimentDO = expResult.getModel();

        IrtResult irtResult = experimentDO.getIrtResult();
        if (irtResult == null) {
            return ResultDO.buildError(ResultCode.IRT_FIRST);
        }

        JSONObject res = new JSONObject();
        JSONArray selectedArray = new JSONArray();
        JSONArray unselectedArray = new JSONArray();
        JSONArray lineArray = new JSONArray();
        for (Double[] pair : irtResult.getSelectedPairs()) {
            selectedArray.add(JSONArray.toJSON(pair));
            lineArray.add(JSONArray.toJSON(new Double[]{pair[0], (pair[0] - irtResult.getSi().getIntercept()) / irtResult.getSi().getSlope()}));
        }
        for (Double[] pair : irtResult.getUnselectedPairs()) {
            unselectedArray.add(JSONArray.toJSON(pair));
        }

        res.put("slope", irtResult.getSi().getSlope());
        res.put("intercept", irtResult.getSi().getIntercept());
        res.put("lineArray", lineArray);
        res.put("selectedArray", selectedArray);
        res.put("unselectedArray", unselectedArray);
        resultDO.setModel(res);
        return resultDO;
    }

    @RequestMapping(value = "/getWindows")
    @ResponseBody
    ResultDO<JSONObject> getWindows(Model model, @RequestParam(value = "expId", required = true) String expId) {

        ResultDO<ExperimentDO> expResult = experimentService.getById(expId);

        List<WindowRange> ranges = expResult.getModel().getWindowRanges();
        ResultDO<JSONObject> resultDO = new ResultDO<>(true);
        //按照mz进行排序
        ranges.sort(Comparator.comparingDouble(WindowRange::getMz));
        JSONObject res = new JSONObject();
        JSONArray mzStartArray = new JSONArray();
        JSONArray mzRangArray = new JSONArray();
        for (int i = 0; i < ranges.size(); i++) {
            mzStartArray.add(ranges.get(i).getStart());
            mzRangArray.add((ranges.get(i).getEnd() - ranges.get(i).getStart()));
        }
        res.put("starts", mzStartArray);
        res.put("rangs", mzRangArray);
        res.put("min", ranges.get(0).getStart());
        res.put("max", ranges.get(ranges.size() - 1).getEnd());
        resultDO.setModel(res);
        return resultDO;
    }

    @RequestMapping(value = "/getPrmWindows")
    @ResponseBody
    ResultDO<JSONObject> getPrmWindows(Model model, @RequestParam(value = "expId", required = true) String expId) {

        ResultDO<ExperimentDO> expResult = experimentService.getById(expId);

        HashMap<Float, Float[]> peptideMap = experimentService.getPrmRtWindowMap(expId);
        JSONArray peptideMs1List = new JSONArray();
        for (Float precursorMz : peptideMap.keySet()) {
            if (Math.abs(peptideMap.get(precursorMz)[0] - peptideMap.get(precursorMz)[1]) < 20) {
                continue;
            }
            JSONArray peptide = new JSONArray();
            peptide.add(new Float[]{peptideMap.get(precursorMz)[0], precursorMz});
            peptide.add(new Float[]{peptideMap.get(precursorMz)[1], precursorMz});
            peptideMs1List.add(peptide);
        }
        JSONObject res = new JSONObject();
        res.put("peptideList", peptideMs1List);
        ResultDO<JSONObject> resultDO = new ResultDO<>(true);
        resultDO.setModel(res);
        return resultDO;
    }

    @RequestMapping(value = "/getPrmDensity")
    @ResponseBody
    ResultDO<JSONObject> getPrmDensity(Model model, @RequestParam(value = "expId", required = true) String expId) {

        ResultDO<ExperimentDO> expResult = experimentService.getById(expId);

        ResultDO<JSONObject> resultDO = new ResultDO<>(true);
        JSONArray ms2Density = new JSONArray();
        SwathIndexQuery query = new SwathIndexQuery();
        query.setExpId(expId);

        query.setLevel(1);
        List<SwathIndexDO> ms1Indexs = swathIndexService.getAll(query);
        if (ms1Indexs == null || ms1Indexs.size() != 1) {
            return ResultDO.buildError(ResultCode.DATA_IS_EMPTY);
        }
        List<Float> ms1RtList = ms1Indexs.get(0).getRts();
        Collections.sort(ms1RtList);

        query.setLevel(2);
        List<SwathIndexDO> ms2Indexs = swathIndexService.getAll(query);
        List<Float> ms2RtList = new ArrayList<>();
        for (SwathIndexDO ms2 : ms2Indexs) {
            ms2RtList.addAll(ms2.getRts());
        }
        Collections.sort(ms2RtList);
        int ms2Index = ms2RtList.size() - 1;
        int max = Integer.MIN_VALUE;
        for (int ms1Index = ms1RtList.size() - 1; ms1Index >= 0; ms1Index--) {
            int count = 0;
            for (; ms2Index >= 0; ms2Index--) {
                if (ms2Index - count >= 0 && ms2RtList.get(ms2Index - count) > ms1RtList.get(ms1Index)) {
                    count++;
                } else {
                    break;
                }
            }
            ms2Density.add(new Float[]{ms1RtList.get(ms1Index), (float) count});
            if (count > max) {
                max = count;
            }
        }

        JSONObject res = new JSONObject();
        res.put("ms2Density", ms2Density);
        res.put("upMax", (int) Math.ceil(max / 10d) * 10d);
        resultDO.setModel(res);
        return resultDO;
    }
}
