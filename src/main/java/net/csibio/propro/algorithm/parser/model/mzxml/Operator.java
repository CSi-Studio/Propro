package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

/**
 * Contact information for the person responsible for the instrument.
 */

@Data
public class Operator {

    /**
     * First name
     */
    @XStreamAsAttribute
    String first;

    /**
     * Last name
     */
    @XStreamAsAttribute
    String last;

    /**
     * Phone number
     */
    @XStreamAsAttribute
    String phone;

    /**
     * email address
     */
    @XStreamAsAttribute
    String email;

    /**
     * Uniform Resource Identifier
     */
    @XStreamAlias("URI")
    @XStreamAsAttribute
    String uri;
}
