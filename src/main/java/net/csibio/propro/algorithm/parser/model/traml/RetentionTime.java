package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Information about predicted or calibrated retention time
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("RetentionTime")
public class RetentionTime {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * Software used to determine the retention time
     * optional
     */
    @XStreamAsAttribute
    String softwareRef;
}
