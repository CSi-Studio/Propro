package com.csi.propro.algorithm.parser;

import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.domain.ResultDO;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by Nico Wang Ruimin
 * Time: 2019-02-01 16:18
 * <p>
 * Fasta文件的存储格式为（一行Protein信息，跟着多行该protein的完整序列）
 */
@Component("fastaParser")
public class FastaParser {

    //TODO: params
    int minPepLen = 0;
    int maxPepLen = 100;

    public ResultDO<HashSet<String>> getUniquePeptide(InputStream in){
        ResultDO<HashMap<String, HashSet<String>>> protMapResultDO = parse(in);
        if(protMapResultDO.isFailed()){
            ResultDO<HashSet<String>> resultDO = new ResultDO(false);
            resultDO.setMsgInfo(protMapResultDO.getMsgInfo());
            return resultDO;
        }
        HashSet<String> uniquePeptides = new HashSet<>();
        HashSet<String> allPeptides = new HashSet<>();
        for (HashSet<String> peptideSet: protMapResultDO.getModel().values()){
            for (String peptide: peptideSet){
                if (allPeptides.contains(peptide) && uniquePeptides.contains(peptide)){
                    //若之前出现过，且在Unique中，移除
                    uniquePeptides.remove(peptide);
                }else {
                    //若之前没出现过，暂且放在Unique中
                    uniquePeptides.add(peptide);
                }
                allPeptides.add(peptide);
            }
        }
        return new ResultDO<HashSet<String>>(true).setModel(uniquePeptides);
    }

    private ResultDO<HashMap<String, HashSet<String>>> parse(InputStream in) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line = reader.readLine();
            String lastProteinMessage = line;
            //设置初始容量为128，避免重新分配空间造成的性能损失
            StringBuilder lastSequence = new StringBuilder(128);
            HashMap<String, HashSet<String>> proteinPeptideMap = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(">")) {
                    //进入下一个protein的分析
                    //此时lastSequence里面存有了当前protein的sequence
                    if (!line.startsWith(">CON")) {
                        HashSet<String> enzymedSequence = getEnzymeResult(lastSequence.toString());
                        proteinPeptideMap.put(lastProteinMessage, enzymedSequence);
                    }
                    //分离为peptide之后，存储新protein的信息，并清空lastSequence
                    lastProteinMessage = line;
                    lastSequence.setLength(0);

                } else {
                    lastSequence.append(line);
                }
            }
            HashSet<String> enzymedSequence = getEnzymeResult(lastSequence.toString());
            proteinPeptideMap.put(lastProteinMessage, enzymedSequence);
            return new ResultDO<HashMap<String, HashSet<String>>>(true).setModel(proteinPeptideMap);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultDO.buildError(ResultCode.PRM_FILE_FORMAT_NOT_SUPPORTED);        }
    }

    /**
     * 将K，R作为切分sequence的标志物
     * @param proteinSequence
     * @return
     */
    private HashSet<String> getEnzymeResult(String proteinSequence) {
        String[] result = proteinSequence.replaceAll("K", "K|").replaceAll("R", "R|").split("\\|");
        HashSet<String> peptideSet = new HashSet<>();
        for(String peptide: result){
            if(peptide.length() >= minPepLen && peptide.length() <= maxPepLen){
                peptideSet.add(peptide);
            }
        }
        return peptideSet;
    }

}
