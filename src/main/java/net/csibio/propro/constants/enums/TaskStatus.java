package net.csibio.propro.constants.enums;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-15 09:42
 */
public enum TaskStatus {

    UNKNOWN("UNKNOWN"),

    WAITING("WAITING"),

    RUNNING("RUNNING"),

    SUCCESS("SUCCESS"),

    FAILED("FAILED"),


    ;

    String name;


    TaskStatus(String name) {
        this.name = name;
    }

    public static TaskStatus getByName(String name) {
        for (TaskStatus status : values()) {
            if (status.getName().equals(name)) {
                return status;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
