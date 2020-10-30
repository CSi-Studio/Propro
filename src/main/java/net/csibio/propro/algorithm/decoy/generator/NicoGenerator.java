package net.csibio.propro.algorithm.decoy.generator;

import net.csibio.propro.constants.ResidueType;
import net.csibio.propro.domain.bean.peptide.Annotation;
import net.csibio.propro.domain.db.FragmentInfo;
import net.csibio.propro.domain.db.PeptideDO;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.csibio.propro.algorithm.decoy.BaseGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nico Wang
 * Time: 2019-07-16 09:51
 */
@Component("nicoGenerator")
public class NicoGenerator extends BaseGenerator {
    public final Logger logger = LoggerFactory.getLogger(NicoGenerator.class);

    public static final String NAME = "nico";

    @Override
    public void generate(PeptideDO peptideDO) {

        String sequence = peptideDO.getSequence();
        HashMap<Integer, String> unimodMap = peptideDO.getUnimodMap();

        //最后一位是K,R时保持最后一位氨基酸位置不变
        char lastAcidChar = sequence.toUpperCase().charAt(sequence.length() - 1);

        //encode
        BiMap<String, String> unimodEncodeMap = HashBiMap.create();
        if (unimodMap != null && !unimodMap.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < sequence.length(); i++){
                String aminoAcid = sequence.substring(i, i + 1);
                if (unimodMap.containsKey(i)){
                    String unimodCode = aminoAcid + unimodMap.get(i);
                    if (unimodEncodeMap.containsKey(unimodCode)){
                        builder.append(unimodEncodeMap.get(unimodCode));
                    }else {
                        unimodEncodeMap.put(unimodCode, String.valueOf(unimodEncodeMap.size()));
                        builder.append(unimodEncodeMap.size() - 1);
                    }
                }else {
                    builder.append(aminoAcid);
                }
            }
            sequence = builder.toString();
        }

        String convertedSequence;
        if (lastAcidChar == 'K' || lastAcidChar == 'P' || lastAcidChar == 'R') {
            convertedSequence = singleConvert(sequence.substring(0, sequence.length() - 1)) + sequence.substring(sequence.length() - 1);
        }else {
            convertedSequence = singleConvert(sequence);
        }

        //decode
        HashMap<Integer, String> newUnimodMap = new HashMap<>();
        if (unimodMap != null && !unimodMap.isEmpty()) {
            BiMap<String, String> unimodDecodeMap = unimodEncodeMap.inverse();
            StringBuilder builder = new StringBuilder();
            char[] seqToDecode = convertedSequence.toCharArray();
            for (int i = 0; i < seqToDecode.length; i++){
                if (seqToDecode[i] <= '9'){
                    String unimodCode = unimodDecodeMap.get(String.valueOf(seqToDecode[i]));
                    newUnimodMap.put(i, unimodCode.substring(1).toLowerCase()
                            .replace("(unimod:","")
                            .replace(")",""));
                    builder.append(unimodCode, 0, 1);
                }else {
                    builder.append(seqToDecode[i]);
                }
            }
            convertedSequence = builder.toString();
        }


        for (String cutInfo : peptideDO.getFragmentMap().keySet()) {
            FragmentInfo targetFi = peptideDO.getFragmentMap().get(cutInfo);
            FragmentInfo decoyFi = new FragmentInfo();
            decoyFi.setCutInfo(cutInfo);
            decoyFi.setIntensity(targetFi.getIntensity());
            decoyFi.setCharge(targetFi.getCharge());
            decoyFi.setAnnotation(targetFi.getAnnotation());
            decoyFi.setAnnotations(targetFi.getAnnotation().toAnnoInfo());
            Annotation oneAnno = targetFi.getAnnotation();

            List<String> unimodIds = new ArrayList<>();
            String subSequence = "";
            if (oneAnno.getType().equals(ResidueType.AIon) || oneAnno.getType().equals(ResidueType.BIon) || oneAnno.getType().equals(ResidueType.CIon)) {
                subSequence = convertedSequence.substring(0, oneAnno.getLocation());
                for (int i = 0; i < oneAnno.getLocation(); i++) {
                    if (newUnimodMap.containsKey(i)) {
                        unimodIds.add(newUnimodMap.get(i));
                    }
                }
            } else if (oneAnno.getType().equals(ResidueType.XIon) || oneAnno.getType().equals(ResidueType.YIon) || oneAnno.getType().equals(ResidueType.ZIon)){
                subSequence = convertedSequence.substring(convertedSequence.length() - oneAnno.getLocation());
                for (int i = convertedSequence.length() - 1; i >= convertedSequence.length() - oneAnno.getLocation(); i--) {
                    if (newUnimodMap.containsKey(i)) {
                        unimodIds.add(newUnimodMap.get(i));
                    }
                }
            }

            double productMz = formulaCalculator.getMonoMz(
                    subSequence,
                    oneAnno.getType(),
                    oneAnno.getCharge(),
                    oneAnno.getAdjust(),
                    oneAnno.getDeviation(),
                    oneAnno.isIsotope(),
                    unimodIds
            );
            decoyFi.setMz(productMz);
            peptideDO.putDecoyFragment(cutInfo, decoyFi);
        }

