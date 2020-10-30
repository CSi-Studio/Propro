package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * General information about the MS instrument.
 */
@Data
public class MsInstrument {

    OntologyEntry msManufacturer;

    OntologyEntry msModel;

    OntologyEntry msIonisation;

    OntologyEntry msMassAnalyzer;

    OntologyEntry msDetector;

    OntologyEntry msResolution;

    Software software;

    Operator operator;

    @XStreamImplicit(itemFieldName="nameValue")
    List<NameValue> nameValueList;

    @XStreamImplicit(itemFieldName="comment")
    List<String> commentList;

}
