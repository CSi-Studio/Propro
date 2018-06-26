package com.westlake.air.swathplatform.parser;

import com.westlake.air.swathplatform.parser.model.chemistry.Unimod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-20 09:51
 */
@Component
public class UnimodParser {

    public final Logger logger = LoggerFactory.getLogger(UnimodParser.class);

    public static HashMap<String, Unimod> unimodMap = new HashMap<>();

    @PostConstruct
    public void parse() {
        String filepath = getClass().getClassLoader().getResource("dbfile/unimod.obo").getPath();
        File file = new File(filepath);

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
        } catch (IOException e) {
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