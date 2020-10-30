package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import lombok.Data;

/**
 *
 * Path to all the ancestor files (up to the native acquisition file) used to generate the current XML instance document.
 *
 * Created by James Lu MiaoShan
 * Time: 2018-07-05 16:51
 */
@Data
public class ParentFile {

    @XStreamAsAttribute
    String fileName;

    @XStreamAsAttribute
    String fileType;

    @XStreamAsAttribute
    String fileSha1;

}
