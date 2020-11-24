package net.csibio.propro.controller;

import net.csibio.propro.async.task.ExperimentTask;
import net.csibio.propro.async.task.LibraryTask;
import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.query.PageQuery;
import net.csibio.propro.service.ExperimentService;
import net.csibio.propro.service.LibraryService;
import net.csibio.propro.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
public class BaseController {

    @Autowired
    LibraryService libraryService;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    TaskService taskService;
    @Autowired
    LibraryTask libraryTask;
    @Autowired
    ExperimentTask experimentTask;

    public final Logger logger = LoggerFactory.getLogger(getClass());
    public static String ERROR_MSG = "error_msg";
    public static String SUCCESS_MSG = "success_msg";

    //0:标准库,1:irt校准库
    public List<LibraryDO> getLibraryList(Integer type) {
        return libraryService.getSimpleAll(type);
    }

    public void buildPageQuery(PageQuery query, Integer currentPage, Integer pageSize) {
        if (currentPage != null) {
            query.setPageNo(currentPage);
        }
        if (pageSize != null) {
            query.setPageSize(pageSize);
        }
    }
}
