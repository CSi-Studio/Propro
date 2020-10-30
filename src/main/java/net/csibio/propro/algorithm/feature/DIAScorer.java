package net.csibio.propro.algorithm.feature;

import net.csibio.propro.algorithm.formula.FragmentFactory;
import net.csibio.propro.constants.Constants;
import net.csibio.propro.constants.IsotopeConstants;
import net.csibio.propro.constants.enums.ScoreType;
import net.csibio.propro.dao.AminoAcidDAO;
import net.csibio.propro.dao.ElementsDAO;
import net.csibio.propro.domain.bean.score.BYSeries;
import net.csibio.propro.domain.bean.score.FeatureScores;
import net.csibio.propro.domain.bean.score.IntegrateWindowMzIntensity;
import net.csibio.propro.domain.bean.score.PeakGroup;
import net.csibio.propro.utils.ScoreUtil;
import net.csibio.propro.dao.UnimodDAO;
import org.apache.commons.math3.util.FastMath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-15 16:09
 * <p>
 * scores.massdev_score
 * scores.weighted_massdev_score
 * <p>
 * scores.isotope_correlation
 * scores.isotope_overlap
 * <p>
 * scores.bseries_score
 * scores.yseries_score
 */
@Component("diaScorer")
public class DIAScorer {

    @Autowired
    UnimodDAO unimodDAO;
    @Autowired
    ElementsDAO elementsDAO;
    @Autowired
    AminoAcidDAO aminoAcidDAO;
    @Autowired
    FragmentFactory fragmentFactory;

