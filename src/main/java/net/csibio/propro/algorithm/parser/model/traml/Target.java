package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * A peptide or compound that is to be included or excluded from a target list of precursor m/z values.
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("Target")
public class Target {

    @XStreamAlias("Precursor")
    Precursor precursor;

    @XStreamAlias("RetentionTime")
    RetentionTime retentionTime;

    @XStreamAlias("ConfigurationList")
    List<Configuration> configurationList;

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * String label for this target
     * required
     */
    @XStreamAsAttribute
    String id;

    /**
     * Reference to a peptide for which this target is the trigger
     * optinal
     */
    @XStreamAsAttribute
    String peptideRef;

    /**
     * Reference to a compound for which this target is the trigger
     * optinal
     */
    @XStreamAsAttribute
    String compoundRef;
}
