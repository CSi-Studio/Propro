package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

/**
 * Controlled vocabulary term adding information to the parent term
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
public class CvParam {

    /**
     * Reference to a controlled vocabulary for which this cvParam is
     * required
     */
    @XStreamAsAttribute
    String cvRef;

    /**
     * Name of the controlled vocabulary term referenced
     * required
     */
    @XStreamAsAttribute
    String name;

    /**
     * Accession number of the controlled vocabulary term referenced
     * required
     */
    @XStreamAsAttribute
    String accession;

    /**
     * Scalar value qualifying the controlled vocabulary term referenced
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
