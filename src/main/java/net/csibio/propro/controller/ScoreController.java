package net.csibio.propro.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import net.csibio.propro.domain.db.AnalyseDataDO;
import net.csibio.propro.domain.db.AnalyseOverviewDO;
import net.csibio.propro.service.AnalyseDataService;
import net.csibio.propro.service.AnalyseOverviewService;
import net.csibio.propro.service.ScoreService;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.constants.enums.ScoreType;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.query.AnalyseDataQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-14 16:02
 */
@Controller
@RequestMapping("/score")
public class ScoreController extends BaseController {

    @Autowired
    ScoreService scoreService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    AnalyseDataService analyseDataService;

    @RequestMapping(value = "/list")
    String list(Model model,
                @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                @RequestParam(value = "overviewId", required = false) String overviewId,
                @RequestParam(value = "peptideRef", required = false) String peptideRef,
                @RequestParam(value = "fdrStart", required = false) Double fdrStart,
                @RequestParam(value = "fdrEnd", required = false) Double fdrEnd,
                @RequestParam(value = "isIdentified", required = false) String isIdentified,
                @RequestParam(value = "isDecoy", required = false) String isDecoy,
                RedirectAttributes redirectAttributes) {
        model.addAttribute("overviewId", overviewId);
        model.addAttribute("peptideRef", peptideRef);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("fdrStart", fdrStart);
        model.addAttribute("fdrEnd", fdrEnd);
        model.addAttribute("isIdentified", isIdentified);
        model.addAttribute("isDecoy", isDecoy);
        AnalyseDataQuery query = new AnalyseDataQuery();
        if (peptideRef != null && !peptideRef.isEmpty()) {
            query.setPeptideRef(peptideRef);
        }
        if (isIdentified != null && isIdentified.equals("Yes")) {
            query.addIndentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_SUCCESS);
        } else if (isIdentified != null && isIdentified.equals("No")) {
            query.addIndentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_NO_FIT);
            query.addIndentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_UNKNOWN);
        }
        if (isDecoy != null && isDecoy.equals("Yes")) {
            query.setIsDecoy(true);
        } else if (isDecoy != null && isDecoy.equals("No")) {
            query.setIsDecoy(false);
        }
        if (fdrStart != null) {
            query.setFdrStart(fdrStart);
        }
        if (fdrEnd != null) {
            query.setFdrEnd(fdrEnd);
        }
        if (overviewId == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.ANALYSE_OVERVIEW_ID_CAN_NOT_BE_EMPTY.getMessage());
            return "redirect:/analyse/overview/list";
        }
        query.setOverviewId(overviewId);
        query.setPageSize(50);
        query.setPageNo(currentPage);
        ResultDO<AnalyseOverviewDO> overviewResult = analyseOverviewService.getById(overviewId);
        if (overviewResult.isFailed()) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.ANALYSE_OVERVIEW_NOT_EXISTED.getMessage());
            return "redirect:/analyse/overview/list";
        }

        ResultDO<List<AnalyseDataDO>> resultDO = analyseDataService.getList(query);
        model.addAttribute("overview", overviewResult.getModel());
        model.addAttribute("scores", resultDO.getModel());
        model.addAttribute("scoreTypes", overviewResult.getModel().getScoreTypes());
        model.addAttribute("totalPage", resultDO.getTotalPage());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalNum", resultDO.getTotalNum());
        return "scores/list";
    }

    @RequestMapping(value = "/result/list")
    String resultList(Model model,
                @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                @RequestParam(value = "overviewId", required = true) String overviewId,
                @RequestParam(value = "peptideRef", required = false) String peptideRef,
                @RequestParam(value = "proteinName", required = false) String proteinName,
                @RequestParam(value = "isIdentified", required = false) String isIdentified,
                RedirectAttributes redirectAttributes) {

        model.addAttribute("overviewId", overviewId);
        model.addAttribute("proteinName", proteinName);
        model.addAttribute("peptideRef", peptideRef);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("isIdentified", isIdentified);

        ResultDO<AnalyseOverviewDO> overviewResult = analyseOverviewService.getById(overviewId);

        AnalyseDataQuery query = new AnalyseDataQuery();
        query.setIsDecoy(false);
        if (peptideRef != null && !peptideRef.isEmpty()) {
            query.setPeptideRef(peptideRef);
        }
        if (proteinName != null && !proteinName.isEmpty()) {
            query.setProteinName(proteinName);
        }
        if (isIdentified != null && isIdentified.equals("Yes")) {
            query.addIndentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_SUCCESS);
        } else if (isIdentified != null && isIdentified.equals("No")) {
            query.addIndentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_NO_FIT);
            query.addIndentifiedStatus(AnalyseDataDO.IDENTIFIED_STATUS_UNKNOWN);
        }
        query.setOverviewId(overviewId);

        List<AnalyseDataDO> analyseDataDOList = analyseDataService.getAll(query);
        if (peptideRef != null && peptideRef.length()>0){
            query.setPeptideRef(null);
            query.setProteinName(analyseDataDOList.get(0).getProteinName());
            analyseDataDOList = analyseDataService.getAll(query);
        }
        HashMap<String, List<AnalyseDataDO>> proteinMap = new HashMap<>();
        for (AnalyseDataDO dataDO: analyseDataDOList){
            if (proteinMap.containsKey(dataDO.getProteinName())){
                proteinMap.get(dataDO.getProteinName()).add(dataDO);
            }else {
                List<AnalyseDataDO> newList = new ArrayList<>();
                newList.add(dataDO);
                proteinMap.put(dataDO.getProteinName(), newList);
            }
        }
        List<String> protList = new ArrayList<>(proteinMap.keySet());
        int totalPage = (int) Math.ceil(protList.size()/(double)pageSize);
        HashMap<String, List<AnalyseDataDO>> pageProtMap = new HashMap<>();

        for(int i=pageSize*(currentPage-1); i<protList.size() && i<pageSize*currentPage; i++){
            pageProtMap.put(protList.get(i), proteinMap.get(protList.get(i)));
        }

        model.addAttribute("protMap", pageProtMap);
        model.addAttribute("overview", overviewResult.getModel());
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalNum", protList.size());
        return "scores/result/list";
    }

    @RequestMapping(value = "/detail")
    String detail(Model model, @RequestParam(value = "overviewId", required = true) String overviewId, RedirectAttributes redirectAttributes) {

        ResultDO<AnalyseOverviewDO> overviewResult = analyseOverviewService.getById(overviewId);
        if (overviewResult.isFailed()) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.ANALYSE_OVERVIEW_NOT_EXISTED.getMessage());
            return "redirect:/analyse/overview/list";
        }

        model.addAttribute("scoreTypes", ScoreType.getUsedTypes());
        model.addAttribute("scoreTypeArray", JSONArray.parseArray(JSON.toJSONString(ScoreType.getUsedTypes())));
        model.addAttribute("overview", overviewResult.getModel());
        return "scores/detail";
    }
}
