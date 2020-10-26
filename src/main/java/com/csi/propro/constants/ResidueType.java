package com.csi.propro.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-12 16:19
 */
public class ResidueType {

    public static final String Full  = "p";
    public static final String NTerm = "NTerm";
    public static final String CTerm = "CTerm";
    public static final String AIon  = "a";
    public static final String BIon  = "b";
    public static final String CIon  = "c";
    public static final String XIon  = "x";
    public static final String YIon  = "y";
    public static final String ZIon  = "z";

    public static final List<String> abcxyz = new ArrayList<>();

    static{
        abcxyz.add(AIon);
        abcxyz.add(BIon);
        abcxyz.add(CIon);
        abcxyz.add(XIon);
        abcxyz.add(YIon);
        abcxyz.add(ZIon);
    }

}
