package com.csi.propro.openapi;

import com.csi.propro.controller.BaseController;
import com.csi.propro.domain.db.ExperimentDO;
import com.csi.propro.domain.db.ProjectDO;
import com.csi.propro.service.ExperimentService;
import com.csi.propro.service.ProjectService;
import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.query.ExperimentQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/experiment")
@Api("OpenAPI 1.0-Beta for Propro")
public class ExperimentApi extends BaseController {

    @Autowired
    ExperimentService experimentService;
    @Autowired
    ProjectService projectService;

    @ApiOperation(value = "Get Experiment by Id", notes = "根据ID获取实验对象")
    @RequestMapping(value = "getById", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "experiment id", dataType = "string", required = true)
    })
    public ResultDO<ExperimentDO> getById(Model model,
                                          @RequestParam(value = "id", required = true) String id) {
        ResultDO<ExperimentDO> resultDO = experimentService.getById(id);
        return resultDO;
    }

    @ResponseBody
    @RequestMapping(value = "getList", method = RequestMethod.GET)
    @ApiOperation(value = "Get Experiment List", notes = "根据条件获取实验列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "expName", value = "experiment name", dataType = "string", required = false),
            @ApiImplicitParam(name = "projectName", value = "experiment project name", dataType = "string", required = false),
            @ApiImplicitParam(name = "pageSize", value = "page size", dataType = "int", required = false, defaultValue = "50"),
            @ApiImplicitParam(name = "currentPage", value = "current page", dataType = "int", required = false, defaultValue = "1")
    })
    public ResultDO<List<ExperimentDO>> getList(Model model,
                                                          @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                                                          @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize,
                                                          @RequestParam(value = "projectName", required = false) String projectName,
                                                          @RequestParam(value = "expName", required = false) String expName) {
        ExperimentQuery query = new ExperimentQuery();
        if (expName != null && !expName.isEmpty()) {
            query.setName(expName);
        }
        if (projectName != null && !projectName.isEmpty()) {
            ProjectDO project = projectService.getByName(projectName);
            if(project == null){
                return ResultDO.buildError(ResultCode.PROJECT_NOT_EXISTED);
            }
            query.setProjectId(project.getId());
        }
        buildPageQuery(query, currentPage, pageSize);
        ResultDO<List<ExperimentDO>> resultDO = experimentService.getList(query);

        return resultDO;
    }
}
