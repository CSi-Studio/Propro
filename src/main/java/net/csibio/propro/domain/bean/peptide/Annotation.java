package net.csibio.propro.domain.bean.peptide;

import lombok.Data;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-12 10:40
 * <p>
 * for example
 * b22-18^2/-0.02
 * type=b
 * location=22
 * adjust=-18
 * charge=2
 * deviation=-0.02
 */
@Data
public class Annotation {

    /**
     * a,b,c,x,y,z,
     * http://www.matrixscience.com/help/fragmentation_help.html
     */
    String type;

    /**
     * 切片位置
     */
    int location;

    /**
     * 误差
     */
    double deviation = 0.00;

    /**
     * 默认为1
     */
    int charge = 1;

    /**
     * 校准
     */
    int adjust = 0;

    boolean isIsotope = false;

    /**
     * 对应处理中括号的一种方式,当同一个肽段下有相同的离子但是不同的荷质比的时候,使用此字段进行标记
     */
    Boolean isBrotherIcon = false;

    public String toAnnoInfo() {
        if (isBrotherIcon) {
            return "[" + type + location + (adjust != 0 ? adjust : "") + (charge != 1 ? ("^" + charge) : "") + (isIsotope ? "i" : "") + "/" + deviation + "]";
        } else {
            return type + location + (adjust != 0 ? adjust : "") + (charge != 1 ? ("^" + charge) : "") + (isIsotope ? "i" : "") + "/" + deviation;
        }
    }

    public String toCutInfo() {
        return type + location + (charge == 1 ? "" : ("^" + charge))+(isBrotherIcon ? ("[" + (int)(deviation*100000) + "]") : "");
    }
}
