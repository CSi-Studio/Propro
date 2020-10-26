package com.csi.propro.algorithm.extract;

import com.csi.propro.constants.Constants;
import com.csi.propro.constants.ExpTypeConst;
import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.bean.aird.Compressor;
import com.csi.propro.domain.bean.aird.WindowRange;
import com.csi.propro.domain.db.*;
import com.csi.propro.domain.db.simple.SimplePeptide;
import com.csi.propro.domain.params.CoordinateBuildingParams;
import com.csi.propro.domain.params.ExtractParams;
import com.csi.propro.domain.params.WorkflowParams;
import com.csi.propro.domain.query.SwathIndexQuery;
import com.csi.propro.service.*;
import com.csi.propro.utils.AnalyseUtil;
import com.csi.propro.utils.ConvolutionUtil;
import com.csi.propro.utils.SortUtil;
import com.csi.propro.algorithm.parser.AirdFileParser;
import com.csi.propro.domain.bean.analyse.MzIntensityPairs;
import com.csi.propro.domain.db.*;
import com.csi.propro.service.*;
import com.csi.propro.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.*;

@Component("extractor")
public class Extractor {

    public final Logger logger = LoggerFactory.getLogger(Extractor.class);

    @Autowired
    AirdFileParser airdFileParser;
    @Autowired
    LibraryService libraryService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;
    @Autowired
    SwathIndexService swathIndexService;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    AnalyseDataService analyseDataService;
    @Autowired
    PeptideService peptideService;
    @Autowired
    ScoreService scoreService;
    @Autowired
    TaskService taskService;

    /**
     * 提取XIC的核心函数,最终返回提取到XIC的Peptide数目
     * 目前只支持MS2的XIC提取
     *
     * @param workflowParams 将XIC提取,选峰及打分合并在一个步骤中执行,可以完整的省去一次IO读取及解析,提升分析速度,
     *                       需要experimentDO,libraryId,rtExtractionWindow,mzExtractionWindow,SlopeIntercept
     */
    public ResultDO<AnalyseOverviewDO> extract(WorkflowParams workflowParams) {
        ResultDO<AnalyseOverviewDO> resultDO = new ResultDO(true);
        TaskDO task = workflowParams.getTaskDO();
        task.addLog("基本条件检查开始");
        ResultDO checkResult = ConvolutionUtil.checkExperiment(workflowParams.getExperimentDO());
        if (checkResult.isFailed()) {
            return checkResult;
        }

        AnalyseOverviewDO overviewDO = createOverview(workflowParams);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile((File) checkResult.getModel(), "r");
            //核心函数在这里
            extract(raf, overviewDO, workflowParams);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtil.close(raf);
        }

