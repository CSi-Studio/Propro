package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Description of any manipulation (from the first conversion to mzXML format until the creation of the current mzXML instance document)
 * applied to the data.
 */
@Data
public class DataProcessing {

    /**
     * Software used to convert the data. If data has been processed (e.g. profile > centroid)
     * by any additional progs these should be added too.
     */
    Software software;

    /**
     * Any additional manipulation not included elsewhere in the dataProcessing element.
     */
    @XStreamImplicit(itemFieldName = "processingOperation")
    List<NameValue> processingOperationList;

    /**
     * Additional comments
     */
    @XStreamImplicit(itemFieldName = "comment")
    List<String> commentList;

    /**
     * Intensity threshold for including a peak in the XML output
     */
    @XStreamAsAttribute
    Float intensityCutoff;

    /**
     * Is the data centroided
     */
    @XStreamAsAttribute
    Boolean centroided;

    /**
     * Is the data deisotoped
     */
    @XStreamAsAttribute
    protected Boolean deisotoped;

    /**
     * Has the charge state been deconvoluted
     */
    @XStreamAsAttribute
    protected Boolean chargeDeconvoluted;

    /**
     * Is the spot integration
     */
    @XStreamAsAttribute
    protected Boolean spotIntegration;
}
