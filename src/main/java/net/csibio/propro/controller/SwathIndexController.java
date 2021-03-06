package net.csibio.propro.controller;

import net.csibio.propro.domain.db.ExperimentDO;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.service.ExperimentService;
import net.csibio.propro.service.SwathIndexService;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.query.SwathIndexQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-11 20:03
 */
@Controller
@RequestMapping("swathindex")
public class SwathIndexController extends BaseController {

    @Autowired
    SwathIndexService swathIndexService;

    @Autowired
    ExperimentService experimentService;

    @RequestMapping(value = "/list")
    String list(Model model,
                @RequestParam(value = "expId", required = false) String expId,
                @RequestParam(value = "msLevel", required = false) Integer msLevel,
                @RequestParam(value = "mzStart", required = false) Float mzStart) {
        long startTime = System.currentTimeMillis();
        model.addAttribute("expId", expId);
        model.addAttribute("msLevel", msLevel);
        model.addAttribute("mzStart", mzStart);

        if (expId == null || expId.isEmpty()) {
            model.addAttribute(ERROR_MSG, ResultCode.SWATH_INDEX_LIST_MUST_BE_QUERY_WITH_EXPERIMENT_ID.getMessage());
            return "swathindex/list";
        }

        ResultDO<ExperimentDO> expResult = experimentService.getById(expId);
        if (expResult.isFailed()) {
            model.addAttribute(ERROR_MSG, ResultCode.EXPERIMENT_NOT_EXISTED.getMessage());
            return "swathindex/list";
        }
        model.addAttribute("experiment", expResult.getModel());
        SwathIndexQuery query = new SwathIndexQuery();
        query.setExpId(expId);
        if (msLevel != null) {
            query.setLevel(msLevel);
        }
        if(mzStart != null){
            query.setMzStart(mzStart);
        }
        List<SwathIndexDO> swathList = swathIndexService.getAll(query);

        model.addAttribute("swathIndexList", swathList);
        model.addAttribute("totalPage", 1);
        model.addAttribute("currentPage", 1);
        StringBuilder builder = new StringBuilder();
        builder.append("本次搜索耗时:").append(System.currentTimeMillis() - startTime).append("毫秒;包含搜索结果总计:")
                .append(swathList.size()).append("条");
        model.addAttribute("searchResult", builder.toString());
        return "swathindex/list";
    }

    @RequestMapping(value = "/detail/{id}")
    String detail(Model model, @PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        SwathIndexDO index = swathIndexService.getById(id);
        if (index != null) {
            model.addAttribute("swathIndex", index);
            return "swathindex/detail";
        } else {
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.SWATH_INDEX_NOT_EXISTED.getMessage());
            return "redirect:/swathindex/list";
        }
    }
}
