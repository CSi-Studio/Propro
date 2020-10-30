package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Peptide for which one or more transitions are intended to identify
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("Peptide")
public class Peptide {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    @XStreamImplicit(itemFieldName = "ProteinRef")
    List<ProteinRef> proteinRefList;

    @XStreamImplicit(itemFieldName = "Modification")
    List<Modification> modificationList;

    /**
     * List of retention time information entries
     */
    @XStreamAlias("RetentionTimeList")
    List<RetentionTime> retentionTimeList;

    /**
     * Information about empirical mass spectrometer observations of the peptide
     */
    @XStreamAlias("Evidence")
    Evidence evidence;
    /**
     * Identifier for the contact to be used for referencing within a document
     * required
     */
    @XStreamAsAttribute
    String id;

    /**
     * Amino acid sequence of the peptide being described
     * required
     */
    @XStreamAsAttribute
    String sequence;
}
