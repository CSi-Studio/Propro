package net.csibio.propro.algorithm.parser.model.mzxml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import javax.xml.datatype.Duration;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-05 16:50
 */
@Data
@XStreamAlias("msRun")
public class MsRun {

    @XStreamImplicit(itemFieldName = "parentFile")
    List<ParentFile> parentFileList;

    @XStreamImplicit(itemFieldName = "msInstrument")
    List<MsInstrument> msInstrumentList;

    @XStreamImplicit(itemFieldName = "dataProcessing")
    List<DataProcessing> dataProcessingList;

    /**
     * Information about the separation technique, if any, used right before the acquisition.
     */
    Separation separation;

    /**
     * Acquisition independent properties of a MALDI experiment.
     */
    Spotting spotting;

    @XStreamImplicit(itemFieldName = "scan")
    List<Scan> scan;

    /**
     * sha-1 sum for this file (from the beginning of the file up to (and including) the opening tag of sha1
     */
    String sha1;

    @XStreamAsAttribute
    protected Long scanCount;

    @XStreamAsAttribute
    Duration startTime;

    @XStreamAsAttribute
    Duration endTime;
}
