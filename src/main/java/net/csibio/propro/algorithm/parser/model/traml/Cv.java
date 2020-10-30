package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

/**
 * Controlled vocabulary used in a TraML document
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("cv")
public class Cv {

    /**
     * Version of controlled vocabulary in use when the document was created
     * required
     */
    @XStreamAsAttribute
    String id;

    /**
     * Uniform Resource Identifier for the controlled vocabulary
     * required
     */
    @XStreamAsAttribute
    String version;

    /**
     * Full name of the controlled vocabulary
     * required
     */
    @XStreamAlias("URI")
    @XStreamAsAttribute
    String uri;

    /**
     * Identifier for the controlled vocabulary to be used for referencing within a document
     * required
     */
    @XStreamAsAttribute
    String fullName;

}
