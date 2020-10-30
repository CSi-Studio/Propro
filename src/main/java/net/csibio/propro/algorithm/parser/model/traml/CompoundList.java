package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * List of compounds (including peptides) for which one or more transitions are intended to identify
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("CompoundList")
public class CompoundList {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;

    @XStreamImplicit(itemFieldName="Peptide")
    List<Peptide> peptideList;

    @XStreamImplicit(itemFieldName="Compound")
    List<Compound> compoundList;
}
