package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-06 09:49
 */
@Data
@XStreamAlias("mzXML")
public class MzXML {

    MsRun msRun;

    Long indexOffset;
}
