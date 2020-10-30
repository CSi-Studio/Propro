package net.csibio.propro.openapi;

import net.csibio.propro.controller.BaseController;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.ProjectDO;
import net.csibio.propro.service.ProjectService;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.domain.query.ProjectQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/project")
@Api("OpenAPI 1.0-Beta for Propro")
public class ProjectApi extends BaseController {

    @Autowired
    ProjectService projectService;

    @ResponseBody
    @RequestMapping(value = "getById", method = RequestMethod.GET)
    @ApiOperation(value = "Get Project by Id", notes = "根据ID获取项目对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "project id", dataType = "string", required = true)
    })
    public ResultDO<ProjectDO> getById(Model model,
                                       @RequestParam(value = "id", required = true) String id) {
        ProjectDO library = projectService.getById(id);
        if (library == null){
            return ResultDO.buildError(ResultCode.PROJECT_NOT_EXISTED);
        }
        ResultDO<ProjectDO> resultDO = new ResultDO<>(true);
        resultDO.setModel(library);
        return resultDO;
    }

    @ResponseBody
    @RequestMapping(value = "getList", method = RequestMethod.GET)
    @ApiOperation(value = "Get Project List", notes = "根据条件获取项目列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "project name", dataType = "string", required = false),
            @ApiImplicitParam(name = "pageSize", value = "page size", dataType = "int", required = false, defaultValue = "50"),
            @ApiImplicitParam(name = "currentPage", value = "current page", dataType = "int", required = false, defaultValue = "1")
    })
    public ResultDO<List<ProjectDO>> getList(Model model,
                                                    @RequestParam(value = "name", required = false) String name,
                                                    @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                                                    @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize) {
        ProjectQuery query = new ProjectQuery();
        if (name != null) {
            query.setName(name);
        }

        buildPageQuery(query, currentPage, pageSize);
        return projectService.getList(query);
    }
}
