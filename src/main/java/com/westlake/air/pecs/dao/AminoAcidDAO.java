package com.westlake.air.pecs.dao;

import com.alibaba.fastjson.JSONObject;
import com.westlake.air.pecs.parser.model.chemistry.AminoAcid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-11 09:40
 */
@Service
public class AminoAcidDAO {

    public final Logger logger = LoggerFactory.getLogger(AminoAcidDAO.class);

    String aminoAcidsStr = "";
    List<AminoAcid> aminoAcidList = new ArrayList<>();
    HashMap<String, AminoAcid> codeAminoAcidMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            File file = new File(getClass().getClassLoader().getResource("dbfile/AminoAcidData.json").getPath());
            FileInputStream fis = new FileInputStream(file);
            int fileLength = fis.available();
            byte[] bytes = new byte[fileLength];
            fis.read(bytes);
            aminoAcidsStr = new String(bytes, 0, fileLength);
            aminoAcidList = JSONObject.parseArray(aminoAcidsStr, AminoAcid.class);
            for (AminoAcid aminoAcid : aminoAcidList) {
                codeAminoAcidMap.put(aminoAcid.getOneLetterCode(), aminoAcid);
            }
            logger.info("Init AminoAcid Database file success");
        } catch (Exception e) {
            logger.error("Init AminoAcid Database file failed!!!", e);
            e.printStackTrace();
        }
    }

    public AminoAcid getAminoAcidByCode(String oneLetterCode) {
        return getCodeAminoAcidMap().get(oneLetterCode);
    }

    public HashMap<String, AminoAcid> getCodeAminoAcidMap() {
        return codeAminoAcidMap;
    }

    public List<AminoAcid> getAminoAcidList() {
        return aminoAcidList;
    }

    public String getJson() {
        return aminoAcidsStr;
    }
}