package com.csi.propro.test.library;

import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.db.LibraryDO;
import com.csi.propro.domain.db.PeptideDO;
import com.csi.propro.domain.db.TaskDO;
import com.csi.propro.service.LibraryService;
import com.csi.propro.service.PeptideService;
import com.csi.propro.algorithm.parser.TraMLParser;
import com.csi.propro.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-20 14:11
 */
public class LibraryTest extends BaseTest {

    @Autowired
    TraMLParser traMLParser;
    @Autowired
    LibraryService libraryService;
    @Autowired
    PeptideService peptideService;

    @Test
    public void extractor_tsv_parser_Test_1() throws Exception {
        LibraryDO libraryDO = new LibraryDO();
        libraryDO.setName("测试用临时库");
        libraryDO.setType(LibraryDO.TYPE_STANDARD);
        libraryService.insert(libraryDO);
        String filePath = getClass().getClassLoader().getResource("ChromatogramExtractor_input.tsv").getPath();
        File file = new File(filePath);
        ResultDO resultDO = libraryService.parseAndInsert(libraryDO, new FileInputStream(file), filePath, null, new TaskDO());
        assert resultDO.isSuccess();
        List<PeptideDO> trans = peptideService.getAllByLibraryId(libraryDO.getId());
        assert trans.size() == 3;

        peptideService.deleteAllByLibraryId(libraryDO.getId());
        libraryService.delete(libraryDO.getId());
    }

}
