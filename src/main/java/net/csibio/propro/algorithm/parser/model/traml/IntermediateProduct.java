package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Intermediate product ion information of the transition when using MS3 or above
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("IntermediateProduct")
public class IntermediateProduct {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * List of possible interprations of fragment ions for a transition
     */
    @XStreamAlias("InterpretationList")
    List<Interpretation> interpretationList;

    /**
     * List of insutrument configurations used in the validation or optimization of the transitions
     */
    @XStreamAlias("ConfigurationList")
    List<Configuration> configurationList;
}
