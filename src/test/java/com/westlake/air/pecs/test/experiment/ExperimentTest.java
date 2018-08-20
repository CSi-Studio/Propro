package com.westlake.air.pecs.test.experiment;

import com.westlake.air.pecs.domain.ResultDO;
import com.westlake.air.pecs.domain.db.ExperimentDO;
import com.westlake.air.pecs.domain.db.LibraryDO;
import com.westlake.air.pecs.domain.db.TaskDO;
import com.westlake.air.pecs.domain.query.ScanIndexQuery;
import com.westlake.air.pecs.service.ExperimentService;
import com.westlake.air.pecs.service.LibraryService;
import com.westlake.air.pecs.service.ScanIndexService;
import com.westlake.air.pecs.test.BaseTest;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-20 14:11
 */
public class ExperimentTest extends BaseTest {

    @Autowired
    ExperimentService experimentService;
    @Autowired
    ScanIndexService scanIndexService;
    @Autowired
    LibraryService libraryService;

    @Test
    public void experiment_upload_mzxml_parser_Test_1() throws Exception {
        String filePath = getClass().getClassLoader().getResource("ChromatogramExtractor_input.mzXML").getPath();
        ExperimentDO experimentDO = new ExperimentDO();
        experimentDO.setName("测试用实验");
        experimentDO.setFileLocation(filePath);
        experimentDO.setFileType(ExperimentDO.FILE_TYPE_MZXML);
        experimentService.insert(experimentDO);

        File file = new File(filePath);
        experimentService.uploadFile(experimentDO, file, new TaskDO());

        ScanIndexQuery query = new ScanIndexQuery();
        query.setExperimentId(experimentDO.getId());
        assert 59 == scanIndexService.count(query);
    }

    @Test
    public void experiment_upload_mzml_parser_Test_1() throws Exception {
        String filePath = getClass().getClassLoader().getResource("ChromatogramExtractor_input.mzML").getPath();
        ExperimentDO experimentDO = new ExperimentDO();
        experimentDO.setName("测试用实验");
        experimentDO.setFileLocation(filePath);
        experimentDO.setFileType(ExperimentDO.FILE_TYPE_MZML);
        experimentService.insert(experimentDO);

        File file = new File(filePath);
        experimentService.uploadFile(experimentDO, file, new TaskDO());

        ScanIndexQuery query = new ScanIndexQuery();
        query.setExperimentId(experimentDO.getId());
        assert 59 == scanIndexService.count(query);

        scanIndexService.deleteAllByExperimentId(experimentDO.getId());
        experimentService.delete(experimentDO.getId());
    }

    @Test
    public void extractor_test() throws FileNotFoundException {
        LibraryDO libraryDO = new LibraryDO();
        libraryDO.setName("测试用临时库");
        libraryDO.setType(LibraryDO.TYPE_STANDARD);
        libraryService.insert(libraryDO);
        String filePath = getClass().getClassLoader().getResource("ChromatogramExtractor_input.tsv").getPath();
        File file = new File(filePath);
        libraryService.parseAndInsert(libraryDO, new FileInputStream(file), filePath, true, new TaskDO());

    }

}