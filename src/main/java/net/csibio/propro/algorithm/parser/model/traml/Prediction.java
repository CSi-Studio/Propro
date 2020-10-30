package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Information about a prediction for a suitable transition using some software
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("Prediction")
public class Prediction {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * Reference to a software package from which this prediction is derived
     * required
     */
    @XStreamAsAttribute
    String softwareRef;

    /**
     * Reference to a contact person that generated this prediction
     * optional
     */
    @XStreamAsAttribute
    String contactRef;
}
