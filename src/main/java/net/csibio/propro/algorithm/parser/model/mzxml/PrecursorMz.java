package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import net.csibio.propro.algorithm.parser.xml.PrecursorMzConverter;
import lombok.Data;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-06 09:18
 */
@Data
@XStreamConverter(PrecursorMzConverter.class)
public class PrecursorMz {

    Float value;

    @XStreamAsAttribute
    Long precursorScanNum;

    @XStreamAsAttribute
    Float precursorIntensity;

    @XStreamAsAttribute
    Integer precursorCharge;

    @XStreamAsAttribute
    String possibleCharges;

    @XStreamAsAttribute
    Float windowWideness;

    @XStreamAsAttribute
    String activationMethod;
}
