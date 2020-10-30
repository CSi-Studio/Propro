package net.csibio.propro.controller;

import net.csibio.propro.component.ChunkUploader;
import net.csibio.propro.config.VMProperties;
import net.csibio.propro.constants.Constants;
import net.csibio.propro.constants.SuccessMsg;
import net.csibio.propro.constants.SuffixConst;
import net.csibio.propro.constants.SymbolConst;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.constants.enums.ScoreType;
import net.csibio.propro.constants.enums.TaskTemplate;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.bean.analyse.SigmaSpacing;
import net.csibio.propro.domain.db.*;
import net.csibio.propro.domain.db.simple.PeptideIntensity;
import net.csibio.propro.domain.db.simple.SimpleExperiment;
import net.csibio.propro.domain.params.ExtractParams;
import net.csibio.propro.domain.params.IrtParams;
import net.csibio.propro.domain.params.WorkflowParams;
import net.csibio.propro.domain.query.ProjectQuery;
import net.csibio.propro.domain.vo.FileBlockVO;
import net.csibio.propro.domain.vo.FileVO;
import net.csibio.propro.domain.vo.UploadVO;
import net.csibio.propro.service.*;
import net.csibio.propro.utils.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import java.io.File;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-04 10:00
 */
@Controller
@RequestMapping("project")
public class ProjectController extends BaseController {

    @Autowired
    ProjectService projectService;
    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    PeptideService peptideService;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    SwathIndexService swathIndexService;
    @Autowired
    VMProperties vmProperties;
    @Autowired
    ChunkUploader chunkUploader;

    @RequestMapping(value = "/list")
    String list(Model model,
                @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize,
                @RequestParam(value = "name", required = false) String name) {
        model.addAttribute("name", name);
        model.addAttribute("pageSize", pageSize);
        ProjectQuery query = new ProjectQuery();
        if (name != null && !name.isEmpty()) {
            query.setName(name);
        }

        if (!isAdmin()) {
            query.setOwnerName(getCurrentUsername());
        }
        query.setPageSize(pageSize);
        query.setPageNo(currentPage);
        ResultDO<List<ProjectDO>> resultDO = projectService.getList(query);

        model.addAttribute("repository", RepositoryUtil.getRepo());
        model.addAttribute("projectList", resultDO.getModel());
        model.addAttribute("totalPage", resultDO.getTotalPage());
        model.addAttribute("currentPage", currentPage);
        return "project/list";
    }

    @RequestMapping(value = "/create")
    String create(Model model) {

        model.addAttribute("libraries", getLibraryList(0, true));
        model.addAttribute("iRtLibraries", getLibraryList(1, true));

        return "project/create";
    }

    @RequestMapping(value = "/add")
    String add(Model model,
               @RequestParam(value = "name", required = true) String name,
               @RequestParam(value = "description", required = false) String description,
               @RequestParam(value = "type", required = true) String type,
               @RequestParam(value = "libraryId", required = false) String libraryId,
               @RequestParam(value = "iRtLibraryId", required = false) String iRtLibraryId,
               RedirectAttributes redirectAttributes) {

        String ownerName = getCurrentUsername();

        model.addAttribute("ownerName", ownerName);
        model.addAttribute("name", name);
        model.addAttribute("description", description);
        model.addAttribute("type", type);
        model.addAttribute("libraryId", libraryId);
        model.addAttribute("iRtLibraryId", iRtLibraryId);

        ProjectDO projectDO = new ProjectDO();
        projectDO.setName(name);
        projectDO.setDescription(description);
        projectDO.setOwnerName(ownerName);
        projectDO.setType(type);
        LibraryDO lib = libraryService.getById(libraryId);
        if (lib != null) {
            projectDO.setLibraryId(lib.getId());
            projectDO.setLibraryName(lib.getName());
        }

        LibraryDO iRtLib = libraryService.getById(iRtLibraryId);
        if (iRtLib != null) {
            projectDO.setIRtLibraryId(iRtLib.getId());
            projectDO.setIRtLibraryName(iRtLib.getName());
        }

        ResultDO result = projectService.insert(projectDO);
        if (result.isFailed()) {
            model.addAttribute(ERROR_MSG, result.getMsgInfo());
            return "project/create";
        }

        //Project创建成功再去创建文件夹
        String repository = vmProperties.getRepository();
        File file = new File(FilenameUtils.concat(repository, name));
        if (!file.exists()) {
            file.mkdirs();
        }

        return "redirect:/project/list";
    }

