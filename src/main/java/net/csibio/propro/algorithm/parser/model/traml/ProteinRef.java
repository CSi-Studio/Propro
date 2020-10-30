package net.csibio.propro.algorithm.parser.model.traml;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

/**
 * Reference to a protein which this peptide is intended to identify
 *
 * Created by James Lu MiaoShan
 * Time: 2018-05-31 09:53
 */
@Data
public class ProteinRef {

    /**
     * Reference to a protein which this peptide is intended to identify
     * optional
     */
    @XStreamAsAttribute
    String ref;



}
