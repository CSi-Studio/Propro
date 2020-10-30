package net.csibio.propro.algorithm.parser.model.chemistry;

import lombok.Data;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-09 15:21
 */
@Data
public class Element {

    public static String H = "H";
    public static String S = "S";
    public static String C = "C";
    public static String N = "N";
    public static String O = "O";
    public static String P = "P";

    String name;

    String symbol;

    Integer atomicNumber;

    double monoWeight;

    double averageWeight;

    double maxAbundanceWeight;

    List<String> isotopes;
}
