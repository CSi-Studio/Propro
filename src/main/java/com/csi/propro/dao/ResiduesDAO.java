package com.csi.propro.dao;

import com.alibaba.fastjson.JSONObject;
import com.csi.propro.algorithm.parser.model.chemistry.Residue;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-11 09:40
 */
@Service
public class ResiduesDAO {

    public final Logger logger = LoggerFactory.getLogger(ResiduesDAO.class);

    String residuesStr = "";
    List<Residue> residueList = new ArrayList<>();
    HashMap<String, Residue> codeResidueMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("dbfile/residues.json");
            File file = new File("dbfile/residues.json");
            FileUtils.copyInputStreamToFile(stream, file);
            FileInputStream fis = new FileInputStream(file);
            int fileLength = fis.available();
            byte[] bytes = new byte[fileLength];
            fis.read(bytes);
            residuesStr = new String(bytes, 0, fileLength);
            residueList = JSONObject.parseArray(residuesStr, Residue.class);
            for (Residue residue : residueList) {
                parseFeatures(residue);
                codeResidueMap.put(residue.getOneLetterCode(), residue);
            }
            logger.info("Init Residues Database file success!!!");
        } catch (IOException e) {
            logger.info("Init Residues Database file failed!!!");
            e.printStackTrace();
        }
    }

    public Residue getResiduByCode(String oneLetterCode) {
        return getCodeResidueMap().get(oneLetterCode);
    }

    public HashMap<String, Residue> getCodeResidueMap() {
        return codeResidueMap;
    }

    public List<Residue> getResidueList() {
        return residueList;
    }

    public String getJson() {
        return residuesStr;
    }

    /**
     * 解析其中的residueSets,synonyms,losses,nTermLosses
     * @param residue
     */
    private void parseFeatures(Residue residue) {

    }
}
