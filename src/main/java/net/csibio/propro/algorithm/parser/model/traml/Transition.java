package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Information about a single transition for a peptide or other compound
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("Transition")
public class Transition {

    @XStreamAlias("Precursor")
    Precursor precursor;

    @XStreamImplicit(itemFieldName = "IntermediateProduct")
    List<IntermediateProduct> intermediateProductList;

    @XStreamAlias("Product")
    Product product;

    @XStreamAlias("RetentionTime")
    RetentionTime retentionTime;

    @XStreamAlias("Prediction")
    Prediction prediction;

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * Reference to a peptide which this transition is intended to identify
     * optional
     */
    @XStreamAsAttribute
    String peptideRef;

    /**
     * Reference to a compound for this transition
     * optional
     */
    @XStreamAsAttribute
    String compoundRef;

    /**
     * String label for this transition
     * required
     */
    @XStreamAsAttribute
    String id;

}