        analyseOverviewService.update(overviewDO);
        resultDO.setModel(overviewDO);
        return resultDO;
    }

    /**
     * 实时提取某一个PeptideRef的XIC图谱,即全时间段XIC提取
     * 不适合用于大批量处理
     *
     * @param exp
     * @param peptide
     * @return
     */
    public ResultDO<AnalyseDataDO> extractOneOnRealTime(ExperimentDO exp, PeptideDO peptide, ExtractParams extractParams) {
        ResultDO checkResult = ConvolutionUtil.checkExperiment(exp);
        if (checkResult.isFailed()) {
            logger.error("条件检查失败:" + checkResult.getMsgInfo());
            return checkResult;
        }

        File file = (File) checkResult.getModel();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
            //Step1.获取窗口信息.
            SwathIndexQuery query = new SwathIndexQuery(exp.getId(), 2);
            query.setMz(peptide.getMz().floatValue());
            SwathIndexDO swathIndexDO = null;
            List<SwathIndexDO> swathIndexList = null;
            TreeMap<Float, MzIntensityPairs> rtMap = new TreeMap<Float, MzIntensityPairs>();
            switch (exp.getType()) {
                case ExpTypeConst.PRM:
                    swathIndexDO = swathIndexService.getPrmIndex(exp.getId(), peptide.getMz().floatValue());
                    if (swathIndexDO == null) {
                        return ResultDO.buildError(ResultCode.SWATH_INDEX_NOT_EXISTED);
                    }
                    break;
                case ExpTypeConst.SCANNING_SWATH:
//                    swathIndexDO = swathIndexService.getSwathIndex(exp.getId(), peptide.getMz().floatValue());
//                    swathIndexList = new ArrayList<>();
//                    swathIndexList.add(swathIndexDO);
                    swathIndexList = swathIndexService.getLinkedSwathIndex(exp.getId(), peptide.getMz().floatValue(), exp.getDeltaMzRange(), Constants.SCANNING_SWATH_COLLECTED_NUMBER);
                    break;
                case ExpTypeConst.DIA_SWATH:
                    swathIndexDO = swathIndexService.getSwathIndex(exp.getId(), peptide.getMz().floatValue());
                    if (swathIndexDO == null) {
                        return ResultDO.buildError(ResultCode.SWATH_INDEX_NOT_EXISTED);
                    }
                    break;
                default:
                    swathIndexDO = swathIndexService.getSwathIndex(exp.getId(), peptide.getMz().floatValue());
                    break;
            }

            //Step2.获取该窗口内的谱图Map,key值代表了RT
            if (exp.getType().equals(ExpTypeConst.SCANNING_SWATH)) {
                for (SwathIndexDO swathIndex : swathIndexList) {
                    rtMap.putAll(airdFileParser.parseSwathBlockValues(raf, swathIndex, exp.fetchCompressor(Compressor.TARGET_MZ), exp.fetchCompressor(Compressor.TARGET_INTENSITY)));
                }
            } else {
                rtMap = airdFileParser.parseSwathBlockValues(raf, swathIndexDO, exp.fetchCompressor(Compressor.TARGET_MZ), exp.fetchCompressor(Compressor.TARGET_INTENSITY));
            }

            SimplePeptide tp = new SimplePeptide(peptide);
            Double rt = peptide.getRt();
            if (extractParams.getRtExtractWindow() == -1) {
                tp.setRtStart(-1);
                tp.setRtEnd(99999);
            } else {
                Double targetRt = (rt - exp.getIrtResult().getSi().getIntercept()) / exp.getIrtResult().getSi().getSlope();
                tp.setRtStart(targetRt.floatValue() - extractParams.getRtExtractWindow() / 2);
                tp.setRtEnd(targetRt.floatValue() + extractParams.getRtExtractWindow() / 2);
            }

            AnalyseDataDO dataDO = extractForOne(tp, rtMap, extractParams, null);
            if (dataDO == null) {
                return ResultDO.buildError(ResultCode.ANALYSE_DATA_ARE_ALL_ZERO);
            }

            ResultDO<AnalyseDataDO> resultDO = new ResultDO<AnalyseDataDO>(true);
            resultDO.setModel(dataDO);
            return resultDO;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return ResultDO.buildError(ResultCode.SWATH_INDEX_NOT_EXISTED);
        } finally {
            FileUtil.close(raf);
        }
    }

    /**
     * 需要传入最终结果集的List对象
     * 最终的XIC结果存储在内存中不落盘,一般用于iRT的计算
     * 由于是直接在内存中的,所以XIC的结果不进行压缩
     *
     * @param finalList
     * @param coordinates
     * @param rtMap
     * @param overviewId
     * @param extractParams
     */
    public void extractForIrt(List<AnalyseDataDO> finalList, List<SimplePeptide> coordinates, TreeMap<Float, MzIntensityPairs> rtMap, String overviewId, ExtractParams extractParams) {
        for (SimplePeptide tp : coordinates) {
            AnalyseDataDO dataDO = extractForOne(tp, rtMap, extractParams, overviewId);
            if (dataDO == null) {
                continue;
            }
            finalList.add(dataDO);
        }
    }

    public void extractForIrtWithLib(List<AnalyseDataDO> finalList, List<SimplePeptide> coordinates, TreeMap<Float, MzIntensityPairs> rtMap, String overviewId, ExtractParams extractParams) {
        long start = System.currentTimeMillis();
        int count = 0;
        for (int i = 0; i < coordinates.size(); i++) {
            if (count >= 1) {
                break;
            }
            if (coordinates.get(i).getSequence().length() <= 13) {
                continue;
            }
            AnalyseDataDO dataDO = extractForOne(coordinates.get(i), rtMap, extractParams, overviewId);
            if (dataDO == null) {
                continue;
            }
            scoreService.strictScoreForOne(dataDO, coordinates.get(i), rtMap, extractParams.getShapeScoreThreshold());

            if (dataDO.getFeatureScoresList() != null) {
                finalList.add(dataDO);
                logger.info("第" + i + "次搜索找到了:" + dataDO.getPeptideRef() + ",BestRT:" + dataDO.getFeatureScoresList().get(0).getRt() + "耗时:" + (System.currentTimeMillis() - start));
                count++;
            }
        }
    }

    private AnalyseDataDO extractForOne(SimplePeptide tp, TreeMap<Float, MzIntensityPairs> rtMap, ExtractParams extractParams, String overviewId) {
        float mzStart = 0;
        float mzEnd = -1;
        //所有的碎片共享同一个RT数组
        ArrayList<Float> rtList = new ArrayList<>();
        for (Float rt : rtMap.keySet()) {
            if (extractParams.getRtExtractWindow() != -1 && rt > tp.getRtEnd()) {
                break;
            }
            if (extractParams.getRtExtractWindow() == -1 || (rt >= tp.getRtStart() && rt <= tp.getRtEnd())) {
                rtList.add(rt);
            }
        }

        Float[] rtArray = new Float[rtList.size()];
        rtList.toArray(rtArray);

        AnalyseDataDO dataDO = new AnalyseDataDO();
        dataDO.setPeptideId(tp.getId());
        dataDO.setRtArray(rtArray);
        dataDO.setOverviewId(overviewId);
        dataDO.setPeptideRef(tp.getPeptideRef());
        dataDO.setProteinName(tp.getProteinName());
        dataDO.setIsDecoy(tp.isAsDecoy());
        dataDO.setRt(tp.getRt());
        dataDO.setMz(tp.getMz());

        boolean isHit = false;

        for (FragmentInfo fi : tp.getFragmentMap().values()) {
            float mz = fi.getMz().floatValue();
            if (extractParams.getIsPpm()) {
                mzStart = mz - mz * extractParams.getHalfMzWindow();
                mzEnd = mz + mz * extractParams.getHalfMzWindow();
            } else {
                mzStart = mz - extractParams.getHalfMzWindow();
                mzEnd = mz + extractParams.getHalfMzWindow();
            }

            Float[] intArray = new Float[rtArray.length];
            boolean isAllZero = true;

            //由于本函数极其注重性能,为整个流程最关键的耗时步骤,每提升10毫秒都可以带来巨大的性能提升  --陆妙善
            if (extractParams.getUseAdaptiveWindow()) {
                for (int i = 0; i < rtArray.length; i++) {
                    float acc = ConvolutionUtil.adaptiveAccumulation(rtMap.get(rtArray[i]), mz);
                    if (acc != 0) {
                        isAllZero = false;
                    }
                    intArray[i] = acc;
                }
            } else {
                for (int i = 0; i < rtArray.length; i++) {
                    float acc = ConvolutionUtil.accumulation(rtMap.get(rtArray[i]), mzStart, mzEnd);
                    if (acc != 0) {
                        isAllZero = false;
                    }
                    intArray[i] = acc;
                }
            }
            if (isAllZero) {
                continue;
            } else {
                isHit = true;
                dataDO.getIntensityMap().put(fi.getCutInfo(), intArray);
            }
            dataDO.getMzMap().put(fi.getCutInfo(), fi.getMz().floatValue());
        }

        //如果所有的片段均没有提取到XIC的结果,则直接返回null
        if (!isHit) {
            return null;
        }

        return dataDO;
    }

    /**
     * 提取MS2 XIC图谱并且输出最终结果,不返回最终的XIC结果以减少内存的使用
     *
     * @param raf            用于读取Aird文件
     * @param overviewDO
     * @param workflowParams
     */
    private void extract(RandomAccessFile raf, AnalyseOverviewDO overviewDO, WorkflowParams workflowParams) {

        TaskDO task = workflowParams.getTaskDO();
        //Step1.获取窗口信息.
        List<WindowRange> rangs = workflowParams.getExperimentDO().getWindowRanges();
        SwathIndexQuery query = new SwathIndexQuery(workflowParams.getExperimentDO().getId(), 2);

        //获取所有MS2的窗口
        List<SwathIndexDO> swathIndexList = swathIndexService.getAll(query);
        HashMap<Float, Float[]> rtRangeMap = null;

        if (workflowParams.getExperimentDO().getType().equals(ExpTypeConst.PRM)) {
            rtRangeMap = experimentService.getPrmRtWindowMap(swathIndexList);
            workflowParams.setRtRangeMap(rtRangeMap);
        }

        taskService.update(task, "总计有窗口:" + rangs.size() + "个,开始进行MS2 提取XIC计算");
        //按窗口开始扫描.如果一共有N个窗口,则一共分N个批次进行XIC提取
        int count = 1;
        try {
            long peakCount = 0L;
            int dataCount = 0;
            swathIndexList = SortUtil.sortSwathIndexByStartPtr(swathIndexList);
            for (SwathIndexDO index : swathIndexList) {
                long start = System.currentTimeMillis();

                List<AnalyseDataDO> dataList = doExtract(raf, index, overviewDO.getId(), workflowParams);
                if (dataList != null) {
                    for (AnalyseDataDO dataDO : dataList) {
                        peakCount += dataDO.getFeatureScoresList().size();
                    }
                    dataCount += dataList.size();
                }
                analyseDataService.insertAll(dataList, false);
                taskService.update(task, "第" + count + "轮数据XIC提取完毕,有效肽段:" + (dataList == null ? 0 : dataList.size()) + "个,耗时:" + (System.currentTimeMillis() - start) / 1000 + "秒");
                count++;
            }

            overviewDO.setTotalPeptideCount(dataCount);
            overviewDO.setPeakCount(peakCount);
            analyseOverviewService.update(overviewDO);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    /**
     * 返回提取到的数目
     *
     * @param raf
     * @param workflowParams
     * @param swathIndex
     * @param overviewId
     * @return
     * @throws Exception
     */
    private List<AnalyseDataDO> doExtract(RandomAccessFile raf, SwathIndexDO swathIndex, String overviewId, WorkflowParams workflowParams) throws Exception {
        List<SimplePeptide> coordinates;
        TreeMap<Float, MzIntensityPairs> rtMap;
        //Step2.获取标准库的目标肽段片段的坐标
        Float[] rtRange = null;
        if (workflowParams.getRtRangeMap() != null) {
            float precursorMz = swathIndex.getRange().getMz();
            rtRange = workflowParams.getRtRangeMap().get(precursorMz);
        }
        ExperimentDO exp = workflowParams.getExperimentDO();
        CoordinateBuildingParams params = new CoordinateBuildingParams();
        params.setSlopeIntercept(workflowParams.getSlopeIntercept());
        params.setRtExtractionWindows(workflowParams.getExtractParams().getRtExtractWindow());
        params.setRtRange(rtRange);
        params.setType(exp.getType());
        params.setUniqueCheck(workflowParams.isUniqueOnly());
        params.setNoDecoy(false);

        coordinates = peptideService.buildCoordinates(workflowParams.getLibrary(), swathIndex.getRange(), params);
        if (coordinates.isEmpty()) {
            logger.warn("No Coordinates Found,Rang:" + swathIndex.getRange().getStart() + ":" + swathIndex.getRange().getEnd());
            return null;
        }
        if (workflowParams.getExperimentDO().getType().equals(ExpTypeConst.PRM) && coordinates.size() != 2) {
            logger.warn("coordinate size != 2,Rang:" + swathIndex.getRange().getStart() + ":" + swathIndex.getRange().getEnd());
        }
        //Step3.提取指定原始谱图
        rtMap = airdFileParser.parseSwathBlockValues(raf, swathIndex, exp.fetchCompressor(Compressor.TARGET_MZ), exp.fetchCompressor(Compressor.TARGET_INTENSITY));

        return epps(coordinates, rtMap, overviewId, workflowParams);

    }

    /**
     * 最终的提取XIC结果需要落盘数据库,一般用于正式XIC提取的计算
     *
     * @param coordinates
     * @param rtMap
     * @param overviewId
     * @param workflowParams
     * @return
     */
    private List<AnalyseDataDO> epps(List<SimplePeptide> coordinates, TreeMap<Float, MzIntensityPairs> rtMap, String overviewId, WorkflowParams workflowParams) {
        List<AnalyseDataDO> dataList = Collections.synchronizedList(new ArrayList<>());
        long start = System.currentTimeMillis();
        //传入的coordinates是没有经过排序的,需要排序先处理真实肽段,再处理伪肽段.如果先处理的真肽段没有被提取到任何信息,或者提取后的峰太差被忽略掉,都会同时删掉对应的伪肽段的XIC
        coordinates.parallelStream().forEach(tp -> {

            //Step1. 常规提取XIC,XIC结果不进行压缩处理,如果没有提取到任何结果,那么加入忽略列表
            AnalyseDataDO dataDO = extractForOne(tp, rtMap, workflowParams.getExtractParams(), overviewId);
            if (dataDO == null) {
                return;
            }

            //Step2. 常规选峰及打分,未满足条件的直接忽略
            scoreService.scoreForOne(dataDO, tp, rtMap, workflowParams);
            if (dataDO.getFeatureScoresList() == null) {
                return;
            }

            //Step3. 忽略过程数据,将数据提取结果加入最终的列表
            AnalyseUtil.compress(dataDO);
            dataList.add(dataDO);

            //Step4. 如果第一,二步均符合条件,那么开始对对应的伪肽段进行数据提取和打分
            tp.setAsDecoy(true);
            AnalyseDataDO decoyData = extractForOne(tp, rtMap, workflowParams.getExtractParams(), overviewId);
            if (decoyData == null) {
                return;
            }

            //Step5. 对Decoy进行打分
            scoreService.scoreForOne(decoyData, tp, rtMap, workflowParams);
            if (decoyData.getFeatureScoresList() == null) {
                return;
            }

            //Step6. 忽略过程数据,将数据提取结果加入最终的列表
            AnalyseUtil.compress(decoyData);
            dataList.add(decoyData);
        });

        logger.info("提取XIC+选峰+打分耗时:" + (System.currentTimeMillis() - start) / 1000 + "秒");
        return dataList;
    }

    /**
     * 根据input入参初始化一个AnalyseOverviewDO
     *
     * @param input
     * @return
     */
    public AnalyseOverviewDO createOverview(WorkflowParams input) {
        AnalyseOverviewDO overviewDO = new AnalyseOverviewDO();
        overviewDO.setExpId(input.getExperimentDO().getId());
        overviewDO.setExpName(input.getExperimentDO().getName());
        overviewDO.setType(input.getExperimentDO().getType());
        overviewDO.setScoreTypes(input.getScoreTypes()); //存储打分分数类型的快照
        overviewDO.setLibraryId(input.getLibrary().getId());
        overviewDO.setLibraryName(input.getLibrary().getName());
        overviewDO.setLibraryPeptideCount(input.getLibrary().getTotalCount().intValue());
        overviewDO.setName(input.getExperimentDO().getName() + "-" + input.getLibrary().getName() + "-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        overviewDO.setOwnerName(input.getOwnerName());
        overviewDO.setCreateDate(new Date());
        overviewDO.setNote(input.getNote());
        overviewDO.setFdr(input.getFdr());
        overviewDO.setRtExtractWindow(input.getExtractParams().getRtExtractWindow());
        overviewDO.setMzExtractWindow(input.getExtractParams().getMzExtractWindow());
        overviewDO.setSigma(input.getSigmaSpacing().getSigma());
        overviewDO.setSpacing(input.getSigmaSpacing().getSpacing());
        overviewDO.setShapeScoreThreshold(input.getXcorrShapeThreshold());
        overviewDO.setShapeScoreWeightThreshold(input.getXcorrShapeWeightThreshold());
        if (input.getSlopeIntercept() != null) {
            overviewDO.setSlope(input.getSlopeIntercept().getSlope());
            overviewDO.setIntercept(input.getSlopeIntercept().getIntercept());
        }

        analyseOverviewService.insert(overviewDO);
        input.setOverviewId(overviewDO.getId());
        return overviewDO;
    }
}
