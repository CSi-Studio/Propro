package net.csibio.propro.algorithm.formula;

import net.csibio.propro.constants.Constants;
import net.csibio.propro.constants.ResidueType;
import net.csibio.propro.dao.AminoAcidDAO;
import net.csibio.propro.dao.ElementsDAO;
import net.csibio.propro.domain.bean.peptide.Annotation;
import net.csibio.propro.domain.bean.peptide.Fragment;
import net.csibio.propro.domain.bean.score.BYSeries;
import net.csibio.propro.domain.db.FragmentInfo;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.service.PeptideService;
import net.csibio.propro.algorithm.parser.model.chemistry.AminoAcid;
import net.csibio.propro.algorithm.parser.model.chemistry.Unimod;
import net.csibio.propro.dao.UnimodDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-12 10:32
 */
@Component("fragmentFactory")
public class FragmentFactory {

    public final Logger logger = LoggerFactory.getLogger(FragmentFactory.class);

    @Autowired
    PeptideService peptideService;
    @Autowired
    FormulaCalculator formulaCalculator;
    @Autowired
    UnimodDAO unimodDAO;
    @Autowired
    AminoAcidDAO aminoAcidDAO;
    @Autowired
    ElementsDAO elementsDAO;

    //根据UnimodMap,肽段的序列以及带电量获取该肽段所有B,Y类型的排列组合的离子MZ列表
    public BYSeries getBYSeries(HashMap<Integer, String> unimodHashMap, String sequence, int charge) {

        BYSeries bySeries = new BYSeries();

        //bSeries 若要提高精度，提高json的精度
        List<Double> bSeries = new ArrayList<>();
        double monoWeight = Constants.PROTON_MASS_U * charge;

        char[] acidCodeArray = sequence.toCharArray();
        for (int i = 0; i < acidCodeArray.length - 1; i++) {
            AminoAcid aa = aminoAcidDAO.getAminoAcidByCode(String.valueOf(acidCodeArray[i]));
            if (aa == null) {
                continue;
            }
            if (unimodHashMap != null && unimodHashMap.containsKey(i)) {
                Unimod unimod = unimodDAO.getUnimod(unimodHashMap.get(i));
                if (unimod != null) {
                    monoWeight += unimod.getMonoMass();
                }
            }
            monoWeight += aa.getMonoIsotopicMass();
            bSeries.add(monoWeight);
        }

        //ySeries
        List<Double> ySeries = new ArrayList<>();
        monoWeight = Constants.PROTON_MASS_U * charge;

        double h2oWeight = elementsDAO.getMonoWeight(ElementsDAO.H2O);
        for (int i = acidCodeArray.length - 1; i > 0; i--) {
            AminoAcid aa = aminoAcidDAO.getAminoAcidByCode(String.valueOf(acidCodeArray[i]));
            if (aa == null) {
                continue;
            }
            if (unimodHashMap != null && unimodHashMap.containsKey(i)) {
                Unimod unimod = unimodDAO.getUnimod(unimodHashMap.get(i));
                if (unimod != null) {
                    monoWeight += unimod.getMonoMass();
                }
            }
            monoWeight += aa.getMonoIsotopicMass();
            ySeries.add(monoWeight + h2oWeight);
        }

        bySeries.setBSeries(bSeries);
        bySeries.setYSeries(ySeries);

        return bySeries;
    }

