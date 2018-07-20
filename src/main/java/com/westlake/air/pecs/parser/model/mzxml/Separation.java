package com.westlake.air.pecs.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

@Data
public class Separation {

    @XStreamImplicit(itemFieldName = "separationTechnique")
    List<SeparationTechnique> separationTechniqueList;
}
