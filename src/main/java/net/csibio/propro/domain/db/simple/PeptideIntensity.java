package net.csibio.propro.domain.db.simple;

import lombok.Data;

@Data
public class PeptideIntensity {

    String proteinName;

    String peptideRef;

    Double intensitySum;
}
