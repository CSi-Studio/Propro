package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

@Data
public class Spotting {

    /**
     * Information about a MALDI plate.
     */
    @XStreamImplicit(itemFieldName = "plate")
    List<Plate> plate;

    Robot robot;

}