        peptideDO.setDecoySequence(convertedSequence);
        peptideDO.setDecoyUnimodMap(newUnimodMap);
    }

    private String singleConvert(String rawSequence){
        char[] oldCharSeq = rawSequence.toCharArray();

        Character[] newCharSeq = new Character[rawSequence.length()];

        //get index map of raw
        HashMap<Character, List<Integer>> indexMap = new HashMap<>();
        for (int index = 0; index < oldCharSeq.length; index ++){
            char tempChar = oldCharSeq[index];
            List<Integer> takenPositions = indexMap.get(tempChar);
            if (takenPositions == null){
                takenPositions = new ArrayList<>();
            }
            takenPositions.add(index);
            indexMap.put(tempChar, takenPositions);
        }

        //get free map
        HashMap<Character,List<Integer>> freeMap = new HashMap<>();

        for (Map.Entry<Character,List<Integer>> takenEntry: indexMap.entrySet()){
            List<Integer> freeIndex = new ArrayList<>();
            List<Integer> takenIndex = takenEntry.getValue();
            for (int i=0; i<rawSequence.length(); i++){
                if (!takenIndex.contains(i)){
                    freeIndex.add(i);
                }
            }
            freeMap.put(takenEntry.getKey(), freeIndex);
        }

        //get wait count map
        HashMap<Character,Integer> waitMap = new HashMap<>();
        for (Map.Entry<Character,List<Integer>> entry: indexMap.entrySet()){
            waitMap.put(entry.getKey(), entry.getValue().size());
        }

        while (getFreeCount(freeMap) != 0) {
            char crowdestChar = getCrowdestChar(freeMap);

            List<Integer> positionList = getPositionList(freeMap, waitMap, crowdestChar, -1);
            for (int index : positionList) {
                newCharSeq[index] = crowdestChar;
            }
            updateMap(freeMap, waitMap, positionList, crowdestChar);
        }

        setRemainSeq(waitMap, newCharSeq);

        StringBuilder converted = new StringBuilder();
        for (char elem: newCharSeq){
            converted.append(elem);
        }

        return converted.toString();
    }

    private char getCrowdestChar(HashMap<Character,List<Integer>> freeMap){
        char selectedChar = '!';
        int minSpace = Integer.MAX_VALUE;
        for (Map.Entry<Character,List<Integer>> entry: freeMap.entrySet()){
            int currentSize = entry.getValue().size();
            if (currentSize == 0){
                continue;
            }
            if (minSpace > currentSize){
                minSpace = currentSize;
                selectedChar = entry.getKey();
            }
        }
        return selectedChar;
    }

    private List<Integer> getPositionList(HashMap<Character,List<Integer>> freeMap, HashMap<Character,Integer> waitMap, char selectedChar, int size){
        List<Integer> freeList = freeMap.get(selectedChar);
        int waitCount = waitMap.get(selectedChar);
        List<Integer> positionList = new ArrayList<>();
        if (size == -1){
            size = Math.min(waitCount, freeList.size());
        }
        for (int i=0; i<size; i++){
            int index = (int) (System.nanoTime() % freeList.size());
            positionList.add(freeList.get(index));
        }
        return positionList;
    }

    private void updateMap(HashMap<Character,List<Integer>> freeMap, HashMap<Character,Integer> waitMap, List<Integer> selectedPositions, char selectedChar){
        for (char tempChar: freeMap.keySet()){
            List<Integer> freePositions = freeMap.get(tempChar);
            if (freePositions.isEmpty()){
                continue;
            }
            int matchCount = 0;
            //update free map
            for (int i=freePositions.size()-1; i>=0; i--){
                if (selectedPositions.contains(freePositions.get(i))){
                    freePositions.remove(i);
                    matchCount ++;
                }
            }
            freeMap.put(tempChar, freePositions);

            //update wait map
            if (tempChar == selectedChar){
                int waitCount = waitMap.get(tempChar);
                waitCount = waitCount - matchCount;
                waitMap.put(tempChar, waitCount);
                if (waitCount == 0) {
                    freeMap.put(tempChar, new ArrayList<>());
                }
            }

        }
    }

    private void setRemainSeq(HashMap<Character,Integer> waitMap, Character[] sequence){
        //get null index
        List<Integer> nullIndexList = new ArrayList<>();
        for (int i=0; i<sequence.length; i++){
            if (sequence[i] == null){
                nullIndexList.add(i);
            }
        }
        int index = 0;
        for (Map.Entry<Character,Integer> entry: waitMap.entrySet()){
            for (int i=0; i<entry.getValue(); i++){
                sequence[nullIndexList.get(index)] = entry.getKey();
                index ++;
            }
        }
    }

    private int getFreeCount(HashMap<Character,List<Integer>> freeMap){
        int count = 0;
        for (List<Integer> free: freeMap.values()){
            count += free.size();
        }
        return count;
    }
}
