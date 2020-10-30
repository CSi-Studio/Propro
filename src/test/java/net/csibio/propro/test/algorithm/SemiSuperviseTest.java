package net.csibio.propro.test.algorithm;

import net.csibio.propro.algorithm.learner.SemiSupervise;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.service.LibraryService;
import net.csibio.propro.service.PeptideService;
import net.csibio.propro.service.ScoreService;
import net.csibio.propro.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-12 21:18
 */
public class SemiSuperviseTest extends BaseTest {


    @Autowired
    SemiSupervise semiSupervise;
    @Autowired
    ScoreService scoreService;
    @Autowired
    PeptideService peptideService;
    @Autowired
    LibraryService libraryService;

    private boolean isSimilar(Double[] array1, Double[] array2, Double tolerance) {
        if (array1.length != array2.length) return false;
        boolean result = true;
        for (int i = 0; i < array1.length; i++) {
            if (Math.abs(array1[i] - array2[i]) > tolerance) {
                result = false;
            }
        }
        return result;
    }

    private boolean isSimilar(double[] array1, double[] array2, Double tolerance) {
        if (array1.length != array2.length) return false;
        boolean result = true;
        for (int i = 0; i < array1.length; i++) {
            if (Math.abs(array1[i] - array2[i]) > tolerance) {
                result = false;
            }
        }
        return result;
    }

    @Test
    public void test2() {
        List<PeptideDO> peptides = peptideService.getAllByLibraryId("5c093e20fc6f9e5a6c2d77a9");
        for (PeptideDO peptideDO : peptides) {
            PeptideDO humanLibPeptide = peptideService.getByLibraryIdAndPeptideRef("5c0a3669fc6f9e1d441ae71f", peptideDO.getPeptideRef());
            peptideDO.setRt(humanLibPeptide.getRt());
            peptideService.update(peptideDO);
        }
    }

    @Test
    public void test() throws IOException {
        File file = new File("D:/001.txt");
        InputStream in = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(in, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        List<String> peptides = new ArrayList<>();
        while (line != null) {
            String[] three = line.split(" ");
            if (Double.parseDouble(three[2]) <= 0.01) {
                if (three[0].split("_").length == 4) {
                    peptides.add(three[0].split("_")[1] + "_" + three[0].split("_")[2]);
                } else {
                    peptides.add(three[0].split("_")[1]);
                }
            }
            line = reader.readLine();
        }

        int count = 0;
        int count1 = 0;
        for (String peptide : peptides) {
            PeptideDO peptideDO = peptideService.getByLibraryIdAndPeptideRef("5c0a237efc6f9e3c5048a6bc", peptide);
            if (peptideDO == null) {
                count1++;
                continue;
            }
            if (peptideDO.getFragmentMap().size() <= 3) {
                count++;
            }
        }

        System.out.println(count);
        System.out.println(count1);
    }
}