    /**
     * scores.massdev_score 按spectrum intensity加权的mz与product mz的偏差ppm百分比之和
     * scores.weighted_massdev_score 按spectrum intensity加权的mz与product mz的偏差ppm百分比按libraryIntensity加权之和
     *
     * @param productMzArray   根据transitionGroup获得存在transition中的productMz，存成Float array
     * @param spectrumMzArray  根据transitionGroup选取的RT选择的最近的Spectrum对应的mzArray
     * @param spectrumIntArray 根据transitionGroup选取的RT选择的最近的Spectrum对应的intensityArray
     * @param normedLibIntMap  unNormalized library intensity(in peptidepeptide)
     * @param scores           scoreForAll for Airus
     */
    public void calculateDiaMassDiffScore(HashMap<String, Float> productMzArray, float[] spectrumMzArray, float[] spectrumIntArray, HashMap<String, Double> normedLibIntMap, FeatureScores scores, List<String> scoreTypes) {

        double ppmScore = 0.0d;
        double ppmScoreWeighted = 0.0d;
        for (String key : productMzArray.keySet()) {
            float productMz = productMzArray.get(key);
            float left = productMz - Constants.DIA_EXTRACT_WINDOW / 2f;
            float right = productMz + Constants.DIA_EXTRACT_WINDOW / 2f;

            try {
                IntegrateWindowMzIntensity mzIntensity = ScoreUtil.integrateWindow(spectrumMzArray, spectrumIntArray, left, right);
                if (mzIntensity.isSignalFound()) {
                    double diffPpm = Math.abs(mzIntensity.getMz() - productMz) * 1000000d / productMz;
                    ppmScore += diffPpm;
                    ppmScoreWeighted += diffPpm * normedLibIntMap.get(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (scoreTypes.contains(ScoreType.MassdevScore.getTypeName())) {
            scores.put(ScoreType.MassdevScore.getTypeName(), ppmScore, scoreTypes);
        }
        if (scoreTypes.contains(ScoreType.MassdevScoreWeighted.getTypeName())) {
            scores.put(ScoreType.MassdevScoreWeighted.getTypeName(), ppmScoreWeighted, scoreTypes);
        }
    }


    /**
     * scores.isotope_correlation
     * scores.isotope_overlap //feature intensity加权的可能（带电量1-4）无法区分同位素峰值的平均发生次数之和
     *
     * @param peakGroupFeature single mrmFeature
     * @param productMzMap     mz of fragment
     * @param spectrumMzArray  mz array of selected Rt
     * @param spectrumIntArray intensity array of selected Rt
     * @param productChargeMap charge in peptide
     * @param scores           scoreForAll for JProphet
     */
    public void calculateDiaIsotopeScores(PeakGroup peakGroupFeature, HashMap<String, Float> productMzMap, float[] spectrumMzArray, float[] spectrumIntArray, HashMap<String, Integer> productChargeMap, FeatureScores scores, List<String> scoreTypes) {
        double isotopeCorr = 0d;
        double isotopeOverlap = 0d;
        int maxIsotope = Constants.DIA_NR_ISOTOPES + 1;

        //getFirstIsotopeRelativeIntensities
        double relIntensity;//离子强度占peak group强度的比例
        double intensitySum = peakGroupFeature.getPeakGroupInt();

        for (String cutInfo : peakGroupFeature.getIonIntensity().keySet()) {
            float monoPeakMz = productMzMap.get(cutInfo);
            int putativeFragmentCharge = productChargeMap.get(cutInfo);
            relIntensity = (peakGroupFeature.getIonIntensity().get(cutInfo) / intensitySum);
            Double[] expDistribution = new Double[maxIsotope];
            double maxIntensity = 0.0d;
            for (int iso = 0; iso < maxIsotope; iso++) {
                float left = monoPeakMz + iso * Constants.C13C12_MASSDIFF_U / putativeFragmentCharge;
                float right = left;
                left -= Constants.DIA_EXTRACT_WINDOW / 2d;
                right += Constants.DIA_EXTRACT_WINDOW / 2d;

                //integrate window
                IntegrateWindowMzIntensity mzIntensity = ScoreUtil.integrateWindow(spectrumMzArray, spectrumIntArray, left, right);
                if (mzIntensity.getIntensity() > maxIntensity) {
                    maxIntensity = mzIntensity.getIntensity();
                }
                expDistribution[iso] = mzIntensity.getIntensity();
            }

            //get scores.isotope_correlation
            double massWeight = monoPeakMz * putativeFragmentCharge;
            double factor = massWeight / Constants.AVG_TOTAL;
            HashMap<String, Integer> formula = new HashMap<>(); //等比放大算多少个？
            formula.put("C", (int) Math.round(Constants.C * factor));
            formula.put("N", (int) Math.round(Constants.N * factor));
            formula.put("O", (int) Math.round(Constants.O * factor));
            formula.put("S", (int) Math.round(Constants.S * factor));

            double theroyWeight = Constants.AVG_WEIGHT_C * formula.get("C") +
                    Constants.AVG_WEIGHT_N * formula.get("N") +
                    Constants.AVG_WEIGHT_O * formula.get("O") +
                    Constants.AVG_WEIGHT_S * formula.get("S");//模拟表达式的weight
            double remainingMass = massWeight - theroyWeight;
            formula.put("H", (int) Math.round(remainingMass / Constants.AVG_WEIGHT_H));//residual添加H

            Double[] isotopeDistributionC = convolvePow(IsotopeConstants.C, formula.get("C"));
            Double[] isotopeDistributionH = convolvePow(IsotopeConstants.H, formula.get("H"));
            Double[] isotopeDistributionN = convolvePow(IsotopeConstants.N, formula.get("N"));
            Double[] isotopeDistributionO = convolvePow(IsotopeConstants.O, formula.get("O"));
            Double[] isotopeDistributionS = convolvePow(IsotopeConstants.S, formula.get("S"));

            Double[] theroyDistribution;
            theroyDistribution = convolve(isotopeDistributionC, isotopeDistributionH, maxIsotope);
            theroyDistribution = convolve(theroyDistribution, isotopeDistributionN, maxIsotope);
            theroyDistribution = convolve(theroyDistribution, isotopeDistributionO, maxIsotope);
            theroyDistribution = convolve(theroyDistribution, isotopeDistributionS, maxIsotope);

//            MathUtil.renormalize(distributionResult);
//            double maxValueOfDistribution = distributionResult.get(MathUtil.findMaxIndex(distributionResult));
//            for (int j = 0; j < distributionResult.size(); j++) {
//                distributionResult.set(j, distributionResult.get(j) / maxValueOfDistribution);
//            }
            double corr = 0.0d, m1 = 0.0d, m2 = 0.0d, s1 = 0.0d, s2 = 0.0d;
            for (int j = 0; j < maxIsotope; j++) {
                corr += expDistribution[j] * theroyDistribution[j];
                m1 += expDistribution[j];
                m2 += theroyDistribution[j];
                s1 += expDistribution[j] * expDistribution[j];
                s2 += theroyDistribution[j] * theroyDistribution[j];
            }
            s1 -= m1 * m1 / maxIsotope;
            s2 -= m2 * m2 / maxIsotope;
            if (s1 * s2 != 0) {
                corr -= m1 * m2 / maxIsotope;
                corr /= FastMath.sqrt(s1 * s2);
                isotopeCorr += relIntensity * corr;
            }


            //get scores.isotope_overlap
            int largePeaksBeforeFirstIsotope = 0;
            double ratio;
            double monoPeakIntensity = expDistribution[0];
            for (int ch = 1; ch < maxIsotope; ch++) {
                double leftPeakMz = monoPeakMz - Constants.C13C12_MASSDIFF_U / ch;
                Double left = leftPeakMz - Constants.DIA_EXTRACT_WINDOW / 2d;
                Double right = leftPeakMz + Constants.DIA_EXTRACT_WINDOW / 2d;

                //对于多种带电量，对-i同位素的mz位置进行数据提取，若强度高于0同位素强度，且-i同位素的理论mz与实际mz差距小于阈值，认为-i同位素出现
                IntegrateWindowMzIntensity mzIntensity = ScoreUtil.integrateWindow(spectrumMzArray, spectrumIntArray, left.floatValue(), right.floatValue());
                if (!mzIntensity.isSignalFound()) {
                    continue;
                }
                if (monoPeakIntensity != 0) {
                    ratio = mzIntensity.getIntensity() / monoPeakIntensity;
                } else {
                    ratio = 0d;
                }
                //从OpenSWATH源代码1.0改为Constants.C13C12_MASSDIFF_U,作为leftPeakMz
                if (ratio > 1 && (Math.abs(mzIntensity.getMz() - (monoPeakMz - 1.0d / ch)) / monoPeakMz) < Constants.PEAK_BEFORE_MONO_MAX_PPM_DIFF) {
                    largePeaksBeforeFirstIsotope++;//-i同位素出现的次数
                }

            }
            isotopeOverlap += largePeaksBeforeFirstIsotope * relIntensity;//带离子强度权重的largePeaksBeforeFirstIsotope数量统计
        }
        scores.put(ScoreType.IsotopeCorrelationScore.getTypeName(), isotopeCorr, scoreTypes);
        scores.put(ScoreType.IsotopeOverlapScore.getTypeName(), isotopeOverlap, scoreTypes);
    }

    /**
     * scores.bseries_score peptideRt对应的spectrumArray中，检测到的b离子的数量
     * scores.yseries_score peptideRt对应的spectrumArray中，检测到的y离子的数量
     *
     * @param spectrumMzArray
     * @param spectrumIntArray
     * @param unimodHashMap
     * @param sequence
     * @param charge
     * @param scores
     */
    public void calculateBYIonScore(float[] spectrumMzArray, float[] spectrumIntArray, HashMap<Integer, String> unimodHashMap, String sequence, int charge, FeatureScores scores, List<String> scoreTypes) {

        //计算理论值
        BYSeries bySeries = fragmentFactory.getBYSeries(unimodHashMap, sequence, charge);
        List<Double> bSeriesList = bySeries.getBSeries();
        int bSeriesScore = getSeriesScore(bSeriesList, spectrumMzArray, spectrumIntArray);

        List<Double> ySeriesList = bySeries.getYSeries();
        int ySeriesScore = getSeriesScore(ySeriesList, spectrumMzArray, spectrumIntArray);

        scores.put(ScoreType.BseriesScore.getTypeName(), (double) bSeriesScore, scoreTypes);
        scores.put(ScoreType.YseriesScore.getTypeName(), (double) ySeriesScore, scoreTypes);
    }


    private List<Double> getIsotopePercent(List<String> isotopeLog) {
        List<Double> isotopePercentList = new ArrayList<>();
        for (String isotope : isotopeLog) {
            isotopePercentList.add(Double.parseDouble(isotope.split(":")[0]) / 100d);
        }
//        Collections.sort(isotopePercentList);
//        Collections.reverse(isotopePercentList);
        return isotopePercentList;
    }

    /**
     * @param factor number of predicted element
     * @return
     */
    private Double[] convolvePow(List<Double[]> distribution, int factor) {

        if (factor == 1) {
            return distribution.get(0);
        }
        int log2n = (int) Math.ceil(FastMath.log(2, factor));

        Double[] distributionResult;
        if ((factor & 1) == 1) {
            distributionResult = distribution.get(0);
        } else {
            distributionResult = new Double[]{1d};
        }
        for (int i = 1; i <= log2n; i++) {
            if ((factor & (1 << i)) == 1 << i) {
                distributionResult = convolve(distributionResult, distribution.get(i), Constants.DIA_NR_ISOTOPES + 1);
            }
        }
        return distributionResult;
    }

    /**
     * @param isotopeDistribution percent list of isotope
     * @param maxIsotope          Constants.DIA_NR_ISOTOPES + 1 = 5
     * @return
     */
    private Double[] convolveSquare(List<Double> isotopeDistribution, int maxIsotope) {

        int rMax = 2 * isotopeDistribution.size() - 1;//3,5
        if (maxIsotope != 0 && maxIsotope + 1 < rMax) {
            rMax = maxIsotope + 1;
        }
        Double[] result = new Double[rMax];//5
        for (int i = 0; i < rMax; i++) {
            result[i] = 0d;
        }
        for (int i = isotopeDistribution.size() - 1; i >= 0; i--) {//each percent
            for (int j = Math.min(rMax - i, isotopeDistribution.size()) - 1; j >= 0; j--) {
                result[i + j] += isotopeDistribution.get(i) * isotopeDistribution.get(j);
            }
        }
        return result;
    }

    private Double[] convolve(Double[] leftDistribution, Double[] rightFormerResult, int maxIsotope) {
        int rMax = leftDistribution.length + rightFormerResult.length - 1;
//        Collections.sort(leftDistribution);
//        Collections.reverse(leftDistribution);
//        Collections.sort(rightFormerResult);
//        Collections.reverse(rightFormerResult);
        if (maxIsotope != 0 && rMax > maxIsotope) {
            rMax = maxIsotope;
        }
        Double[] result = new Double[rMax];
        for (int i = 0; i < rMax; i++) {
            result[i] = 0d;
        }
        for (int i = leftDistribution.length - 1; i >= 0; i--) {
            for (int j = Math.min(rMax - i, rightFormerResult.length) - 1; j >= 0; j--) {
                result[i + j] += leftDistribution[i] * rightFormerResult[j];
            }
        }
        return result;
    }

    /**
     * scoreForAll unit of BYSeries scores
     *
     * @param seriesList       ySeriesList or bSeriesList
     * @param spectrumMzArray  mzArray of certain spectrum
     * @param spectrumIntArray intArray of certain spectrum
     * @return scoreForAll of b or y
     */
    private int getSeriesScore(List<Double> seriesList, float[] spectrumMzArray, float[] spectrumIntArray) {
        int seriesScore = 0;
        for (double seriesMz : seriesList) {
            Double left = seriesMz - Constants.DIA_EXTRACT_WINDOW / 2d;
            Double right = seriesMz + Constants.DIA_EXTRACT_WINDOW / 2d;

            IntegrateWindowMzIntensity mzIntensity = ScoreUtil.integrateWindow(spectrumMzArray, spectrumIntArray, left.floatValue(), right.floatValue());
            if (mzIntensity.isSignalFound() &&
                    (Math.abs(seriesMz - mzIntensity.getMz()) * 1000000 / seriesMz) < Constants.DIA_BYSERIES_PPM_DIFF &&
                    mzIntensity.getIntensity() > Constants.DIA_BYSERIES_INTENSITY_MIN) {
                seriesScore++;
            }
        }
        return seriesScore;
    }
}
