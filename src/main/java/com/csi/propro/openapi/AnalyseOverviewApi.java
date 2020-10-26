package com.csi.propro.openapi;

import com.csi.propro.controller.BaseController;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.db.AnalyseOverviewDO;
import com.csi.propro.service.AnalyseOverviewService;
import com.csi.propro.domain.query.AnalyseOverviewQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/analyse/overview")
@Api("OpenAPI 1.0-Beta for Propro")
public class AnalyseOverviewApi extends BaseController {

    @Autowired
    AnalyseOverviewService analyseOverviewService;


    @ApiOperation(value = "Get Analyse Overview by Id", notes = "根据ID获取分析总览对象")
    @RequestMapping(value = "getById", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "analyse overview id", dataType = "string", required = true)
    })
    public ResultDO<AnalyseOverviewDO> getById(Model model,
                                               @RequestParam(value = "id", required = true) String id) {
        ResultDO<AnalyseOverviewDO> resultDO = analyseOverviewService.getById(id);
        return resultDO;
    }

    @ResponseBody
    @RequestMapping(value = "getList", method = RequestMethod.GET)
    @ApiOperation(value = "Get Analyse Overview List", notes = "根据条件获取分析概览列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "expId", value = "experiment id", dataType = "string", required = false),
            @ApiImplicitParam(name = "libraryId", value = "library id", dataType = "string", required = false),
            @ApiImplicitParam(name = "pageSize", value = "page size", dataType = "int", required = false, defaultValue = "50"),
            @ApiImplicitParam(name = "currentPage", value = "current page", dataType = "int", required = false, defaultValue = "1")
    })
    public ResultDO<List<AnalyseOverviewDO>> getList(Model model,
                                                     @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                                                     @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize,
                                                          @RequestParam(value = "expId", required = false) String expId,
                                                          @RequestParam(value = "libraryId", required = false) String libraryId) {
        AnalyseOverviewQuery query = new AnalyseOverviewQuery();
        if (expId != null && !expId.isEmpty()) {
            query.setExpId(expId);
        }
        if (libraryId != null && !libraryId.isEmpty()) {
            query.setLibraryId(libraryId);
        }

        buildPageQuery(query, currentPage, pageSize);
        ResultDO<List<AnalyseOverviewDO>> resultDO = analyseOverviewService.getList(query);

        return resultDO;
    }
}
