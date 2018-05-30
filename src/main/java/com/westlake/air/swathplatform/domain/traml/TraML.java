package com.westlake.air.swathplatform.domain.traml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

import java.util.List;

@Data
@XStreamAlias("TraML")
public class TraML {

    /**
     * List of controlled vocabularies used in a TraML document
     */
    List<Cv> cvList;

    /**
     * List and descriptions of the source files this TraML document was generated or derived from
     */
    @XStreamAlias("SourceFileList")
    List<SourceFile> sourceFileList;

    /**
     * List of contacts referenced in the generation or validation of transitions
     */
    @XStreamAlias("ContactList")
    List<Contact> contactList;

    /**
     * List of publications from which the transitions were collected or wherein they are published
     */
    @XStreamAlias("PublicationList")
    List<Publication> publicationList;

    /**
     * List of instruments on which transitions are validated
     */
    @XStreamAlias("InstrumentList")
    List<Instrument> instrumentList;

    /**
     * List of software packages used in the generation of one of more transitions described in the document
     */
    @XStreamAlias("SoftwareList")
    List<Software> softwareList;

    /**
     * List of proteins for which one or more transitions are intended to identify
     */
    @XStreamAlias("ProteinList")
    List<Protein> proteinList;

    /**
     * List of compounds (including peptides) for which one or more transitions are intended to identify
     */
    @XStreamAlias("CompoundList")
    CompoundList compoundList;

    /**
     * List of transitions
     */
    @XStreamAlias("TransitionList")
    List<Transition> transitionList;

    @XStreamAlias("TargetList")
    TargetList targetList;

    /**
     * An optional id for the TraML document used for referencing from external files. It is recommended to use LSIDs when possible.
     * optional
     */
    @XStreamAsAttribute
    String id;

    /**
     * Version of the TraML format used by this document
     * required
     */
    @XStreamAsAttribute
    String version;
}
