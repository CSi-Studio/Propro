package net.csibio.propro.async.task;

import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.db.TaskDO;
import net.csibio.propro.service.LibraryService;
import net.csibio.propro.constants.enums.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-17 10:40
 */
@Component("libraryTask")
public class LibraryTask extends BaseTask{

    @Autowired
    LibraryService libraryService;

    @Async(value = "uploadFileExecutor")
    public void saveLibraryTask(LibraryDO library, InputStream libFileStream, String fileName, InputStream prmFileStream, TaskDO taskDO) {
        taskDO.start();
        taskDO.setStatus(TaskStatus.RUNNING.getName());
        taskService.update(taskDO);
        libraryService.uploadFile(library, libFileStream, fileName, prmFileStream, taskDO);
    }
}
