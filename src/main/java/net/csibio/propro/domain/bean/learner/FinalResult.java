package net.csibio.propro.domain.bean.learner;

import lombok.Data;

import java.util.HashMap;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-06-19 13:24
 */

@Data
public class FinalResult {

    HashMap<String, Double> weightsMap;

    ErrorStat finalErrorTable;

    ErrorStat summaryErrorTable;

    ErrorStat allInfo;

    Integer matchedPeptideCount;

    Integer matchedProteinCount;

    String errorInfo;
}
