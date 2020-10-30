package net.csibio.propro.domain.bean.learner;

import lombok.Data;

@Data
public class ScoreData {

    /**
     * groupId represents which peak group the row data belongs to.
     * like decoy_peptideRef
     */
    String[] groupId;

    /**
     * 对应groupId的integer版的groupId
     */
    Integer[] groupNumId;

    @Deprecated
    String[] scoreColumns;

    @Deprecated
    Integer[] runId;

    /**
     * isDecoy == False , row value is from target peptide.
     * isDecoy == True, row value is from decoy peptide.
     */
    Boolean[] isDecoy;

    /**
     * scores
     */
    Double[][] scoreData ;

}
