package com.csi.propro.utils;

import com.csi.propro.algorithm.parser.model.chemistry.AminoAcid;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-27 14:41
 */
public class TransitionUtil {

    /**
     * TODO 没有解决首末位是Modification的情况
     *
     * @param acidList
     * @return
     */
    public static String toSequence(List<AminoAcid> acidList, boolean withUnimod) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < acidList.size(); i++) {
            AminoAcid aa = acidList.get(i);
            sb.append(aa.getName());

            if (withUnimod && aa.getModId() != null && !aa.getModId().isEmpty()) {
                sb.append("(UniMod:").append(aa.getModId()).append(")");
            }
        }

        return sb.toString();
    }

}
