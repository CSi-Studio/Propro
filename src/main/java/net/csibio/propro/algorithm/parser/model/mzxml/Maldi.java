package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

import javax.xml.datatype.Duration;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-06 09:19
 */
@Data
public class Maldi {

    @XStreamAsAttribute
    String plateID;

    @XStreamAsAttribute
    String spotID;

    @XStreamAsAttribute
    Long laserShootCount;

    @XStreamAsAttribute
    Duration laserFrequency;

    @XStreamAsAttribute
    Long laserIntensity;

    @XStreamAsAttribute
    Boolean collisionGas;
}
