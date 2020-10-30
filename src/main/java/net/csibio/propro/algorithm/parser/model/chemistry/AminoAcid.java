package net.csibio.propro.algorithm.parser.model.chemistry;

import lombok.Data;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-11 23:53
 */
@Data
public class AminoAcid {

    //氨基酸全称
    String name;

    //氨基酸缩写,通常为三个字母
    String shortName;

    //氨基酸单字母简写
    String oneLetterCode;

    //氨基酸化学方程式
    String formula;

    //修饰基团的基团ModId
    String modId;

    //氨基酸在自然界中的分布(计算了每一种原子在自然界的同位素分布的权重后所得)
    double monoIsotopicMass;

    //氨基酸在自然界中的平均分布
    double averageMass;
}
