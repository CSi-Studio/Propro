package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

@Data
public class Spot {

    @XStreamAlias("maldiMatrix")
    OntologyEntry maldiMatrix;

    @XStreamAsAttribute
    String spotID;

    @XStreamAsAttribute
    String spotXPosition;

    @XStreamAsAttribute
    String spotYPosition;

    @XStreamAsAttribute
    Long spotDiameter;
}
