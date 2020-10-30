package net.csibio.propro.domain.bean.irt;

import net.csibio.propro.domain.bean.score.SlopeIntercept;
import lombok.Data;

import java.util.List;

@Data
public class IrtResult {

    SlopeIntercept si;

    List<String> peptideRef;

    List<Double[]> selectedPairs;

    List<Double[]> unselectedPairs;
}
