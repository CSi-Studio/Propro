package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

/**
 * Uncontrolled user parameters (essentially allowing free text).
 * Before using these, one should verify whether there is an
 * appropriate CV term available,and if so, use the CV term instead
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
public class UserParam {

    /**
     * The name for the parameter.
     * required
     */
    @XStreamAsAttribute
    String name;

    /**
     * The datatype of the parameter, where appropriate (e.g.: xsd:float).
     * optional
     */
    @XStreamAsAttribute
    String type;

    /**
     * The value for the parameter, where appropriate.
     * optional
     */
    @XStreamAsAttribute
    String value;

    /**
     * An optional CV accession number for the unit term associated with the value
     * if any (e.g., 'UO:0000266' for 'electron volt').
     * optional
     */
    @XStreamAsAttribute
    String unitAccession;

    /**
     * An optional CV name for the unit accession number
     * if any (e.g., 'electron volt' for 'UO:0000266' ).
     * optional
     */
    @XStreamAsAttribute
    String unitName;

    /**
     * If a unit term is referenced, this attribute must refer
     * to the CV 'id' attribute defined in the cvList in this mzML file.
     * optional
     */
    @XStreamAsAttribute
    String unitCvRef;
}
