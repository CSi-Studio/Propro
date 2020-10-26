package com.csi.propro.domain.db.simple;

import lombok.Data;

@Data
public class PeptideIntensity {

    String proteinName;

    String peptideRef;

    Double intensitySum;
}
