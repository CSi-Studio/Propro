package net.csibio.propro.constants.enums;

/**
 * Created by Nico Wang
 * Time: 2019-04-23 17:07
 */
public enum Unimod {

    Oxidation("Oxidation", "(UniMod:35)", "(ox)", new String[]{"[+15.994915]"}),

    Deamidation("Deamidation", "(UniMod:7)", "(de)", new String[]{"[+0.984016]"})
    ;

    String fullName;
    String serialName;
    String msmsAbbr;
    String[] mzList;

    Unimod(){

    }

    Unimod(String fullName, String serialName, String msmsAbbr, String[] mzList) {
        this.fullName = fullName;
        this.serialName = serialName;
        this.msmsAbbr = msmsAbbr;
        this.mzList = mzList;
    }

    public String getFullName(){ return this.fullName; }

    public String getSerialName(){ return this.serialName; }

    public String getMsmsAbbr(){ return this.msmsAbbr; }

    public String[] getMzList(){ return this.mzList; }

}
