package net.csibio.propro.algorithm.parser.xml;

import net.csibio.propro.algorithm.parser.model.mzxml.Peaks;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.codec.binary.Base64;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-06 09:11
 */
public class PeaksConverter implements Converter {

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Peaks peaks = (Peaks) source;
        if (peaks != null) {
            writer.addAttribute("precision", String.valueOf(peaks.getPrecision()));
            writer.addAttribute("byteOrder", peaks.getByteOrder());
            writer.addAttribute("contentType", peaks.getContentType());
            writer.addAttribute("compressionType", peaks.getCompressionType());
            writer.addAttribute("compressedLen", String.valueOf(peaks.getCompressedLen()));
            writer.setValue(new Base64().encodeToString(peaks.getValue()));
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Peaks peaks = new Peaks();
        String precision = reader.getAttribute("precision");
        String byteOrder = reader.getAttribute("byteOrder");
        String contentType = reader.getAttribute("contentType");
        String compressionType = reader.getAttribute("compressionType");
        String compressedLen = reader.getAttribute("compressedLen");
        if(precision != null){
            peaks.setPrecision(Integer.valueOf(precision));
        }
        peaks.setByteOrder(byteOrder);
        peaks.setContentType(contentType);
        peaks.setCompressionType(compressionType);
        if(compressedLen != null){
            peaks.setCompressedLen(Integer.valueOf(compressedLen));
        }

        byte[] value = new Base64().decode(reader.getValue());
        peaks.setValue(value);

        return peaks;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(Peaks.class);
    }
}
