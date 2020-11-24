package net.csibio.propro.controller;

import net.csibio.propro.constants.enums.TaskStatus;
import net.csibio.propro.dao.ConfigDAO;
import net.csibio.propro.domain.query.LibraryQuery;
import net.csibio.propro.domain.query.ProjectQuery;
import net.csibio.propro.domain.query.TaskQuery;
import net.csibio.propro.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Controller
@RequestMapping("/")
public class HomeController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    LibraryService libraryService;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    TaskService taskService;
    @Autowired
    ProjectService projectService;
    @Autowired
    ConfigDAO configDAO;

    @RequestMapping("/")
    String home(Model model) {
        ProjectQuery projectQuery = new ProjectQuery();
        long projectCount = projectService.count(projectQuery);
        TaskQuery query = new TaskQuery();
        query.setStatus(TaskStatus.RUNNING.getName());
        long taskRunningCount = taskService.count(query);
        LibraryQuery libraryQuery = new LibraryQuery();
        long libCount = libraryService.count(libraryQuery);
        model.addAttribute("taskRunningCount", taskRunningCount);
        model.addAttribute("libCount", libCount);
        model.addAttribute("projectCount", projectCount);

        return "home";
    }
}
