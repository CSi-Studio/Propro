package com.csi.propro.service;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Nico Wang
 * Time: 2019-06-27 22:13
 */
public interface ResultCompareService {

    /**
     * 平均蛋白覆盖率
     */
    void printProteinCoverage(String projectId, String libraryId, String filePath);

    /**
     * 从 Peptide Matrix 文件中比较两次重复实验
     * 文件中实验名为 FileName + "_with_dscore_filtered"; Pep在第一列; Prot在第二列
     * 考虑到可能用不同的参数多次提取数据, 使用overviewIndex参数来选择第几次提取数据
     */
    void compareMatrixReplicate(String projectId, String filePath, String expIdA, String expIdB, int overviewIndex, boolean isUnique);

    /**
     * 从 Peptide Matrix 文件中, 比较project中含有的所有实验结果
     * 文件中实验名为 FileName + "_with_dscore_filtered"; Pep在第一列; Prot在第二列
     * 考虑到可能用不同的参数多次提取数据, 使用overviewIndex参数来选择第几次提取数据
     */
    void compareMatrix(String projectId, String filePath, int overviewIndex, boolean isUnique);

    /**
     * 处理单一实验的OpenSWATH结果
     */
    void printProtResults(String analyseOverviewId, String filePath, boolean isUnique);

    /**
     * 处理单一实验的OpenSWATH结果
     */
    void printPepResults(String analyseOverviewId, String filePath);

    /**
     * 处理单一实验的OpenSWATH结果
     */
    void printSeqResults(String analyseOverviewId, String filePath);

    /**
     * 处理单一实验的OpenSWATH结果
     * 将Propro only pep 间隔";" print出来
     */
    void printProproOnlyPep(String analyseOverviewId, String filePath, int length);

    /**
     * 处理单一实验的OpenSWATH结果
     * 将File only pep 间隔";" print出来
     */
    void printFileOnlyPep(String analyseOverviewId, String filePath, int length);

    void printSilacResults(String analyseOverviewId, String filePath);

    /**
     * 某一个分析结果中被鉴定到的蛋白
     * @param overviewId
     * @param isUnique
     * @return
     */
    HashSet<String> getProproProteins(String overviewId, boolean isUnique);
    HashSet<String> getProproPeptideRefs(String overviewId);

    HashMap<String, HashSet<String>> getMatrixFilePepMap(String projectId, String filePath);
}
