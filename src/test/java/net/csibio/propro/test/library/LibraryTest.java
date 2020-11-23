package net.csibio.propro.test.library;

import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.db.TaskDO;
import net.csibio.propro.service.LibraryService;
import net.csibio.propro.service.PeptideService;
import net.csibio.propro.algorithm.parser.TraMLParser;
import net.csibio.propro.test.BaseTest;
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
        String filePath = getClass().getClassLoader().getResource("ChromatogramExtractor_input.tsv").getPath();
        libraryDO.setFilePath(filePath);
        libraryService.insert(libraryDO);
        File file = new File(filePath);
        ResultDO resultDO = libraryService.parseAndInsert(libraryDO, new FileInputStream(file), null, new TaskDO());
        assert resultDO.isSuccess();
        List<PeptideDO> trans = peptideService.getAllByLibraryId(libraryDO.getId());
        assert trans.size() == 3;

        peptideService.deleteAllByLibraryId(libraryDO.getId());
        libraryService.delete(libraryDO.getId());
    }

}
