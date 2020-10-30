package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Information about empirical mass spectrometer observations of the peptide
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
@XStreamAlias("Evidence")
public class Evidence {

    @XStreamImplicit(itemFieldName="cvParam")
    List<CvParam> cvParams;

    @XStreamImplicit(itemFieldName="userParam")
    List<UserParam> userParams;


}
