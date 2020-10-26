package com.csi.propro.algorithm.aspect;

import com.csi.propro.constants.enums.TaskStatus;
import com.csi.propro.domain.db.TaskDO;
import com.csi.propro.domain.params.IrtParams;
import com.csi.propro.domain.params.WorkflowParams;
import com.csi.propro.service.TaskService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Aspect
@Component
public class TaskAspect {

    public final Logger logger = LoggerFactory.getLogger(TaskAspect.class);

    @Autowired
    TaskService taskService;

    @Around("execution(* com.csi.propro.async.task.ExperimentTask.irt(..))")
    public Object irtAround(ProceedingJoinPoint joinPoint) {
        Object result = null;
        Object[] args = joinPoint.getArgs();
        TaskDO taskDO = (TaskDO) args[0];
        ArrayList exps = (ArrayList) args[1];
        IrtParams irtParams = (IrtParams) args[2];
        taskDO.start();
        if (exps != null && irtParams.getLibrary() != null) {
            taskService.update(taskDO, TaskStatus.RUNNING.getName(), "开始分析IRT校准库并且计算iRT值,总计" + exps.size() + "个目标实验,Library ID:" + irtParams.getLibrary().getId() + ";Type:" + irtParams.getLibrary().getType());
        }

        try {
            result = joinPoint.proceed(args);
        } catch (Throwable e) {
            e.printStackTrace();
            taskService.finish(taskDO, TaskStatus.FAILED.getName(), "Error:" + e.getMessage());
        }

        taskService.finish(taskDO, TaskStatus.SUCCESS.getName(), "SUCCESS");

        return result;
    }

    @Around("execution(* com.csi.propro.async.task.ExperimentTask.extract(..))")
    public Object extractAround(ProceedingJoinPoint joinPoint) {
        Object result = null;
        Object[] args = joinPoint.getArgs();
        TaskDO taskDO = (TaskDO) args[0];
        WorkflowParams workflowParams = (WorkflowParams) args[1];
        taskDO.start();
        taskDO.setStatus(TaskStatus.RUNNING.getName());
        taskDO.addLog("数据提取窗口:" + workflowParams.getExtractParams().toString())
                .addLog("Sigma:" + workflowParams.getSigmaSpacing().getSigma() + ",Spacing:" + workflowParams.getSigmaSpacing().getSpacing())
                .addLog("使用标准库ID:" + workflowParams.getLibrary().getId())
                .addLog("FDR:" + workflowParams.getFdr())
                .addLog("Note:" + workflowParams.getNote())
                .addLog("使用限制阈值Shape/ShapeWeight:" + workflowParams.getXcorrShapeThreshold() + "/" + workflowParams.getXcorrShapeWeightThreshold());
        taskService.update(taskDO, TaskStatus.RUNNING.getName(), "");

        try {
            result = joinPoint.proceed(args);
        } catch (Throwable e) {
            e.printStackTrace();
            taskService.update(taskDO, TaskStatus.FAILED.getName(), "Error:" + e.getMessage());
        }

        taskService.finish(taskDO, TaskStatus.SUCCESS.getName(),"SUCCESS");
        return result;
    }

}
