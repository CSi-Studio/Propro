package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Chemical compound other than a peptide for which one or more transitions
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
public class Compound {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * List of retention time information entries
     */
    @XStreamAlias("RetentionTimeList")
    List<RetentionTime> retentionTimeList;
    /**
     * Identifier for the compound to be used for referencing within a document
     */
    @XStreamAsAttribute
    String id;
}
