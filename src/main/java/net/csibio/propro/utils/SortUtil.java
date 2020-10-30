package net.csibio.propro.utils;

import net.csibio.aird.bean.WindowRange;
import net.csibio.propro.domain.bean.score.FeatureScores;
import net.csibio.propro.domain.bean.score.SimpleFeatureScores;
import net.csibio.propro.domain.db.AnalyseOverviewDO;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.db.SwathIndexDO;
import net.csibio.propro.domain.db.simple.PeptideScores;
import com.google.common.collect.Ordering;

import java.util.Comparator;
import java.util.List;

public class SortUtil {

    public static List<PeptideScores> sortByPeptideRef(List<PeptideScores> scores) {
        Ordering<PeptideScores> ordering = Ordering.from(new Comparator<PeptideScores>() {
            @Override
            public int compare(PeptideScores o1, PeptideScores o2) {
                return o1.getPeptideRef().compareTo(o2.getPeptideRef());
            }
        });

        return ordering.sortedCopy(scores);
    }

    public static List<SwathIndexDO> sortSwathIndexByStartPtr(List<SwathIndexDO> swathList) {
        Ordering<SwathIndexDO> ordering = Ordering.from(new Comparator<SwathIndexDO>() {
            @Override
            public int compare(SwathIndexDO o1, SwathIndexDO o2) {
                return o1.getStartPtr().compareTo(o2.getStartPtr());
            }
        });

        return ordering.sortedCopy(swathList);
    }

    public static List<SimpleFeatureScores> sortByMainScore(List<SimpleFeatureScores> scores, boolean isDesc) {
        Ordering<SimpleFeatureScores> ordering = Ordering.from(new Comparator<SimpleFeatureScores>() {
            @Override
            public int compare(SimpleFeatureScores o1, SimpleFeatureScores o2) {
                try {
                    if (isDesc) {
                        return o2.getMainScore().compareTo(o1.getMainScore());
                    } else {
                        return o1.getMainScore().compareTo(o2.getMainScore());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        return ordering.sortedCopy(scores);
    }

    public static List<SimpleFeatureScores> sortByFdr(List<SimpleFeatureScores> scores, boolean isDesc) {
        Ordering<SimpleFeatureScores> ordering = Ordering.from(new Comparator<SimpleFeatureScores>() {
            @Override
            public int compare(SimpleFeatureScores o1, SimpleFeatureScores o2) {
                try {
                    if(o1.getFdr() == null){
                        return 1;
                    }
                    if(o2.getFdr() == null){
                        return -1;
                    }
                    if (isDesc) {
                        return o2.getFdr().compareTo(o1.getFdr());
                    } else {
                        return o1.getFdr().compareTo(o2.getFdr());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        return ordering.sortedCopy(scores);
    }

    public static List<FeatureScores> sortBySelectedScore(List<FeatureScores> scores, String scoreName, boolean isDesc, List<String> scoreTypes) {
        Ordering<FeatureScores> ordering = Ordering.from(new Comparator<FeatureScores>() {
            @Override
            public int compare(FeatureScores o1, FeatureScores o2) {
                if (isDesc) {
                    return o2.get(scoreName, scoreTypes).compareTo(o1.get(scoreName, scoreTypes));
                } else {
                    try {
                        return o1.get(scoreName, scoreTypes).compareTo(o2.get(scoreName, scoreTypes));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            }
        });

        return ordering.sortedCopy(scores);
    }

    /**
     * @param scores
     * @param isDesc 是否降序排序
     * @return
     */
    public static List<SimpleFeatureScores> sortByPValue(List<SimpleFeatureScores> scores, boolean isDesc) {
        Ordering<SimpleFeatureScores> ordering = Ordering.from(new Comparator<SimpleFeatureScores>() {
            @Override
            public int compare(SimpleFeatureScores o1, SimpleFeatureScores o2) {
                if (isDesc) {
                    return o2.getPValue().compareTo(o1.getPValue());
                } else {
                    return o1.getPValue().compareTo(o2.getPValue());
                }
            }
        });

        return ordering.sortedCopy(scores);
    }

    /**
     * @param overviews
     * @param isDesc    是否降序排序
     * @return
     */
    public static List<AnalyseOverviewDO> sortByMatchedPeptideCount(List<AnalyseOverviewDO> overviews, boolean isDesc) {
        Ordering<AnalyseOverviewDO> ordering = Ordering.from(new Comparator<AnalyseOverviewDO>() {
            @Override
            public int compare(AnalyseOverviewDO o1, AnalyseOverviewDO o2) {
                if (isDesc) {
                    return o2.getMatchedPeptideCount().compareTo(o1.getMatchedPeptideCount());
                } else {
                    return o1.getMatchedPeptideCount().compareTo(o2.getMatchedPeptideCount());
                }
            }
        });

        return ordering.sortedCopy(overviews);
    }

    /**
     * @param peptides
     * @param isDesc   是否降序排序
     * @return
     */
    public static List<PeptideDO> sortByMz(List<PeptideDO> peptides, boolean isDesc) {
        Ordering<PeptideDO> ordering = Ordering.from(new Comparator<PeptideDO>() {
            @Override
            public int compare(PeptideDO o1, PeptideDO o2) {
                if (isDesc) {
                    return o2.getMz().compareTo(o1.getMz());
                } else {
                    return o1.getMz().compareTo(o2.getMz());
                }
            }
        });

        return ordering.sortedCopy(peptides);
    }

    /**
     * @param rangs
     * @param isDesc 是否降序排序
     * @return
     */
    public static List<WindowRange> sortByMzStart(List<WindowRange> rangs, boolean isDesc) {
        Ordering<WindowRange> ordering = Ordering.from(new Comparator<WindowRange>() {
            @Override
            public int compare(WindowRange o1, WindowRange o2) {
                if (isDesc) {
                    return o2.getStart().compareTo(o1.getStart());
                } else {
                    return o1.getStart().compareTo(o2.getStart());
                }
            }
        });

        return ordering.sortedCopy(rangs);
    }

}
