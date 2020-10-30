package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Contact person referenced in the generation or validation of transitions
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("Contact")
public class Contact {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * Identifier for the contact to be used for referencing within a document
     * required
     */
    @XStreamAsAttribute
    String id;

}
