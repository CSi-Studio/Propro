package net.csibio.propro.algorithm.parser.xml;

import net.csibio.propro.algorithm.parser.model.mzxml.PrecursorMz;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-06 11:07
 */
public class PrecursorMzConverter implements Converter {
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        PrecursorMz precursorMz = (PrecursorMz) source;
        if (precursorMz != null) {
            writer.addAttribute("precursorScanNum", String.valueOf(precursorMz.getPrecursorScanNum()));
            writer.addAttribute("precursorIntensity", String.valueOf(precursorMz.getPrecursorIntensity()));
            writer.addAttribute("precursorCharge", String.valueOf(precursorMz.getPrecursorCharge()));
            writer.addAttribute("possibleCharges", precursorMz.getPossibleCharges());
            writer.addAttribute("windowWideness", String.valueOf(precursorMz.getWindowWideness()));
            writer.addAttribute("activationMethod", precursorMz.getActivationMethod());
            writer.setValue(String.valueOf(precursorMz.getValue()));
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        PrecursorMz precursorMz = new PrecursorMz();
        String precursorScanNum = reader.getAttribute("precursorScanNum");
        String precursorIntensity = reader.getAttribute("precursorIntensity");
        String precursorCharge = reader.getAttribute("precursorCharge");
        String possibleCharges = reader.getAttribute("possibleCharges");
        String windowWideness = reader.getAttribute("windowWideness");
        String activationMethod = reader.getAttribute("activationMethod");
        if (precursorScanNum != null) {
            precursorMz.setPrecursorScanNum(Long.valueOf(precursorScanNum));
        }
        if (precursorIntensity != null) {
            precursorMz.setPrecursorIntensity(Float.valueOf(precursorIntensity));
        }
        if (precursorCharge != null) {
            precursorMz.setPrecursorCharge(Integer.valueOf(precursorCharge));
        }
        precursorMz.setPossibleCharges(possibleCharges);
        if (windowWideness != null) {
            precursorMz.setWindowWideness(Float.valueOf(windowWideness));
        }
        precursorMz.setActivationMethod(activationMethod);
        String value = reader.getValue();
        if (value != null && !value.isEmpty()) {
            precursorMz.setValue(Float.valueOf(value));
        }

        return precursorMz;
    }

    @Override
    public boolean canConvert(Class type) {
        return type.equals(PrecursorMz.class);
    }
}
