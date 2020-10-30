package net.csibio.propro.openapi;

import net.csibio.propro.controller.BaseController;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.service.PeptideService;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.domain.query.PeptideQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/peptide")
@Api("OpenAPI 1.0-Beta for Propro")
public class PeptideApi extends BaseController {

    @Autowired
    PeptideService peptideService;

    @ResponseBody
    @RequestMapping(value = "getById", method = RequestMethod.GET)
    @ApiOperation(value = "Get Peptide by Id", notes = "根据Id获取肽段对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "peptide id", dataType = "string", required = true)
    })
    public ResultDO<PeptideDO> getById(Model model,
                                       @RequestParam(value = "id", required = true) String id) {

        ResultDO<PeptideDO> result = peptideService.getById(id);
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "getList", method = RequestMethod.GET)
    @ApiOperation(value = "Get Peptide List", notes = "根据条件获取标准库中的肽段列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "libraryId", value = "library id", dataType = "string", required = true),
            @ApiImplicitParam(name = "mzStart", value = "precursor mz start", dataType = "double", required = false),
            @ApiImplicitParam(name = "mzEnd", value = "precursor mz end", dataType = "double", required = false),
            @ApiImplicitParam(name = "pageSize", value = "page size", dataType = "int", required = false, defaultValue = "50"),
            @ApiImplicitParam(name = "currentPage", value = "current page", dataType = "int", required = false, defaultValue = "1")
    })
    public ResultDO<List<PeptideDO>> getList(Model model,
                                                    @RequestParam(value = "libraryId", required = true) String libraryId,
                                                    @RequestParam(value = "mzStart", required = false) Double mzStart,
                                                    @RequestParam(value = "mzEnd", required = false) Double mzEnd,
                                                    @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                                                    @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize) {
        PeptideQuery query = new PeptideQuery();
        if (libraryId != null) {
            query.setLibraryId(libraryId);
        }
        if (mzStart != null) {
            query.setMzStart(mzStart);
        }
        if (mzEnd != null) {
            query.setMzEnd(mzEnd);
        }

        buildPageQuery(query, currentPage, pageSize);
        ResultDO<List<PeptideDO>> resultDO = peptideService.getList(query);
        return resultDO;
    }

    @ResponseBody
    @RequestMapping(value = "getByPeptideRefAndIsDecoy", method = RequestMethod.GET)
    @ApiOperation(value = "Get Peptide by PeptideRef and IsDecoy", notes = "根据PeptideRef和是否是伪肽段来获取肽段对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "libraryId", value = "library id", dataType = "string", required = true),
            @ApiImplicitParam(name = "isDecoy", value = "is decoy or target", dataType = "boolean", required = true),
            @ApiImplicitParam(name = "peptideRef", value = "peptide ref", dataType = "string", required = true)
    })
    public ResultDO<PeptideDO> getByPeptideRefAndIsDecoy(Model model,
                                                         @RequestParam(value = "libraryId", required = true) String libraryId,
                                                         @RequestParam(value = "peptideRef", required = true) String peptideRef) {

        PeptideDO peptide = peptideService.getByLibraryIdAndPeptideRef(libraryId, peptideRef);
        if (peptide == null) {
            return ResultDO.buildError(ResultCode.OBJECT_NOT_EXISTED);
        }
        ResultDO resultDO = new ResultDO(true);
        resultDO.setModel(peptide);
        return resultDO;
    }
}
