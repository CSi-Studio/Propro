package net.csibio.propro.controller;

import com.alibaba.fastjson.JSON;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.TaskDO;
import net.csibio.propro.domain.query.TaskQuery;
import net.csibio.propro.service.TaskService;
import net.csibio.propro.utils.PermissionUtil;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.constants.enums.TaskStatus;
import net.csibio.propro.constants.enums.TaskTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-13 21:33
 */
@Controller
@RequestMapping("task")
public class TaskController extends BaseController {

    @Autowired
    TaskService taskService;

    @RequestMapping(value = "/list")
    String list(Model model,
                @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize,
                @RequestParam(value = "taskTemplate", required = false) String taskTemplate,
                @RequestParam(value = "taskStatus", required = false) String taskStatus) {

        if(taskTemplate != null){
            model.addAttribute("taskTemplate", taskTemplate);
        }
        model.addAttribute("taskTemplates", TaskTemplate.values());

        if(taskStatus != null){
            model.addAttribute("taskStatus", taskStatus);
        }
        model.addAttribute("statusList", TaskStatus.values());

        model.addAttribute("pageSize", pageSize);
        TaskQuery query = new TaskQuery();
        if(taskTemplate != null && !taskTemplate.isEmpty() && !taskTemplate.equals("All")){
            query.setTaskTemplate(taskTemplate);
        }
        if(taskStatus != null && !taskStatus.isEmpty() && !taskStatus.equals("All")){
            query.setStatus(taskStatus);
        }
        if(!isAdmin()){
            query.setCreator(getCurrentUsername());
        }
        query.setPageSize(pageSize);
        query.setPageNo(currentPage);
        query.setSortColumn("createDate");
        query.setOrderBy(Sort.Direction.DESC);
        ResultDO<List<TaskDO>> resultDO = taskService.getList(query);

        model.addAttribute("tasks", resultDO.getModel());
        model.addAttribute("totalPage", resultDO.getTotalPage());
        model.addAttribute("currentPage", currentPage);
        return "task/list";
    }

    @RequestMapping(value = "/detail/{id}")
    String detail(Model model, @PathVariable("id") String id) {
        ResultDO<TaskDO> resultDO = taskService.getById(id);
        if (resultDO.isFailed()) {
            model.addAttribute(ERROR_MSG, ResultCode.OBJECT_NOT_EXISTED.getMessage());
            return "task/detail";
        }
        PermissionUtil.check(resultDO.getModel());

        TaskDO taskDO = resultDO.getModel();
        model.addAttribute("task", taskDO);
        model.addAttribute("taskId", taskDO.getId());
        TaskTemplate taskTemplate = TaskTemplate.getByName(taskDO.getTaskTemplate());
        if (taskTemplate == null) {
            model.addAttribute(ERROR_MSG, ResultCode.OBJECT_NOT_EXISTED.getMessage());
            return "task/detail";
        }
        return "task/detail";
    }

    @RequestMapping(value = "/getTaskInfo/{id}")
    @ResponseBody
    String getTaskInfo(Model model, @PathVariable("id") String id) {
        ResultDO<TaskDO> resultDO = taskService.getById(id);
        if (resultDO.isSuccess() && resultDO.getModel() != null) {
            PermissionUtil.check(resultDO.getModel());
            return JSON.toJSONString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(resultDO.getModel().getLastModifiedDate()));
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/delete/{id}")
    String delete(Model model, @PathVariable("id") String id) {
        ResultDO<TaskDO> resultDO = taskService.getById(id);
        PermissionUtil.check(resultDO.getModel());
        taskService.delete(id);
        return "redirect:/task/list";
    }
}
