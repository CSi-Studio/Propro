package com.csi.propro.dao;

import com.csi.propro.algorithm.parser.model.chemistry.Unimod;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-20 09:51
 */
@Component
public class UnimodDAO {

    public final Logger logger = LoggerFactory.getLogger(UnimodDAO.class);

    public HashMap<String, Unimod> unimodMap = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {

        InputStream stream = getClass().getClassLoader().getResourceAsStream("dbfile/unimod.obo");
        File file = new File("dbfile/unimod.obo");
        FileUtils.copyInputStreamToFile(stream, file);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;

            //如果检测到[Term]这个标签就保存上一组的Unimod对象
            Unimod unimod = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();

                if (line.equals("[term]")) {
                    if (unimod != null) {
                        unimodMap.put(unimod.getId(), unimod);
                    }
                    unimod = new Unimod();
                }

                if (line.startsWith("id:")) {
                    unimod.setId(line.replace("id:", "").trim().replace("unimod:", "").trim());
                }

                if (line.startsWith("name:")) {
                    unimod.setName(line.replace("name:", "").trim());
                }

                if (line.contains("delta_mono_mass")) {
                    unimod.setMonoMass(Double.parseDouble(line.replace("property_value:", "").trim().replace("delta_mono_mass=", "").trim().replace("\"", "")));
                }

                if (line.contains("delta_avge_mass")) {
                    unimod.setAverageMass(Double.parseDouble(line.replace("property_value:", "").trim().replace("delta_avge_mass=", "").trim().replace("\"", "")));
                }
            }

            if (unimod != null) {
                unimodMap.put(unimod.getId(), unimod);
            }
            reader.close();
            logger.info("Init Unimod Database file success!!!");

        } catch (IOException e) {
            logger.info("Init Unimod Database file failed!!!");
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public Unimod getUnimod(String unimodId) {
        return unimodMap.get(unimodId);
    }
}
