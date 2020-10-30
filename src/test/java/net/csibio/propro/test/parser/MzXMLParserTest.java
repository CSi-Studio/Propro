package net.csibio.propro.test.parser;

import net.csibio.propro.test.BaseTest;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;

public class MzXMLParserTest extends BaseTest {

    ArrayList<String> filePaths = new ArrayList<>();

    public void init() {
        logger.info("Start init params");
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_001_Water.mzXML"));
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_002_Water.mzXML"));
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_003_Water.mzXML"));
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_004_Water.mzXML"));
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_005_Water.mzXML"));
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_006_Water.mzXML"));
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_007_Water.mzXML"));
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_008_Water.mzXML"));
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_009_Water.mzXML"));
        filePaths.add(FilenameUtils.normalize("D:\\data\\SGS\\mzxml\\napedro_L120224_010_Water.mzXML"));
    }

}
