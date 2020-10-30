package net.csibio.propro.domain.bean.peptide;

import lombok.Data;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-12 20:27
 */
@Data
public class FragmentResult {

    List<Fragment> decoyList;

    int decoyTotalCount;

    int decoyUniCount;

    List<Fragment> targetList;

    int targetTotalCount;

    int targetUniCount;

    List<Fragment> overlapList;

    String msgInfo;
}
