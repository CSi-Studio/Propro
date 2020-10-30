package net.csibio.propro.async.task;

import net.csibio.propro.service.AnalyseDataService;
import net.csibio.propro.service.ScoreService;
import net.csibio.propro.service.SwathIndexService;
import net.csibio.propro.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-29 20:28
 */
public class BaseTask {

    public final Logger logger = LoggerFactory.getLogger(BaseTask.class);

    @Autowired
    TaskService taskService;
    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    ScoreService scoreService;
    @Autowired
    SwathIndexService swathIndexService;

}
