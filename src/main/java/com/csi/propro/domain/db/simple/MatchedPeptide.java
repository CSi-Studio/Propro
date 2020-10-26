package com.csi.propro.domain.db.simple;

import lombok.Data;

@Data
public class MatchedPeptide {

    String id;
    //蛋白质名称
    String proteinName;

    //肽段名称_带电量,例如:SLMLSYN(UniMod:7)AITHLPAGIFR_3
    String peptideRef;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof MatchedPeptide) {
            MatchedPeptide mp = (MatchedPeptide) obj;
            if (this.peptideRef == null || mp.getPeptideRef() == null) {
                return false;
            }

            return (this.peptideRef.equals(mp.getPeptideRef()));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.getPeptideRef().hashCode();
    }
}
