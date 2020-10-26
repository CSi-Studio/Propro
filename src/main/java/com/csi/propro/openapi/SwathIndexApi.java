package com.csi.propro.openapi;

import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.controller.BaseController;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.db.SwathIndexDO;
import com.csi.propro.domain.query.SwathIndexQuery;
import com.csi.propro.service.SwathIndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/swathIndex")
@Api("OpenAPI 1.0-Beta for Propro")
public class SwathIndexApi extends BaseController {

    @Autowired
    SwathIndexService swathIndexService;

    @ResponseBody
    @RequestMapping(value = "getById", method = RequestMethod.GET)
    @ApiOperation(value = "Get SwathIndex by Id", notes = "根据ID获取索引对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "swathIndex id", dataType = "string", required = true)
    })
    public ResultDO<SwathIndexDO> getScanIndexList(Model model,
                                                   @RequestParam(value = "id", required = true) String id) {
        SwathIndexDO swathIndexDO = swathIndexService.getById(id);
        if(swathIndexDO == null){
            return ResultDO.buildError(ResultCode.SWATH_INDEX_NOT_EXISTED);
        }
        ResultDO<SwathIndexDO> resultDO = new ResultDO<SwathIndexDO>(true);
        return resultDO;
    }

    @ResponseBody
    @RequestMapping(value = "getList", method = RequestMethod.GET)
    @ApiOperation(value = "Get SwathIndex List", notes = "根据条件获取索引列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "expId", value = "experiment id", dataType = "string", required = true),
            @ApiImplicitParam(name = "level", value = "swathIndex ms level", dataType = "int", required = true),
            @ApiImplicitParam(name = "pageSize", value = "page size", dataType = "int", required = false, defaultValue = "50"),
            @ApiImplicitParam(name = "currentPage", value = "current page", dataType = "int", required = false, defaultValue = "1")
    })
    public ResultDO<List<SwathIndexDO>> getSwathIndexList(Model model,
                                                        @RequestParam(value = "expId", required = true) String expId,
                                                        @RequestParam(value = "level", required = true) Integer level,
                                                        @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                                                        @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize) {
        SwathIndexQuery query = new SwathIndexQuery();
        query.setLevel(level);
        query.setExpId(expId);
        buildPageQuery(query, currentPage, pageSize);
        ResultDO<List<SwathIndexDO>> resultDO = swathIndexService.getList(query);

        return resultDO;
    }
}
