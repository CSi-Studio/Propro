package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * A molecule modification specification. If n modifications are present on the peptide,
 * there should be n instances of the modification element. If multiple modifications are provided as cvParams,
 * it is assumed the modification is ambiguous, i.e. one modification or the other.
 * If no cvParams are provided it is assumed that the delta has not been matched to a known modification.
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("Modification")
public class Modification {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * Location of the modification within the peptide sequence, counted from the N-terminus,
     * starting at position 1. Specific modifications to the N-terminus should be given the location 0.
     * Modification to the C-terminus should be given as peptide length + 1.
     * required
     */
    @XStreamAsAttribute
    int location;

    /**
     * Atomic mass delta when assuming only the most common isotope of elements in Daltons.
     * optional
     */
    @XStreamAsAttribute
    double monoisotopicMassDelta;

    /**
     * Atomic mass delta when considering the natural distribution of isotopes in Daltons.
     * optional
     */
    @XStreamAsAttribute
    double averageMassDelta;
}
