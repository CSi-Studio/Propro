package net.csibio.propro.domain.bean.task;

import lombok.Data;

import java.util.Date;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-16 13:53
 */
@Data
public class TaskLog {

    String content;

    Date time;

    public TaskLog(String content){
        this.content = content;
        this.time = new Date();
    }
}
