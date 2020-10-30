package net.csibio.propro.algorithm.parser.model.mzxml;

import lombok.Data;

/**
 * Software identifier
 */

@Data
public class Software {

    /**
     * acquisition: acquisition software running the MS (instrument section)
     * conversion: RAW data to XML converter utility (dataProcessing section)
     * processing: any form of data processing software not included by the two previous definitions (dataProcessing section)
     */
    String type;

    /**
     * Name of the acquisition software
     */
    String name;

    /**
     * Version of the acquisition software. Legal patterns are n.m(a or b)
     */
    String version;

    /**
     * Time of completion of the dataProcessing operation
     */
    String completionTime;
}
