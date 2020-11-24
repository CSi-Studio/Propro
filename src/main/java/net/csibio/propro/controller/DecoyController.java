package net.csibio.propro.controller;

import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.query.PeptideQuery;
import net.csibio.propro.service.PeptideService;
import net.csibio.propro.algorithm.decoy.BaseGenerator;
import net.csibio.propro.algorithm.decoy.generator.NicoGenerator;
import net.csibio.propro.algorithm.decoy.generator.ShuffleGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static net.csibio.propro.constants.Constants.MAX_UPDATE_RECORD_FOR_PEPTIDE;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-19 16:03
 */
@Controller
@RequestMapping("decoy")
public class DecoyController extends BaseController {

    @Autowired
    ShuffleGenerator shuffleGenerator;
    @Autowired
    NicoGenerator nicoGenerator;
    @Autowired
    PeptideService peptideService;

    @RequestMapping(value = "/delete")
    String delete(Model model, @RequestParam(value = "id", required = true) String id) {

        LibraryDO library = libraryService.getById(id);
        peptideService.deleteAllDecoyByLibraryId(id);
        libraryService.countAndUpdateForLibrary(library);
        return "redirect:/library/detail/" + id;
    }

    @RequestMapping(value = "/generate")
    String generate(Model model,
                    @RequestParam(value = "id", required = true) String id,
                    @RequestParam(value = "generator", required = false, defaultValue = "shuffle") String generator) {

        LibraryDO library = libraryService.getById(id);

        logger.info("正在删除原有伪肽段");
        //删除原有的伪肽段
        peptideService.deleteAllDecoyByLibraryId(id);
        logger.info("原有伪肽段删除完毕");
        //计算原始肽段数目
        PeptideQuery query = new PeptideQuery();
        query.setLibraryId(id);
        long totalCount = peptideService.count(query);
        int totalPage = (int) (totalCount / MAX_UPDATE_RECORD_FOR_PEPTIDE) + 1;
        query.setPageSize(MAX_UPDATE_RECORD_FOR_PEPTIDE);
        int countForInsert = 0;
        BaseGenerator bg = null;
        switch (generator) {
            case NicoGenerator.NAME:
                bg = nicoGenerator;
                library.setGenerator(NicoGenerator.NAME);
                break;
            case ShuffleGenerator.NAME:
                bg = shuffleGenerator;
                library.setGenerator(ShuffleGenerator.NAME);
                break;
            default:
                bg = shuffleGenerator;
                library.setGenerator(NicoGenerator.NAME);
        }
        for (int i = 1; i <= totalPage; i++) {
            query.setPageNo(i);
            ResultDO<List<PeptideDO>> resultDO = peptideService.getList(query);
            List<PeptideDO> list = resultDO.getModel();
            bg.generate(list);
            ResultDO resultTmp = peptideService.updateDecoyInfos(list);
            if (resultTmp.isSuccess()) {
                countForInsert += list.size();
                logger.info("新生成伪肽段" + countForInsert + "条");
            }
        }

        libraryService.countAndUpdateForLibrary(library);

        return "redirect:/library/detail/" + id;
    }
}
