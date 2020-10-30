package net.csibio.propro.algorithm.parser.model.chemistry;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-11 18:55
 */
@Data
public class Residue {

    String name;
    String shortName;
    String threeLetterCode;
    String oneLetterCode;
    String formula;
    Double pka;
    Double pkb;
    Double pkc;
    Double gbSC;
    Double gbBbL;
    Double gbBbR;
    String residueSets;
    String synonyms;
    String losses;
    String nTermLosses;
    List<String> residueList;
    List<String> synonymList;
    HashMap<String,String> lossMap;
    HashMap<String,String> nTermLossMap;

}