    /**
     * 标准库中的PeptideDO对象生成该肽段所有B,Y类型的排列组合的离子MZ的Map,key为cutInfo
     *
     * @param peptideDO   标准库中的PeptideDO对象
     * @param limitLength 生成的B,Y离子的最小长度
     * @return
     */
    public HashMap<String, FragmentInfo> buildFragmentMap(PeptideDO peptideDO, int limitLength, List<String> ionTypes, List<Integer> chargeTypes) {
        HashMap<String, FragmentInfo> fragmentMap = new HashMap<>();
        String sequence = peptideDO.getSequence();
        int length = sequence.length();
        if (length < limitLength) {
            return null;
        }
        for (Integer charge : chargeTypes) {
            for (int i = limitLength; i < length; i++) {
                String leftSubstring = sequence.substring(0, i);
                String rightSubstring = sequence.substring(length - i, length);
                List<String> leftUnimodIds = formulaCalculator.parseUnimodIds(peptideDO.getUnimodMap(), 0, i);
                List<String> rightUnimodIds = formulaCalculator.parseUnimodIds(peptideDO.getUnimodMap(), length - i, length);

                if(ionTypes.contains(ResidueType.AIon)){
                    String cutInfoA = "a" + i + (charge == 1 ? "" : ("^" + charge));
                    fragmentMap.put(cutInfoA, new FragmentInfo(cutInfoA, formulaCalculator.getMonoMz(leftSubstring, ResidueType.AIon, charge, 0, 0, false, leftUnimodIds), charge));
                }
                if(ionTypes.contains(ResidueType.BIon)){
                    String cutInfoB = "b" + i + (charge == 1 ? "" : ("^" + charge));
                    fragmentMap.put(cutInfoB, new FragmentInfo(cutInfoB, formulaCalculator.getMonoMz(leftSubstring, ResidueType.BIon, charge, 0, 0, false, leftUnimodIds), charge));
                }
                if(ionTypes.contains(ResidueType.CIon)){
                    String cutInfoC = "c" + i + (charge == 1 ? "" : ("^" + charge));
                    fragmentMap.put(cutInfoC, new FragmentInfo(cutInfoC, formulaCalculator.getMonoMz(leftSubstring, ResidueType.CIon, charge, 0, 0, false, leftUnimodIds), charge));
                }
                if(ionTypes.contains(ResidueType.XIon)){
                    String cutInfoX = "x" + i + (charge == 1 ? "" : ("^" + charge));
                    fragmentMap.put(cutInfoX, new FragmentInfo(cutInfoX, formulaCalculator.getMonoMz(rightSubstring, ResidueType.XIon, charge, 0, 0, false, rightUnimodIds), charge));
                }
                if(ionTypes.contains(ResidueType.YIon)){
                    String cutInfoY = "y" + i + (charge == 1 ? "" : ("^" + charge));
                    fragmentMap.put(cutInfoY, new FragmentInfo(cutInfoY, formulaCalculator.getMonoMz(rightSubstring, ResidueType.YIon, charge, 0, 0, false, rightUnimodIds), charge));
                }
                if(ionTypes.contains(ResidueType.ZIon)){
                    String cutInfoZ = "z" + i + (charge == 1 ? "" : ("^" + charge));
                    fragmentMap.put(cutInfoZ, new FragmentInfo(cutInfoZ, formulaCalculator.getMonoMz(rightSubstring, ResidueType.ZIon, charge, 0, 0, false, rightUnimodIds), charge));
                }
            }
        }

        return fragmentMap;
    }

    public double getTheoryMass(HashMap<Integer, String> unimodHashMap, String sequence) {
        double totalMass = Constants.B_SIDE_MASS + Constants.Y_SIDE_MASS - 2 * Constants.PROTON_MASS_U;
        char[] acidCodeArray = sequence.toCharArray();
        for (char acidCode : acidCodeArray) {
            AminoAcid aa = aminoAcidDAO.getAminoAcidByCode(String.valueOf(acidCode));
            totalMass += aa.getMonoIsotopicMass();
        }
        if (unimodHashMap == null){
            return totalMass;
        }
        for (String unimodCode : unimodHashMap.values()) {
            Unimod unimod = unimodDAO.getUnimod(unimodCode);
            if (unimod != null) {
                totalMass += unimod.getMonoMass();
            }
        }
        return totalMass;
    }

