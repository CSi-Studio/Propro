package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * List and descriptions of the source files this TraML document was generated or derived from
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("SourceFile")
public class SourceFile {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * Identifier for the sourceFile to be used for referencing within a document.
     * required
     */
    @XStreamAsAttribute
    String id;

    /**
     * Name of the source file, without reference to location (either URI or local path).
     * required
     */
    @XStreamAsAttribute
    String name;

    /**
     * URI-formatted location where the file was retrieved.
     * required
     */
    @XStreamAsAttribute
    String location;
}
