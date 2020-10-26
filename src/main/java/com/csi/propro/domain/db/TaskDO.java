package com.csi.propro.domain.db;

import com.csi.propro.domain.bean.task.MachineInfo;
import com.csi.propro.domain.bean.task.TaskLog;
import com.csi.propro.constants.enums.TaskStatus;
import com.csi.propro.constants.enums.TaskTemplate;
import com.csi.propro.domain.BaseDO;
import lombok.Data;
import org.apache.shiro.SecurityUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-13 21:34
 */
@Data
@Document(collection = "task")
public class TaskDO extends BaseDO {

    @Id
    String id;

    String name;

    String creator;

    String status;

    String taskTemplate;

    Date createDate;

    Date lastModifiedDate;

    MachineInfo machineInfo;

    List<TaskLog> logs;

    Long totalCost;

    String features;

    public TaskDO() {
    }

    public TaskDO(TaskTemplate taskTemplate, String taskSuffixName) {
        this.creator = ((UserDO)SecurityUtils.getSubject().getPrincipal()).getUsername();
        this.taskTemplate = taskTemplate.getName();
        this.status = TaskStatus.WAITING.getName();
        this.name = taskTemplate.getName() + "-" + taskSuffixName;
    }

    public TaskDO addLog(String content) {
        if (logs == null) {
            if (status == null || taskTemplate == null) {
                this.taskTemplate = TaskTemplate.DEFAULT.getName();
                this.status = TaskStatus.WAITING.getName();
                this.name = TaskTemplate.DEFAULT.getName() + "-DEFAULT";
            }
            logs = new ArrayList<>();
            logs.add(new TaskLog("Task Started"));
        }
        TaskLog taskLog = new TaskLog(content);

        logs.add(taskLog);

        return this;
    }

    public TaskDO addLog(List<String> contents) {
        if (logs == null) {
            logs = new ArrayList<>();
            logs.add(new TaskLog("Task Started"));
        }
        for (String content : contents) {
            TaskLog taskLog = new TaskLog(content);
            logs.add(taskLog);
        }

        return this;
    }

    public TaskDO start() {
        if (logs == null || logs.size() == 0) {
            logs = new ArrayList<>();
            logs.add(new TaskLog("Task Started"));
        }

        return this;
    }

    public Long getStartTime() {
        if (logs == null || logs.size() == 0) {
            return null;
        }
        TaskLog taskLog = logs.get(0);
        return taskLog.getTime().getTime();
    }

    public TaskDO finish(String status) {
        addLog("Task Ended");
        this.status = status;
        totalCost = System.currentTimeMillis() - getStartTime();

        return this;
    }

    public TaskDO finish(String status, String finishLog) {
        addLog(finishLog);
        addLog("Task Ended");
        this.status = status;
        totalCost = System.currentTimeMillis() - getStartTime();

        return this;
    }
}
