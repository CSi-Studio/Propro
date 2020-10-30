package net.csibio.propro.utils;

import net.csibio.propro.domain.db.PeptideDO;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nico Wang
 * Time: 2019-07-04 16:24
 */
public class PeptideUtil {


    public static final Pattern unimodPattern = Pattern.compile("([a-z])[\\(]unimod[\\:](\\d*)[\\)]");

    public static String removeUnimod(String fullName){
        if (fullName.contains("(")){
            String[] parts = fullName.replaceAll("\\(","|(").replaceAll("\\)","|").split("\\|");
            String sequence = "";
            for(String part: parts){
                if (part.startsWith("(")){
                    continue;
                }
                sequence += part;
            }
            return sequence;
        }else {
            return fullName;
        }
    }


    /**
     * 解析出Modification的位置
     *
     * @param peptideDO
     */
    public static void parseModification(PeptideDO peptideDO) {
        //不论是真肽段还是伪肽段,fullUniModPeptideName字段都是真肽段的完整版
        String peptide = peptideDO.getFullName();
        peptide = peptide.toLowerCase();
        HashMap<Integer, String> unimodMap = new HashMap<>();

        while (peptide.contains("(unimod:") && peptide.indexOf("(unimod:") != 0) {
            Matcher matcher = unimodPattern.matcher(peptide);
            if (matcher.find()) {
                unimodMap.put(matcher.start(), matcher.group(2));
                peptide = StringUtils.replaceOnce(peptide, matcher.group(0), matcher.group(1));
            }
        }
//        if (unimodMap.size() > 0) {
            peptideDO.setUnimodMap(unimodMap);
//        }
    }

    /**
     * 解析出Modification的位置
     *
     * @param fullName
     */
    public static HashMap<Integer, String> parseModification(String fullName) {
        //不论是真肽段还是伪肽段,fullUniModPeptideName字段都是真肽段的完整版

        fullName = fullName.toLowerCase();
        HashMap<Integer, String> unimodMap = new HashMap<>();

        while (fullName.contains("(unimod:") && fullName.indexOf("(unimod:") != 0) {
            Matcher matcher = unimodPattern.matcher(fullName);
            if (matcher.find()) {
                unimodMap.put(matcher.start(), matcher.group(2));
                fullName = StringUtils.replaceOnce(fullName, matcher.group(0), matcher.group(1));
            }
        }
        return unimodMap;
    }

}
