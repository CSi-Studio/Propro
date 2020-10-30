package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
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
@XStreamAlias("TargetList")
public class TargetList {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    /**
     * List of precursor m/z targets to include
     */
    @XStreamAlias("TargetIncludeList")
    List<Target> targetIncludeList;

    /**
     * List of precursor m/z targets to exclude
     */
    @XStreamAlias("TargetExcludeList")
    List<Target> targetExcludeList;
}