    @RequestMapping(value = "/edit/{id}")
    String edit(Model model, @PathVariable("id") String id,
                RedirectAttributes redirectAttributes) {

        ProjectDO project = projectService.getById(id);
        if (project == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.PROJECT_NOT_EXISTED.getMessage());
            return "redirect:/project/list";
        } else {
            PermissionUtil.check(project);
            model.addAttribute("libraryId", project.getLibraryId());
            model.addAttribute("iRtLibraryId", project.getIRtLibraryId());
            model.addAttribute("libraries", getLibraryList(0, true));
            model.addAttribute("iRtLibraries", getLibraryList(1, true));
            model.addAttribute("project", project);
            return "project/edit";
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    String update(Model model, @RequestParam("id") String id,
                  @RequestParam(value = "description", required = false) String description,
                  @RequestParam(value = "type", required = true) String type,
                  @RequestParam(value = "libraryId", required = false) String libraryId,
                  @RequestParam(value = "iRtLibraryId", required = false) String iRtLibraryId,
                  RedirectAttributes redirectAttributes) {


        ProjectDO project = projectService.getById(id);
        PermissionUtil.check(project);

        project.setDescription(description);
        project.setType(type);
        LibraryDO lib = libraryService.getById(libraryId);
        if (lib != null) {
            project.setLibraryId(lib.getId());
            project.setLibraryName(lib.getName());
        }

        LibraryDO iRtLib = libraryService.getById(iRtLibraryId);
        if (iRtLib != null) {
            project.setIRtLibraryId(iRtLib.getId());
            project.setIRtLibraryName(iRtLib.getName());
        }

        ResultDO result = projectService.update(project);
        if (result.isFailed()) {
            model.addAttribute(ERROR_MSG, result.getMsgInfo());
            return "project/create";
        }
        return "redirect:/project/list";
    }

    @RequestMapping(value = "/filemanager")
    String fileManager(Model model, @RequestParam(value = "name", required = true) String name,
                       RedirectAttributes redirectAttributes) {

        ProjectDO project = projectService.getByName(name);
        PermissionUtil.check(project);

        List<File> fileList = FileUtil.scanFiles(name);
        List<FileVO> fileVOList = new ArrayList<>();
        for (File file : fileList) {
            FileVO fileVO = new FileVO();
            fileVO.setName(file.getName());
            fileVO.setSize(file.length());
            if (file.length() / 1024 / 1024 > 0) {
                fileVO.setSizeStr(file.length() / 1024 / 1024 + " MB");
            } else {
                fileVO.setSizeStr(file.length() / 1024 + " KB");
            }
            fileVOList.add(fileVO);
        }
        model.addAttribute("project", project);
        model.addAttribute("fileList", fileVOList);
        return "project/file_manager";

    }

    @RequestMapping(value = "/doupload", method = RequestMethod.POST)
    @ResponseBody
    ResultDO doUpload(Model model,
                      @RequestParam(value = "projectName", required = true) String projectName,
                      @RequestParam("file") MultipartFile file,
                      @FormParam("form-data") UploadVO uploadVO) {

        ProjectDO project = projectService.getByName(projectName);
        PermissionUtil.check(project);
        model.addAttribute("project", project);
        chunkUploader.chunkUpload(file, uploadVO, projectName);
        return new ResultDO(true);
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    @ResponseBody
    public Object check(FileBlockVO block,
                        @RequestParam(value = "projectName", required = true) String projectName) {
        return chunkUploader.check(block, projectName);
    }

    @RequestMapping(value = "/scan")
    String scan(Model model,
                @RequestParam(value = "projectId", required = true) String projectId,
                RedirectAttributes redirectAttributes) {
        ProjectDO project = projectService.getById(projectId);
        PermissionUtil.check(project);

        List<File> fileList = FileUtil.scanFiles(project.getName());
        List<File> newFileList = new ArrayList<>();
        List<ExperimentDO> exps = experimentService.getAllByProjectId(projectId);
        List<String> existedExpNames = new ArrayList<>();
        for (ExperimentDO exp : exps) {
            existedExpNames.add(exp.getName());
        }
        //过滤文件
        for (File file : fileList) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(SuffixConst.JSON) && !existedExpNames.contains(FilenameUtils.getBaseName(file.getName()))) {
                newFileList.add(file);
            }
        }
        if (newFileList.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.NO_NEW_EXPERIMENTS.getMessage());
            return "redirect:/project/list";
        }

