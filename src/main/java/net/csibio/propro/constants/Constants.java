package net.csibio.propro.constants;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-27 21:05
 */
public class Constants {

    public static final double MIN_DOUBLE = Math.pow(10d, -6);
    public static final double SQRT_2PI = Math.sqrt(2 * Math.PI);
    //每批处理的数据
    public static final int MAX_PAGE_SIZE_FOR_FRAGMENT = 100000;
    public static final int DECOY_GENERATOR_TRY_TIMES = 10;
    public static final int MAX_UPDATE_RECORD_FOR_PEPTIDE = 100000;
    public static final int PRECISION = 1000000;
    public static final double SIDE_PEAK_DENSITY = 0.4d;
    public static final int TOP_N_INDEX = 10;
    public static final double ION_PERCENT = 0.4;//最优?

    //RT Normalizer
    public static final boolean CHECK_SPACINGS = false;
    public static final double SPACING_DIFFERENCE = 1.5d;
    public static final double SPACING_DIFFERENCE_GAP = 4d;
    public static final double SIGNAL_TO_NOISE_LIMIT = 1.0d;
    public static final int MISSING_LIMIT = 1;
    public static final double THRESHOLD = 0.000001d;
    public static final double AUTO_MAX_STDEV_FACTOR = 3.0d;
    public static final int MIN_REQUIRED_ELEMENTS = 10;
    public static final float XCORRE_SHAPE_THRESHOLD = 0.7f;

//    public static final double NOISE_FOR_EMPTY_WINDOW = Math.pow(10.0,20);
    public static final double NOISE_FOR_EMPTY_WINDOW = 2.0d;
    public static final double STOP_AFTER_INTENSITY_RATIO = 0.0001d;
    public static final double MIN_RSQ = 0.95d;
    public static final double MIN_COVERAGE = 0.6d;

    public static final String CHROMATOGRAM_PICKER_METHOD = "legacy";
//    public static final String CHROMATOGRAM_PICKER_METHOD = "corrected";

    public static final double PEAK_WIDTH = 5d;///
    public static final double MIN_INTENSITY_RATIO = 0.6d;
    public static final boolean ESTIMATE_BEST_PEPTIDES = false;
    public static final double INITIAL_QUALITY_CUTOFF = 0.5d;
    public static final double OVERALL_QUALITY_CUTOFF = 5.5d;
    public static final int RT_BINS = 10;
    public static final int MIN_PEPTIDES_PER_BIN = 1;
    public static final int MIN_BINS_FILLED = 8;

    //Extractor
    public static final String TRAFO_INVERT_MODEL = "LINEAR";
    public static final double DEFAULT_FDR = 0.01d;
    public static final double DEFAULT_RT_EXTRACTION_WINDOW = 600.0d;
    public static final float DEFAULT_MZ_EXTRACTION_WINDOW = 0.05f;

    public static final String DEFAULT_RT_EXTRACTION_WINDOW_STR = "600";
    public static final String DEFAULT_MZ_EXTRACTION_WINDOW_STR = "0.05";
    public static final String DEFAULT_SIGMA_STR = "3.75";
    public static final String DEFAULT_SPACING_STR = "0.01";
    public static final String DEFAULT_FDR_STR = "0.01";
    public static final String DEFAULT_SHAPE_SCORE_THRESHOLD_STR = "0.5";
    public static final String DEFAULT_SHAPE_WEIGHT_SCORE_THRESHOLD_STR = "0.6";

    public static final float DIA_EXTRACT_WINDOW = 0.05f;
    public static final int DIA_NR_ISOTOPES = 4;
    public static final int DIA_NR_CHARGES = 4;
    public static final float C13C12_MASSDIFF_U = 1.0033548f;
    public static final double PEAK_BEFORE_MONO_MAX_PPM_DIFF = 0.00002d;
    public static final double DIA_BYSERIES_PPM_DIFF = 10.0d;
    public static final double DIA_BYSERIES_INTENSITY_MIN = 300.0d;

    public static final double AVG_WEIGHT_C = 12.0107358985d;
    public static final double AVG_WEIGHT_H = 1.0079407537168315d;
    public static final double AVG_WEIGHT_N = 14.0067430888d;
    public static final double AVG_WEIGHT_O = 15.999405323160001d;
    public static final double AVG_WEIGHT_S = 32.066084735289d;
    public static final double AVG_WEIGHT_P = 30.97376149d;
    public static final double AVG_TOTAL = 111.12375721974328d;

    public static final double C = 4.9384d;
    public static final double H = 7.7583d;
    public static final double N = 1.3577d;
    public static final double O = 1.4773d;
    public static final double S = 0.0417d;
    public static final double P = 0d;
    public static final double PROTON_MASS_U = 1.007276466771d;
    public static final double Y_SIDE_MASS = 19.01784153d;
    public static final double B_SIDE_MASS = 1.007276466771d;
    public static final double ELEMENT_TOLERANCE = 0.5d;

    public static final double EMG_CONST = 2.4055;
    public static final int EMG_MAX_ITERATION = 500;

    //搜索邻近SWATH窗口的窗口数目,3代表向上向下各搜索3个窗口,总计6个
    public static final int SCANNING_SWATH_COLLECTED_NUMBER = 3;
    public static final String CHANGE_LINE = "\r\n";
    public static final String TAB = "\t";
    public static final String COMMA = ",";

    public static final String RESET_PASSWORD = "propro";

    public static final Integer LIBRARY_TYPE_STANDARD = 0;
    public static final Integer LIBRARY_TYPE_IRT = 1;
}
