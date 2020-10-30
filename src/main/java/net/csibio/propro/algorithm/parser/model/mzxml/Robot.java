package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

import javax.xml.datatype.Duration;

@Data
public class Robot {

    @XStreamAlias("robotManufacturer")
    OntologyEntry robotManufacturer;

    @XStreamAlias("robotModel")
    OntologyEntry robotModel;

    @XStreamAsAttribute
    Duration timePerSpot;

    @XStreamAsAttribute
    Long deadVolume;
}
