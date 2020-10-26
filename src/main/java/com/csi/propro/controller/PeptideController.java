package com.csi.propro.controller;

import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.db.LibraryDO;
import com.csi.propro.domain.db.PeptideDO;
import com.csi.propro.domain.db.simple.Protein;
import com.csi.propro.domain.query.PeptideQuery;
import com.csi.propro.service.PeptideService;
import com.csi.propro.utils.PermissionUtil;
import com.csi.propro.algorithm.formula.FormulaCalculator;
import com.csi.propro.algorithm.formula.FragmentFactory;
import com.csi.propro.algorithm.decoy.generator.ShuffleGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-12 12:19
 */
@Controller
@RequestMapping("peptide")
public class PeptideController extends BaseController {

    @Autowired
    FragmentFactory fragmentFactory;
    @Autowired
    FormulaCalculator formulaCalculator;
    @Autowired
    PeptideService peptideService;
    @Autowired
    ShuffleGenerator shuffleGenerator;

    @RequestMapping(value = "/list")
    String list(Model model,
                @RequestParam(value = "libraryId", required = true) String libraryId,
                @RequestParam(value = "proteinName", required = false) String proteinName,
                @RequestParam(value = "peptideRef", required = false) String peptideRef,
                @RequestParam(value = "sequence", required = false) String sequence,
                @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                @RequestParam(value = "uniqueFilter", required = false, defaultValue = "All") String uniqueFilter,
                @RequestParam(value = "pageSize", required = false, defaultValue = "30") Integer pageSize) {
        long startTime = System.currentTimeMillis();
        LibraryDO temp = libraryService.getById(libraryId);
        PermissionUtil.check(temp);

        model.addAttribute("libraryId", libraryId);
        model.addAttribute("libraryName", temp.getName());
        model.addAttribute("proteinName", proteinName);
        model.addAttribute("peptideRef", peptideRef);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("uniqueFilter", uniqueFilter);
        model.addAttribute("libraries",getLibraryList(null, true));
        model.addAttribute("sequence",sequence);

        PeptideQuery query = new PeptideQuery();

        if (libraryId != null && !libraryId.isEmpty()) {
            query.setLibraryId(libraryId);
        }
        if (peptideRef != null && !peptideRef.isEmpty()) {
            query.setPeptideRef(peptideRef);
        }
        if (proteinName != null && !proteinName.isEmpty()) {
            query.setProteinName(proteinName);
        }
        if(sequence != null && !sequence.isEmpty()){
            query.setSequence(sequence);
        }
        if (!uniqueFilter.equals("All")) {
            if(uniqueFilter.equals("Yes")){
                query.setIsUnique(true);
            }else if(uniqueFilter.equals("No")){
                query.setIsUnique(false);
            }
        }

        query.setPageSize(pageSize);
        query.setPageNo(currentPage);
        ResultDO<List<PeptideDO>> resultDO = peptideService.getList(query);

        List<PeptideDO> peptideList = resultDO.getModel();

        model.addAttribute("peptideList", peptideList);
        model.addAttribute("totalPage", resultDO.getTotalPage());
        model.addAttribute("currentPage", currentPage);
        StringBuilder builder = new StringBuilder();
        builder.append("本次搜索耗时:").append(System.currentTimeMillis() - startTime).append("毫秒;包含搜索结果总计:")
                .append(resultDO.getTotalNum()).append("条");
        model.addAttribute("searchResult", builder.toString());
        return "peptide/list";
    }

    @RequestMapping(value = "/protein")
    String protein(Model model,
                @RequestParam(value = "libraryId", required = true) String libraryId,
                @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                @RequestParam(value = "pageSize", required = false, defaultValue = "30") Integer pageSize) {
        long startTime = System.currentTimeMillis();

        LibraryDO temp = libraryService.getById(libraryId);
        PermissionUtil.check(temp);

        model.addAttribute("libraryId", libraryId);
        model.addAttribute("pageSize", pageSize);

        PeptideQuery query = new PeptideQuery();

        if (libraryId != null && !libraryId.isEmpty()) {
            query.setLibraryId(libraryId);
        }

        query.setPageSize(pageSize);
        query.setPageNo(currentPage);
        ResultDO<List<Protein>> resultDO = peptideService.getProteinList(query);

        model.addAttribute("proteins", resultDO.getModel());
        model.addAttribute("totalPage", resultDO.getTotalPage());
        model.addAttribute("currentPage", currentPage);
        StringBuilder builder = new StringBuilder();
        builder.append("本次搜索耗时:").append(System.currentTimeMillis() - startTime).append("毫秒;包含搜索结果总计:")
                .append(resultDO.getTotalNum()).append("条");
        model.addAttribute("searchResult", builder.toString());
        return "peptide/protein";
    }

    @RequestMapping(value = "/detail/{id}")
    String detail(Model model, @PathVariable("id") String id, RedirectAttributes redirectAttributes) {
        ResultDO<PeptideDO> resultDO = peptideService.getById(id);
        if (resultDO.isSuccess()) {

            LibraryDO temp = libraryService.getById(resultDO.getModel().getLibraryId());
            PermissionUtil.check(temp);

            model.addAttribute("peptide", resultDO.getModel());
            return "peptide/detail";
        } else {
            redirectAttributes.addFlashAttribute(ERROR_MSG, resultDO.getMsgInfo());
            return "redirect:/peptide/list";
        }
    }

    @RequestMapping(value = "/calculator")
    String calculator(Model model,
                      @RequestParam(value = "sequence", required = false) String sequence,
                      @RequestParam(value = "type", required = false) String type,
                      @RequestParam(value = "adjust", required = false, defaultValue = "0") int adjust,
                      @RequestParam(value = "deviation", required = false, defaultValue = "0") double deviation,
                      @RequestParam(value = "unimodIds", required = false) String unimodIds,
                      @RequestParam(value = "charge", required = false, defaultValue = "1") int charge
    ) {
        model.addAttribute("charge", charge);
        model.addAttribute("adjust", adjust);
        model.addAttribute("deviation", deviation);
        model.addAttribute("unimodIds", unimodIds);
        if (sequence == null || sequence.isEmpty()) {
            return "peptide/calculator";
        }

        if (type == null || type.isEmpty()) {
            return "peptide/calculator";
        }

        model.addAttribute("sequence", sequence);
        model.addAttribute("type", type);

        List<String> unimodList = null;
        if (unimodIds != null && !unimodIds.isEmpty()) {
            String[] unimodIdArray = unimodIds.split(",");
            unimodList = Arrays.asList(unimodIdArray);
        }

        //默认偏差为0
        double monoMz = formulaCalculator.getMonoMz(sequence, type, charge, adjust, deviation, false, unimodList);
        double averageMz = formulaCalculator.getAverageMz(sequence, type, charge, adjust, deviation, false, unimodList);
        model.addAttribute("monoMz", monoMz);
        model.addAttribute("averageMz", averageMz);

        return "peptide/calculator";
    }

}
