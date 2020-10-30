package net.csibio.propro.algorithm.decoy;

import net.csibio.propro.algorithm.formula.FormulaCalculator;
import net.csibio.propro.algorithm.formula.FragmentFactory;
import net.csibio.propro.algorithm.parser.model.traml.Modification;
import net.csibio.propro.algorithm.parser.model.traml.Peptide;
import net.csibio.propro.domain.db.PeptideDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-07 10:20
 */
public abstract class BaseGenerator {

    public final Logger logger = LoggerFactory.getLogger(BaseGenerator.class);

    @Autowired
    public FormulaCalculator formulaCalculator;

    @Autowired
    public FragmentFactory fragmentFactory;

    protected abstract void generate(PeptideDO peptide);

    public void generate(List<PeptideDO> list) {
        for (PeptideDO target : list) {
            generate(target);
        }
        logger.info("伪肽段生成完毕,总计:"+list.size()+"个");
    }
    /**
     * Compute relative identity (relative number of matches of amino acids at the same position) between two sequences
     *
     * @param sequence
     * @param decoy
     * @return
     */
    protected double aaSequenceIdentify(String sequence, String decoy) {
        //sequence和decoy的长度必须一致
        if (sequence.length() != decoy.length()) {
            return 0;
        }

        double count = 0;
        for (int i = 0; i < sequence.length(); i++) {
            if(sequence.charAt(i) == decoy.charAt(i)){
                count++;
            }
        }

        return count/sequence.length();
    }

    /**
     * Find all K, R, P sites in a sequence to be set as fixed
     *
     * @param sequence
     * @return
     */
    private HashMap<Integer, String> findFixedResidues(String sequence) {
        return null;
    }

    /**
     * Find all K, R, P and C-/N-terminal sites in a sequence to be set as fixed
     * This method was adapted from the SpectraST decoy generator
     *
     * @param sequence
     * @return
     */
    private HashMap<Integer, String> findFixedAndTermResidues(String sequence) {
        return null;
    }

    /**
     * Check if a peptide has C or N terminal modifications
     *
     * @param peptide
     * @return
     */
    private boolean hasCNterminalMods(Peptide peptide) {
        List<Modification> modificationList = peptide.getModificationList();
        for (Modification modification : modificationList) {
            if (modification.getLocation() == 0 || modification.getLocation() == (peptide.getSequence().length() + 1)) {
                return true;
            }
        }
        return false;
    }
}
