package net.csibio.propro.domain.bean.analyse;

import net.csibio.propro.domain.db.simple.SimplePeptide;
import lombok.Data;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-18 09:03
 */
@Data
public class LibraryCoordinate {

    String libraryId;

    Float rtExtractionWindow;

    List<SimplePeptide> ms1List;

    List<SimplePeptide> ms2List;
}
