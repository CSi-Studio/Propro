package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
public class Pattern {

    @XStreamAlias("spottingPattern")
    OntologyEntry spottingPattern;


    Orientation orientation;
}
