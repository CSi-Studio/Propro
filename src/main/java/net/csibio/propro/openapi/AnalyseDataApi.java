package net.csibio.propro.openapi;

import net.csibio.propro.controller.BaseController;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.AnalyseDataDO;
import net.csibio.propro.service.AnalyseDataService;
import net.csibio.propro.domain.query.AnalyseDataQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/analyse/data")
@Api("OpenAPI 1.0-Beta for Propro")
public class AnalyseDataApi extends BaseController {

    @Autowired
    AnalyseDataService analyseDataService;

    @ApiOperation(value = "Get Analyse Data by Id", notes = "根据ID获取分析对象")
    @RequestMapping(value = "getById", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "analyse data id", dataType = "string", required = true)
    })
    public ResultDO<AnalyseDataDO> getById(Model model,
                                           @RequestParam(value = "id", required = true) String id) {
        ResultDO<AnalyseDataDO> resultDO = analyseDataService.getById(id);
        return resultDO;
    }

    @ResponseBody
    @RequestMapping(value = "getList", method = RequestMethod.GET)
    @ApiOperation(value = "Get Analyse Data List", notes = "根据条件获取分析列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "page size", dataType = "int", required = false, defaultValue = "50"),
            @ApiImplicitParam(name = "currentPage", value = "current page", dataType = "int", required = false, defaultValue = "1"),
            @ApiImplicitParam(name = "overviewId", value = "overview id", dataType = "string", required = false),
            @ApiImplicitParam(name = "libraryId", value = "library id", dataType = "string", required = false),
            @ApiImplicitParam(name = "peptideRef", value = "peptide ref", dataType = "string", required = false),
            @ApiImplicitParam(name = "proteinName", value = "protein name", dataType = "string", required = false),
            @ApiImplicitParam(name = "isDecoy", value = "is decoy or target", dataType = "boolean", required = false),
            @ApiImplicitParam(name = "mzStart", value = "precursor mz start point", dataType = "float", required = false),
            @ApiImplicitParam(name = "mzEnd", value = "precursor mz end point", dataType = "float", required = false),
            @ApiImplicitParam(name = "fdrStart", value = "fdr start point", dataType = "double", required = false),
            @ApiImplicitParam(name = "fdrEnd", value = "fdr end point", dataType = "double", required = false),

    })
    public ResultDO<List<AnalyseDataDO>> getList(Model model,
                                                   @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                                                   @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize,
                                                   @RequestParam(value = "overviewId", required = false) String overviewId,
                                                   @RequestParam(value = "libraryId", required = false) String libraryId,
                                                   @RequestParam(value = "peptideRef", required = false) String peptideRef,
                                                   @RequestParam(value = "proteinName", required = false) String proteinName,
                                                   @RequestParam(value = "isDecoy", required = false) Boolean isDecoy,
                                                   @RequestParam(value = "mzStart", required = false) Float mzStart,
                                                   @RequestParam(value = "mzEnd", required = false) Float mzEnd,
                                                   @RequestParam(value = "fdrStart", required = false) Double fdrStart,
                                                   @RequestParam(value = "fdrEnd", required = false) Double fdrEnd
    ) {
        AnalyseDataQuery query = new AnalyseDataQuery();
        if (overviewId != null && !overviewId.isEmpty()) {
            query.setOverviewId(overviewId);
        }
        if (libraryId != null && !libraryId.isEmpty()) {
            query.setLibraryId(libraryId);
        }
        if (peptideRef != null && !peptideRef.isEmpty()) {
            query.setPeptideRef(peptideRef);
        }
        if (proteinName != null && !proteinName.isEmpty()) {
            query.setProteinName(proteinName);
        }
        if (isDecoy != null) {
            query.setIsDecoy(isDecoy);
        }
        if (mzStart != null) {
            query.setMzStart(mzStart);
        }
        if (mzEnd != null) {
            query.setMzEnd(mzEnd);
        }
        if (fdrStart != null) {
            query.setFdrStart(fdrStart);
        }
        if (fdrEnd != null) {
            query.setFdrEnd(fdrEnd);
        }

        buildPageQuery(query, currentPage, pageSize);
        ResultDO<List<AnalyseDataDO>> resultDO = analyseDataService.getList(query);

        return resultDO;
    }
}