    public Fragment getFragment(PeptideDO peptide, FragmentInfo fragmentInfo) {
        Fragment fragment = new Fragment(peptide.getId());
        String sequence = peptide.getSequence();
        Annotation annotation = fragmentInfo.getAnnotation();
        fragment.setUnimodMap(peptide.getUnimodMap());

        String fs = getFragmentSequence(sequence, annotation.getType(), annotation.getLocation());

        String type = annotation.getType();
        if (type.equals(ResidueType.AIon) || type.equals(ResidueType.BIon) || type.equals(ResidueType.CIon)) {
            fragment.setStart(0);
            //因为location是从1开始计数的,而这边的end是从0开始计数的
            fragment.setEnd(annotation.getLocation() - 1);
        } else if (type.equals(ResidueType.XIon) || type.equals(ResidueType.YIon) || type.equals(ResidueType.ZIon)) {
            fragment.setStart(sequence.length() - annotation.getLocation());
            fragment.setEnd(sequence.length() - 1);
        } else if (type.equals(ResidueType.Full)) {
            fragment.setStart(0);
            fragment.setEnd(sequence.length() - 1);
        }
        fragment.setSequence(fs);
        fragment.setIsotope(annotation.isIsotope());
        fragment.setLocation(annotation.getLocation());
        fragment.setDeviation(annotation.getDeviation());
        fragment.setAdjust(annotation.getAdjust());
        fragment.setType(annotation.getType());
        fragment.setCharge(annotation.getCharge());
        fragment.setMonoMz(formulaCalculator.getMonoMz(fragment));
        fragment.setAverageMz(formulaCalculator.getAverageMz(fragment));

        return fragment;
    }

    /**
     * 获取不包含计算分子质量数据的fragment,也不包含修饰基团
     *
     * @param peptideDO
     * @return
     */
    public Fragment getBaseFragment(PeptideDO peptideDO, FragmentInfo fragmentInfo) {
        Fragment fragment = new Fragment();
        String sequence = peptideDO.getSequence();
        Annotation annotation = fragmentInfo.getAnnotation();
        fragment.setUnimodMap(peptideDO.getUnimodMap());

        String fs = getFragmentSequence(sequence, annotation.getType(), annotation.getLocation());

        fragment.setIsotope(annotation.isIsotope());
        fragment.setDeviation(annotation.getDeviation());
        fragment.setLocation(annotation.getLocation());
        fragment.setAdjust(annotation.getAdjust());
        fragment.setSequence(fs);
        fragment.setType(annotation.getType());
        fragment.setCharge(annotation.getCharge());

        return fragment;
    }

    public String getFragmentSequence(String originSequence, String type, int location) {
        if (type.equals(ResidueType.AIon) || type.equals(ResidueType.BIon) || type.equals(ResidueType.CIon)) {
            return originSequence.substring(0, location);
        } else if (type.equals(ResidueType.XIon) || type.equals(ResidueType.YIon) || type.equals(ResidueType.ZIon)) {
            return originSequence.substring(originSequence.length() - location);
        } else if (type.equals(ResidueType.Full)) {
            return originSequence;
        } else {
            logger.error("解析出未识别离子类型:" + type);
            return null;
        }
    }

    public List<AminoAcid> getFragmentSequence(List<AminoAcid> originList, String type, int location) {
        if (type.equals(ResidueType.AIon) || type.equals(ResidueType.BIon) || type.equals(ResidueType.CIon)) {
            return originList.subList(0, location);
        } else if (type.equals(ResidueType.XIon) || type.equals(ResidueType.YIon) || type.equals(ResidueType.ZIon)) {
            return originList.subList(originList.size() - location, originList.size());
        } else if (type.equals(ResidueType.Full)) {
            return originList;
        } else {
            logger.error("解析出未识别离子类型:" + type);
            return null;
        }
    }

    public List<AminoAcid> parseAminoAcid(String sequence, HashMap<Integer, String> unimodMap) {

        List<AminoAcid> aminoAcids = new ArrayList<>();
        char[] sequenceArray = sequence.toCharArray();
        for (int i = 0; i < sequenceArray.length; i++) {
            AminoAcid aa = new AminoAcid();
            aa.setName(String.valueOf(sequenceArray[i]));
            if (unimodMap != null) {
                aa.setModId(unimodMap.get(i));
            }
            aminoAcids.add(aa);
        }
        return aminoAcids;
    }
}