        TaskDO taskDO = new TaskDO(TaskTemplate.SCAN_AND_UPDATE_EXPERIMENTS, project.getName());
        taskDO.addLog(newFileList.size() + " total");
        taskService.insert(taskDO);
        List<ExperimentDO> expsToUpdate = new ArrayList<>();
        for (File file : newFileList) {
            ExperimentDO exp = new ExperimentDO();
            exp.setName(FilenameUtils.getBaseName(file.getName()));
            exp.setOwnerName(project.getOwnerName());
            exp.setProjectId(project.getId());
            exp.setProjectName(project.getName());
            exp.setType(project.getType());
            ResultDO result = experimentService.insert(exp);
            if (result.isFailed()) {
                taskDO.addLog("ERROR-" + exp.getId() + "-" + exp.getName());
                taskDO.addLog(result.getMsgInfo());
                taskService.update(taskDO);
            }
            expsToUpdate.add(exp);
        }


        experimentTask.uploadAird(expsToUpdate, taskDO);

        return "redirect:/task/detail/" + taskDO.getId();
    }

    @RequestMapping(value = "/irt")
    String irt(Model model,
               @RequestParam(value = "id", required = true) String id,
               RedirectAttributes redirectAttributes) {

        ProjectDO project = projectService.getById(id);
        PermissionUtil.check(project);
        List<ExperimentDO> expList = experimentService.getAllByProjectName(project.getName());

        model.addAttribute("exps", expList);
        model.addAttribute("project", project);
        model.addAttribute("iRtLibraryId", project.getIRtLibraryId());
        model.addAttribute("libraries", getLibraryList(1, true));

        return "project/irt";
    }

    @RequestMapping(value = "/doirt")
    String doIrt(Model model,
                 @RequestParam(value = "id", required = true) String id,
                 @RequestParam(value = "iRtLibraryId", required = false) String iRtLibraryId,
                 @RequestParam(value = "sigma", required = true, defaultValue = Constants.DEFAULT_SIGMA_STR) Float sigma,
                 @RequestParam(value = "spacing", required = true, defaultValue = Constants.DEFAULT_SPACING_STR) Float spacing,
                 @RequestParam(value = "mzExtractWindow", required = true, defaultValue = Constants.DEFAULT_MZ_EXTRACTION_WINDOW_STR) Float mzExtractWindow,
                 RedirectAttributes redirectAttributes) {

        ProjectDO project = projectService.getById(id);
        PermissionUtil.check(project);

        List<ExperimentDO> expList = experimentService.getAllByProjectId(id);
        if (expList == null) {
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, ResultCode.NO_EXPERIMENT_UNDER_PROJECT);
            return "redirect:/project/list";
        }

        TaskDO taskDO = new TaskDO(TaskTemplate.IRT, project.getName() + ":" + iRtLibraryId + "-Num:" + expList.size());
        taskService.insert(taskDO);
        SigmaSpacing sigmaSpacing = new SigmaSpacing(sigma, spacing);

        //支持直接使用标准库进行irt预测,在这里进行库的类型的检测,已进入不同的流程渠道
        LibraryDO lib = libraryService.getById(iRtLibraryId);
        IrtParams irtParams = new IrtParams();
        irtParams.setLibrary(lib);
        irtParams.setMzExtractWindow(mzExtractWindow);
        irtParams.setSigmaSpacing(sigmaSpacing);
        experimentTask.irt(taskDO, expList, irtParams);

        return "redirect:/task/list";
    }

    @RequestMapping(value = "/setPublic/{id}")
    String setPublic(@PathVariable("id") String id,
                     RedirectAttributes redirectAttributes) {
        ProjectDO project = projectService.getById(id);
        if (project == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.PROJECT_NOT_EXISTED.getMessage());
            return "redirect:/project/list";
        }
        PermissionUtil.check(project);

        project.setDoPublic(true);
        projectService.update(project);

        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.SET_PUBLIC_SUCCESS);
        return "redirect:/project/list";
    }

    @RequestMapping(value = "/deleteirt/{id}")
    String deleteIrt(@PathVariable("id") String id,
                     RedirectAttributes redirectAttributes) {

        ProjectDO project = projectService.getById(id);
        PermissionUtil.check(project);

        List<ExperimentDO> expList = experimentService.getAllByProjectId(id);
        if (expList == null) {
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, ResultCode.NO_EXPERIMENT_UNDER_PROJECT);
            return "redirect:/project/list";
        }
        for (ExperimentDO experimentDO : expList) {
            experimentDO.setIrtResult(null);
            experimentService.update(experimentDO);
        }

        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.DELETE_SUCCESS);
        return "redirect:/project/list";
    }

    @RequestMapping(value = "/deleteAll/{id}")
    String deleteAll(@PathVariable("id") String id,
                     RedirectAttributes redirectAttributes) {
        ProjectDO project = projectService.getById(id);
        PermissionUtil.check(project);

        List<ExperimentDO> expList = experimentService.getAllByProjectId(id);
        for (ExperimentDO experimentDO : expList) {
            String expId = experimentDO.getId();
            experimentService.delete(expId);
            swathIndexService.deleteAllByExpId(expId);
            analyseOverviewService.deleteAllByExpId(experimentDO.getId());
        }

        projectService.delete(id);
        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.DELETE_SUCCESS);
        return "redirect:/project/list";
    }

    @RequestMapping(value = "/deleteAnalyse/{id}")
    String deleteAnalyse(@PathVariable("id") String id,
                         RedirectAttributes redirectAttributes) {

        ProjectDO project = projectService.getById(id);
        PermissionUtil.check(project);

        String name = project.getName();
        List<ExperimentDO> expList = experimentService.getAllByProjectName(name);
        for (ExperimentDO experimentDO : expList) {
            analyseOverviewService.deleteAllByExpId(experimentDO.getId());
        }
        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.DELETE_SUCCESS);
        return "redirect:/project/list";
    }

    @RequestMapping(value = "/extractor")
    String extractor(Model model,
                     @RequestParam(value = "id", required = true) String id,
                     RedirectAttributes redirectAttributes) {


        ProjectDO project = projectService.getById(id);
        PermissionUtil.check(project);
        if (project == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.PROJECT_NOT_EXISTED.getMessage());
            return "redirect:/project/list";
        }

        List<ExperimentDO> expList = experimentService.getAllByProjectName(project.getName());
        model.addAttribute("libraryId", project.getLibraryId());
        model.addAttribute("iRtLibraryId", project.getIRtLibraryId());
        model.addAttribute("exps", expList);
        model.addAttribute("libraries", getLibraryList(0, true));
        model.addAttribute("iRtLibraries", getLibraryList(1, true));
        model.addAttribute("project", project);
        model.addAttribute("scoreTypes", ScoreType.getShownTypes());

        return "project/extractor";
    }

    @RequestMapping(value = "/doextract")
    String doExtract(Model model,
                     @RequestParam(value = "id", required = true) String id,
                     @RequestParam(value = "iRtLibraryId", required = false) String iRtLibraryId,
                     @RequestParam(value = "libraryId", required = true) String libraryId,
                     @RequestParam(value = "rtExtractWindow", required = true, defaultValue = Constants.DEFAULT_RT_EXTRACTION_WINDOW_STR) Float rtExtractWindow,
                     @RequestParam(value = "mzExtractWindow", required = true, defaultValue = Constants.DEFAULT_MZ_EXTRACTION_WINDOW_STR) Float mzExtractWindow,
                     @RequestParam(value = "note", required = false) String note,
                     @RequestParam(value = "fdr", required = true, defaultValue = Constants.DEFAULT_FDR_STR) Double fdr,
                     //打分相关的入参
                     @RequestParam(value = "sigma", required = false, defaultValue = Constants.DEFAULT_SIGMA_STR) Float sigma,
                     @RequestParam(value = "spacing", required = false, defaultValue = Constants.DEFAULT_SPACING_STR) Float spacing,
                     @RequestParam(value = "shapeScoreThreshold", required = false, defaultValue = Constants.DEFAULT_SHAPE_SCORE_THRESHOLD_STR) Float shapeScoreThreshold,
                     @RequestParam(value = "shapeWeightScoreThreshold", required = false, defaultValue = Constants.DEFAULT_SHAPE_WEIGHT_SCORE_THRESHOLD_STR) Float shapeWeightScoreThreshold,
                     HttpServletRequest request,
                     RedirectAttributes redirectAttributes) {

        ProjectDO project = projectService.getById(id);
        if (project == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.PROJECT_NOT_EXISTED.getMessage());
            return "redirect:/project/extractor?id=" + id;
        }
        PermissionUtil.check(project);

        LibraryDO library = libraryService.getById(libraryId);
        if (library == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.LIBRARY_NOT_EXISTED.getMessage());
            return "redirect:/project/extractor?id=" + id;
        }

        boolean doIrt = false;
        LibraryDO irtLibrary = null;
        if (iRtLibraryId != null && !iRtLibraryId.isEmpty()) {
            irtLibrary = libraryService.getById(iRtLibraryId);
            if (irtLibrary == null) {
                redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.IRT_LIBRARY_NOT_EXISTED.getMessage());
                return "redirect:/project/extractor?id=" + id;
            }
            doIrt = true;
        }

        List<String> scoreTypes = ScoreUtil.getScoreTypes(request);

        List<ExperimentDO> exps = experimentService.getAllByProjectId(id);
        if (exps == null) {
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, ResultCode.NO_EXPERIMENT_UNDER_PROJECT.getMessage());
            return "redirect:/project/list";
        }
        String errorInfo = "";
        TaskTemplate template = null;
        if (doIrt) {
            template = TaskTemplate.IRT_EXTRACT_PEAKPICK_SCORE;
        } else {
            template = TaskTemplate.EXTRACT_PEAKPICK_SCORE;
        }

        for (ExperimentDO exp : exps) {
            if (!doIrt && (exp.getIrtResult() == null)) {
                errorInfo = errorInfo + ResultCode.IRT_FIRST + ":" + exp.getName() + "(" + exp.getId() + ")";
                continue;
            }

            TaskDO taskDO = new TaskDO(template, exp.getName() + ":" + library.getName() + "(" + libraryId + ")");
            taskService.insert(taskDO);

            WorkflowParams input = new WorkflowParams();
            SigmaSpacing ss = new SigmaSpacing(sigma, spacing);
            input.setSigmaSpacing(ss);
            input.setExperimentDO(exp);
            input.setFdr(fdr);
            if (doIrt) {
                input.setIRtLibrary(irtLibrary);
            } else {
                input.setSlopeIntercept(exp.getIrtResult().getSi());
            }
            input.setLibrary(library);
            input.setOwnerName(getCurrentUsername());
            input.setExtractParams(new ExtractParams(mzExtractWindow, rtExtractWindow));
            input.setScoreTypes(scoreTypes);

            input.setXcorrShapeThreshold(shapeScoreThreshold);
            input.setXcorrShapeWeightThreshold(shapeWeightScoreThreshold);
            experimentTask.extract(taskDO, input);
        }

        if (StringUtils.isNotEmpty(errorInfo)) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, errorInfo);
        }
        return "redirect:/task/list";
    }

    @RequestMapping(value = "/portionSelector")
    String portionSelector(Model model,
                           @RequestParam(value = "id", required = true) String id,
                           RedirectAttributes redirectAttributes) {
        ProjectDO project = projectService.getById(id);
        if (project == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.PROJECT_NOT_EXISTED);
            return "redirect:/project/list";
        }

        PermissionUtil.check(project);

        List<ExperimentDO> expList = experimentService.getAllByProjectName(project.getName());
        List<ExperimentDO> collect = expList.stream().sorted(Comparator.comparing(ExperimentDO::getName)).collect(Collectors.toList());
        model.addAttribute("project", project);
        model.addAttribute("expList", collect);
        return "project/portionSelector";
    }

    @RequestMapping(value = "/overview")
    String overview(Model model,
                    @RequestParam(value = "id", required = true) String projectId,
                    @RequestParam(value = "peptideRefInfo", required = false) String peptideRefInfo,
                    @RequestParam(value = "proteinNameInfo", required = false) String proteinNameInfo,
                    HttpServletRequest request,
                    RedirectAttributes redirectAttributes) {

        //get project name
        ProjectDO project = projectService.getById(projectId);
        if (project == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.PROJECT_NOT_EXISTED);
            return "redirect:/project/list";
        }

        PermissionUtil.check(project);
        String projectName = project.getName() + "(" + project.getId() + ")";
        //get corresponding experiments
        List<ExperimentDO> expList = experimentService.getAllByProjectName(project.getName());
        expList.sort(new Comparator<ExperimentDO>() {
            @Override
            public int compare(ExperimentDO o1, ExperimentDO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        String libraryId = "";
        String libName = "";
        List<String> analyseOverviewIdList = new ArrayList<>();
        List<String> expNameList = new ArrayList<>();
        for (ExperimentDO experimentDO : expList) {
            String checkState = request.getParameter(experimentDO.getId());
            if (checkState != null && checkState.equals("on")) {
                //analyse checked experiments
                AnalyseOverviewDO analyseOverviewDO = analyseOverviewService.getAllByExpId(experimentDO.getId()).get(0);
                libraryId = analyseOverviewDO.getLibraryId();
                libName = analyseOverviewDO.getLibraryName() + "(" + libraryId + ")";
                analyseOverviewIdList.add(analyseOverviewDO.getId());
                expNameList.add(experimentDO.getName());
            }
        }
        HashMap<String, PeptideDO> peptideDOMap = new HashMap<>();
        List<String> protNameList = new ArrayList<>();
        HashMap<String, HashMap<String, List<Integer>>> pepFragIntListMap = new HashMap<>();
        HashMap<String, String> intMap = new HashMap<>();
        if (!proteinNameInfo.isEmpty()) {
            for (String proteinName : proteinNameInfo.split(";")) {
                List<PeptideDO> peptideDOList = peptideService.getAllByLibraryIdAndProteinName(libraryId, proteinName);
                for (PeptideDO peptideDO : peptideDOList) {
                    peptideDOMap.put(peptideDO.getPeptideRef(), peptideDO);
                }
            }
        }
        if (!peptideRefInfo.isEmpty()) {
            String[] peptideRefs = peptideRefInfo.split(";");
            for (String peptideRef : peptideRefs) {
                PeptideDO peptideDO = peptideService.getByLibraryIdAndPeptideRef(libraryId, peptideRef);
                if (peptideDO == null) {
                    continue;
                }
                peptideDOMap.put(peptideRef, peptideDO);
            }
        }

        HashMap<String, List<Boolean>> identifyMap = new HashMap<>();
        for (PeptideDO peptideDO : peptideDOMap.values()) {
            //protein name
            protNameList.add(peptideDO.getProteinName());
            //fragment cutInfo list
            Set<String> cutInfoSet = peptideDO.getFragmentMap().keySet();
            //experiments
            HashMap<String, List<Integer>> fragIntListMap = new HashMap<>();
            String intOverall = "";
            List<Boolean> identifyStatList = new ArrayList<>();
            for (String analyseOverviewId : analyseOverviewIdList) {
                //get fragment intensity map
                AnalyseDataDO analyseDataDO = analyseDataService.getByOverviewIdAndPeptideRefAndIsDecoy(analyseOverviewId, peptideDO.getPeptideRef(), false);
                Map<String, Double> fragIntMap;
                if (analyseDataDO == null) {
                    fragIntMap = new HashMap<>();
                    identifyStatList.add(false);
                } else {
                    if (analyseDataDO.getIdentifiedStatus() == 0) {
                        identifyStatList.add(true);
                    } else {
                        identifyStatList.add(false);
                    }
                    fragIntMap = FeatureUtil.toMap(analyseDataDO.getFragIntFeature());
                    intOverall += analyseDataDO.getIntensitySum() + ", ";
                }

                //get fragment intensity list map
                for (String cutInfo : cutInfoSet) {
                    if (fragIntListMap.get(cutInfo) == null) {
                        List<Integer> newList = new ArrayList<>();
                        newList.add(fragIntMap.get(cutInfo) == null ? 0 : (int) Math.round(fragIntMap.get(cutInfo)));
                        fragIntListMap.put(cutInfo, newList);
                    } else {
                        fragIntListMap.get(cutInfo).add(fragIntMap.get(cutInfo) == null ? 0 : (int) Math.round(fragIntMap.get(cutInfo)));
                    }
                }
            }
            if (intOverall.isEmpty()) {
                intOverall = "0";
            }
            identifyMap.put(peptideDO.getPeptideRef(), identifyStatList);
            intMap.put(peptideDO.getPeptideRef(), intOverall);
            pepFragIntListMap.put(peptideDO.getPeptideRef(), fragIntListMap);
        }

        //横坐标实验，纵坐标不同pep
        model.addAttribute("projectName", projectName);
        model.addAttribute("libraryId", libraryId);
        model.addAttribute("libName", libName);
        model.addAttribute("protNameList", protNameList);
        model.addAttribute("pepFragIntListMap", pepFragIntListMap);
        model.addAttribute("expNameList", expNameList);
        model.addAttribute("intMap", intMap);
        model.addAttribute("identifyMap", identifyMap);

        return "project/overview";
    }

    @RequestMapping(value = "/writeToFile")
    String writeToFile(Model model,
                       @RequestParam(value = "id", required = true) String id,
                       RedirectAttributes redirectAttributes) {

        ProjectDO projectDO = projectService.getById(id);
        PermissionUtil.check(projectDO);
        List<ExperimentDO> experimentDOList = experimentService.getAllByProjectId(id);
        String defaultOutputPath = RepositoryUtil.buildOutputPath(projectDO.getName(), projectDO.getName() + ".tsv");
        model.addAttribute("expList", experimentDOList);
        model.addAttribute("project", projectDO);
        model.addAttribute("defaultPathName", defaultOutputPath);
        model.addAttribute("outputAllPeptides", false);
        return "project/outputSelector";
    }

    @RequestMapping(value = "/doWriteToFile", method = RequestMethod.POST)
    void doWriteToFile(Model model,
                         @RequestParam(value = "projectId", required = true) String projectId,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {

        ProjectDO projectDO = projectService.getById(projectId);
        PermissionUtil.check(projectDO);

        List<SimpleExperiment> experimentList = experimentService.getAllSimpleExperimentByProjectId(projectId);
        HashMap<String, HashMap<String, String>> intensityMap = new HashMap<>();//key为PeptideRef, value为另外一个Map,map的key为ExperimentName,value为intensity值
        HashMap<String, String> pepToProt = new HashMap<>();//key为PeptideRef,value为ProteinName

        for (SimpleExperiment simpleExp : experimentList) {
            String checkState = request.getParameter(simpleExp.getId());
            if (checkState != null && checkState.equals("on")) {
                //取每一个实验的第一个分析结果进行分析
                AnalyseOverviewDO analyseOverview = analyseOverviewService.getFirstAnalyseOverviewByExpId(simpleExp.getId());
                if(analyseOverview == null){
                    continue;
                }
                List<PeptideIntensity> peptideIntensityList = analyseDataService.getPeptideIntensityByOverviewId(analyseOverview.getId());
                for (PeptideIntensity peptideIntensity : peptideIntensityList) {
                    if (intensityMap.containsKey(peptideIntensity.getPeptideRef())) {
                        intensityMap.get(peptideIntensity.getPeptideRef()).put(simpleExp.getName(), peptideIntensity.getIntensitySum().toString());
                    } else {
                        HashMap<String, String> map = new HashMap<>();
                        map.put(simpleExp.getName(), peptideIntensity.getIntensitySum().toString());
                        intensityMap.put(peptideIntensity.getPeptideRef(), map);
                        pepToProt.put(peptideIntensity.getPeptideRef(), peptideIntensity.getProteinName());
                    }
                }
            }
        }

        try {
            OutputStream os = response.getOutputStream();
            StringBuffer sb = new StringBuffer();

            sb.append("ProteinName").append(SymbolConst.TAB).append("PeptideRef");
            for (SimpleExperiment simpleExperiment : experimentList) {
                sb.append(SymbolConst.TAB).append(simpleExperiment.getName());
            }
            sb.append(SymbolConst.RETURN);
            for (String peptideRef : intensityMap.keySet()) {
                sb.append(pepToProt.get(peptideRef)).append(SymbolConst.TAB).append(peptideRef);
                for (SimpleExperiment simpleExperiment : experimentList) {
                    if (intensityMap.get(peptideRef).containsKey(simpleExperiment.getName())) {
                        sb.append(SymbolConst.TAB).append(intensityMap.get(peptideRef).get(simpleExperiment.getName()));
                    } else {
                        sb.append(SymbolConst.TAB);
                    }
                }
                sb.append(SymbolConst.RETURN);
            }

            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("gbk");
            response.setHeader("Cache-Control","max-age=60");
            response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(projectDO.getName()+".tsv", "gbk"));
            os.write(sb.toString().getBytes("gbk"));
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
