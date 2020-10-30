package net.csibio.propro.constants.enums;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-15 09:42
 */
public enum TaskTemplate {

    /**
     * default template
     */
    DEFAULT("DEFAULT"),

    /**
     * upload experiment file
     */
    SCAN_AND_UPDATE_EXPERIMENTS("SCAN_AND_UPDATE_EXPERIMENTS"),

    /**
     * upload library file(including standard library and irt library)
     */
    UPLOAD_LIBRARY_FILE("UPLOAD_LIBRARY_FILE"),

    /**
     * extract for mzxml with standard library
     */
    EXTRACTOR("EXTRACTOR"),

    IRT_EXTRACTOR("IRT_EXTRACTOR"),

    EXTRACT_PEAKPICK_SCORE("EXTRACT_PEAKPICK_SCORE"),

    IRT_EXTRACT_PEAKPICK_SCORE("IRT_EXTRACT_PEAKPICK_SCORE"),

    /**
     * compute irt for slope and intercept
     */
    IRT("IRT"),

    /**
     * compute sub scores
     */
    SCORE("SCORE"),

    /**
     * compute sub scores
     */
    AIRUS("AIRUS"),

    /**
     * the whole workflow for swath including(irt -> extractor -> sub scores -> final scoreForAll)
     */
    SWATH_WORKFLOW("SWATH_WORKFLOW"),

    /**
     * compress the mzxml and sort the mzxml scan for swath
     */
    COMPRESSOR_AND_SORT("COMPRESSOR_AND_SORT"),

    /**
     * export subscores tsv file for pyprophet
     */
    EXPORT_SUBSCORES_TSV_FILE_FOR_PYPROPHET("EXPORT_SUBSCORES_TSV_FILE_FOR_PYPROPHET"),

    /**
     * generate all the scoreForAll types' distribute map
     */
    BUILD_SCORE_DISTRIBUTE("BUILD_SCORE_DISTRIBUTE"),

    ;

    String name;

    TaskTemplate(String templateName) {
        this.name = templateName;
    }

    public static TaskTemplate getByName(String name) {
        for (TaskTemplate template : values()) {
            if (template.getName().equals(name)) {
                return template;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
